/*
 * Copyright 2020 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ab.weather;

import ab.tts.Voice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
@Slf4j
@Service
@ConfigurationProperties("awos")
public class Awos {

  @Getter @Setter private String city;

  @Getter @Setter private String station;

  @Getter @Setter private String metarurl;

  private String icaoNumber(String s) {
    List<String> list = new ArrayList<>();
    for (char c : s.toCharArray()) {
      switch (c) {
        case '0': list.add("zero"); break;
        case '1': list.add("one"); break;
        case '2': list.add("two"); break;
        case '3': list.add("tree"); break;
        case '4': list.add("fower"); break;
        case '5': list.add("fife"); break;
        case '6': list.add("six"); break;
        case '7': list.add("seven"); break;
        case '8': list.add("eight"); break;
        case '9': list.add("niner"); break;
        default: throw new IllegalStateException(s);
      }
    }
    return String.join(" ", list);
  }

  private String icaoNumber(int i) {
    return icaoNumber(Integer.toString(i));
  }

  private List<String> awPlace(Queue<String> queue) {
    String s = queue.remove();
    if (!s.equals("METAR") && !s.equals("SPECI")) {
      throw new IllegalStateException(s);
    }
    queue.remove(); // TODO: 2020-12-05 pick city from METAR station code that skipped in this line
    return Collections.singletonList(city + " automated weather observation system");
  }

  private List<String> awTime(Queue<String> queue) {
    String time = queue.remove();
    if (!time.endsWith("Z") || time.length() != 7) {
      throw new IllegalStateException(time);
    }
    String s = queue.peek();
    if (s.equals("AUTO") || s.startsWith("CC")) {
      queue.remove();
    }
    return Collections.singletonList("observation taken at " + icaoNumber(time.substring(2, 6)) + " zulu");
  }

  private List<String> awWind(Queue<String> queue) {
    List<String> list = new ArrayList<>();
    String s = queue.remove();
    if (!s.endsWith("KT")) {
      throw new IllegalStateException(s);
    }
    if (s.length() == 7) {
      list.add("wind " + icaoNumber(s.substring(0, 3)) + " at " + icaoNumber(Integer.parseInt(s.substring(3, 5))));
    } else if ((s.length() == 10) && (s.charAt(5) == 'G')) {
      list.add("wind " + icaoNumber(s.substring(0, 3)) + " at " + icaoNumber(Integer.parseInt(s.substring(3, 5))));
      list.add("gusting " + icaoNumber(s.substring(6, 8)));
    } else {
      throw new IllegalStateException(s);
    }

    s = queue.peek();
    if (s.length() == 7 && s.charAt(3) == 'V') {
      list.add("wind variable " + icaoNumber(s.substring(0, 3)) + " degrees to "
          + icaoNumber(s.substring(4, 7)) + " degrees");
      queue.remove();
    }
    return list;
  }

  private List<String> awVisibility(Queue<String> queue) {
    String s = queue.remove();
    String v1 = "";
    if (s.length() == 1) {
      v1 = icaoNumber(s) + " and ";
      s = queue.remove();
    }
    if (!s.endsWith("SM")) {
      throw new IllegalStateException(s); // FIXME: 2020-12-07 support kilometers
    }
    s = s.substring(0, s.length() - 2);
    String v2 = "";
    if (s.endsWith("/4")) {
      v2 = " quarter";
      s = s.substring(0, s.length() - 2);
    } else if (s.endsWith("/2")) {
      v2 = " half";
      s = s.substring(0, s.length() - 2);
    }
    if (v1.isEmpty() && s.equals("9")) {
      v1 = "greater than "; // automated systems measuring visibility up to nine miles
    }
    return Collections.singletonList("visibility " + v1 + icaoNumber(s) + v2);
  }

  private final static Pattern PHENOMENON = Pattern.compile("(\\+/-)?\\w\\w");
  private List<String> awPhenomenon(Queue<String> queue) {
    List<String> list = new ArrayList<>();
    while (PHENOMENON.matcher(queue.peek()).matches()) {
      String s = queue.remove();
      String s1 = "moderate ";
      if (s.startsWith("-")) {
        s1 = "light ";
        s = s.substring(1);
      } else if (s.startsWith("+")) {
        s1 = "heavy ";
        s = s.substring(1);
      }
      switch (s) {
        case "DZ": s ="drizzle"; break;
        case "RA": s ="rain"; break;
        case "SN": s ="snow"; break;
        case "SG": s ="snow grains"; break;
        case "IC": s ="ice crystals"; break;
        case "PL": s ="ice pellets"; break;
        case "GR": s ="hail"; break;
        case "GS": s ="snow pellets"; break;
        default: throw new IllegalStateException(s);
      }
      list.add(s1 + s);
    }
    return list;
  }

  private boolean isSky(String s) {
    switch ((s + "000").substring(0, 3)) {
      case "SKC":
      case "CLR":
      case "FEW":
      case "SCT":
      case "BKN":
      case "OVC":
        return true;
    }
    return false;
  }

  private List<String> awCeiling(Queue<String> queue) {
    String s = "";
    while (isSky(queue.peek())) {
      String q = queue.remove();
      s = s.isEmpty() ? q : s;
    }
    int i = Integer.parseInt((s + "000").substring(3));
    switch (s.substring(0, 3)) {
      case "CLR": return Collections.singletonList("sky clear");
      case "FEW": s = "few"; break;
      case "SCT": s = "scattered"; break;
      case "BKN": s = "broken"; break;
      case "OVC": s = "overcast"; break;
      default: throw new IllegalStateException(s);
    }
    return Collections.singletonList("ceiling " + icaoNumber(i * 100) + ' ' + s);
  }

  private List<String> awTemperature(Queue<String> queue) {
    List<String> list = new ArrayList<>();
    String s = queue.remove();
    if (s.indexOf('/') < 0) {
      throw new IllegalStateException(s);
    }
    String[] a = s.split("/");
    for (int i = 0; i < Math.min(2, a.length); i++) {
      if (!a[i].isEmpty()) {
        s = a[i].startsWith("M") ? "minus " : "plus ";
        list.add((i == 0 ? "temperature " : "dew point ") + s
            + icaoNumber(Integer.parseInt(a[i].substring(a[i].length() - 2))));
      }
    }
    return list;
  }

  private List<String> awAltimeter(Queue<String> queue) {
    String s = queue.remove();
    if (!s.startsWith("A")) {
      throw new IllegalStateException(s);
    }
    return Collections.singletonList("altimeter " + icaoNumber(s.substring(1)));
  }

  public List<String> getAwosReport() {
    String metar;
    if (metarurl == null || metarurl.isEmpty()) {
      try {
        metar = new String(
            IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("metar.txt")),
            StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String, String> postMap = new LinkedMultiValueMap<>();
      postMap.add("Stations", station);
      postMap.add("format", "raw");
      postMap.add("Langue", "anglais");
      metar = new RestTemplate().postForObject(metarurl, new HttpEntity<>(postMap, httpHeaders), String.class);

      // log
      Scanner scanner = new Scanner(metar);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("METAR ") || line.startsWith("SPECI ")) {
          log.info("metar: " + line);
          break;
        }
      }
      // log end
    }

    Scanner scanner = new Scanner(metar);
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (line.startsWith("METAR ") || line.startsWith("SPECI ")) {
        metar = line;
        break;
      }
    }
    Queue<String> queue = new LinkedList<>(Arrays.asList(metar.split("\\s+")));

    List<String> awosReport = new ArrayList<>();
    awosReport.addAll(awPlace(queue));
    awosReport.addAll(awTime(queue));
    awosReport.addAll(awWind(queue));
    awosReport.addAll(awVisibility(queue));
    awosReport.addAll(awPhenomenon(queue));
    awosReport.addAll(awCeiling(queue));
    awosReport.addAll(awTemperature(queue));
    awosReport.addAll(awAltimeter(queue));
    // TODO: 2020-12-06 mean sea level pressure
    return awosReport;
  }

  public String getMp3(Voice voice, String recommendedFileName, String cacheFolder) {
    String pause = voice.mp3File("<speak><break time=\"100ms\"/></speak>",
        cacheFolder + "/_awos_pause_" + voice.getName() + ".mp3");
    String pauseLong = voice.mp3File("<speak><break time=\"2000ms\"/></speak>",
        cacheFolder + "/_awos_long_pause_" + voice.getName() + ".mp3");
    List<String> audiofiles = new ArrayList<>();
    List<String> weatherLines = getAwosReport();
    for (String weatherLine : weatherLines) {
      String fileName = weatherLine
          .replace("-", " minus ").replace("+", " plus ").replaceAll("\\W", " ")
          .trim().toLowerCase().replaceAll("\\s+", "_");
      fileName = fileName.isEmpty() ? "_" : fileName;
      if (!weatherLine.isEmpty()) {
        audiofiles.add(voice.mp3File(weatherLine, cacheFolder + "/" + fileName + ".mp3"));
      }
      audiofiles.add(pause);
    }
    audiofiles.add(pauseLong);

    try (OutputStream outputStream = new FileOutputStream(recommendedFileName)) {
      Files.write(Paths.get(recommendedFileName + ".txt"), String.join("\n", weatherLines).getBytes());
      for (String audiofile : audiofiles) {
        Files.copy(Paths.get(audiofile), outputStream);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return recommendedFileName;
  }

}

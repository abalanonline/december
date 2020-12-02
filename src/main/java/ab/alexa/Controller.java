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

package ab.alexa;

import ab.tts.TtsService;
import ab.tts.Voice;
import ab.weather.Noaa;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class Controller {

  @Value("${fileLocal:target}")
  private String fileLocal;

  @Value("${fileUrl:http://localhost}")
  private String fileUrl;

  @Value("${fileCache:target}")
  private String fileCache;

  public static final String INTENT_NAME = "intent";
  public static final String SLOT_NAME = "slot";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TtsService ttsService;

  @Autowired
  private Noaa noaa;

  public ResponseMeta dialogPlain(String text) {
    ResponseMeta responseMeta = text != null && !text.isEmpty() ? new ResponseMeta(text) :
        new ResponseMeta(); // which make no sense, it will be rejected by api
    responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveDialogElicitSlot(INTENT_NAME, SLOT_NAME));
    return responseMeta;
  }

  private static int currentVoiceIndex = 0; // FIXME: 2020-11-16 read from configuration file

  public ResponseMeta playMp3(String fileName) {
    ResponseMeta responseMeta = new ResponseMeta();
    responseMeta.getResponse().getDirectives()
        .add(new DirectiveAudioPlayerPlay(fileUrl + "/" + fileName.substring(fileName.lastIndexOf('/') + 1)));
    return responseMeta;
  }

  public ResponseMeta sayAudio(String text) {
    String fileName = ttsService.getVoiceList().get(currentVoiceIndex)
        .mp3File(text, fileLocal.endsWith(".mp3") ? fileLocal :
            (fileLocal + "/" + Instant.now().toString().replace(':', '-').replace('.', '-') + ".mp3"));
    return playMp3(fileName);
  }

  public ResponseMeta dialogAudio(String text) {
    ResponseMeta responseMeta = new ResponseMeta(text);
    //responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveAudioPlayerPlay("https://github.com/abalanonline/december/releases/download/v0.1/a.mp3"));
    return responseMeta;
  }

  public ResponseMeta justlisten(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        return dialogPlain("I'm listening");
      case "IntentRequest":
        String input = requestMeta.getAnyIntentValue();
        log.info("i: " + input);
        return dialogPlain("mmm");
    }
    return null;
  }

  public ResponseMeta thenews(RequestMeta requestMeta) {
    if ("LaunchRequest".equals(requestMeta.getRequestType())) {
      if (Files.exists(Paths.get("news.txt"))) {
        try {
          return sayAudio(new String(Files.readAllBytes(Paths.get("news.txt")), StandardCharsets.UTF_8));
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
      String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
          .format(DateTimeFormatter.ofPattern("h:mm"));
      return sayAudio("In Montreal, it's " + timeInMontreal + ". And everything is fine.");
    }
    return null;
  }

  public ResponseMeta decemberweather(RequestMeta requestMeta) {
    if ("LaunchRequest".equals(requestMeta.getRequestType())) {
      String fileName = noaa.getMp3(
          ttsService.getVoiceList().get(currentVoiceIndex),
          fileLocal.endsWith(".mp3")
              ? fileLocal
              : fileLocal + "/noaa-" + Instant.now().toString().replaceAll("\\D", "-").substring(0, 23) + ".mp3",
          fileCache);
      return playMp3(fileName);
    }
    return null;
  }

  static List<Integer> listVoices = null;
  static int listVoicesCurrent = 0;
  public ResponseMeta listvoices(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        log.info("i: list voices");
        if (listVoices == null) {
          listVoices = IntStream.range(0, ttsService.getVoiceList().size()).boxed().collect(Collectors.toList());
        }
        Collections.shuffle(listVoices);
        listVoicesCurrent = -1;
      case "AudioPlayer.PlaybackNearlyFinished":
        listVoicesCurrent += 1;
        try {
          currentVoiceIndex = listVoices.get(listVoicesCurrent);
        } catch (IndexOutOfBoundsException e) {
          log.info("o: end of the list");
          return null;
        }
        Voice v = ttsService.getVoiceList().get(currentVoiceIndex);
        log.info("o: " + currentVoiceIndex + " " + v.getName() + " " + v);
        ResponseMeta response =
            sayAudio("number " + currentVoiceIndex + ", " + randomGreeting(v.getName()) + ", ");
        DirectiveAudioPlayerPlay directive = (DirectiveAudioPlayerPlay) response.getResponse().getDirectives().get(0);
        directive.setPlayBehavior("REPLACE_ENQUEUED");
        return response;
    }
    return null;
  }

  private static final String[] RANDOM_GREETINGS = {
      "Hi, it's %s",
      "Hi, it's %s speaking",
      "Hi, this is %s",
      "Hi, this is %s speaking",
      "Hi, I'm %s",
      "Hi, my name is %s",
      "Hi, %s here",
      "Hello, it's %s",
      "Hello, it's %s speaking",
      "Hello, this %s",
      "Hello, this %s speaking",
      "Hello, I'm %s",
      "Hello, my name is %s",
      "Hello, %s here",
      "My name is %s",
      "My name is %s, nice to meet you",
      "This is %s, nice to meet you",
      "This is %s speaking, hello",
      "This is %s"};
  public static String randomGreeting(String name) {
    return String.format(RANDOM_GREETINGS[ThreadLocalRandom.current().nextInt(RANDOM_GREETINGS.length)], name);
  }

  public ResponseMeta selectvoice(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        log.info("i: select voice");
        return dialogPlain("mmm");
      case "IntentRequest":
        String input = requestMeta.getAnyIntentValue();
        if ("?".equals(input)) {
          return null;
        }
        log.info("i: " + input);
        currentVoiceIndex = Integer.parseInt(input);
        return sayAudio(randomGreeting(ttsService.getVoiceList().get(currentVoiceIndex).getName()));
    }
    return null;
  }

  public ResponseMeta genericResponse(RequestMeta requestMeta) {
    if ("System.ExceptionEncountered".equals(requestMeta.getRequestType())) {
      log.error(requestMeta.getError());
    }
    return new ResponseMeta();
  }

  @PostMapping("/alexa/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) throws IOException {
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    log.debug("i: " + skill + " - " + requestMeta.getRequestType() + ": " + requestString);
    ResponseMeta responseMeta = null;
    switch (skill) {
      case "listvoices": responseMeta = listvoices(requestMeta); break;
      case "thenews": responseMeta = thenews(requestMeta); break;
      case "justlisten": responseMeta = justlisten(requestMeta); break;
      case "selectvoice": responseMeta = selectvoice(requestMeta); break;
      case "decemberweather": responseMeta = decemberweather(requestMeta); break;
    }
    if (responseMeta == null) {
      responseMeta = genericResponse(requestMeta);
    }

    String responseString = objectMapper.writeValueAsString(responseMeta);
    log.debug("o: " +
        (responseMeta.getResponse().getOutputSpeech() != null || responseMeta.getResponse().getDirectives().size() > 0 ?
        responseString : "empty response"));
    return responseString;
  }

  @GetMapping("/alexa/{skill}")
  public String test(@PathVariable("skill") String skill) {
    return "alexa " + skill;
  }

}

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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Noaa {

  public List<String> getWeather() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("accuweather5day.json");
    AccuWeatherFiveDays accuWeatherFiveDays = null;
    try {
      accuWeatherFiveDays = objectMapper.readValue(is, AccuWeatherFiveDays.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    List<AccuWeatherDailyForecast> dailyForecasts = accuWeatherFiveDays.getDailyForecasts();

    List<String> weatherList = new ArrayList<>();
    weatherList.add("The outlook");
    for (AccuWeatherDailyForecast forecast : dailyForecasts) {
      DayOfWeek dayOfWeek =
          OffsetDateTime.parse(forecast.getDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).getDayOfWeek();
      String dayOfWeekString =
          dayOfWeek.toString().substring(0, 1).toUpperCase() + dayOfWeek.toString().substring(1).toLowerCase();

      weatherList.add("For " + dayOfWeekString);
      weatherList.add(forecast.getDay().getLongPhrase()); // getShortPhrase()?
      weatherList.add(String.format("High %+.0f", forecast.getTemperature().getMaximum().getValue()));
      weatherList.add("");

      weatherList.add("For " + dayOfWeekString + " night");
      weatherList.add(forecast.getNight().getLongPhrase()); // getShortPhrase()?
      weatherList.add(String.format("Low %+.0f", forecast.getTemperature().getMinimum().getValue()));
      weatherList.add("");
    }
    weatherList.add("Normals for the period");
    weatherList.add("Low -0?");
    weatherList.add("High +0?");
    weatherList.add("");

    return weatherList;
  }

  public String getMp3(Voice voice, String mp3Folder, String cacheFolder) {
    String pause = voice.mp3File("<speak><break time=\"100ms\"/></speak>",
        cacheFolder + "/adjustable_noaa_pause_" + voice.getName() + ".mp3");
    List<String> audiofiles = new ArrayList<>();
    for (String weatherLine : getWeather()) {
      String fileName = weatherLine
          .replace("-", " minus ").replace("+", " plus ").replaceAll("\\W", " ")
          .trim().toLowerCase().replaceAll("\\s+", "_");
      fileName = fileName.isEmpty() ? "_" : fileName;
      if (!weatherLine.isEmpty()) {
        audiofiles.add(voice.mp3File(weatherLine, cacheFolder + "/" + fileName + ".mp3"));
      }
      audiofiles.add(pause);
    }

    String outputFile = mp3Folder + "/noaa-" + Instant.now().toString().replaceAll("\\D", "-").substring(0, 23) + ".mp3";
    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
      for (String audiofile : audiofiles) {
        Files.copy(Paths.get(audiofile), outputStream);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return outputFile;
  }

}

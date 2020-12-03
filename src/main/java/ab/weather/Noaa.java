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
import ab.weather.aw.AccuWeather;
import ab.weather.aw.AccuWeatherAir;
import ab.weather.aw.DailyForecast;
import ab.weather.aw.AccuWeatherDayNight;
import ab.weather.aw.WeeklyForecast;
import ab.weather.aw.Observation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@ConfigurationProperties("noaa")
public class Noaa {

  public static final int BRIEF_INDEX = 3;

  @Getter @Setter private String greeting;

  @Getter @Setter private String city;

  @Autowired
  @Setter AccuWeather accuWeather;

  public static final String[] WIND_NAMES = {
      "N", "NbE", "NNE", "NEbN", "NE", "NEbE", "ENE", "EbN", "E", "EbS", "ESE", "SEbE", "SE", "SEbS", "SSE", "SbE",
      "S", "SbW", "SSW", "SWbS", "SW", "SWbW", "WSW", "WbS", "W", "WbN", "WNW", "NWbW", "NW", "NWbN", "NNW", "NbW"};

  public String degreeToDirection(int degrees, int bitWidth) {
    int wind2x = (int) ((degrees + 360) * (1 << (bitWidth + 1)) / 360.0);
    int wind = ((wind2x + 1) >> 1) & ((1 << bitWidth) - 1);
    return WIND_NAMES[wind << (5 - bitWidth)];
  }

  public String directionToString(String direction) {
    List<String> result = new ArrayList<>();
    for (char c : direction.toCharArray()) {
      switch (c) {
        case 'N': result.add("north"); break;
        case 'E': result.add("east"); break;
        case 'S': result.add("south"); break;
        case 'W': result.add("west"); break;
        case 'b': result.add("by"); break;
        default: throw new IllegalStateException(direction);
      }
    }
    return String.join(" ", result);
  }

  public List<String> getWeather12(DailyForecast forecast, boolean night, int index, String[] airQuality) {
    if (index < 0) { // skip the morning forecast
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<>();
    DayOfWeek dayOfWeek =
        OffsetDateTime.parse(forecast.getDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).getDayOfWeek();
    String dayOfWeekString =
        dayOfWeek.toString().substring(0, 1).toUpperCase() + dayOfWeek.toString().substring(1).toLowerCase();
    // night activity
    AccuWeatherDayNight weatherDayNight;
    String temperatureHighLow;
    if (!night) {
      weatherDayNight = forecast.getDay();
      temperatureHighLow = String.format("High %+.0f", forecast.getTemperature().getMaximum().getValue());
    } else {
      weatherDayNight = forecast.getNight();
      temperatureHighLow = String.format("Low %+.0f", forecast.getTemperature().getMinimum().getValue());
    }
    dayOfWeekString = dayOfWeekString + (night ? " night" : "");
    if ((index == 0) || (index == 1 && night)) {
      dayOfWeekString = night ? "tonight" : "today";
    }
    boolean brief = index >= BRIEF_INDEX;

    if (index == 0) { // forecast for
      list.add("");
      list.add("Forecast for " + city);
    }

    if (index == BRIEF_INDEX) { // air, outlook
      list.addAll(Arrays.asList(airQuality));
      list.add("");
      list.add("The outlook");
    }

    list.add((index == BRIEF_INDEX ? "" : "For ") + dayOfWeekString);
    if (!brief) {
      list.add(weatherDayNight.getLongPhrase());
      Optional<AccuWeatherAir> air = forecast.getAirAndPollen().stream()
          .filter(a -> a.getName().toLowerCase().equals("airquality")).findAny();
      airQuality[index] = air.isPresent()
          ? (index == 0 ? "" : "For ") + dayOfWeekString + " air quality " + air.get().getCategory().toLowerCase() : "";
      list.add(temperatureHighLow);

      if (!night) { // UV index
        Optional<AccuWeatherAir> uv = forecast.getAirAndPollen().stream()
            .filter(a -> a.getName().toLowerCase().equals("uvindex")).findAny();
        if (uv.isPresent()) {
          list.add("The UV index for " + dayOfWeekString + " is "
              + uv.get().getValue() + " or " + uv.get().getCategory().toLowerCase());
        }
      }
      // TODO: 2020-12-02 Windchill
    } else {
      list.add(weatherDayNight.getShortPhrase());
      list.add(temperatureHighLow);
    }
    list.add("");
    return list;
    // TODO: 2020-12-02 Normals for the period: Low -0, High +0
  }

  public List<String> getWeather() {
    List<DailyForecast> dailyForecasts = accuWeather.getWeeklyForecast().getDailyForecasts();
    Observation currentObservation = accuWeather.getCurrentObservation();

    List<String> weatherList = new ArrayList<>(Arrays.asList(greeting.split("\n")));
    int hourNow = OffsetDateTime.now().getHour();

    // 1. forecast
    int index12 = ((hourNow < 5) || (hourNow >= 17)) ? -1 : 0; // nighttime
    String[] airQuality = new String[BRIEF_INDEX];
    for (DailyForecast forecast : dailyForecasts) {
      weatherList.addAll(getWeather12(forecast, false, index12++, airQuality));
      weatherList.addAll(getWeather12(forecast, true, index12++, airQuality));
    }

    // 2. weather conditions
    OffsetDateTime observationDateTime =
        OffsetDateTime.parse(currentObservation.getLocalObservationDateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    weatherList.add("");
    weatherList.add("Weather conditions at " + observationDateTime.format(DateTimeFormatter.ofPattern("h a")));
    weatherList.add(city + ":");
    weatherList.add(currentObservation.getWeatherText());
    weatherList.add(String.format("Temperature %+.0f", currentObservation.getTemperature().getMetric().getValue()));
    weatherList.add(String.format("Wind %s %.0f kilometers per hour",
        directionToString(degreeToDirection(currentObservation.getWind().getDirection().getDegrees(), 3)),
        currentObservation.getWind().getSpeed().getMetric().getValue()));
    weatherList.add(String.format("Barometric pressure %.0f kilopascal", currentObservation.getPressure().getMetric().getValue() / 10.0));
    Integer humidity = currentObservation.getRelativeHumidity();
    if (humidity != null) {
      weatherList.add(String.format("Relative humidity %s percent", humidity));
    }

    return weatherList;
  }

  public String getMp3(Voice voice, String recommendedFileName, String cacheFolder) {
    String pause = voice.mp3File("<speak><break time=\"100ms\"/></speak>",
        cacheFolder + "/_noaa_pause_" + voice.getName() + ".mp3");
    List<String> audiofiles = new ArrayList<>();
    List<String> weatherLines = getWeather();
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

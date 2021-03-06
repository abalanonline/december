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

import ab.tts.TtsService;
import ab.tts.Voice;
import ab.weather.aw.AccuWeather;
import ab.weather.aw.WeeklyForecast;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@Log
public class NoaaTest {

  @Test
  public void getDailyForecasts() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream is = classloader.getResourceAsStream("accuweather_5day.json");
    WeeklyForecast weeklyForecast = objectMapper.readValue(is, WeeklyForecast.class);
    assertThat(weeklyForecast.getDailyForecasts().size(), equalTo(5)); // 5 days forecast
  }

  @Test
  public void degreeToDirectionTest() {
    Noaa noaa = new Noaa();
    int i = 120;
    assertThat(noaa.degreeToDirection(i, 0), equalTo("N")); // 1-wind, wind or no wind
    assertThat(noaa.degreeToDirection(i, 1), equalTo("S")); // 2-wind, cold wind or hot wind
    assertThat(noaa.degreeToDirection(i, 2), equalTo("E")); // 4-wind, NESW
    assertThat(noaa.degreeToDirection(i, 3), equalTo("SE")); // 8-wind
    assertThat(noaa.degreeToDirection(i, 4), equalTo("ESE")); // 16
    assertThat(noaa.degreeToDirection(i, 5), equalTo("SEbE")); // 32
  }

  @Ignore
  @Test
  public void getWeather() {
    List<String> weather = new Noaa().getWeather();
    assertThat(weather.size(), not(equalTo(0)));
  }

  @Ignore
  @Test
  public void getMp3Test() {
    TtsService ttsService = new TtsService(new String[]{"en-US"}, false, null);
    Voice voice = ttsService.getVoiceMap().get("Boston");
    AccuWeather accuWeather = new AccuWeather();
    accuWeather.setApikey("00000000000000000000000000000000");
    accuWeather.setLocation("56186");
    Noaa noaa = new Noaa();
    noaa.setGreeting("hello");
    noaa.setCity("Montreal");
    noaa.setAccuWeather(accuWeather);
    String fileName = noaa.getMp3(voice,
        "./target/_noaa-" + Instant.now().toString().replaceAll("\\D", "-").substring(0, 19) + ".mp3", "./target");
    log.info(fileName);
    assertThat(fileName, containsString("mp3"));
  }

  @Ignore
  @Test
  public void awosTest() {
    TtsService ttsService = new TtsService(new String[]{"en-US"}, false, null);
    Voice voice = ttsService.getVoiceMap().get("Boston");
    Awos awos = new Awos();
    String fileName = awos.getMp3(voice,
        "./target/_awos-" + Instant.now().toString().replaceAll("\\D", "-").substring(0, 19) + ".mp3", "./target");
    log.info(fileName);
    assertThat(fileName, containsString("mp3"));
  }

}

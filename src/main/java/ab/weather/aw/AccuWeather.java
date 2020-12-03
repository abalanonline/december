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

package ab.weather.aw;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@ConfigurationProperties("accuweather")
public class AccuWeather {

  @Getter @Setter private String location;

  @Getter @Setter private String apiKey;

  public WeeklyForecast getWeeklyForecast() {
    if (apiKey.equals("00000000000000000000000000000000")) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        return objectMapper.readValue(classloader.getResourceAsStream("accuweather_5day.json"), WeeklyForecast.class);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return null;
  }

  public Observation getCurrentObservation() {
    if (apiKey.equals("00000000000000000000000000000000")) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      try {
        List<Observation> accuWeatherObservations = objectMapper.readValue(classloader.getResourceAsStream("accuweather_current.json"),
            new TypeReference<List<Observation>>() {});
        return accuWeatherObservations.get(0);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return null;
  }

}

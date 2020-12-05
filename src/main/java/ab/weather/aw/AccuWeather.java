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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Slf4j
@Service
@ConfigurationProperties("accuweather")
public class AccuWeather {

  public static final String DATA_SERVICE = "https://dataservice.accuweather.com/";

  @Getter @Setter private String location;

  @Getter @Setter private String apikey;

  public <T> T readMock(String resourceName, TypeReference<T> typeReference) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try {
      return objectMapper.readValue(classloader.getResourceAsStream(resourceName), typeReference);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public <T> T getJson(String restUrl, TypeReference<T> typeReference) {
    RestTemplate restTemplate = new RestTemplate();
    byte[] bytes = restTemplate.getForObject(DATA_SERVICE + restUrl + '/' + location
        + "?apikey=" + apikey + "&details=true&metric=true", byte[].class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    try {
      return objectMapper.readValue(new ByteArrayInputStream(bytes), typeReference);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public WeeklyForecast getWeeklyForecast() {
    if (apikey.equals("00000000000000000000000000000000")) {
      return readMock("accuweather_5day.json", new TypeReference<WeeklyForecast>() {});
    }
    log.info("accuweather: forecast 5day");
    return getJson("forecasts/v1/daily/5day", new TypeReference<WeeklyForecast>() {});
  }

  public Observation getCurrentObservation() {
    if (apikey.equals("00000000000000000000000000000000")) {
      return readMock("accuweather_current.json", new TypeReference<List<Observation>>() {}).get(0);
    }
    log.info("accuweather: current conditions");
    return getJson("currentconditions/v1", new TypeReference<List<Observation>>() {}).get(0);
  }

}

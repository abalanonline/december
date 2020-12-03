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

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DailyForecast {
  private String date;
  private long epochDate;
  private Map<String, Object> sun;
  private Map<String, Object> moon;
  private AccuWeatherMinMax temperature;
  private AccuWeatherMinMax realFeelTemperature;
  private AccuWeatherMinMax realFeelTemperatureShade;
  private Object hoursOfSun;
  private Map<String, Object> degreeDaySummary;
  private List<AccuWeatherAir> airAndPollen;
  private AccuWeatherDayNight day;
  private AccuWeatherDayNight night;
  private List<Object> sources;
  private Object mobileLink;
  private Object link;
}

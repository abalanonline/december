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

import java.util.Map;

@Getter
@Setter
public class AccuWeatherObservation {
  private String localObservationDateTime;
  private String epochTime;
  private String weatherText;
  private String weatherIcon;
  private String hasPrecipitation;
  private String precipitationType;
  private String isDayTime;
  private AccuWeatherMetricImperial temperature;
  private Map<String, Object> realFeelTemperature;
  private Map<String, Object> realFeelTemperatureShade;
  private Integer relativeHumidity;
  private String indoorRelativeHumidity;
  private Map<String, Object> dewPoint;
  private AccuWeatherWind wind;
  private Map<String, Object> windGust;
  private String UVIndex;
  private String UVIndexText;
  private Map<String, Object> visibility;
  private String obstructionsToVisibility;
  private String cloudCover;
  private Map<String, Object> ceiling;
  private AccuWeatherMetricImperial pressure;
  private Map<String, Object> pressureTendency;
  private Map<String, Object> past24HourTemperatureDeparture;
  private Map<String, Object> apparentTemperature;
  private Map<String, Object> windChillTemperature;
  private Map<String, Object> wetBulbTemperature;
  private Map<String, Object> precip1hr;
  private Map<String, Object> precipitationSummary;
  private Map<String, Object> temperatureSummary;
  private String mobileLink;
  private String link;
}

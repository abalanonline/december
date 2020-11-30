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

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AccuWeatherDayNight {
  private int icon;
  private String iconPhrase;
  private boolean hasPrecipitation;
  private String precipitationType;
  private String precipitationIntensity;
  private String shortPhrase;
  private String longPhrase;
  private int precipitationProbability;
  private int thunderstormProbability;
  private int rainProbability;
  private int snowProbability;
  private int iceProbability;
  private Map<String, Object> wind;
  private Map<String, Object> windGust;
  private Map<String, Object> totalLiquid;
  private Map<String, Object> rain;
  private Map<String, Object> snow;
  private Map<String, Object> ice;
  private double hoursOfPrecipitation;
  private double hoursOfRain;
  private double hoursOfSnow;
  private double hoursOfIce;
  private int cloudCover;
}

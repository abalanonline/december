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

package ab.tts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoiceConfiguration {
  // search and copy options
  private String copy; // copy from existing name
  private String move; // move/edit without duplication
  private String provider; // limit task to this provider

  // new voice variables
  private String name; // new name - optional
  private String language; // new language - optional

  // transformation
  private String pitch;
  private String pitch_range;
  private String glottal_tension;
  private String breathiness;
  private String rate;
  private String timbre;
  private String timbre_extent;

  // test points
  private String json; // json will be saved here for test purposes
}

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

/**
 * Examples:
 * rename ordinary Enrique to a stylish Rafael
 * {"name":"Rafael","move":"Enrique","provider":"Polly"}
 * invite french canadian Chantal to an anglophone party
 * {"copy":"Chantal","provider":"Polly","language":"en-US","xml_lang":"en-US"}
 * make a copy of Lisa with 60% breeze timbre and name her Julie
 * {"name":"Julie","copy":"Lisa","provider":"Watson","timbre":"Breeze","timbre_extent":"60%"}
 */
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

  // bilingual https://docs.aws.amazon.com/polly/latest/dg/bilingual-voices.html
  private String xml_lang;

  // ssml https://aws.amazon.com/polly/features/#Adjust_Speaking_Style.2C_Speech_Rate.2C_Pitch.2C_and_Loudness
  // https://docs.aws.amazon.com/polly/latest/dg/supportedtags.html
  private String pitch; // x-low, low, medium/default, high, x-high
  private String rate; // x-slow, slow, medium/default, fast, x-fast
  private String volume; // x-soft, soft, medium, loud, x-loud

  // transformation https://cloud.ibm.com/docs/text-to-speech?topic=text-to-speech-transformation
  //private String pitch; // same as polly
  private String pitch_range;
  private String glottal_tension;
  private String breathiness;
  //private String rate; // same as polly
  private String timbre;
  private String timbre_extent;

  // test points
  private String json; // json will be saved here for test purposes
}

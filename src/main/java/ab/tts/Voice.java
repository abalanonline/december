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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@Getter
@AllArgsConstructor
public class Voice {

  private final String name;

  private final Provider provider;

  private final String systemId;

  private final String configuration;

  private final Language language;

  private final NeuralEngine engine;

  private final Gender gender;

  public Voice(String name, Provider provider, String systemId, Language language) {
    this.name = name;
    this.provider = provider;
    this.systemId = systemId;
    this.configuration = "{}";
    this.language = language;
    this.engine = NeuralEngine.STANDARD;
    this.gender = Gender.NEUTRAL;
  }

  public InputStream mp3Stream(String text) {
    return getProvider().mp3Stream(this, text);
  }

  public String mp3File(String text, String recommendedFileName) {
    return getProvider().mp3File(this, text, recommendedFileName);
  }

}

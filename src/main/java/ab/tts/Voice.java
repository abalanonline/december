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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@Getter
@RequiredArgsConstructor
public class Voice {

  private final String name;

  private final Provider provider;

  private final String systemId;

  private final String configuration = "";

  private final String language;

  private final boolean neural = false;

  private final Gender gender = Gender.NEUTRAL;

  public InputStream mp3Stream(String text) {
    return getProvider().mp3Stream(this, text);
  }

  public String mp3File(String text, String recommendedFileName) {
    return getProvider().mp3File(this, text, recommendedFileName);
  }

}

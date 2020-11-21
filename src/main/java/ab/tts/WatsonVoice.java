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

import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class WatsonVoice extends Voice {

  @Getter private final String name;

  @Getter private final Provider provider;

  @Getter private final String systemId;

  @Getter private final String configuration = "";

  @Getter private final String language;

  @Getter private final boolean neural = false;

  @Getter private final Gender gender = Gender.NEUTRAL;

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
        .text(text).voice(voice.getSystemId()).accept("audio/mp3").build();
    return ((Watson) voice.getProvider()).getService().synthesize(synthesizeOptions).execute().getResult();
  }

}

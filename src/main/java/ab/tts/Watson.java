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

import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
public class Watson extends Voice {
  private final TextToSpeech textToSpeech;
  private final String voice;

  @Override
  public InputStream mp3Stream(String text) {
    SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
        .text(text).voice(voice).accept("audio/mp3").build();
    return textToSpeech.synthesize(synthesizeOptions).execute().getResult();
  }

}

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

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class Watson extends Voice {
  private final TextToSpeech textToSpeech;
  private final String voice;

  public static Map<String, Voice> voices(String ibmApiKey, String ibmTtsUrl) {
    try {
      Map<String, Voice> voiceMap = new LinkedHashMap<>();
      TextToSpeech textToSpeech = new TextToSpeech(new IamAuthenticator(ibmApiKey));
      textToSpeech.setServiceUrl(ibmTtsUrl);
      voiceMap.put("Michael", new Watson(textToSpeech, SynthesizeOptions.Voice.EN_US_MICHAELVOICE));
      voiceMap.put("James", new Watson(textToSpeech, SynthesizeOptions.Voice.EN_GB_JAMESV3VOICE));
      voiceMap.entrySet().iterator().next().getValue().mp3Stream("a").close();
      return voiceMap;
    } catch (Exception e) {
      log.warn("Failed to initialize Watson voices", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public InputStream mp3Stream(String text) {
    SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
        .text(text).voice(voice).accept("audio/mp3").build();
    return textToSpeech.synthesize(synthesizeOptions).execute().getResult();
  }

}

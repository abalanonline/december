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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * IBM Watson Text to Speech https://www.ibm.com/cloud/watson-text-to-speech
 * Environment variables: IBM_API_KEY, IBM_TTS_URL
 * https://cloud.ibm.com/apidocs/text-to-speech?code=java
 */
public class Watson extends Provider {

  @Getter(lazy=true) private final TextToSpeech service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural) {
    Set<Voice> set = new LinkedHashSet<>();
    set.add(new WatsonVoice("Michael", this, SynthesizeOptions.Voice.EN_US_MICHAELVOICE));
    set.add(new WatsonVoice("James", this, SynthesizeOptions.Voice.EN_GB_JAMESV3VOICE));
    return set;
  }

  private TextToSpeech lazyBuildService() {
    TextToSpeech textToSpeech = new TextToSpeech(new IamAuthenticator(System.getenv("IBM_API_KEY")));
    textToSpeech.setServiceUrl(System.getenv("IBM_TTS_URL"));
    return textToSpeech;
  }

}

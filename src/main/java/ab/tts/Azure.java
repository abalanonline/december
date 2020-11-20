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
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Microsoft Azure Text to Speech https://azure.microsoft.com/en-ca/services/cognitive-services/text-to-speech/
 * Environment variables: MICROSOFT_API_KEY, MICROSOFT_API_LOCATION
 */
public class Azure extends Provider {

  @Getter(lazy=true) private final Object service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<Voice> set = new LinkedHashSet<>();
        set.add(new AzureVoice("Azure", this, "voiceId"));
    return set;
  }

  @Override
  public List<String> downloadVoices() {
    List<String> list = new ArrayList<>();
    return list;
  }

  private TextToSpeech lazyBuildService() {
    return null;
  }

}

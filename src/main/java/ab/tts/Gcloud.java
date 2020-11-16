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

import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import lombok.Getter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Gcloud extends Provider {

  @Getter(lazy=true) private final TextToSpeechClient service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural) {
    Set<Voice> set = new LinkedHashSet<>();
    set.add(new GcloudVoice("G", this));
    return set;
  }

  private TextToSpeechClient lazyBuildService() {
    try {
      return TextToSpeechClient.create();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}

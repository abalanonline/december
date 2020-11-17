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
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Amazon Polly https://aws.amazon.com/polly/
 * Environment variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
 * https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 */
public class Polly extends Provider {

  @Getter(lazy=true) private final PollyClient service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<Voice> set = new LinkedHashSet<>();
    set.add(new PollyVoice("Joey", this, VoiceId.JOEY));
    set.add(new PollyVoice("Kimberly", this, VoiceId.KIMBERLY));
    set.add(new PollyVoice("Salli", this, VoiceId.SALLI));
    set.add(new PollyVoice("Matthew", this, VoiceId.MATTHEW));
    return set;
  }

  @Override
  public List<String> downloadVoices() {
    return null;
  }

  private PollyClient lazyBuildService() {
    return PollyClient.builder().build();
  }

}

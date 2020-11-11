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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class Polly extends Voice {
  private final PollyClient pollyClient;
  private final VoiceId voiceId;

  public static Map<String, Voice> voices() {
    try {
      Map<String, Voice> voiceMap = new LinkedHashMap<>();
      PollyClient pollyClient = PollyClient.builder().build();
      voiceMap.put("Joey", new Polly(pollyClient, VoiceId.JOEY));
      voiceMap.put("Matthew", new Polly(pollyClient, VoiceId.MATTHEW));
      voiceMap.entrySet().iterator().next().getValue().mp3Stream("a").close();
      return voiceMap;
    } catch (Exception e) {
      log.warn("Failed to initialize Polly voices", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public InputStream mp3Stream(String text) {
    SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
        .text(text).voiceId(voiceId).outputFormat(OutputFormat.MP3).build();
    return pollyClient.synthesizeSpeech(request);
  }

}

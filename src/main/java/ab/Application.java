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

package ab;

import ab.tts.Polly;
import ab.tts.Voice;
import ab.tts.Watson;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
public class Application {

  @Bean
  public Map<String, Voice> voiceMap(@Value("${ibm.apiKey:key}") String ibmApiKey, @Value("${ibm.tts.url:url}") String ibmTtsUrl) {
    Map<String, Voice> voiceMap = new LinkedHashMap<>();
    try {
      PollyClient pollyClient = PollyClient.builder().build();
      voiceMap.put("Joey", new Polly(pollyClient, VoiceId.JOEY));
      voiceMap.put("Matthew", new Polly(pollyClient, VoiceId.MATTHEW));
    } catch (Exception e) {
      log.warn("Failed to initialize Polly voices", e);
    }
    try {
      TextToSpeech textToSpeech = new TextToSpeech(new IamAuthenticator(ibmApiKey));
      textToSpeech.setServiceUrl(ibmTtsUrl);
      voiceMap.put("Michael", new Watson(textToSpeech, SynthesizeOptions.Voice.EN_US_MICHAELVOICE));
      voiceMap.put("James", new Watson(textToSpeech, SynthesizeOptions.Voice.EN_GB_JAMESV3VOICE));
    } catch (Exception e) {
      log.warn("Failed to initialize Watson voices", e);
    }
    return voiceMap;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

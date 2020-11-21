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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class AzureVoice extends Voice { // FIXME: 2020-11-19 Hello I'm a clumsy design, please do something with me

  @Getter private final String name;

  @Getter private final Provider provider;

  @Getter private final String systemId;

  @Getter private final String configuration = "";

  @Getter private final String language;

  @Getter private final boolean neural = false;

  @Getter private final Gender gender = Gender.NEUTRAL;

  @SneakyThrows
  @Override
  public InputStream mp3Stream(Voice voice, String text) {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers1 = new HttpHeaders();
    headers1.set("Ocp-Apim-Subscription-Key", System.getenv("MICROSOFT_API_KEY"));
    HttpEntity<String> entity1 = new HttpEntity<>("", headers1);
    String accessToken = restTemplate.postForObject(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".api.cognitive.microsoft.com/sts/v1.0/issueToken",
        entity1, String.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/ssml+xml");
    headers.set("X-Microsoft-OutputFormat", "audio-24khz-96kbitrate-mono-mp3");
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity<String> entity = new HttpEntity<>("<speak version=\"1.0\" xml:lang=\"" + voice.getLanguage() + "\">" +
        "<voice name=\"" + voice.getSystemId() + "\">" + text + "</voice></speak>", headers); // xml:lang="languageCode"
    byte[] response = restTemplate.postForObject(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".tts.speech.microsoft.com/cognitiveservices/v1",
        entity, byte[].class);
    return new ByteArrayInputStream(response);
  }

}

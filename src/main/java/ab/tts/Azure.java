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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

  @SneakyThrows
  @Override
  public List<String> downloadVoices() {
    List<String> list = new ArrayList<>();
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers1 = new HttpHeaders();
    headers1.set("Ocp-Apim-Subscription-Key", System.getenv("MICROSOFT_API_KEY"));
    HttpEntity<String> entity1 = new HttpEntity<>("", headers1);
    String accessToken = restTemplate.postForObject(
        "https://canadacentral.api.cognitive.microsoft.com/sts/v1.0/issueToken", entity1, String.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    ResponseEntity<String> response = restTemplate.exchange(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".tts.speech.microsoft.com/cognitiveservices/voices/list",
        HttpMethod.GET, new HttpEntity<>(headers), String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    String s = objectMapper.writeValueAsString(new AzureVoiceDescription());

    List<AzureVoiceDescription> azureList;
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    azureList = objectMapper.readValue(response.getBody(), new TypeReference<List<AzureVoiceDescription>>() { });
    AzureVoiceDescription[] azureArray = objectMapper.readValue(response.getBody(), AzureVoiceDescription[].class);

    return list;
  }

  private TextToSpeech lazyBuildService() {
    return null;
  }

}

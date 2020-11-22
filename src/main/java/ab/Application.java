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

import ab.tts.Azure;
import ab.tts.Gcloud;
import ab.tts.Linux;
import ab.tts.Polly;
import ab.tts.Provider;
import ab.tts.Voice;
import ab.tts.Watson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@SpringBootApplication
public class Application {

  public static final Provider[] PROVIDERS = {new Linux(), new Watson(), new Polly(), new Gcloud(), new Azure()};

  public static final Set<Voice> VOICE_SET = Arrays.stream(PROVIDERS).map(Provider::getVoiceSet)
      .flatMap(Set::stream).collect(Collectors.toCollection(LinkedHashSet::new)); // this thing is huge, 500+ items

  @Bean
  public Map<String, Voice> voiceMap() {
    Set<String> languageCodes = Arrays.stream(new String[]{"en-US", "en-GB"}).collect(Collectors.toSet());
    Map<String, Voice> voiceMap = VOICE_SET.stream()
        .filter(v -> languageCodes.contains(v.getLanguage().toLanguageCode()))
        .filter(v -> !v.getEngine().isNeural())
        .collect(Collectors.toMap(Voice::getName, v -> v,
            (a, b) -> { log.warn("Duplicate item discarded: " + b); return a; }, LinkedHashMap::new));

    ArrayList<String> voiceNamesList = new ArrayList<>(voiceMap.keySet());
    for (int i = 0; i < voiceNamesList.size(); i++) {
      String key = voiceNamesList.get(i);
      log.info(i + ": " + key + " - " + voiceMap.get(key));
    }
    return voiceMap;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

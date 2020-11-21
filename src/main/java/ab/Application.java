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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
public class Application {

  public static final Provider[] PROVIDERS = {new Linux(), new Polly(), new Watson(), new Gcloud(), new Azure()};

  @Bean
  public Map<String, Voice> voiceMap() {
    Map<String, Voice> voiceMap = new LinkedHashMap<>();
    Arrays.stream(PROVIDERS)
        .forEachOrdered(p -> p.filter(false, "en-US,en-GB").forEach(v -> voiceMap.put(v.getId(), v)));
    voiceMap.keySet().forEach(log::info);
    return voiceMap;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

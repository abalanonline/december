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

import ab.tts.TtsService;
import ab.tts.Voice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Slf4j
@ConfigurationProperties("december")
@SpringBootApplication
public class Application {

  @Getter @Setter private String[] voiceAdd;

  @Bean
  public TtsService ttsServiceBean() {
    TtsService ttsService = new TtsService(new String[]{"en-US", "en-GB"}, false, voiceAdd);

    List<Voice> voiceList = ttsService.getVoiceList();
    for (int i = 0; i < voiceList.size(); i++) {
      Voice v = voiceList.get(i);
      log.info(i + ": " + v.getName() + " - " + v);
    }
    return ttsService;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

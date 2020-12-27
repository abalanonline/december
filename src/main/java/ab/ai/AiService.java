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

package ab.ai;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService { // FIXME: 2020-12-27 This service do not belong here
  @Value("${fileLocal:target}")
  private String fileLocal;

  @Value("${fileUrl:http://localhost}")
  private String fileUrl;

  @Value("${fileCache:target}")
  private String fileCache;

  @Value("${voice.default}")
  private String defaultVoice;

  public Pair<String, String> apply(String input, boolean generateMp3) {
    Chatbot chatbot = new Repeater();
    String output = chatbot.talk(input);
    return Pair.of(output, chatbot.pronounce(output));
  }


}

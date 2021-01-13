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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Repeater implements Chatbot {

  @Value("${repeat.stimulus:stimulus}")
  private String stimulus;

  @Value("${repeat.response:response}")
  private String response;

  private int responseLine;

  @Override
  public String talk(String s) {
    if (s.isEmpty()) {
      responseLine = 0;
    }
    if ((responseLine > 0) || (s.contains(stimulus))) {
      String[] strings = response.split("\n");
      s = strings[responseLine++];
      if (responseLine >= strings.length) {
        responseLine = 0;
      }
      return s;
    }
    return s + "?";
  }

  @Override
  public String pronounce(String s) {
    return s;
  }
}

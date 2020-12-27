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

public class Repeater implements Chatbot {
  @Override
  public String talk(String s) {
    return s + "?";
  }

  @Override
  public String pronounce(String s) {
    s = s.replace("0", " zero ");
    s = s.replace("1", " one ");
    s = s.replace("2", " two ");
    s = s.replace("3", " tree ");
    s = s.replace("4", " four ");
    s = s.replace("5", " five ");
    s = s.replace("6", " six ");
    s = s.replace("7", " seven ");
    s = s.replace("8", " eight ");
    s = s.replace("9", " nine ");
    return s;
  }
}

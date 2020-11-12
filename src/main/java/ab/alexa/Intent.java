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

package ab.alexa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Intent {
  private String name;
  private String confirmationStatus = "NONE";
  private Map<String, Slot> slots;

  public Intent(String intentName, String slotName) {
    this.name = intentName;
    slots = new LinkedHashMap<>();
    slots.put(slotName, new Slot(slotName));
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Slot {
    private String name;
    private String value;
    private String confirmationStatus = "NONE";
    private String source;

    public Slot(String name) {
      this.name = name;
    }
  }
}

/*
 * Copyright 2023 Aleksei Balan
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

package ab.spk;

import jakarta.json.JsonObject;

public interface SmartSpeaker {

  /**
   * Auto detect if json belong to this device.
   * @param jsonObject json
   * @return true if json belongs, false if not
   */
  boolean detected(JsonObject jsonObject);

  /**
   * Create new hardware dependent task from json request.
   * @param jsonObject request
   * @return task
   */
  Task newTask(JsonObject jsonObject);

}

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

import java.util.Set;

public abstract class Provider {

  /**
   * Provider must provide the service that is connected, authorized, initialized, whatever and ready to use.
   * Implementation may vary, but cached service with lazy initialization is preferable.
   * @return the service in generic class
   */
  public abstract Object getService();

  public abstract Set<Voice> filter(); // FIXME: 2020-11-15 poor name

}

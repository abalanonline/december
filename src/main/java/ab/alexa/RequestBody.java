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
import lombok.Setter;

@Getter
@Setter
public class RequestBody {
  private String type;
  private Intent intent;
  private RequestBody.Error error;

  @Getter
  @Setter
  public static class Error {
    private String type;
    private String message;
  }
}

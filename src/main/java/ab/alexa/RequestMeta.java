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

import java.util.Map;

@Getter
@Setter
public class RequestMeta {
  private String version;
  private Map<String, Object> session;
  private Map<String, Object> context;
  private RequestBody request;

  private String test1;
  private Map<String, Object> test2;
  private Map<String, String> test3;

  public String getRequestType() {
    return request.getType();
  }

  public String getIntentName() {
    return request.getIntent().getName();
  }

  public String getAnyIntentValue() {
    return request.getIntent().getSlots().entrySet().iterator().next().getValue().getValue();
  }

  public String getError() {
    return request.getError().getType() + ": " + request.getError().getMessage();
  }

}

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

package example;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class HelloTest {

  @Test
  @SuppressWarnings("unchecked")
  public void handleRequest() {
    Map<String, Object> event = Collections.singletonMap("request", Collections.singletonMap("type", "UnknownRequest"));
    Map<String, Map<String, Map<String, String>>> result = (Map) new Hello().handleRequest(event, null);
    assertTrue(result.get("response").get("outputSpeech").get("text").contains("Montreal"));
  }
}
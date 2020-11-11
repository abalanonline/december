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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class Controller {

  @Autowired
  ObjectMapper objectMapper;

  Hello hello = new Hello();

  @PostMapping("/alexa")
  public String alexa(@RequestBody String request) throws IOException {
    log.info(request);
    Map<String, Object> responseMap = hello.handleRequest(objectMapper.readValue(request, Map.class), null);
    String response = objectMapper.writeValueAsString(responseMap);
    log.info(response);
    return response;
  }

  @GetMapping("/alexa")
  public String test() {
    return "alexa";
  }

}

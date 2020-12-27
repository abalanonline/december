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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
public class AiController {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AiService aiService;

  @PostMapping("/ga/01")
  public String ga01(@RequestBody String requestString) throws IOException {
    Map<String, Map> request = objectMapper.readValue(requestString, Map.class);
    Map<String, String> session = request.get("session");
    String sessionId = session.get("id");
    Map<String, String> scene = request.get("scene");
    String sceneName = scene.get("name");
    log.info(requestString);
    String response = new String(Files.readAllBytes(Paths.get("ga"
        + ("actions.scene.START_CONVERSATION".equals(sceneName) ? '0' : '1')
        + ".json")), StandardCharsets.UTF_8);
    response = response.replace("\"example_session_id\"", "\"session_id\"");
    response = response.replace("\"session_id\"", '"' + sessionId + '"');
    return response;
  }

  @PostMapping("/ga/aiservice")
  public String aiservice(@RequestBody String requestString) throws IOException {
    log.info(requestString);

    requestString = '{' + requestString.substring(requestString.indexOf("\"scene\""));

    int i0 = requestString.indexOf("\"value\"") + 7;
    i0 = requestString.indexOf('"', i0) + 1;
    int i1 = requestString.indexOf('}', i0);
    i1 = requestString.lastIndexOf('"', i1);
    String s = requestString.substring(i0, i1);

    Pair<String, String> response = aiService.apply(s, false);

    requestString = requestString.replace("\"SLOT_UNSPECIFIED\"", "\"INVALID\"");
    requestString = requestString.substring(0, requestString.indexOf("\"user\""))
        + "\"prompt\": {\"override\": false, \"firstSimple\": {\"speech\": \""
        + response.getRight() + "\", \"text\": \""
        + response.getLeft() + "\"}}}";

    log.info(requestString);
    return requestString;
  }

  @GetMapping("/ga/{skill}")
  public String test(@PathVariable("skill") String skill) {
    return "get " + skill;
  }

}

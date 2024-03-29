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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
public class AiController {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AiService aiService;

  @Value("${mp3folder.local:target}")
  private String fileLocal;

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

  private String getUserInput(String requestString) {
    if (requestString.contains("\"actions.intent.NO_INPUT_")) {
      return "";
    }
    if (requestString.contains("\"actions.intent.MAIN\"")) {
      return "";
    }

    JsonNode jsonNode;
    try {
      jsonNode = objectMapper.readTree(requestString);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return jsonNode.get("scene").get("slots").fields().next().getValue().get("value").textValue();
  }

  private String getUserInputAl(String requestString) {
    int i0 = requestString.indexOf("\"slot\":{");
    if (i0 < 0) {
      return "";
    }
    i0 = requestString.indexOf("\"value\"", i0) + 7;
    i0 = requestString.indexOf('"', i0) + 1;
    int i1 = requestString.indexOf('"', i0);
    return requestString.substring(i0, i1);
  }

  @PostMapping("/ga/{skill}")
  public String aiservice(@RequestBody String requestString, @PathVariable("skill") String skill) {
    log.debug(requestString);
    int localeIndex = requestString.indexOf('"', requestString.indexOf("\"locale\"") + 8) + 1;
    String locale = requestString.substring(localeIndex, requestString.indexOf('"', localeIndex));

    String userInput = getUserInput(requestString);
    if (!userInput.isEmpty()) {
      log.info("g " + locale + ": " + userInput);
    }
    Pair<String, String> response = aiService.apply(skill, locale, userInput, true);
    if (!userInput.isEmpty()) {
      log.info("o: " + response.getLeft());
    }

    requestString = '{' + requestString.substring(requestString.indexOf("\"scene\""));
    requestString = requestString.replace("\"SLOT_UNSPECIFIED\"", "\"INVALID\"");
    requestString = requestString.substring(0, requestString.indexOf("\"user\""))
        + "\"prompt\": {\"override\": false, \"firstSimple\": {\"speech\": \"<speak><audio src=\\\""
        + response.getRight() + "\\\" soundLevel=\\\"+0dB\\\"/></speak>\", \"text\": \""
        + response.getLeft() + "\"}}}";

    log.debug(requestString);
    return requestString;
  }

  @PostMapping("/alexa/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) {
    log.debug(requestString);
    int localeIndex = requestString.indexOf('"', requestString.indexOf("\"locale\"") + 8) + 1;
    String locale = requestString.substring(localeIndex, requestString.indexOf('"', localeIndex));

    if (requestString.contains("\"name\":\"AMAZON.StopIntent\"") || requestString.contains("\"name\":\"AMAZON.CancelIntent\"")) {
      requestString = "{\"version\":\"1.0\",\"response\":{\"shouldEndSession\":true}}";
    } else {
      String userInput = getUserInputAl(requestString);
      if (!userInput.isEmpty()) {
        log.info("a " + locale + ": " + userInput);
      }
      Pair<String, String> response = aiService.apply(skill, locale, userInput, true);
      if (!userInput.isEmpty()) {
        log.info("o: " + response.getLeft());
      }

      requestString = "{\"version\":\"1.0\",\"response\":{\"outputSpeech\":{\"type\":\"SSML\",\"ssml\":\"" +
          "<speak><audio src=\\\"" + response.getRight() + "\\\" /></speak>" +
          "\"},\"directives\":[{\"type\":\"Dialog.ElicitSlot\",\"slotToElicit\":\"slot\",\"updatedIntent\":\n" +
          "{\"name\":\"intent\",\"confirmationStatus\":\"NONE\",\"slots\":{\"slot\":{\"name\":\"slot\",\"value\":null," +
          "\"confirmationStatus\":\"NONE\",\"source\":null}}}}],\"shouldEndSession\":false}}";
    }

    log.debug(requestString);
    return requestString;
  }

  @GetMapping({"/ga/{skill}", "/alexa/{skill}"})
  public String test(@PathVariable("skill") String skill) {
    return "get " + skill;
  }

  @GetMapping("/static/**")
  public byte[] getFile(HttpServletRequest request) {
    // mp3folder.url: http://localhost/static
    String uri = request.getRequestURI();
    Path path = Paths.get(fileLocal, uri.substring(uri.indexOf("/static") + 7));
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UncheckedIOException(uri, e);
    }
  }

}

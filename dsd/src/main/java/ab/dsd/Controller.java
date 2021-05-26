/*
 * Copyright 2021 Aleksei Balan
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

package ab.dsd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;

/**
 * location /dsd { proxy_pass http://127.0.0.1:8085/; }
 * https://192.168.0.1/dsd/a/radio
 */
@Slf4j
@RestController
public class Controller {

  public static final String A_PLAY =
      "{\n" +
      "    \"version\": \"1.0\",\n" +
      "    \"response\": {\n" +
      "        \"directives\": [\n" +
      "            {\n" +
      "                \"type\": \"AudioPlayer.Play\",\n" +
      "                \"playBehavior\": \"REPLACE_ENQUEUED\",\n" +
      "                \"audioItem\": {\n" +
      "                    \"stream\": {\n" +
      "                        \"url\": \"%s\",\n" +
      "                        \"token\": \"%s\",\n" +
      "                        \"offsetInMilliseconds\": 0\n" +
      "                    }\n" +
      "                }\n" +
      "            }\n" +
      "        ],\n" +
      "        \"shouldEndSession\": true\n" +
      "    }\n" +
      "}";

  public static final String A_STOP = "{\"version\":\"1.0\",\"response\":{\"directives\":[" +
      "{\"type\":\"AudioPlayer.Stop\"}],\"shouldEndSession\":true}}";

  public static final String A_NOP = "{\"version\":\"1.0\",\"response\":{\"shouldEndSession\":true}}";

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${web.radio:http://localhost}")
  private String webRadio;

  @PostMapping("/a/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) throws IOException {
    JsonNode requestNode = objectMapper.readTree(requestString);
    log.debug("i: " + requestString);
    String aPlay = String.format(A_PLAY, webRadio, Instant.now().toString());
    switch (requestNode.get("request").get("type").asText()) {
      case "LaunchRequest":
        return aPlay;
      case "IntentRequest":
        switch (requestNode.get("request").get("intent").get("name").asText()) {
          case "AMAZON.PauseIntent":
          case "AMAZON.CancelIntent":
            return A_STOP;
          case "AMAZON.ResumeIntent":
          case "AMAZON.NextIntent":
            return aPlay;
        }
        break;
    }
    return A_NOP;
  }

  @GetMapping({"/g/{skill}", "/a/{skill}"})
  public String test(HttpServletRequest request, @PathVariable("skill") String skill) {
    log.info("get test " + request.getRequestURI());
    return "get test " + skill;
  }

}

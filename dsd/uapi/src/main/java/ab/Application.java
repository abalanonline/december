/*
 * Copyright 2022 Aleksei Balan
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

package ab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;

@SpringBootApplication
@RestController
public class Application {

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

  @Value("${hostname}")
  private String hostname;

  public String alexa(JsonNode request) {
    String aPlay = String.format(A_PLAY, "https://" + hostname + "/audio", Instant.now().toString());
    switch (request.get("request").get("type").asText()) {
      case "LaunchRequest":
        return aPlay;
      case "IntentRequest":
        switch (request.get("request").get("intent").get("name").asText()) {
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


  @PostMapping("**")
  public String post(@RequestBody String requestString) throws IOException {
    JsonNode request = objectMapper.readTree(requestString);
    if (!request.path("request").path("type").isMissingNode()) {
      return alexa(request);
    }
    throw new IllegalStateException();
  }

  @GetMapping("**")
  public String get(HttpServletRequest request) {
    return "get test " + request.getRequestURI() + "\r\n";
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

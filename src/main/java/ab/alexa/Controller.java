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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
public class Controller {

  public static final String INTENT_NAME = "repeat";
  public static final String SLOT_NAME = "value";

  @Autowired
  ObjectMapper objectMapper;

  //Hello hello = new Hello();

  public ResponseMeta dialogPlain(String text) {
    ResponseMeta responseMeta = new ResponseMeta(text);
    responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveDialogElicitSlot(INTENT_NAME, SLOT_NAME));
    return responseMeta;
  }

  public ResponseMeta process(RequestMeta requestMeta) {
    ResponseMeta responseMeta;
    switch (requestMeta.getRequestType()) {

      case "LaunchRequest":
        return dialogPlain("What time is it?");

      case "IntentRequest":
        switch (requestMeta.getIntentName()) {
          case INTENT_NAME: return dialogPlain(requestMeta.getAnyIntentValue() + "?");
          // And stop repeating everything I say and turning it into a question.
          case "AMAZON.StopIntent": return new ResponseMeta("Goodbye");
        }

      default:
        String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
            .format(DateTimeFormatter.ofPattern("h:mm"));
        responseMeta = new ResponseMeta("In Montreal, it's " + timeInMontreal + ".");
    }
    return responseMeta;
  }

  @PostMapping("/alexa")
  public String alexa(@RequestBody String requestString) throws IOException {
    log.info(requestString);
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    ResponseMeta responseMeta = process(requestMeta);
    String responseString = objectMapper.writeValueAsString(responseMeta);

    // FIXME: 2020-11-11
    //Map<String, Object> testEvent = Collections.singletonMap("request", Collections.singletonMap("type", "LaunchRequest"));
    //responseString = objectMapper.writeValueAsString(hello.handleRequest(testEvent, null));

    log.info(responseString);
    return responseString;
  }

  @GetMapping("/alexa")
  public String test() {
    return "alexa";
  }

}

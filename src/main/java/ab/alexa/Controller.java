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

  public ResponseMeta dialogAudio(String text) {
    ResponseMeta responseMeta = new ResponseMeta(text);
    //responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveAudioPlayerPlay("https://github.com/abalanonline/december/releases/download/v0.1/a.mp3"));
    return responseMeta;
  }

  public ResponseMeta process(RequestMeta requestMeta) {
    ResponseMeta responseMeta;
    switch (requestMeta.getRequestType()) {

      case "LaunchRequest":
        log.info("s: LaunchRequest");
        return dialogPlain("What time is it?");
        //return dialogAudio("What time is it?");

      case "IntentRequest":
        switch (requestMeta.getIntentName()) {
          case INTENT_NAME:
            String input = requestMeta.getAnyIntentValue();
            log.info("i: " + input);
            return dialogPlain(input + "?");
          // And stop repeating everything I say and turning it into a question.
          case "AMAZON.StopIntent": return new ResponseMeta("Goodbye");
        }

      case "AudioPlayer.PlaybackStarted":
      case "AudioPlayer.PlaybackNearlyFinished":
      case "AudioPlayer.PlaybackFinished":
        return new ResponseMeta();

      case "System.ExceptionEncountered":
        log.error(requestMeta.getError());
        return new ResponseMeta();

      default:
        log.info(staticRequestString);
        String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
            .format(DateTimeFormatter.ofPattern("h:mm"));
        responseMeta = new ResponseMeta("In Montreal, it's " + timeInMontreal + ".");
    }
    return responseMeta;
  }

  static String staticRequestString = "";

  @PostMapping("/alexa")
  public String alexa(@RequestBody String requestString) throws IOException {
    //log.info(requestString);
    staticRequestString = requestString;
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    ResponseMeta responseMeta = process(requestMeta);
    String responseString = objectMapper.writeValueAsString(responseMeta);

    log.info(responseString);
    return responseString;
  }

  @GetMapping("/alexa")
  public String test() {
    return "alexa";
  }

}

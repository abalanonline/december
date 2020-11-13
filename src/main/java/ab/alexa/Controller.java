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

import ab.tts.Voice;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class Controller {

  @Value("${fileLocal:target}")
  private String fileLocal;

  @Value("${fileUrl:http://localhost}")
  private String fileUrl;

  public static final String INTENT_NAME = "repeat";
  public static final String SLOT_NAME = "value";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Map<String, Voice> voiceMap;

  public ResponseMeta dialogPlain(String text) {
    ResponseMeta responseMeta = new ResponseMeta(text);
    responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveDialogElicitSlot(INTENT_NAME, SLOT_NAME));
    return responseMeta;
  }

  private static int currentVoice = 1;
  public ResponseMeta sayAudio(String text) {
    Map.Entry<String, Voice> voice = (Map.Entry<String, Voice>) voiceMap.entrySet().toArray()[currentVoice - 1];
    String fileName = "/" + Instant.now().toString().replace(':', '-').replace('.', '-') + "-" + UUID.randomUUID() + ".mp3";
    voice.getValue().mp3File(text, fileLocal + fileName);
    ResponseMeta responseMeta = new ResponseMeta();
    responseMeta.getResponse().getDirectives().add(new DirectiveAudioPlayerPlay(fileUrl + fileName));
    return responseMeta;
  }

  public ResponseMeta dialogAudio(String text) {
    ResponseMeta responseMeta = new ResponseMeta(text);
    //responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveAudioPlayerPlay("https://github.com/abalanonline/december/releases/download/v0.1/a.mp3"));
    return responseMeta;
  }

  public ResponseMeta process(RequestMeta requestMeta) {

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
    }

    // default, all other types
    //log.info(staticRequestString);
    String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
        .format(DateTimeFormatter.ofPattern("h:mm"));
    return new ResponseMeta("In Montreal, it's " + timeInMontreal + ".");

  }

  public ResponseMeta thenews(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        return sayAudio("In Montreal, everything is fine.");
      case "System.ExceptionEncountered":
        log.error(requestMeta.getError());
    }
    return new ResponseMeta();
  }

  static String staticRequestString = "";

  @PostMapping("/alexa/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) throws IOException {
    staticRequestString = requestString;
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    log.info(skill + ": " + requestMeta.getRequestType());
    ResponseMeta responseMeta;
    switch (skill) {
      case "thenews": responseMeta = thenews(requestMeta); break;
      default: responseMeta = process(requestMeta);
    }

    String responseString = objectMapper.writeValueAsString(responseMeta);
    if (responseMeta.getResponse().getOutputSpeech() != null || responseMeta.getResponse().getDirectives().size() > 0) {
      log.info(responseString);
    }
    return responseString;
  }

  @GetMapping("/alexa/{skill}")
  public String test(@PathVariable("skill") String skill) {
    return "alexa " + skill;
  }

}

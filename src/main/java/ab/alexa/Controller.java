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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
public class Controller {

  @Value("${fileLocal:target}")
  private String fileLocal;

  @Value("${fileUrl:http://localhost}")
  private String fileUrl;

  public static final String INTENT_NAME = "intent";
  public static final String SLOT_NAME = "slot";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Map<String, Voice> voiceMap;

  public ResponseMeta dialogPlain(String text) {
    ResponseMeta responseMeta = text != null && !text.isEmpty() ? new ResponseMeta(text) :
        new ResponseMeta(); // which make no sense, it will be rejected by api
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

  public ResponseMeta justlisten(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        return dialogPlain("I'm listening");
      case "IntentRequest":
        String input = requestMeta.getAnyIntentValue();
        log.info("i: " + input);
        return dialogPlain("mmm");
    }
    return null;
  }

  public ResponseMeta thenews(RequestMeta requestMeta) {
    if ("LaunchRequest".equals(requestMeta.getRequestType())) {
      if (Files.exists(Paths.get("news.txt"))) {
        try {
          return sayAudio(new String(Files.readAllBytes(Paths.get("news.txt")), StandardCharsets.UTF_8));
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
      String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
          .format(DateTimeFormatter.ofPattern("h:mm"));
      return sayAudio("In Montreal, it's " + timeInMontreal + ". And everything is fine.");
    }
    return null;
  }

  private static final String[] RANDOM_GREETINGS = {
      "Hi, it's %s",
      "Hi, it's %s speaking",
      "Hi, I'm %s",
      "Hi, my name is %s",
      "Hi, %s here",
      "Hello, it's %s",
      "Hello, it's %s speaking",
      "Hello, I'm %s",
      "Hello, my name is %s",
      "Hello, %s here",
      "My name is %s",
      "My name is %s, nice to meet you",
      "This is %s"};
  public static String randomGreeting(String name) {
    return String.format(RANDOM_GREETINGS[ThreadLocalRandom.current().nextInt(RANDOM_GREETINGS.length)], name);
  }

  public ResponseMeta selectvoice(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        log.info("i: select voice");
        return dialogPlain("mmm");
      case "IntentRequest":
        String input = requestMeta.getAnyIntentValue();
        log.info("i: " + input);
        currentVoice = Integer.parseInt(input);
        return sayAudio(randomGreeting(((Map.Entry<String, Voice>) voiceMap.entrySet().toArray()[currentVoice - 1]).getKey()));
    }
    return null;
  }

  public ResponseMeta genericResponse(RequestMeta requestMeta) {
    if ("System.ExceptionEncountered".equals(requestMeta.getRequestType())) {
      log.error(requestMeta.getError());
    }
    return new ResponseMeta();
  }

  @PostMapping("/alexa/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) throws IOException {
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    log.debug("i: " + skill + " - " + requestMeta.getRequestType() + ": " + requestString);
    ResponseMeta responseMeta = null;
    switch (skill) {
      case "thenews": responseMeta = thenews(requestMeta); break;
      case "justlisten": responseMeta = justlisten(requestMeta); break;
      case "selectvoice": responseMeta = selectvoice(requestMeta); break;
    }
    if (responseMeta == null) {
      responseMeta = genericResponse(requestMeta);
    }

    String responseString = objectMapper.writeValueAsString(responseMeta);
    log.debug("o: " +
        (responseMeta.getResponse().getOutputSpeech() != null || responseMeta.getResponse().getDirectives().size() > 0 ?
        responseString : "empty response"));
    return responseString;
  }

  @GetMapping("/alexa/{skill}")
  public String test(@PathVariable("skill") String skill) {
    return "alexa " + skill;
  }

}

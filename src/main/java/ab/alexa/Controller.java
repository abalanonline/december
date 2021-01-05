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

import ab.ai.RandomGreeting;
import ab.ai.TheNewsStub;
import ab.tts.TtsService;
import ab.tts.Voice;
import ab.weather.Noaa;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class Controller {

  @Value("${fileLocal:target}")
  private String fileLocal;

  @Value("${fileUrl:http://localhost}")
  private String fileUrl;

  @Value("${fileCache:target}")
  private String fileCache;

  @Value("${voice.default}")
  private String defaultVoice;

  private int currentVoiceIndex = 0;

  public static final String INTENT_NAME = "intent";
  public static final String SLOT_NAME = "slot";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TtsService ttsService;

  @Autowired
  private Noaa noaa;

  private String currentMp3Url;

  public ResponseMeta dialogPlain(String text) {
    ResponseMeta responseMeta = text != null && !text.isEmpty() ? new ResponseMeta(text) :
        new ResponseMeta(); // which make no sense, it will be rejected by api
    responseMeta.getResponse().setShouldEndSession(false);
    responseMeta.getResponse().getDirectives().add(new DirectiveDialogElicitSlot(INTENT_NAME, SLOT_NAME));
    return responseMeta;
  }

  public ResponseMeta playMp3(String fileName) {
    ResponseMeta responseMeta = new ResponseMeta();
    currentMp3Url = fileUrl + "/" + fileName.substring(fileName.lastIndexOf('/') + 1);
    responseMeta.getResponse().getDirectives().add(new DirectiveAudioPlayerPlay(currentMp3Url));
    return responseMeta;
  }

  public ResponseMeta repeatMp3() {
    ResponseMeta response = new ResponseMeta();
    DirectiveAudioPlayerPlay directive = new DirectiveAudioPlayerPlay(currentMp3Url);
    directive.setPlayBehavior("REPLACE_ENQUEUED");
    response.getResponse().getDirectives().add(directive);
    return response;
  }

  public ResponseMeta sayAudio(String text) {
    String fileName = getCurrentVoice().mp3File(text, fileLocal.endsWith(".mp3") ? fileLocal :
            (fileLocal + "/" + Instant.now().toString().replace(':', '-').replace('.', '-') + ".mp3"));
    return playMp3(fileName);
  }

  private Voice getCurrentVoice() {
    return currentVoiceIndex == 0
        ? ttsService.getVoiceMap().get(defaultVoice)
        : ttsService.getVoiceList().get(currentVoiceIndex);
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
      return sayAudio(new TheNewsStub().talk(""));
    }
    return null;
  }

  public ResponseMeta decemberweather(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        String fileName = noaa.getMp3(
            getCurrentVoice(),
            fileLocal.endsWith(".mp3")
                ? fileLocal
                : fileLocal + "/noaa-" + Instant.now().toString().replaceAll("\\D", "-").substring(0, 19) + ".mp3",
            fileCache);
        return playMp3(fileName);
      case "AudioPlayer.PlaybackNearlyFinished":
        return repeatMp3();
    }
    return null;
  }

  static List<Integer> listVoices = null; // FIXME: 2020-12-02 static variable
  static int listVoicesCurrent = 0;
  public ResponseMeta listvoices(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        log.info("i: list voices");
        if (listVoices == null) {
          listVoices = IntStream.range(0, ttsService.getVoiceList().size()).boxed().collect(Collectors.toList());
        }
        Collections.shuffle(listVoices);
        listVoicesCurrent = -1;
      case "AudioPlayer.PlaybackNearlyFinished":
        listVoicesCurrent += 1;
        try {
          currentVoiceIndex = listVoices.get(listVoicesCurrent);
        } catch (IndexOutOfBoundsException e) {
          log.info("o: end of the list");
          return null;
        }
        Voice v = ttsService.getVoiceList().get(currentVoiceIndex);
        log.info("o: " + currentVoiceIndex + " " + v.getName() + " " + v);
        ResponseMeta response =
            sayAudio("number " + currentVoiceIndex + ", " + new RandomGreeting().talk(v.getName()) + ", ");
        DirectiveAudioPlayerPlay directive = (DirectiveAudioPlayerPlay) response.getResponse().getDirectives().get(0);
        directive.setPlayBehavior("REPLACE_ENQUEUED");
        return response;
    }
    return null;
  }

  public ResponseMeta selectvoice(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "LaunchRequest":
        log.info("i: select voice");
        return dialogPlain("mmm");
      case "IntentRequest":
        String input = requestMeta.getAnyIntentValue();
        if ("?".equals(input)) {
          return null;
        }
        log.info("i: " + input);
        currentVoiceIndex = Integer.parseInt(input);
        return sayAudio(new RandomGreeting().talk(ttsService.getVoiceList().get(currentVoiceIndex).getName()));
    }
    return null;
  }

  public ResponseMeta genericResponse(RequestMeta requestMeta) {
    switch (requestMeta.getRequestType()) {
      case "System.ExceptionEncountered":
        log.error(requestMeta.getError());
        break;
      case "IntentRequest":
        if (requestMeta.getIntentName().equals("AMAZON.PauseIntent")) {
          log.info("pause/stop");
          ResponseMeta response = new ResponseMeta();
          DirectiveAudioPlayerPlay directive = new DirectiveAudioPlayerPlay(null);
          directive.setType("AudioPlayer.Stop");
          directive.setAudioItem(null);
          response.getResponse().getDirectives().add(directive);
          return response;
        }
        break;
    }
    return new ResponseMeta();
  }

  @PostMapping("/legacy/{skill}")
  public String alexa(@RequestBody String requestString, @PathVariable("skill") String skill) throws IOException {
    RequestMeta requestMeta = objectMapper.readValue(requestString, RequestMeta.class);
    log.debug("i: " + skill + " - " + requestMeta.getRequestType() + ": " + requestString);
    ResponseMeta responseMeta = null;
    switch (skill) {
      case "listvoices": responseMeta = listvoices(requestMeta); break;
      case "thenews": responseMeta = thenews(requestMeta); break;
      case "justlisten": responseMeta = justlisten(requestMeta); break;
      case "selectvoice": responseMeta = selectvoice(requestMeta); break;
      case "decemberweather": responseMeta = decemberweather(requestMeta); break;
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

}

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

package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Hello implements RequestHandler<Map, Map> {

  public static final String INTENT_NAME = "repeat";
  public static final String SLOT_NAME = "value";

  private AudioPlayer ap = new AudioPlayer();

  @Override
  public Map<String, Object> handleRequest(Map event, Context context)
  {
    Map<String, Object> response;
    switch (((Map<String, Map<String, String>>) event).get("request").get("type")) {
      case "LaunchRequest":
        //response = responseElicitSlot("What time is it");
        response = responseAudio();
        break;
      case "IntentRequest":
        response = responseElicitSlot(((Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>) event)
            .get("request").get("intent").get("slots").get(SLOT_NAME).get("value"));
        break;
      default:
        String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
            .format(DateTimeFormatter.ofPattern("h:mm"));
        response = responseDefault("In Montreal, it's " + timeInMontreal + ".");
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("version", "1.0");
    body.put("response", response);

    return body;
  }

  public Map<String, Object> responseAudio()
  {
    Map<String, List> response = (Map) responseDefault("sound");
    response.get("directives").add(ap.playDirective());
    return (Map) response;
  }

  public Map<String, Object> responseElicitSlot(String value)
  {
    Map<String, String> outputSpeech = new LinkedHashMap<>();
    outputSpeech.put("type", "PlainText");
    outputSpeech.put("text", value + "?");

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("outputSpeech", outputSpeech);
    response.put("shouldEndSession", Boolean.FALSE);

    // directives

    Map<String, String> slot = new LinkedHashMap<>();
    slot.put("name", SLOT_NAME);
    slot.put("confirmationStatus", "NONE");

    Map<String, Object> slots = new LinkedHashMap<>();
    slots.put(SLOT_NAME, slot);

    Map<String, Object> updatedIntent = new LinkedHashMap<>();
    updatedIntent.put("name", INTENT_NAME);
    updatedIntent.put("confirmationStatus", "NONE");
    updatedIntent.put("slots", slots);

    Map<String, Object> directive = new LinkedHashMap<>();
    directive.put("type", "Dialog.ElicitSlot");
    directive.put("slotToElicit", SLOT_NAME);
    directive.put("updatedIntent", updatedIntent);

    response.put("directives", Collections.singletonList(directive));

    return response;
  }

  public Map<String, Object> responseDefault(String value)
  {
    Map<String, String> outputSpeech = new LinkedHashMap<>();
    outputSpeech.put("type", "PlainText");
    outputSpeech.put("text", value); // I can speak

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("outputSpeech", outputSpeech);
    response.put("directives", new ArrayList<>());
    response.put("shouldEndSession", Boolean.TRUE);

    return response;
  }

}

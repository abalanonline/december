/*
 * Copyright 2023 Aleksei Balan
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

package ab.spk;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import java.io.StringReader;

public class Amzn implements SmartSpeaker {
  // FIXME: 2023-02-14 accept any element names
  // TODO: 2023-02-14 no json literals in code

  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(null);

  @Override
  public boolean detected(JsonObject jsonObject) {
    JsonObject request = jsonObject.getJsonObject("request");
    return request != null && request.getString("type") != null;
  }

  @Override
  public String input(JsonObject jsonObject) {
    JsonObject intent = jsonObject.getJsonObject("request").getJsonObject("intent");
    return intent == null ? "" : intent.getJsonObject("slots").getJsonObject("slot").getString("value");
  }

  @Override
  public JsonObject output(JsonObject jsonObject, String s) {
    // https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html
    // <speak>speech<break time="3s"/>speech</speak> <break strength="strong"/> <break time="3000ms"/>
    // none x-weak weak medium strong x-strong
    String directives = "[{\"type\":\"Dialog.ElicitSlot\",\"slotToElicit\":\"slot\",\"updatedIntent\":\n" +
        "{\"name\":\"intent\",\"confirmationStatus\":\"NONE\",\"slots\":{\"slot\":{\"name\":\"slot\",\"value\":null," +
        "\"confirmationStatus\":\"NONE\",\"source\":null}}}}]";
    JsonObject outputSpeech = JSON.createObjectBuilder()
        .add("type", "SSML")
        .add("ssml", "<speak>" + s + "</speak>")
        .build();
    JsonObject response = JSON.createObjectBuilder()
        .add("outputSpeech", outputSpeech)
        .add("directives", Json.createReader(new StringReader(directives)).readArray())
        .add("shouldEndSession", false)
        .build();
    return JSON.createObjectBuilder().add("version", "1.0").add("response", response).build();
  }

  @Override
  public boolean systemRequest(JsonObject jsonObject) {
    JsonObject intent = jsonObject.getJsonObject("request").getJsonObject("intent");
    if (intent != null) {
      switch (intent.getString("name")) {
        case "AMAZON.StopIntent":
        case "AMAZON.CancelIntent":
          return true;
      }
    }
    return false;
  }

  @Override
  public JsonObject systemResponse(JsonObject jsonObject) {
    String o = "{\"version\":\"1.0\",\"response\":{\"shouldEndSession\":true}}";
    return Json.createReader(new StringReader(o)).readObject();
  }
}

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
    String o = "{\"version\":\"1.0\",\"response\":{\"outputSpeech\":{\"type\":\"SSML\",\"ssml\":\"" +
        "<speak>" + s + "</speak>" +
        "\"},\"directives\":[{\"type\":\"Dialog.ElicitSlot\",\"slotToElicit\":\"slot\",\"updatedIntent\":\n" +
        "{\"name\":\"intent\",\"confirmationStatus\":\"NONE\",\"slots\":{\"slot\":{\"name\":\"slot\",\"value\":null," +
        "\"confirmationStatus\":\"NONE\",\"source\":null}}}}],\"shouldEndSession\":false}}";
    return Json.createReader(new StringReader(o)).readObject();
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

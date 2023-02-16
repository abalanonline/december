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
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import java.io.StringReader;

public class Amzn implements SmartSpeaker {
  // FIXME: 2023-02-14 accept any element names
  // TODO: 2023-02-14 no json literals in code

  private final JsonBuilderFactory JSON;
  private final JsonArray DIRECTIVES;
  private final JsonObject END_SESSION;

  public Amzn() {
    JSON = Json.createBuilderFactory(null);
    String directives = "[{\"type\":\"Dialog.ElicitSlot\",\"slotToElicit\":\"slot\",\"updatedIntent\":\n" +
        "{\"name\":\"intent\",\"confirmationStatus\":\"NONE\",\"slots\":{\"slot\":" +
        "{\"name\":\"slot\",\"value\":null,\"confirmationStatus\":\"NONE\",\"source\":null}}}}]";
    DIRECTIVES = Json.createReader(new StringReader(directives)).readArray();
    String endSession = "{\"version\":\"1.0\",\"response\":{\"shouldEndSession\":true}}";
    END_SESSION = Json.createReader(new StringReader(endSession)).readObject();
  }

  @Override
  public boolean detected(JsonObject jsonObject) {
    JsonObject request = jsonObject.getJsonObject("request");
    return request != null && request.getString("type") != null;
  }

  @Override
  public Task newTask(JsonObject jsonObject) {
    return new AmznTask(jsonObject);
  }

  public class AmznTask implements Task {

    private final JsonObject jsonObject;
    private final String slotValue;
    private final String intentName;
    private final String session;
    private final String requestType;

    public AmznTask(JsonObject jsonObject) {
      this.jsonObject = jsonObject;
      JsonObject intent = jsonObject.getJsonObject("request").getJsonObject("intent");
      this.requestType = jsonObject.getJsonObject("request").getString("type");
      this.slotValue = intent == null ? "" : intent.getJsonObject("slots").getJsonObject("slot").getString("value");
      this.intentName = intent == null ? "" : intent.getString("name");
      this.session = jsonObject.getJsonObject("session").getString("sessionId");
    }

    @Override
    public String session() {
      return this.session;
    }

    @Override
    public String input() {
      return slotValue;
    }

    @Override
    public JsonObject output(String s) {
      // https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html
      // <speak>speech<break time="3s"/>speech</speak> <break strength="strong"/> <break time="3000ms"/>
      // none x-weak weak medium strong x-strong
      JsonObject outputSpeech = JSON.createObjectBuilder()
          .add("type", "SSML")
          .add("ssml", "<speak>" + s + "</speak>")
          .build();
      JsonObject response = JSON.createObjectBuilder()
          .add("outputSpeech", outputSpeech)
          .add("directives", DIRECTIVES)
          .add("shouldEndSession", false)
          .build();
      return JSON.createObjectBuilder().add("version", "1.0").add("response", response).build();
    }

    @Override
    public boolean systemRequest() {
      if ("SessionEndedRequest".equals(requestType)) return true;
      switch (intentName) {
        case "AMAZON.StopIntent":
        case "AMAZON.CancelIntent":
          return true;
      }
      return false;
    }

    @Override
    public JsonObject systemResponse() {
      return END_SESSION;
    }

  }
}

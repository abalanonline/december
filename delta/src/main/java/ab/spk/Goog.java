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
import jakarta.json.JsonValue;

import java.util.Map;

public class Goog implements SmartSpeaker {
  // FIXME: 2023-02-14 accept any element names

  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(null);

  @Override
  public boolean detected(JsonObject jsonObject) {
    JsonObject scene = jsonObject.getJsonObject("scene");
    return scene != null && scene.getJsonObject("slots") != null;
  }

  @Override
  public Task newTask(JsonObject jsonObject) {
    return new GoogTask(jsonObject);
  }

  public static class GoogTask implements Task {

    private final JsonObject jsonObject;
    private final JsonObject scene;
    private final Map.Entry<String, JsonValue> slotEntry;
    private final String slotValue;
    private final String intentName;
    private final String session;

    public GoogTask(JsonObject jsonObject) {
      this.jsonObject = jsonObject;
      this.scene = jsonObject.getJsonObject("scene");
      JsonObject intent = jsonObject.getJsonObject("intent");
      this.intentName = intent == null ? "" : intent.getString("name");
      this.slotEntry = scene.getJsonObject("slots").entrySet().stream().findAny().orElse(null);
      this.slotValue = slotEntry == null ? "" : slotEntry.getValue().asJsonObject().getString("value");
      this.session = jsonObject.getJsonObject("session").getString("id");
    }

    @Override
    public String session() {
      return this.session;
    }

    @Override
    public String input() {
      return intentName.equals("actions.intent.MAIN") || intentName.startsWith("actions.intent.NO_INPUT_")
          ? "" : slotValue;
    }

    @Override
    public JsonObject output(String s) {
      JsonObject scene;
      if (slotEntry != null) {
        JsonObject slot = JSON.createObjectBuilder(slotEntry.getValue().asJsonObject())
            .add("status", "INVALID").build();
        JsonObject slots = JSON.createObjectBuilder().add(slotEntry.getKey(), slot).build();
        scene = JSON.createObjectBuilder(this.scene)
            .add("slots", slots).build();
      } else {
        scene = this.scene;
      }

      // https://developers.google.com/assistant/conversational/ssml
      // <speak>speech<break time="200ms"/>speech</speak> <break time="3s"/> <break strength="weak"/>
      // none x-weak weak medium strong x-strong
      JsonObject firstSimple = JSON.createObjectBuilder()
          .add("speech", "<speak>" + s + "</speak>").add("text", s).build();
      JsonObject prompt = JSON.createObjectBuilder()
          .add("override", false).add("firstSimple", firstSimple).build();

      JsonObject output = JSON.createObjectBuilder()
          .add("scene", scene)
          .add("session", jsonObject.getJsonObject("session"))
          .add("prompt", prompt).build();
      return output;
    }

    @Override
    public boolean systemRequest() {
      return false;
    }

    @Override
    public JsonObject systemResponse() {
      return null;
    }

  }

}

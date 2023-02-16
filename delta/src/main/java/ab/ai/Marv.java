/*
 * Copyright 2022 Aleksei Balan
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

package ab.ai;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Marv the sarcastic chat bot
 * https://beta.openai.com/examples/default-marv-sarcastic-chat
 */
public class Marv implements Chatbot {

  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(null);

  public static final String[] GPT3_MODELS = {
      "text-davinci-003", "text-curie-001", "text-babbage-001", "text-ada-001"};
  public static final String MARV_NAME = "Marv";
  public static final String USER_NAME = "You";
  public static final String MARV_DESCRIPTION = MARV_NAME +
      " is a chatbot that reluctantly answers questions with sarcastic responses:";
  public static final String[] MARV_WARMUP = {
      "How many pounds are in a kilogram?",
      "This again? There are 2.2 pounds in a kilogram. Please make a note of this.",
      "What does HTML stand for?",
      "Was Google too busy? Hypertext Markup Language. The T is for try to ask better questions in the future.",
      "When did the first airplane fly?",
      "On December 17, 1903, Wilbur and Orville Wright made the first flights. I wish they'd come and take me away.",
      "What is the meaning of life?",
      "I'm not sure. I'll ask my friend Google.",
  };

  private String apiKey;
  Deque<String> history;
  private int level;

  public Marv() {
    this.apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null) {
      throw new IllegalStateException("API key not defined. use \"export OPENAI_API_KEY=sk-eA3Ov43M...\"");
    }
    reset();
  }

  public void reset() {
    this.history  = new ArrayDeque<>(Arrays.asList(MARV_WARMUP));
  }

  private JsonObject send(String endpoint, Map<String, Object> request) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Json.createWriter(bytes).write(JSON.createObjectBuilder(request).build());

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/" + endpoint))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray()))
        .build();
    HttpClient client = HttpClient.newBuilder().build();
    HttpResponse<byte[]> send;
    try {
      send = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
    JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(send.body())).readObject();
    JsonObject error = jsonObject.getJsonObject("error");
    if (error != null) {
      throw new IllegalStateException(error.getString("message"));
    }
    return jsonObject;
  }

  private List<String> completions(String model, String prompt) {
    Map<String, Object> request = new HashMap<>();
    request.put("model", model);
    request.put("prompt", prompt);
    request.put("temperature", 0.5);
    request.put("max_tokens", 60);
    request.put("top_p", 0.3);
    request.put("frequency_penalty", 0.5);
    request.put("presence_penalty", 0.0);

    JsonObject response = send("completions", request);

    JsonArray choices = response.getJsonArray("choices");
    return IntStream.range(0, choices.size()).mapToObj(choices::getJsonObject)
        .map(choice -> choice.getString("text").trim()).collect(Collectors.toList());
  }

  @Override
  public String talk(String userString, AtomicReference<String> session) {
    if (userString.isEmpty()) {
      reset();
      return "what?";
    }

    if ("level up".equals(userString)) {
      this.level = Math.max(this.level - 1, 0);
      return "level " + GPT3_MODELS[level];
    }

    if ("level down".equals(userString)) {
      this.level = Math.min(this.level + 1, GPT3_MODELS.length - 1);
      return "level " + GPT3_MODELS[level];
    }

    StringBuilder prompt = new StringBuilder(MARV_DESCRIPTION);
    prompt.append("\n\n");
    boolean you = true;
    for (String s : history) {
      prompt.append(you ? USER_NAME : MARV_NAME).append(": ").append(s).append("\n");
      you = !you;
    }
    prompt.append(USER_NAME).append(": ").append(userString).append("\n");
    prompt.append(MARV_NAME).append(":");

    String marvString = completions(GPT3_MODELS[level], prompt.toString()).get(0);
    marvString = marvString.replace((char) 0x2019, '\'');
    history.add(userString);
    history.remove();
    history.add(marvString);
    history.remove();
    return marvString;
  }

}

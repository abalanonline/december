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

package ab;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * GptClient jakarta.json flavor
 */
public class GptClient {

  private static final Logger log = Logger.getLogger(GptClient.class.getName());

  public static final String[] GPT3_MODELS = {
      "text-davinci-003", "text-curie-001", "text-babbage-001", "text-ada-001"};
  public static final String URL_LOCAL = "http://localhost:2741/";
  public static final String URL_OPENAI = "https://api.openai.com/v1/";
  private String apiKey;
  private final String gptUrl;
  private String requestModel = GPT3_MODELS[1];
  private double requestTemperature = 0.8;
  private final JsonBuilderFactory JSON;

  public GptClient() {
    this(autoDetectGptUrl(), System.getenv("OPENAI_API_KEY"));
  }

  public GptClient(String gptUrl, String apiKey) {
    this.gptUrl = gptUrl;
    this.apiKey = apiKey;
    JSON = Json.createBuilderFactory(null);
  }

  private static String autoDetectGptUrl() {
    boolean isLocal;
    try {
      String s = httpGet(URL_LOCAL + "moderations");
      isLocal = !s.isEmpty() && s.charAt(0) == '{';
    } catch (IOException | InterruptedException e) {
      if (System.getenv("OPENAI_API_KEY") == null) {
        throw new IllegalStateException("API key not defined. use \"export OPENAI_API_KEY=sk-eA3Ov43M...\"");
      }
      isLocal = false;
    }
    String gptUrl = isLocal ? URL_LOCAL : URL_OPENAI;
    log.info("GPT url: " + gptUrl);
    return gptUrl;
  }

  public GptClient model(String requestModel) {
    // @Accessors(fluent = true, prefix = "request") @Setter
    this.requestModel = requestModel;
    return this;
  }

  public GptClient temperature(double requestTemperature) {
    this.requestTemperature = requestTemperature;
    return this;
  }

  public static String httpGet(String url) throws IOException, InterruptedException {
    return HttpClient.newBuilder().build().send(
        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
        HttpResponse.BodyHandlers.ofString()).body();
  }

  private JsonObject httpPost(String endpoint, Map<String, Object> body) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Json.createWriter(bytes).write(JSON.createObjectBuilder(body).build());

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(gptUrl + endpoint))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray()))
        .build();
    HttpClient client = HttpClient.newBuilder().build();
    HttpResponse<byte[]> send;
    try {
      send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
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

  public boolean isFlagged(String s) {
    Map<String, Object> body = Collections.singletonMap("input", s);
    JsonArray results = httpPost("moderations", body).getJsonArray("results");
    boolean flagged = IntStream.range(0, results.size()).mapToObj(results::getJsonObject)
        .anyMatch(a -> a.getBoolean("flagged"));
    if (flagged) log.warning("Flagged: " + results.toString());
    return flagged;
  }

  public String completions(String prompt, int maxTokens) {
    return completions(prompt, maxTokens, 1).get(0);
  }

  public List<String> completions(String prompt, int maxTokens, int n) {
    Map<String, Object> body = new HashMap<>();
    body.put("model", this.requestModel);
    body.put("prompt", prompt);
    body.put("temperature", this.requestTemperature);
    body.put("max_tokens", maxTokens);
    body.put("n", n);
    JsonArray choices = httpPost("completions", body).getJsonArray("choices");
    return IntStream.range(0, choices.size()).mapToObj(choices::getJsonObject)
        .map(choice -> choice.getString("text")).collect(Collectors.toList());
  }

}

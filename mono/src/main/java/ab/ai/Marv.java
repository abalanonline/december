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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Marv the sarcastic chat bot
 * https://beta.openai.com/examples/default-marv-sarcastic-chat
 */
@Service
public class Marv implements Chatbot {

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

  private ObjectMapper objectMapper;
  private String apiKey;
  Deque<String> history;

  public Marv() {
    this.apiKey = System.getenv("OPENAI_API_KEY");
    this.objectMapper = new ObjectMapper();
    this.history  = new ArrayDeque<>(Arrays.asList(MARV_WARMUP));
  }

  private JsonNode send(String endpoint, Map<String, Object> request) {
    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(request);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<byte[]> httpEntity = new HttpEntity<>(bytes, httpHeaders);
    RestTemplate restTemplate = new RestTemplate();
    byte[] response = restTemplate.postForObject("https://api.openai.com/v1/" + endpoint, httpEntity, byte[].class);

    JsonNode jsonNode;
    try {
      jsonNode = objectMapper.readTree(response);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    JsonNode error = jsonNode.get("error");
    if (error != null) {
      throw new IllegalStateException(error.get("message").textValue());
    }
    return jsonNode;
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

    JsonNode response = send("completions", request);

    JsonNode choices = response.get("choices");
    return IntStream.range(0, choices.size()).mapToObj(choices::get)
        .map(choice -> choice.get("text").textValue().trim()).collect(Collectors.toList());
  }

  @Override
  public String talk(String userString) {
    if (userString.isEmpty()) return "what?";

    StringBuilder prompt = new StringBuilder(MARV_DESCRIPTION);
    prompt.append("\n\n");
    boolean you = true;
    for (String s : history) {
      prompt.append(you ? USER_NAME : MARV_NAME).append(": ").append(s).append("\n");
      you = !you;
    }
    prompt.append(USER_NAME).append(": ").append(userString).append("\n");
    prompt.append(MARV_NAME).append(":");

    String marvString = completions(GPT3_MODELS[0], prompt.toString()).get(0);
    marvString = marvString.replace((char) 0x2019, '\'');
    history.add(userString);
    history.remove();
    history.add(marvString);
    history.remove();
    return marvString;
  }

  @Override
  public String pronounce(String s) {
    return s;
  }
}

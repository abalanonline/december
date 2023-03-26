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

import ab.GptClient;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Marv the sarcastic chat bot
 * https://beta.openai.com/examples/default-marv-sarcastic-chat
 */
public class Marv implements Chatbot {

  private String USER_NAME = "You";
  private String MARV_NAME = "Marv";
  private String MARV_DESCRIPTION = MARV_NAME +
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

  private final GptClient gptClient;
  private Map<String, MarvSession> sessions;

  public Marv() {
    this.gptClient = new GptClient();
    this.sessions = new HashMap<>();
  }

  public Marv(String name, String description) {
    this();
    this.MARV_NAME = name;
    this.MARV_DESCRIPTION = description;
  }

  @Override
  public String talk(String userString, String sessionId) {
    java.util.logging.Logger.getLogger("M").warning("m: " + userString);
    if (userString.isEmpty()) {
      return sessions.containsKey(sessionId) ? "what!" : "what?";
    }
    MarvSession session = sessions.computeIfAbsent(sessionId, ms -> new MarvSession());

    StringBuilder prompt = new StringBuilder(MARV_DESCRIPTION);
    prompt.append("\n\n");
    boolean you = true;
    for (String s : session.history) {
      prompt.append(you ? USER_NAME : MARV_NAME).append(": ").append(s).append("\n");
      you = !you;
    }
    prompt.append(USER_NAME).append(": ").append(userString).append("\n");
    prompt.append(MARV_NAME).append(":");

    String marvString = gptClient.completions(prompt.toString(), 20);
    marvString = marvString.replace((char) 0x2019, '\'');
    session.history.add(userString);
    session.history.remove();
    session.history.add(marvString);
    session.history.remove();
    return marvString;
  }

  public static class MarvSession {
    Deque<String> history;

    public MarvSession() {
      this.history  = new ArrayDeque<>(Arrays.asList(MARV_WARMUP));
    }
  }

}

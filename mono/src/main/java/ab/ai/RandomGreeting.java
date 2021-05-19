/*
 * Copyright 2021 Aleksei Balan
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

import java.util.concurrent.ThreadLocalRandom;

public class RandomGreeting implements Chatbot {
  private static final String[] RANDOM_GREETINGS = {
      "Hi, it's %s",
      "Hi, it's %s speaking",
      "Hi, this is %s",
      "Hi, this is %s speaking",
      "Hi, I'm %s",
      "Hi, my name is %s",
      "Hi, %s here",
      "Hello, it's %s",
      "Hello, it's %s speaking",
      "Hello, this %s",
      "Hello, this %s speaking",
      "Hello, I'm %s",
      "Hello, my name is %s",
      "Hello, %s here",
      "My name is %s",
      "My name is %s, nice to meet you",
      "This is %s, nice to meet you",
      "This is %s speaking, hello",
      "This is %s"};

  @Override
  public String talk(String s) {
    return String.format(RANDOM_GREETINGS[ThreadLocalRandom.current().nextInt(RANDOM_GREETINGS.length)], s);
  }

  @Override
  public String pronounce(String s) {
    return s;
  }
}

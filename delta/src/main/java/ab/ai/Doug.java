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

package ab.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class Doug implements Chatbot {

  public static final Random RANDOM = ThreadLocalRandom.current();
  public static final String FIGURE = "cube";
  public static final String POETRY =
      " not in cruelty not in wrath the reaper came today" +
      " an angel visited this gray path and took the cube away";

  @Override
  public String talk(String s, AtomicReference<String> session) {
    if (s.length() < FIGURE.length()) return FIGURE;
    s = s.toLowerCase();
    StringBuilder stringBuilder = new StringBuilder(s);
    while (stringBuilder.length() < POETRY.length()) stringBuilder.append(' ').append(s);
    stringBuilder.append(POETRY);
    List<String> list = Arrays.asList(stringBuilder.toString().split("\\s+"));
    Collections.shuffle(list);
    return String.join(" ", list.subList(0, Math.min(list.size(), RANDOM.nextInt(4) + 4)));
  }
}

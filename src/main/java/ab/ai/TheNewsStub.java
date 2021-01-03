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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TheNewsStub implements Chatbot {
  @Override
  public String talk(String s) {
    if (Files.exists(Paths.get("news.txt"))) {
      try {
        return new String(Files.readAllBytes(Paths.get("news.txt")), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
        .format(DateTimeFormatter.ofPattern("h:mm"));
    return "In Montreal, it's " + timeInMontreal + ".";
  }

  @Override
  public String pronounce(String s) {
    return s;
  }
}

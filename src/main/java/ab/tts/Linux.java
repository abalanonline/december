/*
 * Copyright 2020 Aleksei Balan
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

package ab.tts;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class Linux extends Provider {

  @Getter private final Consumer<String> service = this::exec;

  @Override
  public Set<Voice> filter() {
    Set<Voice> set = new LinkedHashSet<>();
    set.add(new LinuxVoice("Linux", this, "texttospeech %1$s %2$s"));
    return set;
  }

  public void exec(String commandLine) {
    try {
      log.debug(commandLine);
      Process process = Runtime.getRuntime().exec(System.getProperty("os.name").startsWith("Windows") ?
          new String[]{"cmd", "/C", commandLine} : new String[]{"bash", "-c", commandLine});
      String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
      if (!error.isEmpty()) {
        throw new IOException(error);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command line text to speech for on-premise engines.
 *
 * Default command line is "texttospeech file.mp3.txt file.mp3" where file.mp3.txt is the source text
 * and file.mp3 is the output mp3 file to be created.
 * It is called linux because of the standard system used in on-premise machines. But it can also execute windows
 * texttospeech.exe or texttospeech.bat files if they are in the path.
 */
@Slf4j
@RequiredArgsConstructor
public class Linux extends Voice {

  private final String commandLineFormat;

  public static Map<String, Voice> voices() {
    try {
      Map<String, Voice> voiceMap = new LinkedHashMap<>();
      voiceMap.put("Linux", new Linux("texttospeech %1$s %2$s"));
      return voiceMap;
    } catch (Exception e) {
      log.warn("Failed to initialize Linux voices", e);
      return Collections.emptyMap();
    }
  }

  @Override
  public InputStream mp3Stream(String text) {
    try {
      Path tempFile = Files.createTempFile(null, ".mp3");
      mp3File(text, tempFile.toString());
      return Files.newInputStream(tempFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void mp3File(String text, String filename) {
    try {
      String textFileName = filename + ".txt";
      String commandLine = String.format(commandLineFormat, textFileName, filename);
      log.debug(commandLine);
      Files.write(Paths.get(textFileName), text.getBytes(StandardCharsets.UTF_8));
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

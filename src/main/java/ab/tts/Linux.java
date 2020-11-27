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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Command line for on-premise linux text-to-speech engine. Works on windows too.
 * Default is "texttospeech input.txt output.mp3" where texttospeech can be a batch/shell script in path doing anything
 * and input.txt and output.mp3 are a placeholders that will be replaced by real paths in the file system
 */
@Slf4j
public class Linux extends Provider {

  @Getter private final Consumer<String> service = this::exec;

  @Override
  public List<Voice> getVoiceList() {
    List<Voice> list = new ArrayList<>();
    list.add(new Voice("Linux", this, "texttospeech input.txt output.mp3", null,
        Language.fromLanguageCode("en-US"), NeuralEngine.STANDARD, Gender.NEUTRAL));
    return list;
  }

  @Override
  public List<String> downloadVoices() {
    return Collections.emptyList();
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

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    try {
      Path tempFile = Files.createTempFile(null, ".mp3");
      mp3File(voice, text, tempFile.toString());
      return Files.newInputStream(tempFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String mp3File(Voice voice, String text, String recommendedFileName) {
    if (!recommendedFileName.endsWith(".mp3")) {
      throw new IllegalArgumentException("Wrong file extension: " + recommendedFileName);
    }
    String fileName = recommendedFileName.substring(0, recommendedFileName.length() - 4)
        + "-" + toUuid(voice.toUuid() + text) + ".mp3";
    Path filePath = Paths.get(fileName);
    if (!Files.exists(filePath)) {
      try {
        String textFileName = fileName + ".txt";
        Files.write(Paths.get(textFileName), text.getBytes(StandardCharsets.UTF_8));

        String commandLineFormat = voice.getConfiguration() == null ? null : voice.getConfiguration().getCommand_line();
        commandLineFormat = commandLineFormat == null ? voice.getSystemId() : commandLineFormat;
        commandLineFormat = commandLineFormat.replace("input.txt", "%1$s").replace("output.mp3", "%2$s");
        String commandLine = String.format(commandLineFormat, textFileName, fileName); // system id is the command line
        ((Linux) voice.getProvider()).getService().accept(commandLine);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return fileName;
  }

}

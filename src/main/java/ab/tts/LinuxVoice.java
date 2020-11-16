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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LinuxVoice extends Voice {

  @Getter private final String id;

  private final Linux provider;

  private final String commandLineFormat;

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
  public String mp3File(String text, String recommendedFileName) {
    if (!recommendedFileName.endsWith(".mp3")) {
      throw new IllegalArgumentException("Wrong file extension: " + recommendedFileName);
    }
    String fileName = recommendedFileName.substring(0, recommendedFileName.length() - 4) + "-" + UUID.randomUUID() + ".mp3";
    Path filePath = Paths.get(fileName);
    if (!Files.exists(filePath)) {
      try {
        String textFileName = fileName + ".txt";
        Files.write(Paths.get(textFileName), text.getBytes(StandardCharsets.UTF_8));

        String commandLine = String.format(commandLineFormat, textFileName, fileName);
        provider.getService().accept(commandLine);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return fileName;
  }

}

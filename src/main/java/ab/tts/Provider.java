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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public abstract class Provider {

  /**
   * Provider must provide the service that is connected, authorized, initialized, whatever and ready to use.
   * Implementation may vary, but cached service with lazy initialization is preferable.
   * @return the service in generic class
   */
  public abstract Object getService();

  /**
   * In the design of v0.2 there were Set<Voice> getVoiceSet() in ab.tts.Provider
   * and Map<String, Voice> voiceMap() in Application.
   * Then Enrique and friends appeared and questioned why the names or even voices must be unique?
   * There is no obvious reason for such restriction. And v0.3 was redesigned to plain old java array/list
   */
  public abstract List<Voice> getVoiceList();

  public abstract List<String> downloadVoices();

  public abstract InputStream mp3Stream(Voice voice, String text);

  public String mp3File(Voice voice, String text, String recommendedFileName) {
    if (!recommendedFileName.endsWith(".mp3")) {
      throw new IllegalArgumentException("Wrong file extension: " + recommendedFileName);
    }
    String fileName = recommendedFileName.substring(0, recommendedFileName.length() - 4) + "-" + UUID.randomUUID() + ".mp3";
    Path filePath = Paths.get(fileName);
    if (!Files.exists(filePath)) {
      try {
        Files.write(Paths.get(fileName + ".txt"), text.getBytes(StandardCharsets.UTF_8));
        Files.copy(mp3Stream(voice, text), filePath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return fileName;
  }

}

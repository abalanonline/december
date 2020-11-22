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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Provider {

  public static final String[] LANGUAGES = {
      "en-US", "en-GB", "de-DE", "fr-FR", "es-ES", "it-IT", "ja-JP", "pt-BR", "nl-NL", "zh-CN",
      "ko-KR", "pl-PL", "en-AU", "es-US", "ru-RU", "en-IN", "fr-CA", "pt-PT", "da-DK", "tr-TR",
      "nb-NO", "hi-IN", "id-ID", "vi-VN", "cmn-CN", "es-MX", "sv-SE", "fil-PH", "ar-XA", "ro-RO", // top 30
      "bn-IN", "cmn-TW", "cs-CZ", "el-GR", "fi-FI", "gu-IN", "hu-HU", "kn-IN", "ml-IN", "sk-SK",
      "ta-IN", "te-IN", "th-TH", "uk-UA", "yue-HK", // 45
      "ar-AR", "ar-EG", "ar-SA", "bg-BG", "ca-ES", "cy-GB", "de-AT", "de-CH", "en-CA", "en-GB-WLS",
      "en-IE", "es-LA", "fr-CH", "he-IL", "hr-HR", "is-IS", "ms-MY", "sl-SI", "zh-HK", "zh-TW",
  };

  /**
   * Provider must provide the service that is connected, authorized, initialized, whatever and ready to use.
   * Implementation may vary, but cached service with lazy initialization is preferable.
   * @return the service in generic class
   */
  public abstract Object getService();

  public abstract Set<Voice> filter(boolean useNeural, String languages); // FIXME: 2020-11-15 poor name

  public abstract Map<String, Integer> getVoicesPerLanguage();

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

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

import java.util.Arrays;

public class Language {

  /**
   * List of languages supported by December.
   * It was created from the languages supported by different tts providers by applying arguable weight function.
   * This list is unreliable, but I like it for coverage/line ratio and will use it in this and future projects. ab
   */
  private static final String[] LANGUAGE_CODES = {
      "en-US", "en-GB", "de-DE", "fr-FR", "es-ES", "it-IT", "ja-JP", "pt-BR", "nl-NL", "zh-CN",
      "ko-KR", "pl-PL", "en-AU", "es-US", "ru-RU", "en-IN", "fr-CA", "pt-PT", "da-DK", "tr-TR",
      "nb-NO", "hi-IN", "id-ID", "vi-VN", "cmn-CN", "es-MX", "sv-SE", "fil-PH", "ar-XA", "ro-RO", // top 30
      "bn-IN", "cmn-TW", "cs-CZ", "el-GR", "fi-FI", "gu-IN", "hu-HU", "kn-IN", "ml-IN", "sk-SK",
      "ta-IN", "te-IN", "th-TH", "uk-UA", "yue-HK", // 45
      "ar-AR", "ar-EG", "ar-SA", "bg-BG", "ca-ES", "cy-GB", "de-AT", "de-CH", "en-CA", "en-GB-WLS",
      "en-IE", "es-LA", "fr-CH", "he-IL", "hr-HR", "is-IS", "ms-MY", "sl-SI", "zh-HK", "zh-TW",
  };

  @Getter private final int index;

  public Language(int index) {
    this.index = index;
  }

  public Language(String languageCode) {
    index = Arrays.asList(LANGUAGE_CODES).indexOf(languageCode);
    if (index < 0) {
      throw new IllegalArgumentException("language: " + languageCode);
    }
  }

  public String toLanguageCode() {
    return LANGUAGE_CODES[index];
  }

  public static Language fromLanguageCode(String languageCode) {
    return new Language(languageCode);
  }

  public static Language findFirstLanguageCode(String languageCode) {
    return new Language(Arrays.stream(LANGUAGE_CODES).filter(s -> s.startsWith(languageCode)).findFirst().get());
  }

  public String toDoubleChar() {
    return String.format("%02d", index);
  }

  public static Language fromDoubleChar(String doubleChar) {
    return new Language(Integer.parseInt(doubleChar));
  }

  @Override
  public String toString() {
    return toLanguageCode();
  }

}

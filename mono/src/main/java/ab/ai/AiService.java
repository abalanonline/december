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

package ab.ai;

import ab.tts.TtsService;
import ab.tts.Voice;
import ab.weather.Noaa;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService { // FIXME: 2020-12-27 This service do not belong here
  @Value("${mp3folder.local:target}")
  private String fileLocal;

  @Value("${mp3folder.url:http://localhost}")
  private String fileUrl;

  @Value("${mp3folder.cache:target}")
  private String fileCache;

  @Value("${voice.default}")
  private String defaultVoice;

  @Autowired
  private TtsService ttsService;

  @Autowired
  private Noaa noaa;

  @Autowired
  private Repeater repeater;

  @Autowired
  private Marv marv;

  /**
   * Example of bilingual voice configuration:
   * voice:
   *   add:
   *     - '{"name":"Default","copy":"Brian"}'
   *     - '{"name":"Default","copy":"Brandon"}'
   *     - '{"name":"Default","copy":"Linux","language":"es-ES"}'
   *   default: Default
   *
   * Brian have en-GB locale that will be accepted by any en-?? request, same with Brandon fr-CA that will accept fr-??
   * and for language-unaware linux command line engine - the language must be explicitly set
   *
   * @param skill bot/skill name
   * @param locale language locale en-US, en-AU, de-DE
   * @param input text from user
   * @param generateMp3 usually true
   * @return pair of text and mp3 url with generated audio
   */
  public Pair<String, String> apply(String skill, String locale, String input, boolean generateMp3) {
    Chatbot chatbot;
    switch (skill) {
      case "repeat": chatbot = repeater; break;
      case "weather": chatbot = noaa; break;
      case "coach": chatbot = marv; break;
      default: throw new IllegalStateException("Unknown bot/skill: " + skill);
    }
    String output = chatbot.talk(input);
    String output2 = chatbot.pronounce(output);
    if (generateMp3) {
      Voice voice = ttsService.findLocaleVoice(locale, defaultVoice);
      output2 = ttsService.multiLineCachedUrl(fileLocal, fileUrl, fileCache, voice, output2);
    }
    return Pair.of(output, output2);
  }

}

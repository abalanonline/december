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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The text-to-speech service perform the tasks of the factory, create objects, apply filters by supported language,
 * customize tts voices and do everything else that require specification of exact provider classes in tts package.
 */
public class TtsService {

  public static final Provider[] PROVIDERS = {new Linux(), new Watson(), new Polly(), new Gcloud(), new Azure()};

  @Getter private final List<Voice> systemVoiceList;

  @Getter private final List<Voice> voiceList;

  @Getter private final Map<String, Voice> voiceMap;

  public TtsService(String[] languages, boolean neural, String[] jsonAdd) {
    // generate system list
    systemVoiceList = Arrays.stream(PROVIDERS).map(Provider::getVoiceList)
        .flatMap(List::stream).collect(Collectors.toList()); // this thing is huge, 500+ items

    // apply modifications
    ObjectMapper objectMapper = new ObjectMapper();
    int systemVoiceListSize = systemVoiceList.size(); // do not apply modifications to the new list elements
    for (String json : jsonAdd == null ? new String[0] : jsonAdd) {
      VoiceConfiguration vc;
      try {
        vc = objectMapper.readValue(json, VoiceConfiguration.class);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      vc.setJson(json);
      Assert.isTrue((vc.getCopy() == null) ^ (vc.getMove() == null), "voiceAdd - should be copy or move");
      boolean doCopy = vc.getCopy() != null;
      String fromName = doCopy ? vc.getCopy() : vc.getMove();
      String provider = vc.getProvider();
      for (int i = 0; i < systemVoiceListSize; i++) {
        Voice v = systemVoiceList.get(i);
        if ((!v.getName().equals(fromName))
            || ((provider != null) && (!v.getProvider().getClass().getSimpleName().equals(provider)))) {
          continue;
        }
        if (doCopy) {
          v = new Voice(v);
          systemVoiceList.add(v);
        }
        v.setName(vc.getName() == null ? v.getName() : vc.getName());
        v.setLanguage(vc.getLanguage() == null ? v.getLanguage() : Language.fromLanguageCode(vc.getLanguage()));
        v.setConfiguration(vc);
      }
    }

    // filter
    Set<String> languageCodes = Arrays.stream(languages).collect(Collectors.toSet());

    voiceList = systemVoiceList.stream()
        .filter(v -> languageCodes.contains(v.getLanguage().toLanguageCode()))
        .filter(v -> (v.getEngine().isNeural() == neural))
        .collect(Collectors.toList());

    voiceMap = voiceList.stream().collect(Collectors.toMap(Voice::getName, v -> v, (a, b) -> a, // keep the first
        LinkedHashMap::new));
  }

}

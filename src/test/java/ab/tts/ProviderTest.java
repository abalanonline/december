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

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProviderTest {

  public static final Provider[] PROVIDERS = {new Polly(), new Watson(), new Gcloud(), new Azure()};

  private String languagesOrdered(Map<String, Integer> voicesPerLanguage) {
    List<String> languages = Arrays.asList(Provider.LANGUAGES);
    return voicesPerLanguage.entrySet().stream()
        .filter(e -> !languages.contains(e.getKey())) // not in existing list
        .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
        .map(e -> e.getKey() + "-" + e.getValue())
        .collect(Collectors.joining(","));
  }

  @Test
  @Ignore
  public void getVoicesPerLanguageTest() {

    List<String> languages = Arrays.asList(Provider.LANGUAGES);
    Map<String, Integer> mapSum = new LinkedHashMap<>();
    Map<String, Integer> mapWeighted = new LinkedHashMap<>();

    for (Provider provider : PROVIDERS) {
      //System.out.println(provider.getClass().getSimpleName());
      Map<String, Integer> voicesPerLanguage = provider.getVoicesPerLanguage();
      int weight = 1_000_000 / voicesPerLanguage.values().stream().mapToInt(Integer::intValue).sum();
      voicesPerLanguage.forEach((language, value) -> {
        mapSum.put(language, value + mapSum.getOrDefault(language, 0));
        mapWeighted.put(language, value * weight + mapWeighted.getOrDefault(language, 0));
      });
      //System.out.println(languagesOrdered(voicesPerLanguage));
    }
    // downvote
    for (Provider provider : PROVIDERS) {
      Map<String, Integer> voicesPerLanguage = provider.getVoicesPerLanguage();
      int weight = 1_000_000 / voicesPerLanguage.values().stream().mapToInt(Integer::intValue).sum();
      mapSum.keySet().forEach(l -> {
        if (!voicesPerLanguage.containsKey(l)) {
          mapSum.put(l, mapSum.get(l) - 1);
          mapWeighted.put(l, mapWeighted.get(l) - weight);
        }
      });
    }
    //System.out.println(languagesOrdered(mapSum));
    //System.out.println(languagesOrdered(mapWeighted));
    System.out.println(mapSum.keySet().stream()
        .filter(k -> !languages.contains(k)) // not in existing list
        .sorted().collect(Collectors.joining("\", \"")));
  }

}

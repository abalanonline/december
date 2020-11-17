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
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesRequest;
import software.amazon.awssdk.services.polly.model.DescribeVoicesResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Amazon Polly https://aws.amazon.com/polly/
 * Environment variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
 * https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 */
public class Polly extends Provider {

  public static final String[] CACHE = {
      "nl-NL,Lotte,s,f", "ru-RU,Maxim,s,m", "en-US,Salli,ns,f", "en-GB-WLS,Geraint,s,m", "es-US,Miguel,s,m",
      "de-DE,Marlene,s,f", "it-IT,Giorgio,s,m", "pt-PT,Ines,s,f", "arb,Zeina,s,f", "cmn-CN,Zhiyu,s,f",
      "cy-GB,Gwyneth,s,f", "is-IS,Karl,s,m", "en-US,Joanna,ns,f", "es-ES,Lucia,s,f", "pt-PT,Cristiano,s,m",
      "sv-SE,Astrid,s,f", "de-DE,Vicki,s,f", "es-MX,Mia,s,f", "it-IT,Bianca,s,f", "pt-BR,Vitoria,s,f",
      "en-IN,Raveena,s,f", "fr-CA,Chantal,s,f", "en-GB,Amy,ns,f", "en-GB,Brian,ns,m", "en-US,Kevin,n,m",
      "en-AU,Russell,s,m", "en-IN/hi-IN,Aditi,s,f", "en-US,Matthew,ns,m", "is-IS,Dora,s,f", "es-ES,Enrique,s,m",
      "de-DE,Hans,s,m", "ro-RO,Carmen,s,f", "en-US,Ivy,ns,f", "pl-PL,Ewa,s,f", "pl-PL,Maja,s,f",
      "en-AU,Nicole,s,f", "pt-BR,Camila,ns,f", "tr-TR,Filiz,s,f", "pl-PL,Jacek,s,m", "en-US,Justin,ns,m",
      "fr-FR,Celine,s,f", "en-US,Kendra,ns,f", "pt-BR,Ricardo,s,m", "da-DK,Mads,s,m", "fr-FR,Mathieu,s,m",
      "fr-FR,Lea,s,f", "da-DK,Naja,s,f", "es-US,Penelope,s,f", "ru-RU,Tatyana,s,f", "en-AU,Olivia,n,f",
      "nl-NL,Ruben,s,m", "ja-JP,Mizuki,s,f", "ja-JP,Takumi,s,m", "es-ES,Conchita,s,f", "it-IT,Carla,s,f",
      "en-US,Kimberly,ns,f", "pl-PL,Jan,s,m", "nb-NO,Liv,s,f", "en-US,Joey,ns,m", "es-US,Lupe,ns,f",
      "ko-KR,Seoyeon,s,f", "en-GB,Emma,ns,f"};
  public static final int CACHE_LANGUAGE = 0;
  public static final int CACHE_VOICE_ID = 1;
  public static final int CACHE_ENGINE = 2;

  @Getter(lazy=true) private final PollyClient service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<String> expectedLanguageSet = Arrays.stream(languages.split(",")).collect(Collectors.toSet());
    String expectedEngine = useNeural ? "n" : "s";
    Set<Voice> set = new LinkedHashSet<>();
    for (String s : CACHE) {
      String[] a = s.split(",");
      Set<String> languageSet = Arrays.stream(a[CACHE_LANGUAGE].split("/")).collect(Collectors.toSet());
      languageSet.retainAll(expectedLanguageSet);
      if (!languageSet.isEmpty() && a[CACHE_ENGINE].contains(expectedEngine)) {
        set.add(new PollyVoice(a[CACHE_VOICE_ID], this, a[CACHE_VOICE_ID]));
      }
    }
    return set;
  }

  @Override
  public List<String> downloadVoices() {
    List<String> list = new ArrayList<>();
    DescribeVoicesRequest request = DescribeVoicesRequest.builder().build();
    DescribeVoicesResponse response = getService().describeVoices(request);
    assert null == response.nextToken();
    for (software.amazon.awssdk.services.polly.model.Voice voice : response.voices()) {
      List<String> languages = new ArrayList<>();
      languages.add(voice.languageCodeAsString());
      languages.addAll(voice.additionalLanguageCodesAsStrings());
      list.add(String.join("/", languages) + "," + voice.idAsString() + "," +
          voice.supportedEnginesAsStrings().stream().map(e -> e.substring(0, 1)).collect(Collectors.joining()) + "," +
          voice.genderAsString().substring(0, 1).toLowerCase());
    }
    return list;
  }

  private PollyClient lazyBuildService() {
    return PollyClient.builder().build();
  }

}

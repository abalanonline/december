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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Microsoft Azure Text to Speech https://azure.microsoft.com/en-ca/services/cognitive-services/text-to-speech/
 * Environment variables: MICROSOFT_API_KEY, MICROSOFT_API_LOCATION
 */
public class Azure extends Provider {

  public static final String[] CACHE = {
      "ar-EG-Hoda,f", "ar-EG-SalmaNeural,f", "ar-SA-Naayf,m", "ar-SA-ZariyahNeural,f", "bg-BG-Ivan,m",
      "bg-BG-KalinaNeural,f", "ca-ES-AlbaNeural,f", "ca-ES-HerenaRUS,f", "cs-CZ-Jakub,m", "cs-CZ-VlastaNeural,f",
      "da-DK-ChristelNeural,f", "da-DK-HelleRUS,f", "de-AT-IngridNeural,f", "de-AT-Michael,m", "de-CH-Karsten,m",
      "de-CH-LeniNeural,f", "de-DE-ConradNeural,m", "de-DE-HeddaRUS,f", "de-DE-KatjaNeural,f", "de-DE-Stefan,m",
      "el-GR-AthinaNeural,f", "el-GR-Stefanos,m", "en-AU-Catherine,f", "en-AU-HayleyRUS,f", "en-AU-NatashaNeural,f",
      "en-AU-WilliamNeural,m", "en-CA-ClaraNeural,f", "en-CA-HeatherRUS,f", "en-CA-Linda,f", "en-GB-George,m",
      "en-GB-HazelRUS,f", "en-GB-LibbyNeural,f", "en-GB-MiaNeural,f", "en-GB-RyanNeural,m", "en-GB-Susan,f",
      "en-IE-EmilyNeural,f", "en-IE-Sean,m", "en-IN-Heera,f", "en-IN-NeerjaNeural,f", "en-IN-PriyaRUS,f",
      "en-IN-Ravi,m", "en-US-AriaNeural,f", "en-US-AriaRUS,f", "en-US-BenjaminRUS,m", "en-US-GuyNeural,m",
      "en-US-GuyRUS,m", "en-US-JennyNeural,f", "en-US-ZiraRUS,f", "es-ES-AlvaroNeural,m", "es-ES-ElviraNeural,f",
      "es-ES-HelenaRUS,f", "es-ES-Laura,f", "es-ES-Pablo,m", "es-MX-DaliaNeural,f", "es-MX-HildaRUS,f",
      "es-MX-JorgeNeural,m", "es-MX-Raul,m", "fi-FI-HeidiRUS,f", "fi-FI-NooraNeural,f", "fr-CA-Caroline,f",
      "fr-CA-HarmonieRUS,f", "fr-CA-JeanNeural,m", "fr-CA-SylvieNeural,f", "fr-CH-ArianeNeural,f", "fr-CH-Guillaume,m",
      "fr-FR-DeniseNeural,f", "fr-FR-HenriNeural,m", "fr-FR-HortenseRUS,f", "fr-FR-Julie,f", "fr-FR-Paul,m",
      "he-IL-Asaf,m", "he-IL-HilaNeural,m", "hi-IN-Hemant,m", "hi-IN-Kalpana,f", "hi-IN-SwaraNeural,f",
      "hr-HR-GabrijelaNeural,f", "hr-HR-Matej,m", "hu-HU-NoemiNeural,f", "hu-HU-Szabolcs,m", "id-ID-Andika,m",
      "id-ID-ArdiNeural,m", "it-IT-Cosimo,m", "it-IT-DiegoNeural,m", "it-IT-ElsaNeural,f", "it-IT-IsabellaNeural,f",
      "it-IT-LuciaRUS,f", "ja-JP-Ayumi,f", "ja-JP-HarukaRUS,f", "ja-JP-Ichiro,m", "ja-JP-KeitaNeural,m",
      "ja-JP-NanamiNeural,f", "ko-KR-HeamiRUS,f", "ko-KR-InJoonNeural,m", "ko-KR-SunHiNeural,f", "ms-MY-Rizwan,m",
      "ms-MY-YasminNeural,f", "nb-NO-HuldaRUS,f", "nb-NO-IselinNeural,f", "nl-NL-ColetteNeural,f", "nl-NL-HannaRUS,f",
      "pl-PL-PaulinaRUS,f", "pl-PL-ZofiaNeural,f", "pt-BR-AntonioNeural,m", "pt-BR-Daniel,m", "pt-BR-FranciscaNeural,f",
      "pt-BR-HeloisaRUS,f", "pt-PT-FernandaNeural,f", "pt-PT-HeliaRUS,f", "ro-RO-AlinaNeural,f", "ro-RO-Andrei,m",
      "ru-RU-DariyaNeural,f", "ru-RU-EkaterinaRUS,f", "ru-RU-Irina,f", "ru-RU-Pavel,m", "sk-SK-Filip,m",
      "sk-SK-ViktoriaNeural,f", "sl-SI-Lado,m", "sl-SI-PetraNeural,f", "sv-SE-HedvigRUS,f", "sv-SE-HilleviNeural,f",
      "ta-IN-PallaviNeural,f", "ta-IN-Valluvar,m", "te-IN-Chitra,f", "te-IN-ShrutiNeural,f", "th-TH-AcharaNeural,f",
      "th-TH-Pattara,m", "th-TH-PremwadeeNeural,f", "tr-TR-EmelNeural,f", "tr-TR-SedaRUS,f", "vi-VN-An,m",
      "vi-VN-HoaiMyNeural,f", "zh-CN-HuihuiRUS,f", "zh-CN-Kangkang,m", "zh-CN-XiaoxiaoNeural,f",
      "zh-CN-XiaoyouNeural,f", "zh-CN-Yaoyao,f", "zh-CN-YunyangNeural,m", "zh-CN-YunyeNeural,m", "zh-HK-Danny,m",
      "zh-HK-HiugaaiNeural,f", "zh-HK-TracyRUS,f", "zh-TW-HanHanRUS,f", "zh-TW-HsiaoYuNeural,f", "zh-TW-Yating,f",
      "zh-TW-Zhiwei,m"};

  @Getter(lazy=true) private final Object service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<String> expectedLanguageSet = Arrays.stream(languages.split(",")).collect(Collectors.toSet());
    Set<Voice> set = new LinkedHashSet<>();
    for (String s : CACHE) {
      s = s.substring(0, s.indexOf(','));
      String language = s.substring(0, s.lastIndexOf('-'));
      String name = s.substring(s.lastIndexOf('-') + 1);
      if (name.endsWith("RUS")) {
        name = name.substring(0, name.length() - 3);
      }
      if (name.endsWith("Neural")) {
        name = name.substring(0, name.length() - 6);
      }
      if ((s.endsWith("Neural") == useNeural) && expectedLanguageSet.contains(language)) {
        set.add(new AzureVoice(name, this, s, language));
      }
    }
    return set;
  }

  @SneakyThrows
  @Override
  public List<String> downloadVoices() {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers1 = new HttpHeaders();
    headers1.set("Ocp-Apim-Subscription-Key", System.getenv("MICROSOFT_API_KEY"));
    HttpEntity<String> entity1 = new HttpEntity<>("", headers1);
    String accessToken = restTemplate.postForObject(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".api.cognitive.microsoft.com/sts/v1.0/issueToken",
        entity1, String.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    ResponseEntity<String> response = restTemplate.exchange(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".tts.speech.microsoft.com/cognitiveservices/voices/list",
        HttpMethod.GET, new HttpEntity<>(headers), String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    AzureVoiceDescription[] azureArray = objectMapper.readValue(response.getBody(), AzureVoiceDescription[].class);
    return Arrays.stream(azureArray)
        .map(description -> description.getShortName() + "," + description.getGender().substring(0, 1).toLowerCase())
        .collect(Collectors.toList());
  }

  private TextToSpeech lazyBuildService() {
    return null;
  }

}

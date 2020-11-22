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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Microsoft Azure Text to Speech https://azure.microsoft.com/en-ca/services/cognitive-services/text-to-speech/
 * Environment variables: MICROSOFT_API_KEY, MICROSOFT_API_LOCATION
 */
public class Azure extends Provider {

  public static final Pattern ID_PATTERN = Pattern.compile("(?<language>.+)-(?<name>\\w*?)(?<engine>|RUS|Neural)");

  public static final String CACHE =
      "46sfHoda,46nfSalma,47smNaayf,47nfZariyah,48smIvan,48nfKalina,49nfAlba,49rfHerena,32smJakub,32nfVlasta," +
      "18nfChristel,18rfHelle,51nfIngrid,51smMichael,52smKarsten,52nfLeni,02nmConrad,02rfHedda,02nfKatja,02smStefan," +
      "33nfAthina,33smStefanos,12sfCatherine,12rfHayley,12nfNatasha,12nmWilliam,53nfClara,53rfHeather,53sfLinda," +
      "01smGeorge,01rfHazel,01nfLibby,01nfMia,01nmRyan,01sfSusan,55nfEmily,55smSean,15sfHeera,15nfNeerja,15rfPriya," +
      "15smRavi,00nfAria,00rfAria,00rmBenjamin,00nmGuy,00rmGuy,00nfJenny,00rfZira,04nmAlvaro,04nfElvira,04rfHelena," +
      "04sfLaura,04smPablo,25nfDalia,25rfHilda,25nmJorge,25smRaul,34rfHeidi,34nfNoora,16sfCaroline,16rfHarmonie," +
      "16nmJean,16nfSylvie,57nfAriane,57smGuillaume,03nfDenise,03nmHenri,03rfHortense,03sfJulie,03smPaul,58smAsaf," +
      "58nmHila,21smHemant,21sfKalpana,21nfSwara,59nfGabrijela,59smMatej,36nfNoemi,36smSzabolcs,22smAndika,22nmArdi," +
      "05smCosimo,05nmDiego,05nfElsa,05nfIsabella,05rfLucia,06sfAyumi,06rfHaruka,06smIchiro,06nmKeita,06nfNanami," +
      "10rfHeami,10nmInJoon,10nfSunHi,61smRizwan,61nfYasmin,20rfHulda,20nfIselin,08nfColette,08rfHanna,11rfPaulina," +
      "11nfZofia,07nmAntonio,07smDaniel,07nfFrancisca,07rfHeloisa,17nfFernanda,17rfHelia,29nfAlina,29smAndrei," +
      "14nfDariya,14rfEkaterina,14sfIrina,14smPavel,39smFilip,39nfViktoria,62smLado,62nfPetra,26rfHedvig,26nfHillevi," +
      "40nfPallavi,40smValluvar,41sfChitra,41nfShruti,42nfAchara,42smPattara,42nfPremwadee,19nfEmel,19rfSeda,23smAn," +
      "23nfHoaiMy,09rfHuihui,09smKangkang,09nfXiaoxiao,09nfXiaoyou,09sfYaoyao,09nmYunyang,09nmYunye,63smDanny," +
      "63nfHiugaai,63rfTracy,64rfHanHan,64nfHsiaoYu,64sfYating,64smZhiwei";

  @Getter(lazy=true) private final Object service = lazyBuildService();

  private Voice voice(String cache) {
    Language language = Language.fromDoubleChar(cache.substring(0, 2));
    NeuralEngine engine = NeuralEngine.fromChar(cache.charAt(2));
    Gender gender = Gender.fromChar(cache.charAt(3));
    final String engineName;
    switch (engine) {
      case STANDARD: engineName = ""; break;
      case RICHCONTEXT: engineName = "RUS"; break;
      case NEURAL: engineName = "Neural"; break;
      default: throw new IllegalArgumentException();
    }
    String name = cache.substring(4);
    return new Voice(name, this, language.toLanguageCode() + '-' + name + engineName, null, language, engine, gender);
  }

  @Override
  public Set<Voice> getVoiceSet() {
    return Arrays.stream(CACHE.split(",")).map(this::voice).collect(Collectors.toCollection(LinkedHashSet::new));
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
    return Collections.singletonList(Arrays.stream(azureArray).map(v -> {
      String systemId = v.getShortName();
      Matcher matcher = ID_PATTERN.matcher(systemId);
      boolean matches = matcher.matches();
      assert matches;
      Language language = Language.fromLanguageCode(matcher.group("language"));
      String name = matcher.group("name");
      NeuralEngine engine = NeuralEngine.fromString(matcher.group("engine"));
      Gender gender = Gender.fromString(v.getGender());
      String cache = language.toDoubleChar() + engine.toChar() + gender.toChar() + name;
      assert voice(cache).getSystemId().equals(systemId);
      return cache;
    }).collect(Collectors.joining(",")));
  }

  private TextToSpeech lazyBuildService() {
    return null;
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers1 = new HttpHeaders();
    headers1.set("Ocp-Apim-Subscription-Key", System.getenv("MICROSOFT_API_KEY"));
    HttpEntity<String> entity1 = new HttpEntity<>("", headers1);
    String accessToken = restTemplate.postForObject(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".api.cognitive.microsoft.com/sts/v1.0/issueToken",
        entity1, String.class);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/ssml+xml");
    headers.set("X-Microsoft-OutputFormat", "audio-24khz-96kbitrate-mono-mp3");
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity<String> entity = new HttpEntity<>("<speak version=\"1.0\" xml:lang=\"" + voice.getLanguage().toLanguageCode() + "\">" +
        "<voice name=\"" + voice.getSystemId() + "\">" + text + "</voice></speak>", headers); // xml:lang="languageCode"
    byte[] response = restTemplate.postForObject(
        "https://" + System.getenv("MICROSOFT_API_LOCATION") + ".tts.speech.microsoft.com/cognitiveservices/v1",
        entity, byte[].class);
    return new ByteArrayInputStream(response);
  }

}

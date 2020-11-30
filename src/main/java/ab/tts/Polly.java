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
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesRequest;
import software.amazon.awssdk.services.polly.model.DescribeVoicesResponse;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.TextType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Amazon Polly https://aws.amazon.com/polly/
 * Environment variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
 * https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 * mp3: 22050 Hz 48 kbit/s https://docs.aws.amazon.com/polly/latest/dg/API_SynthesizeSpeech.html
 */
@Slf4j
public class Polly extends Provider {

  public static final String CACHE =
      "08sfLotte,14smMaxim,00nfSalli,00sfSalli,54smGeraint,13smMiguel,02sfMarlene,05smGiorgio,17sfInes,24sfZhiyu," +
      "50sfGwyneth,60smKarl,00nfJoanna,00sfJoanna,04sfLucia,17smCristiano,26sfAstrid,02sfVicki,25sfMia,05sfBianca," +
      "07sfVitoria,15sfRaveena,16sfChantal,01nfAmy,01sfAmy,01nmBrian,01smBrian,00nmKevin,12smRussell,15sfAditi," +
      "21sfAditi,00nmMatthew,00smMatthew,60sfDora,04smEnrique,02smHans,29sfCarmen,00nfIvy,00sfIvy,11sfEwa,11sfMaja," +
      "12sfNicole,07nfCamila,07sfCamila,19sfFiliz,11smJacek,00nmJustin,00smJustin,03sfCeline,00nfKendra,00sfKendra," +
      "07smRicardo,18smMads,03smMathieu,03sfLea,18sfNaja,13sfPenelope,14sfTatyana,12nfOlivia,08smRuben,06sfMizuki," +
      "06smTakumi,04sfConchita,05sfCarla,00nfKimberly,00sfKimberly,11smJan,20sfLiv,00nmJoey,00smJoey,13nfLupe," +
      "13sfLupe,10sfSeoyeon,01nfEmma,01sfEmma";

  @Getter(lazy=true) private final PollyClient service = lazyBuildService();

  private Voice voice(String cache) {
    Language language = Language.fromDoubleChar(cache.substring(0, 2));
    NeuralEngine engine = NeuralEngine.fromChar(cache.charAt(2));
    Gender gender = Gender.fromChar(cache.charAt(3));
    String name = cache.substring(4);
    return new Voice(name, this, name, null, language, engine, gender, engine.isNeural() ? 24000 : 22050);
  }

  @Override
  public List<Voice> getVoiceList() {
    return Arrays.stream(CACHE.split(",")).map(this::voice).collect(Collectors.toList());
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
      List<String> supportedEngines = voice.supportedEnginesAsStrings();
      for (String l : languages) {
        for (String supportedEngine : supportedEngines) {
          Language language = null;
          try {
            language = Language.fromLanguageCode(l);
          } catch (IllegalArgumentException e) {
            log.warn(e.getMessage()); // arb
            continue;
          }
          NeuralEngine engine = NeuralEngine.fromString(supportedEngine);
          Gender gender = Gender.fromString(voice.genderAsString());
          String cache = language.toDoubleChar() + engine.toChar() + gender.toChar() + voice.idAsString();
          list.add(cache);
        }
      }
    }
    return Collections.singletonList(String.join(",", list));
  }

  private PollyClient lazyBuildService() {
    return PollyClient.builder().build();
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    VoiceConfiguration vc = voice.getConfiguration();
    if (vc == null) {
      vc = new VoiceConfiguration();
    }
    StringBuilder prosody = new StringBuilder();
    if (vc.getPitch() != null) {
      prosody.append(" pitch=\"").append(vc.getPitch()).append('"');
    }
    if (vc.getRate() != null) {
      prosody.append(" rate=\"").append(vc.getRate()).append('"');
    }
    if (vc.getVolume() != null) {
      prosody.append(" volume=\"").append(vc.getVolume()).append('"');
    }

    TextType textType = TextType.TEXT;
    if (prosody.length() > 0) {
      text = "<prosody" + prosody.toString() + '>' + text + "</prosody>";
      textType = TextType.SSML;
    }
    if (vc.getXml_lang() != null) {
      text = "<lang xml:lang=\"" + vc.getXml_lang() + "\">" + text + "</lang>";
      textType = TextType.SSML;
    }
    if (textType == TextType.SSML) {
      text = "<speak>" + text + "</speak>";
    }

    Engine engine = Engine.fromValue(voice.getEngine().toString().toLowerCase());
    if (engine.equals(Engine.UNKNOWN_TO_SDK_VERSION)) {
      throw new IllegalStateException("engine: " + voice.getEngine());
    }
    SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder().textType(textType)
        .text(text).engine(engine).voiceId(voice.getSystemId()).outputFormat(OutputFormat.MP3).build();
    return getService().synthesizeSpeech(request);
  }

}

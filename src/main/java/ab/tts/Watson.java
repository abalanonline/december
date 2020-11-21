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

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * IBM Watson Text to Speech https://www.ibm.com/cloud/watson-text-to-speech
 * Environment variables: IBM_API_KEY, IBM_TTS_URL
 * https://cloud.ibm.com/apidocs/text-to-speech?code=java
 */
public class Watson extends Provider {

  public static final Pattern ID_PATTERN = Pattern.compile("(?<language>[^_]+)_(?<name>\\D+)(?<version>V[23])?Voice");

  public static final String[] CACHE = {
      "en-GB_KateV3Voice,f,t,f", "pt-BR_IsabelaVoice,f,t,f", "en-US_LisaV3Voice,f,t,f", "en-US_EmilyV3Voice,f,t,f",
      "es-US_SofiaV3Voice,f,t,f", "en-GB_CharlotteV3Voice,f,t,f", "en-US_HenryV3Voice,m,t,f", "en-US_MichaelV3Voice,m,t,f",
      "en-US_OliviaV3Voice,f,t,f", "pt-BR_IsabelaV3Voice,f,t,f", "ja-JP_EmiV3Voice,f,t,f", "en-US_KevinV3Voice,m,t,f",
      "en-GB_JamesV3Voice,m,t,f", "es-LA_SofiaVoice,f,t,f", "de-DE_BirgitV3Voice,f,t,f", "de-DE_DieterV3Voice,m,t,f",
      "en-US_AllisonV3Voice,f,t,f", "de-DE_ErikaV3Voice,f,t,f", "fr-FR_NicolasV3Voice,m,t,f", "fr-FR_ReneeV3Voice,f,t,f",
      "es-LA_SofiaV3Voice,f,t,f", "es-ES_LauraV3Voice,f,t,f", "it-IT_FrancescaV3Voice,f,t,f", "es-ES_EnriqueV3Voice,m,t,f",
      "de-DE_BirgitVoice,f,t,f", "de-DE_DieterVoice,m,t,f", "it-IT_FrancescaVoice,f,t,f", "es-ES_LauraVoice,f,t,f",
      "ja-JP_EmiVoice,f,t,f", "en-US_AllisonVoice,f,t,t", "es-US_SofiaVoice,f,t,f", "en-US_LisaVoice,f,t,t",
      "en-GB_KateVoice,f,t,f", "fr-FR_ReneeVoice,f,t,f", "es-ES_EnriqueVoice,m,t,f", "en-US_MichaelVoice,m,t,t",
      "ar-AR_OmarVoice,m,f,f", "ko-KR_YoungmiVoice,f,t,f", "ko-KR_YunaVoice,f,t,f", "nl-NL_EmmaVoice,f,t,f",
      "nl-NL_LiamVoice,m,t,f", "zh-CN_LiNaVoice,f,t,f", "zh-CN_WangWeiVoice,m,t,f", "zh-CN_ZhangJingVoice,f,t,f"};

  @Getter(lazy=true) private final TextToSpeech service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<String> expectedLanguageSet = Arrays.stream(languages.split(",")).collect(Collectors.toSet());
    Set<Voice> set = new LinkedHashSet<>();
    for (String s : CACHE) {
      String voiceId = s.substring(0, s.indexOf(','));
      Matcher matcher = ID_PATTERN.matcher(voiceId);
      boolean matches = matcher.matches();
      String language = matcher.group("language");
      String name = matcher.group("name");
      String version = matcher.group("version");
      if (expectedLanguageSet.contains(language) && ((null == version) != useNeural)) {
        set.add(new WatsonVoice(name, this, voiceId, language));
      }
    }
    return set;
  }

  @Override
  public List<String> downloadVoices() {
    List<String> list = new ArrayList<>();
    for (com.ibm.watson.text_to_speech.v1.model.Voice voice : getService().listVoices().execute().getResult().getVoices()) {
      Matcher matcher = ID_PATTERN.matcher(voice.getName());
      boolean matches = matcher.matches();
      assert matches;
      String language = matcher.group("language");
      String name = matcher.group("name");
      String version = matcher.group("version");
      if ("V2".equals(version)) {
        continue; // discontinued https://cloud.ibm.com/docs/text-to-speech?topic=text-to-speech-voices
      }
      // Description
      assert voice.getDescription().replace(" ", "").startsWith(name + ":");
      assert voice.getDescription().endsWith(" " + voice.getGender() + " voice." + (null == version ? "" : " Dnn technology."));
      // Customizable
      assert voice.isCustomizable().equals(voice.getSupportedFeatures().isCustomPronunciation());
      // Customization
      assert voice.getCustomization() == null;
      // Language
      assert voice.getLanguage().equals(language);

      list.add(voice.getName() + "," + voice.getGender().substring(0, 1) + "," +
          voice.getSupportedFeatures().isCustomPronunciation().toString().substring(0, 1) + "," +
          voice.getSupportedFeatures().isVoiceTransformation().toString().substring(0, 1));
    }
    return list;
  }

  private TextToSpeech lazyBuildService() {
    TextToSpeech textToSpeech = new TextToSpeech(new IamAuthenticator(System.getenv("IBM_API_KEY")));
    textToSpeech.setServiceUrl(System.getenv("IBM_TTS_URL"));
    return textToSpeech;
  }

}

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

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Google Cloud Text-to-Speech https://cloud.google.com/text-to-speech/
 * Environment variables: GOOGLE_APPLICATION_CREDENTIALS
 * https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries
 */
public class Gcloud extends Provider {

  public static final Pattern ID_PATTERN = Pattern.compile("(?<language>.+)-(?<engine>Wavenet|Standard)-(?<name>[A-Z])");

  public static final String[] CACHE = {
      "ar-XA,bn-IN,cmn-CN,cmn-TW,cs-CZ,da-DK,de-DE,el-GR,en-AU,en-GB,en-IN,en-US,es-ES,fi-FI,fil-PH,fr-CA," +
      "fr-FR,gu-IN,hi-IN,hu-HU,id-ID,it-IT,ja-JP,kn-IN,ko-KR,ml-IN,nb-NO,nl-NL,pl-PL,pt-BR,pt-PT,ru-RU," +
      "sk-SK,sv-SE,ta-IN,te-IN,th-TH,tr-TR,uk-UA,vi-VN,yue-HK",
      "AWf0fffffffffm0ffff0fffff0f0ffffffff000fff0", "ASfffffffffff0fffffffffffffffffffffffffffff",
      "BWm0mm00m0mmmmm0fmm0m0mff0f0mmm0mm00000m0m0", "BSmmmm00m0mmmmm0fmmmm0mffmfmmmm0mm00mm0m0mm",
      "CWm0mm0mf0ffmf00mff0m0mmm0m0fmm0mf00000f0f0", "CSm0mm0m00ffmf00mff0m0mmm0m0fmm0mf00000f0ff",
      "DW00f00fm0mmfm00mmm0f0fmm0m0mff0fm00000f0m0", "DSf0f00f00mmfm00mmm0f0fmm0m0mff0fm00000f0mm",
      "EW00000fm0000f0000f0000000000ff00f00000m000", "ES00000fm0000f0000f0000000000ff00f00000m000",
      "FW000000f00f0f00000000000000000000000000000", "FS000000f00f0000000000000000000000000000000",
      "GW00000000000f00000000000000000000000000000", "GS00000000000f00000000000000000000000000000",
      "HW00000000000f00000000000000000000000000000", "HS00000000000f00000000000000000000000000000",
      "IW00000000000m00000000000000000000000000000", "IS00000000000m00000000000000000000000000000",
      "JW00000000000m00000000000000000000000000000", "JS00000000000m00000000000000000000000000000"};

  public static final String[] CUSTOM_NAMES = { // Let's give names for A, B, C, D folks
      ",Alfa,Bravo,Charlie,Delta,Echo,Foxtrot,Golf,Hotel,India,Juliett",
      "en-US,Austin,Boston,Chicago,Dallas,Elpaso,Fortworth,Greensboro,Houston,Indianapolis,Jacksonville",
      "fr-CA,Abbotsford,Brandon,Charlottetown,Drummondville,Edmonton,Fredericton,Guelph,Halifax,Iqaluit,Joliette",
      "fr-FR,Angers,Bordeaux,Caen,Dijon,Evreux,Frejus,Grenoble,Hyeres,Istres,Joué-lès-Tours",
      "en-GB,Aberdeen,Birmingham,Cardiff,Derby,Edinburgh,Fareham,Glasgow,Hereford,Inverness,Jarrow"};

  @Getter(lazy=true) private final TextToSpeechClient service = lazyBuildService();

  @Override
  public Set<Voice> filter(boolean useNeural, String languages) {
    Set<String> expectedLanguageSet = Arrays.stream(languages.split(","))
        .collect(Collectors.toCollection(LinkedHashSet::new));
    List<String> cachedLanguages = Arrays.asList(CACHE[0].split(","));
    expectedLanguageSet.retainAll(cachedLanguages);
    char expectedEngine = useNeural ? 'W' : 'S';
    Map<String, String[]> customNamesMap = new LinkedHashMap<>();
    for (String customName : CUSTOM_NAMES) {
      String[] strings = customName.split(",");
      customNamesMap.put(strings[0], strings);
    }
    Set<Voice> set = new LinkedHashSet<>();
    for (String expectedLanguage : expectedLanguageSet) {
      int languageIndex = cachedLanguages.indexOf(expectedLanguage) + 2;
      for (int i = 1; i < CACHE.length; i++) {
        String s = CACHE[i];
        if ((expectedEngine == s.charAt(1)) && (s.charAt(languageIndex) != '0')) {
          String[] customNames = customNamesMap.getOrDefault(expectedLanguage, customNamesMap.get(""));
          String customName = customNames[s.charAt(0) - 'A' + 1];
          set.add(new Voice(customName, this,
              expectedLanguage + "-" + (s.charAt(1) == 'W' ? "Wavenet" : "Standard") + "-" + s.charAt(0),
              expectedLanguage));
        }
      }
    }
    return set;
  }

  @Override
  public List<String> downloadVoices() {
    ListVoicesRequest request = ListVoicesRequest.newBuilder().build();
    ListVoicesResponse response = getService().listVoices(request);
    List<String> languageList = response.getVoicesList().stream()
        .map(v -> v.getLanguageCodes(0)).distinct().sorted().collect(Collectors.toList());
    List<String> namesList = response.getVoicesList().stream()
        .map(v -> v.getName().substring(v.getName().lastIndexOf('-') + 1))
        .distinct().sorted().collect(Collectors.toList());
    char[][][] contains = new char[2][namesList.size()][languageList.size()];
    for (char[][] engine : contains) {
      for (char[] name : engine) {
        Arrays.fill(name, '-');
      }
    }
    for (com.google.cloud.texttospeech.v1.Voice voice : response.getVoicesList()) {
      Matcher matcher = ID_PATTERN.matcher(voice.getName());
      boolean matches = matcher.matches();
      assert matches;
      String language = matcher.group("language");
      if ("nb-no".equals(language)) {
        continue; // small mistake here (;-_-)
      }
      String name = matcher.group("name");
      String engineName = matcher.group("engine");
      assert languageList.contains(language) : language;
      assert namesList.contains(name);
      int engine = "Wavenet".equals(engineName) ? 0 : "Standard".equals(engineName) ? 1 : -1;
      assert engine >= 0;
      assert contains[engine][namesList.indexOf(name)][languageList.indexOf(language)] == '-';
      String g = voice.getSsmlGender().toString();
      contains[engine][namesList.indexOf(name)][languageList.indexOf(language)] =
          voice.getSsmlGender().toString().toLowerCase().charAt(0);
    }
    List<String> list = new ArrayList<>();
    list.add(String.join(",", languageList));
    for (int name = 0; name < namesList.size(); name++) {
      for (int engine = 0; engine < 2; engine++) {
        list.add(namesList.get(name) + (engine == 0 ? "W" : "S") + new String(contains[engine][name]));
      }
    }
    return list;
  }

  private TextToSpeechClient lazyBuildService() {
    try {
      return TextToSpeechClient.create();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    VoiceSelectionParams voiceSelectionParams = VoiceSelectionParams.newBuilder()
        .setLanguageCode(voice.getLanguage())
        .setName(voice.getSystemId())
        .build();
    SynthesizeSpeechResponse response = ((Gcloud) voice.getProvider()).getService().synthesizeSpeech(
        SynthesisInput.newBuilder().setText(text).build(), voiceSelectionParams,
        AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build());
    return new ByteArrayInputStream(response.getAudioContent().toByteArray());
  }

}

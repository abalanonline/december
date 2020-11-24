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
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Google Cloud Text-to-Speech https://cloud.google.com/text-to-speech/
 * Environment variables: GOOGLE_APPLICATION_CREDENTIALS
 * https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries
 */
@Slf4j
public class Gcloud extends Provider {

  public static final Pattern ID_PATTERN = Pattern.compile("(?<language>.+)-(?<engine>Wavenet|Standard)-(?<name>[A-Z])");

  public static final String[] CACHE = {
      "As0fffffff00fff0fffffffffff0f0f0fffffffffffffff", "Awmfff0ffff0fff0fffffffffff0fff00ffff0f00f000f0",
      "Bsmmmmmff0m0fmm0mmmm0mmmmmm00fm0mm000m0mm0mm00m", "Bwmmmmmff0m0fmm0mmmm0mmmmmm00fm00m0000000000000",
      "Csff0f0mm0m0mmf0fmfmmffmmfm00mm00m000000000000f", "Cwffff0mm0m0mmf0fmfmmffmmfm00mm00m0000000000000",
      "Dsmm0m0mm0f0mfm0mfmfffmffmf00mf000000000000000m", "Dwmmmm0mm0f0mfm0mfmfffmffmf00m00000000000000000",
      "Esf0mf0000f00f00f000fm0000000000000000000000000", "Ewf0mf0000f00f00f000fm0000000000000000000000000",
      "Fs0ff000000000000000000000000000000000000000000", "Fwfff000000000000000000000000000000000000000000",
      "Gsf00000000000000000000000000000000000000000000", "Gwf00000000000000000000000000000000000000000000",
      "Hsf00000000000000000000000000000000000000000000", "Hwf00000000000000000000000000000000000000000000",
      "Ism00000000000000000000000000000000000000000000", "Iwm00000000000000000000000000000000000000000000",
      "Jsm00000000000000000000000000000000000000000000", "Jwm00000000000000000000000000000000000000000000"};

  public static final String[] CUSTOM_NAMES = { // Let's give names for A, B, C, D folks
      ",Alfa,Bravo,Charlie,Delta,Echo,Foxtrot,Golf,Hotel,India,Juliett",
      "en-US,Austin,Boston,Chicago,Dallas,Elpaso,Fortworth,Greensboro,Houston,Indianapolis,Jacksonville",
      "fr-CA,Abbotsford,Brandon,Charlottetown,Drummondville,Edmonton,Fredericton,Guelph,Halifax,Iqaluit,Joliette",
      "fr-FR,Angers,Bordeaux,Caen,Dijon,Evreux,Frejus,Grenoble,Hyeres,Istres,Joué-lès-Tours",
      "en-GB,Aberdeen,Birmingham,Cardiff,Derby,Edinburgh,Fareham,Glasgow,Hereford,Inverness,Jarrow"};

  public static final char NOT_EXIST = '0';

  @Getter(lazy=true) private final TextToSpeechClient service = lazyBuildService();

  private String systemId(Language language, NeuralEngine engine, char name) {
    final String engineName;
    switch (engine) {
      case STANDARD: engineName = "Standard"; break;
      case WAVENET: engineName = "Wavenet"; break;
      default: throw new IllegalArgumentException();
    }
    return language.toLanguageCode() + "-" + engineName + "-" + name;
  }

  @Override
  public List<Voice> getVoiceList() {
    // London Tokyo
    Map<String, Map<Character, String>> customNamesMap = new LinkedHashMap<>();
    for (String customName : CUSTOM_NAMES) {
      String[] strings = customName.split(",");
      Map<Character, String> map = new LinkedHashMap<>();
      for (int i = 1; i < strings.length; i++) {
        map.put((char) ('A' + i - 1), strings[i]);
      }
      customNamesMap.put(strings[0], map);
    }

    List<Voice> list = new ArrayList<>();
    for (String cache : CACHE) {
      char charName = cache.charAt(0);
      NeuralEngine engine = NeuralEngine.fromChar(cache.charAt(1));
      for (int i = 2; i < cache.length(); i++) {
        char c = cache.charAt(i);
        if (c == NOT_EXIST) {
          continue;
        }
        Gender gender = Gender.fromChar(c);
        Language language = new Language(i - 2);
        list.add(new Voice(
            customNamesMap.getOrDefault(language.toLanguageCode(), customNamesMap.get("")).get(charName),
            this, systemId(language, engine, charName), null, language, engine, gender));
      }
    }
    return list;
  }

  @Override
  public List<String> downloadVoices() {
    // make request
    List<com.google.cloud.texttospeech.v1.Voice> voicesList =
        getService().listVoices(ListVoicesRequest.newBuilder().build()).getVoicesList();

    // language size, name list
    int languageListSize = voicesList.stream().map(v -> v.getLanguageCodesList()).flatMap(List::stream)
        .mapToInt(s -> Language.fromLanguageCode(s).getIndex()).max().orElse(0) + 1;
    List<String> namesList = voicesList.stream()
        .map(v -> v.getName().substring(v.getName().lastIndexOf('-') + 1))
        .distinct().sorted().collect(Collectors.toList());
    // make array, fill with dashes
    char[][][] contains = new char[NeuralEngine.values().length][namesList.size()][languageListSize];
    Arrays.stream(contains).forEach(e -> Arrays.stream(e).forEach(n -> Arrays.fill(n, NOT_EXIST)));

    // fill the array
    for (com.google.cloud.texttospeech.v1.Voice voice : voicesList) {
      Matcher matcher = ID_PATTERN.matcher(voice.getName());
      boolean matches = matcher.matches();
      assert matches;
      Language language = null;
      try {
        language = Language.fromLanguageCode(matcher.group("language"));
      } catch (IllegalArgumentException e) {
        log.warn(e.getMessage()); // nb-no
        continue;
      }
      String name = matcher.group("name");
      assert name.length() == 1;
      assert namesList.contains(name);
      NeuralEngine engine = NeuralEngine.fromString(matcher.group("engine"));
      assert systemId(language, engine, name.charAt(0)).equals(voice.getName()); // system name can be recreated
      assert contains[engine.ordinal()][namesList.indexOf(name)][language.getIndex()] == NOT_EXIST;
      Gender gender = Gender.fromString(voice.getSsmlGender().toString());
      contains[engine.ordinal()][namesList.indexOf(name)][language.getIndex()] = gender.toChar();
    }

    // array to string list
    List<String> list = new ArrayList<>();
    for (int name = 0; name < namesList.size(); name++) {
      for (NeuralEngine engine : NeuralEngine.values()) {
        boolean empty = true;
        for (char c : contains[engine.ordinal()][name]) {
          empty &= (c == NOT_EXIST);
        }
        if (!empty) {
          list.add(namesList.get(name) + engine.toChar() + new String(contains[engine.ordinal()][name]));
        }
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
        .setLanguageCode(voice.getLanguage().toLanguageCode())
        .setName(voice.getSystemId())
        .build();
    SynthesizeSpeechResponse response = ((Gcloud) voice.getProvider()).getService().synthesizeSpeech(
        SynthesisInput.newBuilder().setText(text).build(), voiceSelectionParams,
        AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build());
    return new ByteArrayInputStream(response.getAudioContent().toByteArray());
  }

}

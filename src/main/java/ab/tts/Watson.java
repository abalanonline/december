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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * IBM Watson Text to Speech https://www.ibm.com/cloud/watson-text-to-speech
 * Environment variables: IBM_API_KEY, IBM_TTS_URL
 * https://cloud.ibm.com/apidocs/text-to-speech?code=java
 */
public class Watson extends Provider {

  public static final Pattern ID_PATTERN = Pattern.compile("(?<language>[^_]+)_(?<name>\\D+)(?<engine>|V2|V3)Voice");

  public static final String CACHE =
      "563fSofia,07sfIsabela,003mKevin,013fKate,063fEmi,033fRenee,003fLisa,053fFrancesca,133fSofia,043mEnrique," +
      "003fOlivia,013fCharlotte,073fIsabela,043fLaura,003mMichael,023fErika,003mHenry,003fEmily,033mNicolas," +
      "013mJames,023mDieter,56sfSofia,003fAllison,023fBirgit,02sfBirgit,00sfAllison,00sfLisa,00smMichael,03sfRenee," +
      "04smEnrique,13sfSofia,05sfFrancesca,06sfEmi,04sfLaura,02smDieter,01sfKate,45smOmar,10sfYoungmi,10sfYuna," +
      "08sfEmma,08smLiam,09sfLiNa,09smWangWei,09sfZhangJing";

  @Getter(lazy=true) private final TextToSpeech service = lazyBuildService();

  private Voice voice(String cache) {
    Language language = Language.fromDoubleChar(cache.substring(0, 2));
    NeuralEngine engine = NeuralEngine.fromChar(cache.charAt(2));
    Gender gender = Gender.fromChar(cache.charAt(3));
    final String engineName;
    switch (engine) {
      case STANDARD: engineName = ""; break;
      case V3: engineName = "V3"; break;
      default: throw new IllegalArgumentException();
    }
    String name = cache.substring(4);
    return new Voice(name, this, language.toLanguageCode() + '_' + name + engineName + "Voice",
        null, language, engine, gender);
  }

  @Override
  public List<Voice> getVoiceList() {
    return Arrays.stream(CACHE.split(",")).map(this::voice).collect(Collectors.toList());
  }

  @Override
  public List<String> downloadVoices() {
    List<String> list = new ArrayList<>();
    for (com.ibm.watson.text_to_speech.v1.model.Voice voice : getService().listVoices().execute().getResult().getVoices()) {
      Matcher matcher = ID_PATTERN.matcher(voice.getName());
      boolean matches = matcher.matches();
      assert matches;
      Language language = Language.fromLanguageCode(matcher.group("language"));
      String name = matcher.group("name");
      String version = matcher.group("engine");
      if ("V2".equals(version)) {
        continue; // discontinued https://cloud.ibm.com/docs/text-to-speech?topic=text-to-speech-voices
      }
      NeuralEngine engine = NeuralEngine.fromString(version);
      Gender gender = Gender.fromString(voice.getGender());
      String cache = language.toDoubleChar() + engine.toChar() + gender.toChar() + name;
      assert voice(cache).getSystemId().equals(voice.getName());
      list.add(cache);
    }
    return Collections.singletonList(String.join(",", list));
  }

  private TextToSpeech lazyBuildService() {
    TextToSpeech textToSpeech = new TextToSpeech(new IamAuthenticator(System.getenv("IBM_API_KEY")));
    textToSpeech.setServiceUrl(System.getenv("IBM_TTS_URL"));
    return textToSpeech;
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    VoiceConfiguration vc = voice.getConfiguration();
    if (vc == null) {
      vc = new VoiceConfiguration();
    }
    StringBuilder voiceTransformation = new StringBuilder();
    if (vc.getPitch() != null) {
      voiceTransformation.append(" pitch=\"").append(vc.getPitch()).append('"');
    }
    if (vc.getPitch_range() != null) {
      voiceTransformation.append(" pitch_range=\"").append(vc.getPitch_range()).append('"');
    }
    if (vc.getGlottal_tension() != null) {
      voiceTransformation.append(" glottal_tension=\"").append(vc.getGlottal_tension()).append('"');
    }
    if (vc.getBreathiness() != null) {
      voiceTransformation.append(" breathiness=\"").append(vc.getBreathiness()).append('"');
    }
    if (vc.getRate() != null) {
      voiceTransformation.append(" rate=\"").append(vc.getRate()).append('"');
    }
    if (vc.getTimbre() != null) {
      voiceTransformation.append(" timbre=\"").append(vc.getTimbre()).append('"');
    }
    if (vc.getTimbre_extent() != null) {
      voiceTransformation.append(" timbre_extent=\"").append(vc.getTimbre_extent()).append('"');
    }
    if (voiceTransformation.length() > 0) {
      text = "<voice-transformation type=\"Custom\"" + voiceTransformation.toString() + '>'
          + text + "</voice-transformation>";
    }
    SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
        .text(text).voice(voice.getSystemId()).accept("audio/mp3").build();
    return getService().synthesize(synthesizeOptions).execute().getResult();
  }

}

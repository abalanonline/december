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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generator of noise, silence and tones.
 */
public class Noise extends Provider {

  public static final Pattern SSML_BREAK = Pattern.compile("<speak><break time=\"(?<duration>\\d+)ms\"/></speak>");

  public static final String[][] SILENCE_MP3 = { // I have no idea what I'm doing, these are from the silent mp3 files.
      {"22050", "FFF231C47366FF0FC0025C00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
          "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"},
      {"24000", "FFF234C0AB75FF0E40025C00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
          "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"}};

  private final Map<Integer, byte[]> silenceMp3;

  private static byte[] hexStringToByteArray(String hex) {
    byte[] unsignedBytes = new BigInteger("10" + hex, 16).toByteArray();
    return Arrays.copyOfRange(unsignedBytes, 1, unsignedBytes.length);
  }

  public Noise() {
    silenceMp3 = new HashMap<>();
    for (String[] rate : SILENCE_MP3) {
      silenceMp3.put(Integer.valueOf(rate[0]), hexStringToByteArray(rate[1]));
    }
  }

  @Override
  public Object getService() {
    return null;
  }

  @Override
  public List<Voice> getVoiceList() {
    return null;
  }

  @Override
  public List<String> downloadVoices() {
    return null;
  }

  private boolean isProcessable(String text) {
    return SSML_BREAK.matcher(text).matches();
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    if (!isProcessable(text)) {
      return null;
    }

    Matcher matcher = SSML_BREAK.matcher(text);
    matcher.matches();
    int durationMs = Integer.parseInt(matcher.group("duration"));

    // 576 samples per frame, https://www.codeproject.com/Articles/8295/MPEG-Audio-Frame-Header
    int frames = (int) Math.round(voice.getSampleRate() * durationMs / 576000.0);
    byte[] frameContent = silenceMp3.get(voice.getSampleRate());
    
    byte[] response = new byte[frameContent.length * frames];
    for (int i = 0; i < response.length; i += frameContent.length) {
      System.arraycopy(frameContent, 0, response, i, frameContent.length);
    }
    return new ByteArrayInputStream(response);
  }

  @Override
  public String mp3File(Voice voice, String text, String recommendedFileName) {
    return isProcessable(text) ? super.mp3File(voice, text, recommendedFileName) : null;
  }

}

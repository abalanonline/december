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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class Voice {

  public static final Provider PROVIDER_NOISE = new Noise();

  private String name;

  private final Provider provider;

  private final String systemId;

  private VoiceConfiguration configuration;

  private Language language;

  private final NeuralEngine engine;

  private final Gender gender;

  private final int sampleRate; // Hertz

  public Voice(Voice v) { // copy constructor
    this.name = v.name;
    this.provider = v.provider;
    this.systemId = v.systemId;
    this.configuration = v.configuration;
    this.language = v.language;
    this.engine = v.engine;
    this.gender = v.gender;
    this.sampleRate = v.sampleRate;
  }

  public InputStream mp3Stream(String text) {
    final InputStream noise = PROVIDER_NOISE.mp3Stream(this, text);
    return noise == null ? getProvider().mp3Stream(this, text) : noise;
  }

  public String mp3File(String text, String recommendedFileName) {
    String noise = PROVIDER_NOISE.mp3File(this, text, recommendedFileName);
    if (noise == null) {
      noise = getProvider().mp3File(this, text, recommendedFileName);
    }
    String noise48 = noise + ".mp3";
    try {
      // The bit rate must be 48 kbps
      // https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#audio
      Process process = Runtime.getRuntime().exec(new String[]{"lame", "--resample", "22050", "--quiet", "-b", "48", noise, noise48});
      String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
      if (!error.isEmpty()) {
        throw new IOException(error);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return noise48;
  }

  /**
   * To UUID method perform tasks similar to hashCode but better.
   * It remains consistent between application executions. (Not necessary between builds)
   * The size is 128 bit which is 4x to java hashCode.
   * If two objects are not equal their UUIDs will be different.
   * @return hash code UUID
   */
  public UUID toUuid() {
    return Provider.toUuid(name + '/' + provider.getClass().getSimpleName() + '/' + systemId + '/'
        + language.getIndex() + '/' + engine.ordinal() + '/' + gender.ordinal() + '/'
        + (configuration == null ? "" : configuration.getJson()));
  }

  @Override
  public String toString() {
    return configuration == null
        ? "Voice{" + provider.getClass().getSimpleName() + ":" + systemId + '}'
        : "Voice{" + name + ":configured}";
  }
}

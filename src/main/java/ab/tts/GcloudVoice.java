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
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class GcloudVoice extends Voice {

  @Getter private final String id;

  private final Gcloud provider;

  @Override
  public InputStream mp3Stream(String text) {
    VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
        .setLanguageCode("en-US")
        .build();
    SynthesizeSpeechResponse response = provider.getService().synthesizeSpeech(
        SynthesisInput.newBuilder().setText(text).build(), voice,
        AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build());
    return new ByteArrayInputStream(response.getAudioContent().toByteArray());
  }

}

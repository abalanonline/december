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

package ab.alexa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DirectiveAudioPlayerPlay {
  private String type = "AudioPlayer.Play";
  private String playBehavior = "REPLACE_ALL";
  private AudioItem audioItem;

  public DirectiveAudioPlayerPlay(String url) {
    this.audioItem = new AudioItem(url);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class AudioItem {
    private Stream stream;
    private Map<String, String> metadata = new LinkedHashMap<>();

    public AudioItem(String url) {
      this.stream = new Stream(url);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Stream {
      private String url;
      private String token = Instant.now().toString();
      private long offsetInMilliseconds;

      public Stream(String url) {
        this.url = url;
      }

    }
  }
}

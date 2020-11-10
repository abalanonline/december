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

package example;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioPlayer {
  public Map playDirective() {
    Map<String, Object> stream = new LinkedHashMap<>();
    stream.put("url", "https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_700KB.mp3");
    stream.put("token", Instant.now().toString());
    stream.put("offsetInMilliseconds", 0L);

    Map<String, Object> audioItem = new LinkedHashMap<>();
    audioItem.put("stream", stream);
    audioItem.put("metadata", new LinkedHashMap<>());

    Map<String, Object> directive = new LinkedHashMap<>();
    directive.put("type", "AudioPlayer.Play");
    directive.put("playBehavior", "REPLACE_ALL");
    directive.put("audioItem", audioItem);

    return directive;
  }
}

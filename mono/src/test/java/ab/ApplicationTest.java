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

package ab;

import ab.tts.Provider;
import ab.tts.TtsService;
import ab.tts.Voice;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationTest {

  @Ignore
  @Test
  public void voiceMap() {
    String text = "Man is distinguished, not only by his reason, but by this singular passion from other animals, " +
        "which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable " +
        "generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
    String[] jsonAdd = {
        "{\"copy\":\"Guy\",\"name\":\"Test1\"}",
        "{\"copy\":\"Brian\",\"name\":\"Test2\"}"};
    Map<String, Voice> voiceMap = new TtsService(new String[]{"en-US", "en-GB"}, false, jsonAdd).getVoiceMap();
    voiceMap.get("Test1").mp3File("<speak><break time=\"10000ms\"/></speak>", "target/test0.mp3");
    voiceMap.get("Test2").mp3File("<speak><break time=\"10000ms\"/></speak>", "target/test0.mp3");
    voiceMap.get("Test1").mp3File(text, "target/test1.mp3");
    voiceMap.get("Test2").mp3File(text, "target/test2.mp3");
  }

  @Ignore
  @Test
  public void providerTest() {
    //System.out.println(new Azure().downloadVoices().stream().collect(Collectors.joining("\", \"")));
    Voice voice = (Voice) new ab.tts.Azure().getVoiceList().toArray()[0];
    voice.mp3File("Success is a lousy teacher. It seduces smart people into thinking they can't lose.", "target/azure.mp3");
  }

  @Ignore
  @Test
  public void downloadVoices() {
    for (Provider provider : TtsService.PROVIDERS) {
      System.out.println("\"" + provider.downloadVoices().stream().collect(Collectors.joining("\", \"")) + "\"");
    }
  }

}

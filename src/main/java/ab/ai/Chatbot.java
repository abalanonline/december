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

package ab.ai;

public interface Chatbot {

  /**
   * Respond something to simulate a conversation.
   * @param s free form text string from user
   * @return chatbot text response that will appear on the screen
   */
  String talk(String s);

  /**
   * Modify text to be properly pronounced by tts engine.
   * @param s chatbot text response
   * @return text that will be passed to tts to create an audio response
   */
  String pronounce(String s);

}

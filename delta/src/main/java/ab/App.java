/*
 * Copyright 2023 Aleksei Balan
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

import ab.ai.Chatbot;
import ab.ai.Doug;
import ab.ai.Marv;
import ab.spk.Amzn;
import ab.spk.Goog;
import ab.spk.SmartSpeaker;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class App extends Application {

  @Produces
  Chatbot chatbot() {
    Chatbot chatbot = new Doug();
    try {
      chatbot = new Marv();
    } catch (IllegalStateException ignore) {
      // do nothing
    }
    return chatbot;
  }

  @Produces
  SmartSpeaker[] smartSpeakers() {
    return new SmartSpeaker[]{new Amzn(), new Goog()};
  }

}

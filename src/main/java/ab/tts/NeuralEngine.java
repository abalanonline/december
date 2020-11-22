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

public enum NeuralEngine {

  STANDARD, NEURAL, WAVENET, V3, RICHCONTEXT;

  public char toChar() {
    return this == V3 ? '3' : this.toString().toLowerCase().charAt(0);
  }

  public static NeuralEngine fromChar(char e) {
    switch (e) {
      case 's': return NeuralEngine.STANDARD;
      case 'n': return NeuralEngine.NEURAL;
      case 'w': return NeuralEngine.WAVENET;
      case '3': return NeuralEngine.V3;
      case 'r': return NeuralEngine.RICHCONTEXT;
    }
    throw new IllegalArgumentException("engine: " + e);
  }

  public static NeuralEngine fromString(String engine) {
    switch (engine) {
      case "":
      case "standard":
      case "Standard": return NeuralEngine.STANDARD;
      case "Wavenet": return NeuralEngine.WAVENET;
      case "RUS": return NeuralEngine.RICHCONTEXT;
      case "neural":
      case "Neural": return NeuralEngine.NEURAL;
      case "V3": return NeuralEngine.V3;
    }
    throw new IllegalArgumentException("engine: " + engine);
  }

}

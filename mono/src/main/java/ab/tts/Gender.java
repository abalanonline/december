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

public enum Gender {

  MALE, FEMALE, NEUTRAL;

  public char toChar() {
    return this.toString().toLowerCase().charAt(0);
  }

  public static Gender fromChar(char g) {
    switch (g) {
      case 'M':
      case 'm': return Gender.MALE;
      case 'F':
      case 'f': return Gender.FEMALE;
      case 'N':
      case 'n': return Gender.NEUTRAL;
    }
    throw new IllegalArgumentException("gender: " + g);
  }

  public static Gender fromString(String gender) {
    switch (gender.toLowerCase()) {
      case "male": return Gender.MALE;
      case "female": return Gender.FEMALE;
      case "neutral": return Gender.NEUTRAL;
    }
    throw new IllegalArgumentException("gender: " + gender);
  }

}

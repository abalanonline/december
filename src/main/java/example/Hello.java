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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class Hello implements RequestHandler<Map, Map> {
  @Override
  public Map<String, Object> handleRequest(Map event, Context context)
  {
    String timeInMontreal = LocalTime.now(ZoneId.of("America/Montreal"))
        .format(DateTimeFormatter.ofPattern("h:mm"));
    String text = "In Montreal, it's " + timeInMontreal + ".";
    Map<String, String> outputSpeech = new LinkedHashMap<>();
    outputSpeech.put("type", "PlainText");
    outputSpeech.put("text", text);

    Map<String, Object> reprompt = new LinkedHashMap<>();
    reprompt.put("outputSpeech", outputSpeech);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("outputSpeech", outputSpeech);
    response.put("reprompt", reprompt);
    response.put("shouldEndSession", Boolean.FALSE);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("version", "1.0");
    body.put("response", response);

    return body;
  }
}

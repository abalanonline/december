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

import ab.ai.Doug;
import ab.spk.Amzn;
import ab.spk.Goog;
import ab.spk.SmartSpeaker;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;

@Path("/")
public class Controller {

  public static final SmartSpeaker[] SPEAKERS = {new Amzn(), new Goog()};

  @GET
  public String getTime() {
    return "time " + Instant.now();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response post(JsonObject jsonObject) {
    try {
      for (SmartSpeaker speaker : SPEAKERS) {
        if (speaker.detected(jsonObject)) {
          JsonObject output;
          if (speaker.systemRequest(jsonObject)) {
            output = speaker.systemResponse(jsonObject);
          } else {
            String input = speaker.input(jsonObject);
            output = speaker.output(jsonObject, new Doug().talk(input, null));
          }
          return Response.status(Response.Status.OK).entity(output).build();
        }
      }
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}

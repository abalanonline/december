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
import ab.spk.SmartSpeaker;
import ab.spk.Task;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Singleton // TODO: 2023-02-14 use jakarta sessions
@Path("/")
public class Controller {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("Controller");

  public static final String SYNC_ON = "orange";
  public static final String SYNC_OFF = "hello";

  @Inject
  Chatbot chatbot;

  @Inject
  SmartSpeaker[] SPEAKERS;

  private HashMap<String, Boolean> syncing = new HashMap<>();

  @GET
  public String getTime() {
    return "time " + Instant.now();
  }

  // FIXME: 2023-02-15 make a bean class for this method
  private JsonObject completeTask(Task task) {
    if (task.systemRequest()) return task.systemResponse();
    String session = task.session();
    log.warning("s: " + session);
    String input = task.input();
    log.warning("i: " + input);

    // sync logic
    boolean sync0 = input.toLowerCase().equals(SYNC_ON);
    boolean sync1 = input.toLowerCase().equals(SYNC_OFF);
    boolean sync = syncing.getOrDefault(session, false);
    if (sync) {
      input = "<break time=\""+ (ThreadLocalRandom.current().nextInt(2000) + 300) +"ms\"/>" + SYNC_ON;
      if (sync0) {
        syncing.put(session, false);
        input = SYNC_OFF;
      }
      if (sync1) {
        syncing.put(session, false);
        sync = false;
        input = SYNC_OFF; // let it start the dialog
      }
    } else {
      if (sync0) {
        syncing.put(session, true);
        sync = true;
        input = "<break time=\"5s\"/>" + SYNC_ON;
      }
    }

    // chatbot
    String output;
    if (sync) {
      output = input;
    } else {
      output = chatbot.talk(input, session);
    }

    // output
    log.warning("o: " + output);
    return task.output(output);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response post(JsonObject jsonObject) {
    try {
      for (SmartSpeaker speaker : SPEAKERS) {
        if (speaker.detected(jsonObject)) {
          Task task = speaker.newTask(jsonObject);
          JsonObject completedTask = completeTask(task);
          return Response.status(Response.Status.OK).entity(completedTask).build();
        }
      }
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}

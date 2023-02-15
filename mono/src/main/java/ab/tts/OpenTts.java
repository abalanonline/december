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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * OpenTTS https://github.com/synesthesiam/opentts
 * This project of high quality must be supported
 *
 * fr-FR quality: marytts:enst-dennys-hsmm
 * fr-FR performance: nanotts:fr-FR
 * en-EN perf: festival:us3_mbrola
 */
@Slf4j
public class OpenTts extends Provider {

  public static final String OPENTTS_URL = "http://localhost:5500/api/";

  @Getter private final Object service = null;

  private Language getLanguageCode(OpenTtsVoiceDescription voiceDescription) {
    String locale = voiceDescription.getLocale();
    int i = locale.indexOf('-');
    if (i < 0) {
      i = locale.length();
    }
    locale = locale.substring(0, i).toLowerCase() + locale.substring(i).toUpperCase();
    try {
      return Language.fromLanguageCode(locale);
    } catch (IllegalArgumentException e) {
      // do nothing, next try with short code
    }
    try {
      return Language.findFirstLanguageCode(locale);
    } catch (NoSuchElementException e) {
      log.debug("OpenTts language not found: {}", locale);
      return null;
    }
  }

  @Override
  public List<Voice> getVoiceList() {

    ParameterizedTypeReference<Map<String, OpenTtsVoiceDescription>> mapOpenTts =
        new ParameterizedTypeReference<Map<String, OpenTtsVoiceDescription>>() {};

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map<String, OpenTtsVoiceDescription>> response;
    try {
      response = restTemplate.exchange(OPENTTS_URL + "voices", HttpMethod.GET, null, mapOpenTts);
    } catch (RestClientException e) {
      return Collections.emptyList(); // could not connect - no service, return empty response
    }
    return response.getBody().entrySet().stream()
        .filter(e -> getLanguageCode(e.getValue()) != null)
        .map(e -> new Voice(e.getKey(), this, e.getKey(), null, getLanguageCode(e.getValue()),
            NeuralEngine.STANDARD, Gender.fromChar(e.getValue().getGender().charAt(0)), 22050)).collect(Collectors.toList());

  }

  @Override
  public List<String> downloadVoices() {
    return Collections.emptyList();
  }

  @Override
  public InputStream mp3Stream(Voice voice, String text) {
    try {
      Path tempFile = Files.createTempFile(null, ".mp3");
      mp3File(voice, text, tempFile.toString());
      return Files.newInputStream(tempFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String mp3File(Voice voice, String text, String recommendedFileName) {
    if (!recommendedFileName.endsWith(".mp3")) {
      throw new IllegalArgumentException("Wrong file extension: " + recommendedFileName);
    }
    String fileName = recommendedFileName.substring(0, recommendedFileName.length() - 4)
        + "-" + toUuid(voice.toUuid() + text) + ".mp3";
    Path filePath = Paths.get(fileName);
    if (!Files.exists(filePath)) {
      try {
        String textFileName = fileName + ".txt";
        Files.write(Paths.get(textFileName), text.getBytes(StandardCharsets.UTF_8));

        String wavFileName = fileName + ".wav";
        byte[] wavAudio = new RestTemplate()
            .getForObject(OPENTTS_URL + "tts?voice={1}&text={2}", byte[].class, voice.getSystemId(), text);
        Files.write(Paths.get(wavFileName), wavAudio);

        try {
          // The bit rate must be 48 kbps
          // https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#audio
          Process process = Runtime.getRuntime().exec(new String[]{"ffmpeg", "-loglevel", "error", "-i", wavFileName, "-ar", "22050", "-ab", "48k", "-y", fileName});
          String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
          if (!error.isEmpty()) {
            throw new IOException(error);
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }


//        String commandLineFormat = voice.getConfiguration() == null ? null : voice.getConfiguration().getCommand_line();
//        commandLineFormat = commandLineFormat == null ? voice.getSystemId() : commandLineFormat;
//        commandLineFormat = commandLineFormat.replace("input.txt", "%1$s").replace("output.mp3", "%2$s");
//        String commandLine = String.format(commandLineFormat, textFileName, fileName); // system id is the command line
//        ((Linux) voice.getProvider()).getService().accept(commandLine);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return fileName;
  }

  @Getter
  @Setter
  public static class OpenTtsVoiceDescription {
    private String gender;
    private String id;
    private String language;
    private String locale;
    private String name;
    private String tts_name;
  }

}

#!/bin/sh
date +"year %Y %B %d time %H:%M" | flite --setf duration_stretch=1.2 -o en/audio.wav
textfr=$(date +"temps %H heure %M minutes")
curl --data-urlencode "INPUT_TYPE=TEXT" --data-urlencode "OUTPUT_TYPE=AUDIO" --data-urlencode "AUDIO=WAVE_FILE" \
  --data-urlencode "LOCALE=fr" --data-urlencode "VOICE=upmc-pierre-hsmm" \
  --data-urlencode "INPUT_TEXT=$textfr" http://localhost:59125/process > fr/audio.wav

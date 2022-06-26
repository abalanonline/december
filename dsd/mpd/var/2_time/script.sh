#!/bin/sh
date +"year %Y %B %d time %H:%M" | flite --setf duration_stretch=1.2 -o en/audio.wav

#!/bin/sh
echo "You are listening to the online radio. December stream driver" | flite -voice awb --setf duration_stretch=1.2 -o audio.wav

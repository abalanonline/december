#!/bin/sh
service mpd start
service mpd status
cat /etc/services | flite -o /var/lib/mpd/music/audio.wav
mpc update --wait
mpc insert audio.wav
mpc repeat on
mpc play
#read -p "Press enter to continue" ignore
tail -f /var/log/mpd/mpd.log

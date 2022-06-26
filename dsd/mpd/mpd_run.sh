#!/bin/sh
ffmpeg -f lavfi -i anullsrc -t 60 -y /var/lib/mpd/music/silence.mp3
mpc update --wait
mpc insert silence.mp3
mpc repeat on
mpc play

wget -O /var/local/music.mp3 https://audionautix.com/Music/Cycles.mp3
cat /etc/services | flite -o /var/local/audio.wav

sh /hourly.sh
echo "*/10 * * * * sh /hourly.sh" | crontab -
tail -f /var/log/mpd/mpd.log

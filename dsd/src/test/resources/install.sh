sudo apt install mpd mpc
sudo vi /etc/mpd.conf
# uncomment audio_output httpd
sudo systemctl restart mpd
sudo cp *.mp3 /var/lib/mpd/music/
mpc update
mpc add 1.mp3
mpc add 2.mp3
mpc repeat on
mpc replaygain track
mpc play

sudo vi /etc/nginx/sites-enabled/default
# location /radio { proxy_pass http://127.0.0.1:8000/; }
sudo systemctl restart nginx

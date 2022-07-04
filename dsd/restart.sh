export HOSTNAME=example.com
# docker build -t dsmpd .
docker run -d --rm --name dsmpd --network=host dsmpd
docker run -d --rm --name dsuapi -p 8080:8080 dsuapi $HOSTNAME
docker run -it --rm --name certbot -p 80:80 -v "/etc/letsencrypt:/etc/letsencrypt" \
  certbot/certbot certonly --register-unsafely-without-email --standalone -d $HOSTNAME
docker run -it --rm --name certbot -p 80:80 -v "/etc/letsencrypt:/etc/letsencrypt" \
  certbot/certbot renew --cert-name $HOSTNAME
docker run -d --rm --name nginx --network=host -v "/etc/letsencrypt:/etc/letsencrypt" \
  -v $PWD/nginx/:/etc/nginx/templates/ -e HOSTNAME=$HOSTNAME nginx:1.23-alpine
docker run -d --rm --name marytts -p 59125:59125 synesthesiam/marytts:5.2
#curl "http://localhost:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&AUDIO=WAVE_FILE&LOCALE=fr&VOICE=upmc-pierre-hsmm&INPUT_TEXT=Bienvenue%20dans%20le%20monde%20de%20la%20synth%C3%A8se%20de%20la%20parole!" | aplay

# MaryTTS compatible
#docker run -d --rm --name marytts -p 59125:59125 mycroftai/mimic3
#curl "http://localhost:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&AUDIO=WAVE_FILE&LOCALE=en_UK&VOICE=en_UK/apope_low&INPUT_TEXT=hello%20world" | aplay
#docker run -d --rm --name marytts -p 59125:5500 synesthesiam/opentts:all # warning 10GB
#curl "http://localhost:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&AUDIO=WAVE_FILE&LOCALE=en_US&VOICE=nanotts:en-US&INPUT_TEXT=hello%20world" | aplay
#curl --data-urlencode "INPUT_TYPE=TEXT" --data-urlencode "OUTPUT_TYPE=AUDIO" --data-urlencode "AUDIO=WAVE_FILE" --data-urlencode "LOCALE=en_US" --data-urlencode "VOICE=nanotts:en-US" --data-urlencode "INPUT_TEXT=<speed level=\"70\">hello world" http://localhost:59125/process | aplay

export HOSTNAME=example.com
# docker build -t dsmpd .
# docker run -d --rm --name dsmpd -p 8000:8000 dsmpd
docker run -it --rm --name certbot -p 80:80 -v "/etc/letsencrypt:/etc/letsencrypt" \
  certbot/certbot certonly --register-unsafely-without-email --standalone -d $HOSTNAME
docker run -d --rm --name nginx -p 443:443 -v "/etc/letsencrypt:/etc/letsencrypt" \
  -v $PWD/nginx/:/etc/nginx/templates/ -e HOSTNAME=$HOSTNAME nginx:1.23-alpine

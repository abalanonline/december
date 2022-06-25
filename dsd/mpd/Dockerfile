FROM debian:11.3-slim

RUN apt-get update &&\
  apt-get install -y mpd mpc flite &&\
  rm -rf /var/lib/apt/lists/*

RUN apt-get update &&\
  apt-get install -y bash procps mc vim &&\
  rm -rf /var/lib/apt/lists/*
# FIXME: 2022-06-25 delete this

COPY mpd_run.sh .
COPY mpd_httpd.conf /etc/

RUN echo "#!/bin/sh\n\
service mpd start\n\
exec \"\$@\"" > /usr/local/bin/docker-entrypoint.sh &&\
  chmod a+x /usr/local/bin/docker-entrypoint.sh &&\
  echo 'include "/etc/mpd_httpd.conf"' >> /etc/mpd.conf

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["sh", "mpd_run.sh"]

EXPOSE 8000

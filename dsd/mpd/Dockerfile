FROM alpine:3.16

RUN apk add mpd mpc flite ffmpeg --no-cache
RUN apk add bash mc --no-cache # FIXME: 2022-06-25 delete this line

COPY mpd_run.sh .
COPY hourly.sh /
COPY mpd_httpd.conf /etc/
COPY var /var/local/dsd

RUN echo -e "#!/bin/sh\n\
setcap -r /usr/bin/mpd\n\
crond\n\
mpd\n\
exec \"\$@\"" > /usr/local/bin/docker-entrypoint.sh &&\
  chmod a+x /usr/local/bin/docker-entrypoint.sh &&\
  echo 'include "/etc/mpd_httpd.conf"' >> /etc/mpd.conf

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["sh", "mpd_run.sh"]

EXPOSE 8000

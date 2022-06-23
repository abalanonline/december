FROM debian:11.3

RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections &&\
  apt-get update &&\
  apt-get -y install mpd mpc flite mc vim &&\
  echo 'include "/mpd_httpd.conf"' >> /etc/mpd.conf &&\
  rm -rf /var/lib/apt/lists/* &&\
  rm -rf chrome-linux*

COPY mpd_run.sh .
COPY mpd_httpd.conf .

CMD ["sh", "mpd_run.sh"]

EXPOSE 8000

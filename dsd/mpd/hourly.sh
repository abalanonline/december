#!/bin/sh

pwd=""

play () {
  local pwdsave=$pwd
  [ -n "$pwd" ] && pwd="$pwd/"
  pwd="$pwd$1"
  echo cd ---- $pwd
  cd "$1"
  for f in *; do
    case "$f" in
      *.sh) echo run --- $f; sh "$f"; break;;
    esac
  done
  local audio=""
  for f in *; do
    case "$f" in
      *.mp3) ;;
      *.wav) ;;
      *) f="";;
    esac
    [ -n "$f" ] && audio="$audio `echo -n $f | base64`"
  done
  if [ -n "$audio" ]; then
    audio=`shuf -n1 -e $audio`
    audio=`echo -n $audio | base64 -d`
    echo play -- $audio
    local f="`echo "$pwd" | tr / _`_$audio"
    cp "$audio" "/var/lib/mpd/music/$f"
    mpc update --wait
    mpc add "$f"
  fi
  for f in *; do
    [ -d "$f" ] && play "$f"
  done
  cd ..
  pwd=$pwdsave
}

mpc play 1
mpc crop
play /var/local/dsd
mpc next
#mpc stop

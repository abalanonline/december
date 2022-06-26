#!/bin/sh

pwd=""

# recursive update of audio content, made in three steps
play () {
  local pwdsave=$pwd
  [ -n "$pwd" ] && pwd="$pwd/"
  pwd="$pwd$1"
  echo cd ---- $pwd
  cd "$1"
  # 1. run one .sh script in the folder, choose first
  for f in *; do
    case "$f" in
      *.sh) echo run --- $f; sh "$f"; break;;
    esac
  done
  # 2. add one audio file to the playlist, choose random
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
  # 3. repeat the same for all the sub-folders
  for f in *; do
    [ -d "$f" ] && play "$f"
  done
  cd ..
  pwd=$pwdsave
}

# first track is silence, it always kept in the playlist
mpc play 1
mpc crop
play /var/local/dsd

# it is possible to add a static mp3 file, start its playback with "mpc next"
# and then proceed to generating of the slow neural speech
# this code will conditionally click "mpc next" if it was not clicked before
currenttrack=`mpc | head -n 2 | tail -n 1 | awk -F '#' '{print $2}' | awk -F '/' '{print $1}'`
echo $currenttrack
[ "1" = "$currenttrack" ] && mpc next
#mpc stop

#!/bin/sh
service nginx start
if [ ! -d "/mnt/hub" ]; then
  echo model cache not mounted to /mnt exiting
  exit 1
fi
opyrator launch-api app:completions

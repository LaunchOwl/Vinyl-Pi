#!/bin/bash
# Start ffserver stream and ffmpeg feed
ffserver -f /etc/ffserver_vinylpi.conf -d > /dev/null 2>&1 & ffmpeg -vn -f alsa -i hw:0 http://localhost:8090/pi-feed.ffm > /dev/null 2>&1 &

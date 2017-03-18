#!/bin/bash
# Start ffserver stream and ffmpeg feed
node bin/node-app/app.js &
icecast -c /etc/icecast.xml &
ffmpeg -vn -f alsa -i hw:0 -codec:a libvorbis -b:a 128k -f ogg - | ezstream -qvc /etc/ezstream_pi_vorbis.xml

#!/usr/bin/env bash

filename=$1
#fileid="1yPTGcPe5DZd3ubzAgUBp3aAQRAOK9eKQ"
fileid=$2
rm -rf ./cookie
curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=${fileid}" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=${fileid}" -o ${filename}
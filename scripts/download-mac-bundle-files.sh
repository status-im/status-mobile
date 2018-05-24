#!/bin/bash

filename="StatusIm.app.zip"
fileid="1yPTGcPe5DZd3ubzAgUBp3aAQRAOK9eKQ"
curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=${fileid}" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=${fileid}" -o ${filename}
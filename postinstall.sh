#!/bin/sh

# rn-nodeify
./node_modules/.bin/rn-nodeify --install --hack;

# symlink for re-natal
if ! [ -f re-natal ]; then 
  ln -s ./node_modules/re-natal/index.js re-natal; 
else
  echo "exists"
fi

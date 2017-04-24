#!/bin/sh

# rn-nodeify
# temporary hack due to https://github.com/facebook/react-native/issues/4968
./node_modules/.bin/rn-nodeify --install --hack;
npm install --save react@16.0.0-alpha.6;
npm install --save react-native-tcp@3.2.1;
npm install --save react-native-udp@2.0.0;

# symlink for re-natal
if ! [ -f re-natal ]; then 
  ln -s ./node_modules/re-natal/index.js re-natal; 
else
  echo "re-natal exists"
fi
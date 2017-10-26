#!/bin/sh

npm install --save react@16.0.0;
npm install --save react-native-tcp@3.2.1;
ln -s ../node_modules/react-native-mapbox-gl/ios/Mapbox.framework ios;

# symlink for re-natal
if ! [ -f re-natal ]; then
  ln -s ./node_modules/re-natal/index.js re-natal;
else
  echo "re-natal exists"
fi

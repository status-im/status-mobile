#!/bin/sh

# rn-nodeify
# temporary hack due to https://github.com/facebook/react-native/issues/4968
./node_modules/.bin/rn-nodeify --install --hack;
npm install --save react@15.3.1;
npm install --save react-native-tcp@2.0.4;

# symlink for re-natal
if ! [ -f re-natal ]; then 
  ln -s ./node_modules/re-natal/index.js re-natal; 
else
  echo "re-natal exists"
fi

# symlink for status-dev-cli
if ! [ -f status-dev-cli ]; then
  ln -s ./node_modules/status-dev-cli/index.js status-dev-cli;
else
  echo "status-dev-cli exists"
fi
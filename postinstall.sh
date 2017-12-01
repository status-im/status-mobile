#!/bin/sh

# rn-nodeify
# temporary hack due to https://github.com/facebook/react-native/issues/4968
./node_modules/.bin/rn-nodeify --install "assert,zlib,buffer,inherits,console,constants,crypto,dns,domain,events,http,https,os,path,process,punycode,querystring,fs,stream,string_decoder,timers,tty,url,util,net,vm" --hack;
npm install --save react@16.0.0-alpha.6;
npm install --save react-native-tcp@3.2.1;

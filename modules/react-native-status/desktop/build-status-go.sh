#!/bin/bash

 export GOROOT=$1
 export GOPATH=$2
 export PATH=$GOROOT/bin:$GOROOT:$GOPATH:$PATH
 
 cd $3/lib
 go get .
 cd ..
 make statusgo-library
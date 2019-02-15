#!/bin/bash

export GOROOT=$2
export GOPATH=$3
export PATH=$GOROOT/bin:$GOROOT:$GOPATH:$PATH
if [ "$1" = 'Windows' ]; then
  export GOOS=windows
  export GOARCH=amd64
  export CGO_ENABLED=1
fi
export CC=$5
export CC_FOR_TARGET=$5
export CXX_FOR_TARGET=$6

cd $4/lib
go get ./
cd ..

make statusgo-library

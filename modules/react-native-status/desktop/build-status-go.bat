 set GOROOT=%1
 set GOPATH=%2
 set PATH=%GOROOT%/bin;%GOROOT%;%GOPATH%;%PATH%
 
 cd %3/lib
 go get .
 cd ..
 mingw32-make statusgo-library
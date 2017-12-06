#!/bin/bash

set -e

export PATH=$PATH:$PWD

# npm
sudo apt-get install nodejs npm

# lein
sudo su -c "wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
    mv lein /usr/bin && chmod a+x /usr/bin/lein && lein"

# react native
npm install -g react-native-cli

# java 8
sudo su << EOF
add-apt-repository ppa:webupd8team/java -y
apt-get update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
apt-get install -y oracle-java8-installer
EOF

# maven
sudo apt install maven ruby
sudo gem install cocoapods

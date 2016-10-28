#!/usr/bin/env bash
brew update
brew cask install java
brew outdated ant || brew unlink ant && brew install ant
brew outdated maven || brew unlink maven && brew install maven
brew outdated gradle || brew unlink gradle && brew install gradle
brew outdated android-sdk || brew unlink android-sdk && brew install android-sdk
brew outdated android-ndk || brew unlink android-ndk && brew install android-ndk
[ -z "$ANT_HOME" ]          && echo 'export ANT_HOME=/usr/local/opt/ant' >> ~/.bash_profile
[ -z "$MAVEN_HOME" ]        && echo 'export MAVEN_HOME=/usr/local/opt/maven' >> ~/.bash_profile
[ -z "$GRADLE_HOME" ]       && echo 'export GRADLE_HOME=/usr/local/opt/gradle' >> ~/.bash_profile
[ -z "$ANDROID_HOME" ]      && echo 'export ANDROID_HOME=/usr/local/opt/android-sdk' >> ~/.bash_profile
[ -z "$ANDROID_NDK_HOME" ]  && echo 'export ANDROID_NDK_HOME=/usr/local/opt/android-ndk' >> ~/.bash_profile
echo 'export PATH=$ANT_HOME/bin:$PATH' >> ~/.bash_profile
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> ~/.bash_profile
echo 'export PATH=$GRADLE_HOME/bin:$PATH' >> ~/.bash_profile
echo 'export PATH=$ANDROID_HOME/tools:$PATH' >> ~/.bash_profile
echo 'export PATH=$ANDROID_HOME/platform-tools:$PATH' >> ~/.bash_profile
echo 'export PATH=$ANDROID_HOME/build-tools/$(ls $ANDROID_HOME/build-tools | sort | tail -1):$PATH' >> ~/.bash_profile
( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | android update sdk --all --filter android-23,tools,platform-tools,build-tools-23.0.1,build-tools-23.0.2,build-tools-23.0.3,extra-android-m2repository,extra-google-m2repository --no-ui --force
source ~/.bash_profile
echo "y" | android update sdk -a --no-ui --filter sys-img-armeabi-v7a-android-23,sys-img-x86-android-23
brew outdated node || brew unlink node && brew install node
brew outdated watchman || brew unlink watchman && brew install watchman
brew outdated leiningen || brew unlink leiningen && brew install leiningen
npm install -g react-native-cli
npm install -g appium
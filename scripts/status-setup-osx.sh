#!/bin/bash

# Check & Install Homebrew
if [ ! "which brew" ]; then
    brew update
    echo brew updated
else
    sudo /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    echo brew installed
    brew update
    echo brew updated
fi

# Check & Install Node
if [ ! "which node" ]; then
    brew upgrade node
    echo node updated
else
    brew install node
    echo node installed
fi

# Check & Install Watchman
if [ ! "which watchman" ]; then
    brew install watchman
    echo watchman updated
else
    brew install watchman
    echo watchman installed
fi

# Check & Install Leiningen
if [ ! "which leiningen" ]; then
    brew upgrade leiningen
    echo leiningen updated
else
    brew install leiningen
    echo leiningen installed
fi

# Check & Install react-native-cli
if [ ! "which npm" ]; then
    if [ ! "which react-native" ]; then
        npm update -g react-native-cli
        echo react-native-cli updated
    else
        npm install -g react-native-cli
        echo react-native-cli installed
    fi
else
    brew install node
    echo node installed
fi

# Check & Install Java
if [ ! "which java" ]; then
    brew upgrade java
    echo java updated
else
    brew cask install java
    echo java installed
fi

# Check & Install Android SDK
if [ ! "which sdkmanager" ]; then
    sudo sdkmanager --update
    echo Android SDK updated
else
    brew cask install android-sdk
    echo Android SDK installed
fi

# Check & Install Maven
if [ ! "which mvn" ]; then
    brew upgrade maven
    echo Maven updated
else
    brew install maven
    echo Maven installed
fi

# Check & Install Ruby
if [ ! "which gem" ]; then
    brew upgrade ruby
    echo Ruby updated
else
    brew install ruby
    echo Ruby installed
fi

# Check & Install Cocoapods
if [ ! "which pod" ]; then
    sudo gem install cocoapods
    echo Cocoapods updated
else
    sudo gem install cocoapods
    echo Cocoapods installed
fi

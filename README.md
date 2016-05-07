# syng-im

A Clojure library designed to ... well, that part is up to you.

## Usage

### Requirements
- [Node & NPM](https://nodejs.org/en/)
- [Lein](http://leiningen.org)
- [react-native](https://facebook.github.io/react-native/docs/getting-started.html) - npm install -g react-native
- [re-natal](https://github.com/drapanjanas/re-natal) - npm install -g re-natal
- [Latest JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Android SDK [Mac](brew install android-sdk) or [Windows/Linux](https://developer.android.com/sdk/installing/index.html)
- [Genymotion](https://www.genymotion.com) (optinal, you may use an AVD - Android Virtual Device)
- Status lib - git clone git@github.com:status-im/status-lib.git && cd status-lib/protocol && lein install
- [Setup Android Development Environment / Simulator](https://facebook.github.io/react-native/docs/android-setup.html)


### Run the app

    re-natal deps
    re-natal use-android-device genymotion (or avd)
    re-natal use-figwheel
    lein figwheel android

    # new tab
    react-native start

    # new tab
    adb reverse tcp:8081 tcp:8081
    adb reverse tcp:3449 tcp:3449
    react-native run-android


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

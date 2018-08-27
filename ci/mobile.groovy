common = load 'ci/common.groovy'
ios = load 'ci/ios.groovy'
android = load 'ci/android.groovy'

def prep(type = 'nightly') {
  /* select type of build */
  switch (type) {
    case 'nightly':
      sh 'cp .env.nightly .env'; break
    case 'release':
      sh 'cp .env.prod .env'; break
    case 'e2e':
      sh 'cp .env.e2e .env'; break
  }
  common.installJSDeps('mobile')
  /* install Maven dependencies */
  sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
  /* generate ios/StatusIm.xcworkspace */
  dir('ios') {
    sh 'pod install'
  }
}

def runLint() {
  sh 'lein cljfmt check'
}

def runTests() {
  sh 'lein test-cljs'
}

def leinBuild() {
  sh 'lein prod-build'
}

return this

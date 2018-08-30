common = load 'ci/common.groovy'
ios = load 'ci/ios.groovy'
android = load 'ci/android.groovy'

def wait(lockFile) {
  /* Crude wait for a lock file to disappear */
  def maxAttempts = 20
  def success = false
  for (i = 0; i <= maxAttempts; i++) {
    rval = fileExists(lockFile)
    if (!rval) {
      return
    }
    sleep 10
  }
  error("Failed to acquire lock: ${lockFile}")
}

def podUpdate() {
  /**
   * This is awful BUT multiple jobs running on the same host can
   * clash when trying to update the CocoaPods maste repo.
   * We could set CP_REPOS_DIR, but the would result in
   * multiple ~3GB directories all over the place and would be slow.
   **/
  def lockFile = "${env.HOME}/.cocoapods.lock"
  try {
    wait(lockFile)
    sh "touch ${lockFile}"
    sh 'pod update --silent'
  } finally {
    sh "rm ${lockFile}"
  }
}

def prep(type = 'nightly') {
  /* select type of build */
  switch (type) {
    case 'nightly':
      sh 'cp .env.nightly .env'; break
    case 'release':
      sh 'cp .env.prod .env'; break
    case 'e2e':
      sh 'cp .env.e2e .env'; break
    default:
      sh 'cp .env.jenkins .env'; break
  }
  common.installJSDeps('mobile')
  /* install Maven dependencies */
  sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
  /* generate ios/StatusIm.xcworkspace */
  dir('ios') {
    podUpdate()
    sh 'pod install --silent'
  }
}

def runLint() {
  sh 'lein cljfmt check'
}

def runTests() {
  sh 'lein test-cljs'
}

def leinBuild(platform) {
  sh "lein prod-build-${platform}"
}

return this

common = load 'ci/common.groovy'

def uploadArtifact() {
  def artifact_dir = pwd() + '/android/app/build/outputs/apk/release/'
  println (artifact_dir + 'app-release.apk')
  def artifact = (artifact_dir + 'app-release.apk')
  def server = Artifactory.server('artifacts')
  def filename = "im.status.ethereum-${GIT_COMMIT.take(6)}-n-fl.apk"
  def newArtifact = (artifact_dir + filename)
  sh "cp ${artifact} ${newArtifact}"
  def uploadSpec = '{ "files": [ { "pattern": "*apk/release/' + filename + '", "target": "nightlies-local" }]}'
  def buildInfo = server.upload(uploadSpec)
  return 'http://artifacts.status.im:8081/artifactory/nightlies-local/' + filename
}

def prepDeps() {
  sh 'rm -rf node_modules'
  sh 'cp .env.nightly .env'
  common.installJSDeps('mobile')
  sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
  /* generate ios/StatusIm.xcworkspace */
  dir('ios') {
    sh 'pod install'
  }
}

def runTests() {
  sh 'lein test-cljs'
}

def leinBuild() {
  sh 'lein prod-build'
}

def compileAndroid(e2e = false) {
  build_no = common.tagBuild()
  if (e2e) {
    env.ENVFILE=".env.e2e"
  }
  dir('android') {
    sh './gradlew react-native-android:installArchives'
    sh './gradlew assembleRelease'
  }
  def pkg = "StatusIm-${GIT_COMMIT.take(6)}.apk"
  sh "cp android/app/build/outputs/apk/release/app-release.apk ${pkg}"
  return pkg
}

def bundleAndroid() {
  withCredentials([
    string(credentialsId: "SUPPLY_JSON_KEY_DATA", variable: 'GOOGLE_PLAY_JSON_KEY'),
    string(credentialsId: "SLACK_URL", variable: 'SLACK_URL')
  ]) {
    sh 'bundle exec fastlane android nightly'
  }
}

def uploadSauceLabs() {
  env.SAUCE_LABS_APK = "im.status.ethereum-e2e-${GIT_COMMIT.take(6)}.apk"
  withCredentials([
    string(credentialsId: 'SAUCE_ACCESS_KEY', variable: 'SAUCE_ACCESS_KEY'),
    string(credentialsId: 'SAUCE_USERNAME', variable: 'SAUCE_USERNAME'),
    string(credentialsId: 'GIT_HUB_TOKEN', variable: 'GITHUB_TOKEN'),
    string(credentialsId: 'SLACK_JENKINS_WEBHOOK', variable: 'SLACK_URL')
  ]) {
    sh 'fastlane android saucelabs'
  }
  return env.SAUCE_LABS_APK
}

def compileiOS() {
  version = readFile("${env.WORKSPACE}/VERSION").trim()
  build_no = common.tagBuild()
  withCredentials([
    string(credentialsId: 'SLACK_URL', variable: 'SLACK_URL'),
    string(credentialsId: "slave-pass-${env.NODE_NAME}", variable: 'KEYCHAIN_PASSWORD'),
    string(credentialsId: 'FASTLANE_PASSWORD', variable: 'FASTLANE_PASSWORD'),
    string(credentialsId: 'APPLE_ID', variable: 'APPLE_ID'),
    string(credentialsId: 'fastlane-match-password', variable:'MATCH_PASSWORD')
  ]) {
    sh "plutil -replace CFBundleShortVersionString  -string ${version} ios/StatusIm/Info.plist"
    sh "plutil -replace CFBundleVersion -string ${build_no} ios/StatusIm/Info.plist"
    sh 'fastlane ios nightly'
  }
  def pkg = "StatusIm-${GIT_COMMIT.take(6)}.ipa"
  sh "cp status-adhoc/StatusIm.ipa ${pkg}"
  return pkg
}

def uploadiOS() {
  withCredentials([
    string(credentialsId: 'diawi-token', variable: 'DIAWI_TOKEN'),
    string(credentialsId: 'GIT_HUB_TOKEN', variable: 'GITHUB_TOKEN'),
    string(credentialsId: 'SLACK_JENKINS_WEBHOOK', variable: 'SLACK_URL')
  ]) {
    sh 'fastlane android upload_diawi'
  }
  diawiUrl = readFile "${env.WORKSPACE}/fastlane/diawi.out"
  return diawiUrl
}

return this

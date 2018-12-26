cmn = load('ci/common.groovy')

def plutil(name, value) {
  sh "plutil -replace ${name} -string ${value} ios/StatusIm/Info.plist"
}

def compile(type = 'nightly') {
  def target
  switch (type) {
    case 'release': 	target = 'adhoc'; 	break;
    case 'testflight': 	target = 'release'; break;
    case 'e2e': 		target = 'e2e'; 	break;
    default:  			target = 'nightly';
  }
  /* configure build metadata */
  plutil('CFBundleShortVersionString', common.version())
  plutil('CFBundleVersion', common.buildNumber())
  plutil('CFBundleBuildUrl', currentBuild.absoluteUrl)
  /* the dir might not exist */
  sh 'mkdir -p status-e2e'
  /* build the actual app */
  withCredentials([
    string(credentialsId: 'SLACK_URL', variable: 'SLACK_URL'),
    string(credentialsId: "slave-pass-${env.NODE_NAME}", variable: 'KEYCHAIN_PASSWORD'),
    string(credentialsId: 'FASTLANE_PASSWORD', variable: 'FASTLANE_PASSWORD'),
    string(credentialsId: 'APPLE_ID', variable: 'APPLE_ID'),
    string(credentialsId: 'fastlane-match-password', variable:'MATCH_PASSWORD')
  ]) {
    sh "bundle exec fastlane ios ${target}"
  }
  /* rename built file for uploads and archivization */
  def pkg = ''
  if (type == 'e2e') {
    pkg = cmn.pkgFilename('e2e', 'app.zip')
    sh "cp status-e2e/StatusIm.app.zip ${pkg}"
  } else if (type != 'testflight') {
    pkg = cmn.pkgFilename(type, 'ipa')
    sh "cp status-adhoc/StatusIm.ipa ${pkg}"
  }
  return pkg
}

def uploadToDiawi() {
  withCredentials([
    string(credentialsId: 'diawi-token', variable: 'DIAWI_TOKEN'),
  ]) {
    sh 'bundle exec fastlane ios upload_diawi'
  }
  diawiUrl = readFile "${env.WORKSPACE}/fastlane/diawi.out"
  return diawiUrl
}

def uploadToSauceLabs() {
  def changeId = cmn.getParentRunEnv('CHANGE_ID')
  if (changeId != null) {
    env.SAUCE_LABS_NAME = "${changeId}.app.zip"
  } else {
    env.SAUCE_LABS_NAME = "im.status.ethereum-e2e-${cmn.gitCommit()}.app.zip"
  }
  withCredentials([
    string(credentialsId: 'SAUCE_ACCESS_KEY', variable: 'SAUCE_ACCESS_KEY'),
    string(credentialsId: 'SAUCE_USERNAME', variable: 'SAUCE_USERNAME'),
  ]) {
    sh 'bundle exec fastlane ios saucelabs'
  }
  return env.SAUCE_LABS_NAME
}

return this

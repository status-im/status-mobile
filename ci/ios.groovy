common = load('ci/common.groovy')

def plutil(name, value) {
  sh "plutil -replace ${name} -string ${value} ios/StatusIm/Info.plist"
}

def compile(type = 'nightly') {
  def target = 'nightly'

  if (type == 'release') {
      target = 'adhoc'
  }
  
  if (type == 'testflight') {
      target = 'release'
  }

  /* configure build metadata */
  plutil('CFBundleShortVersionString', common.version())
  plutil('CFBundleVersion', common.buildNumber())
  plutil('CFBundleBuildUrl', currentBuild.absoluteUrl)
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
  if (type != 'testflight') {
      def pkg = common.pkgFilename(type, 'ipa')
      sh "cp status-adhoc/StatusIm.ipa ${pkg}"
      return pkg
  }
  return ''
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

return this

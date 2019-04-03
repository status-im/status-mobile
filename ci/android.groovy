utils = load 'ci/utils.groovy'

def bundle() {
  def btype = utils.getBuildType()
  /* Disable Gradle Daemon https://stackoverflow.com/questions/38710327/jenkins-builds-fail-using-the-gradle-daemon */
  def gradleOpt = "-PbuildUrl='${currentBuild.absoluteUrl}' -Dorg.gradle.daemon=false "
  def target = "release"

  if (params.BUILD_TYPE == 'pr') {
    /* PR builds shouldn't replace normal releases */
    target = 'pr'
  } else if (btype == 'release') {
    gradleOpt += "-PreleaseVersion='${utils.getVersion('mobile_files')}'"
  }
  dir('android') {
    withCredentials([
      string(
        credentialsId: 'android-keystore-pass',
        variable: 'STATUS_RELEASE_STORE_PASSWORD'
      ),
      usernamePassword(
        credentialsId: 'android-keystore-key-pass',
        usernameVariable: 'STATUS_RELEASE_KEY_ALIAS',
        passwordVariable: 'STATUS_RELEASE_KEY_PASSWORD'
      )
    ]) {
      utils.nix_sh "./gradlew assemble${target.capitalize()} ${gradleOpt}"
    }
  }
  sh 'find android/app/build/outputs/apk'
  def outApk = "android/app/build/outputs/apk/${target}/app-${target}.apk"
  def pkg = utils.pkgFilename(btype, 'apk')
  /* rename for upload */
  sh "cp ${outApk} ${pkg}"
  /* necessary for Fastlane */
  env.APK_PATH = pkg
  env.DIAWI_APK = pkg
  return pkg
}

def uploadToPlayStore(type = 'nightly') {
  withCredentials([
    string(credentialsId: "SUPPLY_JSON_KEY_DATA", variable: 'GOOGLE_PLAY_JSON_KEY'),
  ]) {
    utils.nix_fastlane_sh "android ${type}"
  }
}

def uploadToSauceLabs() {
  def changeId = utils.changeId()
  if (changeId != null) {
    env.SAUCE_LABS_NAME = "${changeId}.apk"
  } else {
    def pkg = utils.pkgFilename(utils.getBuildType(), 'apk')
    env.SAUCE_LABS_NAME = "${pkg}"
  }
  withCredentials([
    string(credentialsId: 'SAUCE_ACCESS_KEY', variable: 'SAUCE_ACCESS_KEY'),
    string(credentialsId: 'SAUCE_USERNAME', variable: 'SAUCE_USERNAME'),
  ]) {
    utils.nix_fastlane_sh 'android saucelabs'
  }
  return env.SAUCE_LABS_NAME
}

def uploadToDiawi() {
  env.SAUCE_LABS_NAME = "im.status.ethereum-e2e-${GIT_COMMIT.take(6)}.apk"
  withCredentials([
    string(credentialsId: 'diawi-token', variable: 'DIAWI_TOKEN'),
  ]) {
    utils.nix_fastlane_sh 'android upload_diawi'
  }
  diawiUrl = readFile "${env.WORKSPACE}/fastlane/diawi.out"
  return diawiUrl
}

return this

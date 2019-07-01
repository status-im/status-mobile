nix = load 'ci/nix.groovy'
utils = load 'ci/utils.groovy'

def bundle() {
  def btype = utils.getBuildType()
  /* Disable Gradle Daemon https://stackoverflow.com/questions/38710327/jenkins-builds-fail-using-the-gradle-daemon */
  def gradleOpt = "-PbuildUrl='${currentBuild.absoluteUrl}' -Dorg.gradle.daemon=false "
  def target = "release"
  /* we don't need x86 for any builds except e2e */
  env.NDK_ABI_FILTERS="armeabi-v7a;arm64-v8a"

  switch (btype) {
    case 'pr': /* PR builds shouldn't replace normal releases */
      target = 'pr'; break;
    case 'e2e':
      target = 'pr';
      env.NDK_ABI_FILTERS="x86"; break
    case 'release':
      gradleOpt += "-PreleaseVersion='${utils.getVersion()}'"
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
      nix.shell(
        "./gradlew assemble${target.capitalize()} ${gradleOpt}",
        keep: [
          'REALM_DISABLE_ANALYTICS', 'NDK_ABI_FILTERS',
          'STATUS_RELEASE_STORE_FILE', 'STATUS_RELEASE_STORE_PASSWORD',
          'STATUS_RELEASE_KEY_ALIAS', 'STATUS_RELEASE_KEY_PASSWORD'
        ]
      )
    }
  }
  sh 'find android/app/build/outputs/apk'
  def outApk = "android/app/build/outputs/apk/${target}/app-${target}.apk"
  def pkg = utils.pkgFilename(btype, 'apk')
  /* rename for upload */
  sh "cp ${outApk} ${pkg}"
  /* necessary for Fastlane */
  env.APK_PATH = pkg
  return pkg
}

def uploadToPlayStore(type = 'nightly') {
  withCredentials([
    string(credentialsId: "SUPPLY_JSON_KEY_DATA", variable: 'GOOGLE_PLAY_JSON_KEY'),
  ]) {
    nix.shell(
      "fastlane android ${type}",
      keep: ['FASTLANE_DISABLE_COLORS', 'GOOGLE_PLAY_JSON_KEY']
    )
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
    usernamePassword(
      credentialsId:  'sauce-labs-api',
      usernameVariable: 'SAUCE_USERNAME',
      passwordVariable: 'SAUCE_ACCESS_KEY'
    ),
  ]) {
    nix.shell(
      'fastlane android saucelabs',
      keep: [
        'FASTLANE_DISABLE_COLORS', 'APK_PATH',
        'SAUCE_ACCESS_KEY', 'SAUCE_USERNAME', 'SAUCE_LABS_NAME'
      ]
    )
  }
  return env.SAUCE_LABS_NAME
}

def uploadToDiawi() {
  withCredentials([
    string(credentialsId: 'diawi-token', variable: 'DIAWI_TOKEN'),
  ]) {
    nix.shell(
      'fastlane android upload_diawi',
      keep: ['FASTLANE_DISABLE_COLORS', 'APK_PATH', 'DIAWI_TOKEN']
    )
  }
  diawiUrl = readFile "${env.WORKSPACE}/fastlane/diawi.out"
  return diawiUrl
}

return this

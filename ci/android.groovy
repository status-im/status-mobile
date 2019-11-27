nix = load 'ci/nix.groovy'
utils = load 'ci/utils.groovy'

def bundle() {
  /* we use the method because parameter build type does not take e2e into account */
  def btype = utils.getBuildType()
  /* Disable Gradle Daemon https://stackoverflow.com/questions/38710327/jenkins-builds-fail-using-the-gradle-daemon */
  def gradleOpt = "-PbuildUrl='${currentBuild.absoluteUrl}' --console plain "
  /* we don't need x86 for any builds except e2e */
  env.ANDROID_ABI_INCLUDE="armeabi-v7a;arm64-v8a"
  env.ANDROID_ABI_SPLIT="false"

  /* some builds tyes require different architectures */
  switch (btype) {
    case 'e2e':
      env.ANDROID_ABI_INCLUDE="x86" /* e2e builds are used with simulators */
      break
    case 'release':
      env.ANDROID_ABI_SPLIT="true"
      gradleOpt += "-PreleaseVersion='${utils.getVersion()}'"
      break
  }

  /* credentials necessary to open the keystore and sign the APK */
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
    /* Nix target which produces the final APKs */
    nix.build(
      attr: 'targets.mobile.android.release',
      args: [
        'gradle-opts': gradleOpt,
        'build-number': utils.readBuildNumber(),
        'build-type': btype,
      ],
      safeEnv: [
        'STATUS_RELEASE_KEY_ALIAS',
        'STATUS_RELEASE_STORE_PASSWORD',
        'STATUS_RELEASE_KEY_PASSWORD',
      ],
      keep: [
        'ANDROID_ABI_SPLIT',
        'ANDROID_ABI_INCLUDE',
        'STATUS_RELEASE_STORE_FILE',
      ],
      sbox: [
        env.STATUS_RELEASE_STORE_FILE,
      ],
      link: false
    )
  }
  /* necessary for Fastlane */
  def apks = renameAPKs()
  /* for use with Fastlane */
  env.APK_PATHS = apks.join(";")
  return apks
}

def extractArchFromAPK(name) {
  def pattern = /app-(.+)-[^-]+.apk/
  /* extract architecture from filename */
  def matches = (name =~ pattern)
  if (matches.size() > 0) {
    return matches[0][1]
  }
  if (utils.getBuildType() == 'e2e') {
    return 'x86'
  }
  /* non-release builds make universal APKs */
  return 'universal'
}

/**
 * We need more informative filenames for all builds.
 * For more details on the format see utils.pkgFilename().
 **/
def renameAPKs() {
  /* find all APK files */
  def apkGlob = 'result/*.apk'
  def found = findFiles(glob: apkGlob)
  if (found.size() == 0) {
    throw "APKs not found via glob: ${apkGlob}"
  }
  def renamed = []
  /* rename each for upload & archiving */
  for (apk in found) {
    def arch = extractArchFromAPK(apk)
    def pkg = utils.pkgFilename(env.BUILD_TYPE, 'apk', arch)
    def newApk = "result/${pkg}"
    renamed += newApk
    sh "cp ${apk.path} ${newApk}"
  }
  return renamed
}

def uploadToPlayStore(type = 'nightly') {
  withCredentials([
    string(credentialsId: "SUPPLY_JSON_KEY_DATA", variable: 'GOOGLE_PLAY_JSON_KEY'),
  ]) {
    nix.shell(
      "fastlane android ${type}",
      attr: 'targets.mobile.fastlane.shell',
      keep: ['FASTLANE_DISABLE_COLORS', 'APK_PATHS', 'GOOGLE_PLAY_JSON_KEY']
    )
  }
}

def uploadToSauceLabs() {
  def changeId = utils.changeId()
  if (changeId != null) {
    env.SAUCE_LABS_NAME = "${changeId}.apk"
  } else {
    def pkg = utils.pkgFilename(env.BUILD_TYPE, 'apk', 'x86')
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
      attr: 'targets.mobile.fastlane.shell',
      keep: [
        'FASTLANE_DISABLE_COLORS', 'APK_PATHS',
        'SAUCE_ACCESS_KEY', 'SAUCE_USERNAME', 'SAUCE_LABS_NAME'
      ],
      pure: false
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
      attr: 'targets.mobile.fastlane.shell',
      keep: ['FASTLANE_DISABLE_COLORS', 'APK_PATHS', 'DIAWI_TOKEN'],
      pure: false
    )
  }
  diawiUrl = readFile "${env.WORKSPACE}/fastlane/diawi.out"
  return diawiUrl
}

def coverage() {
  withCredentials([
    string(credentialsId: 'coveralls-status-react-token', variable: 'COVERALLS_REPO_TOKEN'),
  ]) {
    nix.shell(
      'make coverage',
      keep: ['COVERALLS_REPO_TOKEN', 'COVERALLS_SERVICE_NAME', 'COVERALLS_SERVICE_JOB_ID']
    )
  }
}

return this

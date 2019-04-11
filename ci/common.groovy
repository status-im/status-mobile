import groovy.json.JsonBuilder

gh = load 'ci/github.groovy'
ci = load 'ci/jenkins.groovy'
gh = load 'ci/github.groovy'
utils = load 'ci/utils.groovy'
ghcmgr = load 'ci/ghcmgr.groovy'

/* Small Helpers -------------------------------------------------------------*/

def pkgUrl(build) {
  return utils.getEnv(build, 'PKG_URL')
}

def updateBucketJSON(urls, fileName) {
  /* latest.json has slightly different key names */
  def content = [
    DIAWI: urls.Diawi,
    APK: urls.Apk, IOS: urls.iOS,
    APP: urls.App, MAC: urls.Mac,
    WIN: urls.Win, SHA: urls.SHA
  ]
  def filePath = "${pwd()}/pkg/${fileName}"
  /* it might not exist */
  sh 'mkdir -p pkg'
  def contentJson = new JsonBuilder(content).toPrettyString()
  println "${fileName}:\n${contentJson}"
  new File(filePath).write(contentJson)
  return utils.uploadArtifact(filePath)
}

def notifyPR(success) {
  if (utils.changeId() == null) { return }
  try {
    ghcmgr.postBuild(success)
  } catch (ex) { /* fallback to posting directly to GitHub */
    println "Failed to use GHCMGR: ${ex}"
    switch (success) {
      case true:  gh.NotifyPRSuccess(); break
      case false: gh.NotifyPRFailure(); break
    }
  }
}

def prepNixEnvironment() {
  if (env.TARGET_PLATFORM == 'linux' || env.TARGET_PLATFORM == 'windows' || env.TARGET_PLATFORM == 'android') {
    def glibcLocales = sh(
      returnStdout: true,
      script: ". ~/.nix-profile/etc/profile.d/nix.sh && nix-build --no-out-link '<nixpkgs>' -A glibcLocales"
    ).trim()
    env.LOCALE_ARCHIVE_2_27 = "${glibcLocales}/lib/locale/locale-archive"
  }
}

def prep(type = 'nightly') {
  /* build/downloads all nix deps in advance */
  prepNixEnvironment()
  /* rebase unless this is a release build */
  utils.doGitRebase()
  /* ensure that we start from a known state */
  sh 'make clean'
  /* pick right .env and update from params */
  utils.updateEnv(type)

  if (env.TARGET_PLATFORM == 'android' || env.TARGET_PLATFORM == 'ios') {
    /* Run at start to void mismatched numbers */
    utils.genBuildNumber()
    /* install ruby dependencies */
    utils.nix_sh 'bundle install --quiet'
  }

  def prepareTarget=env.TARGET_PLATFORM
  if (env.TARGET_PLATFORM == 'macos' || env.TARGET_PLATFORM == 'linux' || env.TARGET_PLATFORM == 'windows') {
    prepareTarget='desktop'
  }
  /* node deps, pods, and status-go download */
  utils.nix_sh "scripts/prepare-for-platform.sh ${prepareTarget}"
}

return this

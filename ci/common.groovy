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

return this

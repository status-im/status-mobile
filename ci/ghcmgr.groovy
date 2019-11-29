import groovy.json.JsonBuilder

utils = load 'ci/utils.groovy'

/**
 * Methods for interacting with ghcmgr API.
 * For more details see:
 * https://github.com/status-im/github-comment-manager
 **/

def buildObj(success) {
  def pkg_url = env.PKG_URL
  /* a bare ipa file is not installable on iOS */
  if (env.TARGET == 'ios') {
    pkg_url = env.DIAWI_URL 
  }
  /* assemble build object valid for ghcmgr */
  return [
    id: env.BUILD_DISPLAY_NAME,
    commit: GIT_COMMIT.take(8),
    success: success != null ? success : true,
    platform: env.TARGET + (utils.getBuildType() == 'e2e' ? '-e2e' : ''),
    duration: utils.buildDuration(),
    url: currentBuild.absoluteUrl,
    pkg_url: pkg_url,
  ]
}

def postBuild(success) {
  /**
   * This is our own service for avoiding comment spam.
   * https://github.com/status-im/github-comment-manager
   **/
  def ghcmgrurl = 'https://ghcmgr.status.im'
  def body = buildObj(success)
  def json = new JsonBuilder(body).toPrettyString()
  def stdout = null
  withCredentials([usernamePassword(
    credentialsId:  'ghcmgr-auth',
    usernameVariable: 'GHCMGR_USER',
    passwordVariable: 'GHCMGR_PASS'
  )]) {
    stdout = sh(
      returnStdout: true,
      script: """
        curl --silent \
          -XPOST --data '${json}' \
          -u '${GHCMGR_USER}:${GHCMGR_PASS}' \
          -w '\nHTTP_CODE:%{http_code}' \
          -H "content-type: application/json" \
          '${ghcmgrurl}/builds/status-react/${utils.changeId()}'
      """
    )
  }
  /* We're not using --fail because it suppresses server response */
  if (!stdout.contains('HTTP_CODE:201')) {
    println("STDOUT:\n${stdout}")
    error("Notifying GHCMGR failed")
  }
}

return this

def version() {
  return readFile("${env.WORKSPACE}/VERSION").trim()
}

def getBuildType() {
  def jobName = env.JOB_NAME
  if (jobName.startsWith('status-react/pull requests')) {
      return 'pr'
  }
  if (jobName.startsWith('status-react/nightly')) {
      return 'nightly'
  }
  if (jobName.startsWith('status-react/release')) {
      return 'release'
  }
  return params.BUILD_TYPE
}
  
def buildBranch(name = null, buildType) {
  /* need to drop origin/ to match definitions of child jobs */
  def branchName = env.GIT_BRANCH.replace('origin/', '')
  /* always pass the BRANCH and BUILD_TYPE params with current branch */
  def b = build(
    job: name,
    /* this allows us to analize the job even after failure */
    propagate: false,
    parameters: [
      [name: 'BRANCH',     value: branchName, $class: 'StringParameterValue'],
      [name: 'BUILD_TYPE', value: buildType,  $class: 'StringParameterValue'],
  ])
  /* BlueOcean seems to not show child-build links */
  print "Build: ${b.getAbsoluteUrl()} (${b.result})"
  if (b.result != 'SUCCESS') {
    error("Build Failed")
  }
  return b
}

def copyArts(projectName, buildNo) {
  copyArtifacts(
    projectName: projectName,
    target: 'pkg',
    flatten: true,
    selector: specific("${buildNo}")
  )
}

def installJSDeps(platform) {
  def attempt = 1
  def maxAttempts = 10
  def installed = false
  /* prepare environment for specific platform build */
  sh "scripts/prepare-for-platform.sh ${platform}"
  while (!installed && attempt <= maxAttempts) {
    println "#${attempt} attempt to install npm deps"
    sh 'npm install'
    installed = fileExists('node_modules/web3/index.js')
    attemp = attempt + 1
  }
}

def doGitRebase() {
  try {
    sh 'git rebase origin/develop'
  } catch (e) {
    sh 'git rebase --abort'
    throw e
  }
}

def tagBuild(increment = false) {
  def opts = (increment ? '--increment' : '')
  withCredentials([[
    $class: 'UsernamePasswordMultiBinding',
    credentialsId: 'status-im-auto',
    usernameVariable: 'GIT_USER',
    passwordVariable: 'GIT_PASS'
  ]]) {
    return sh(
      returnStdout: true,
      script: "./scripts/build_no.sh ${opts}"
    ).trim()
  }
}

def getDirPath(path) {
  return path.tokenize('/')[0..-2].join('/')
}

def getFilename(path) {
  return path.tokenize('/')[-1]
}

def uploadArtifact(path) {
  /* defaults for upload */
  def domain = 'ams3.digitaloceanspaces.com'
  def bucket = 'status-im'
  withCredentials([usernamePassword(
    credentialsId: 'digital-ocean-access-keys',
    usernameVariable: 'DO_ACCESS_KEY',
    passwordVariable: 'DO_SECRET_KEY'
  )]) {
    sh """
      s3cmd \\
        --acl-public \\
        --host='${domain}' \\
        --host-bucket='%(bucket)s.${domain}' \\
        --access_key=${DO_ACCESS_KEY} \\
        --secret_key=${DO_SECRET_KEY} \\
        put ${path} s3://${bucket}/
    """
  }
  return "https://${bucket}.${domain}/${getFilename(path)}"
}

def timestamp() {
  def now = new Date(currentBuild.timeInMillis)
  return now.format('yyMMdd.HHmmss', TimeZone.getTimeZone('UTC'))
}

def gitCommit() {
  return GIT_COMMIT.take(6)
}

def pkgFilename(type, ext) {
  return "StatusIm.${timestamp()}.${gitCommit()}.${type}.${ext}"
}


def githubNotify(apkUrl, e2eUrl, ipaUrl, dmgUrl, appUrl, changeId) {
  withCredentials([string(credentialsId: 'GIT_HUB_TOKEN', variable: 'githubToken')]) {
    def message = (
      "#### :white_check_mark: CI BUILD SUCCESSFUL\\n" +
      "Jenkins job: [${currentBuild.displayName}](${currentBuild.absoluteUrl})\\n"+
      "##### Mobile\\n" +
      "* [Android](${apkUrl}), ([e2e](${e2eUrl}))\\n" +
      "* [iOS](${ipaUrl})\\n" +
      "##### Desktop\\n" +
      "* [MacOS](${dmgUrl})\\n" +
      "* [AppImage](${appUrl})"
    )
    def script = (
      "curl "+ 
      "-u status-im:${githubToken} " + 
      "-H 'Content-Type: application/json' " + 
      "--data '{\"body\": \"${message}\"}' " +
      "https://api.github.com/repos/status-im/status-react/issues/${changeId}/comments"
    )
    def ghOutput = sh(returnStdout: true, script: script) 
    println("Result of github comment curl: " + ghOutput);
  }
}

return this

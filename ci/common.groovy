import groovy.json.JsonBuilder

def version() {
  return readFile("${env.WORKSPACE}/VERSION").trim()
}

def getBuildType() {
  def jobName = env.JOB_NAME
  if (jobName.contains('e2e')) {
      return 'e2e'
  }
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

def buildBranch(name = null, buildType = null) {
  /* default to current build type */
  buildType = buildType ? buildType : getBuildType()
  /* need to drop origin/ to match definitions of child jobs */
  def branchName = env.GIT_BRANCH.replace('origin/', '')
  /* always pass the BRANCH and BUILD_TYPE params with current branch */
  def b = build(
    job: name,
    /* this allows us to analize the job even after failure */
    propagate: false,
    parameters: [
      [name: 'BRANCH',     value: branchName,    $class: 'StringParameterValue'],
      [name: 'BUILD_TYPE', value: buildType,     $class: 'StringParameterValue'],
      [name: 'CHANGE_ID',  value: env.CHANGE_ID, $class: 'StringParameterValue'],
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
  sh "scripts/run-environment-check.sh ${platform}"
  sh "scripts/prepare-for-platform.sh ${platform}"
  while (!installed && attempt <= maxAttempts) {
    println("#${attempt} attempt to install npm deps")
    sh 'yarn install --frozen-lockfile'
    installed = fileExists('node_modules/web3/index.js')
    attemp = attempt + 1
  }
}

def doGitRebase() {
  sh 'git status'
  sh 'git fetch --force origin develop:develop'
  try {
    sh 'git rebase develop'
  } catch (e) {
    sh 'git rebase --abort'
    throw e
  }
}

def buildNumber() {
  def number = sh(
    returnStdout: true,
    script: "./scripts/gen_build_no.sh"
  ).trim()
  println("Build Number: ${number}")
  return number
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
  /* There's so many PR builds we need a separate bucket */
  if (getBuildType() == 'pr') {
    bucket = 'status-im-prs'
  }
  /* WARNING: s3cmd can't guess APK MIME content-type */
  def customOpts = ''
  if (path.endsWith('apk')) {
    customOpts += "--mime-type='application/vnd.android.package-archive'"
  }
  /* We also need credentials for the upload */
  withCredentials([usernamePassword(
    credentialsId: 'digital-ocean-access-keys',
    usernameVariable: 'DO_ACCESS_KEY',
    passwordVariable: 'DO_SECRET_KEY'
  )]) {
    sh """
      s3cmd \\
        --acl-public \\
        ${customOpts} \\
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
  return now.format('yyMMdd-HHmmss', TimeZone.getTimeZone('UTC'))
}

def gitCommit() {
  return GIT_COMMIT.take(6)
}

def pkgFilename(type, ext) {
  return "StatusIm-${timestamp()}-${gitCommit()}-${type}.${ext}"
}

def buildDuration() {
  def duration = currentBuild.durationString
  return '~' + duration.take(duration.lastIndexOf(' and counting'))
}

def gitHubNotify(message) {
  def githubIssuesUrl = 'https://api.github.com/repos/status-im/status-react/issues'
  /* CHANGE_ID can be provided via the build parameters */
  def changeId = params.CHANGE_ID ? params.CHANGE_ID : env.CHANGE_ID
  def msgObj = [body: message]
  def msgJson = new JsonBuilder(msgObj).toPrettyString()
  withCredentials([usernamePassword(
    credentialsId:  'status-im-auto',
    usernameVariable: 'GH_USER',
    passwordVariable: 'GH_PASS'
  )]) {
    sh """
      curl --silent \
        -u '${GH_USER}:${GH_PASS}' \
        --data '${msgJson}' \
        -H "Content-Type: application/json" \
        "${githubIssuesUrl}/${changeId}/comments"
    """.trim()
  }
}

def gitHubNotifyFull(urls) {
  def msg = "#### :white_check_mark: "
  msg += "[${env.JOB_NAME}${currentBuild.displayName}](${currentBuild.absoluteUrl}) "
  msg += "CI BUILD SUCCESSFUL in ${buildDuration()} (${GIT_COMMIT.take(8)})\n"
  msg += '| | | | | |\n'
  msg += '|-|-|-|-|-|\n'
  msg += "| [Android](${urls.Apk}) ([e2e](${urls.Apke2e})) "
  msg += "| [iOS](${urls.iOS}) ([e2e](${urls.iOSe2e})) |"
  if (urls.Mac != null) {
    msg += " [MacOS](${urls.Mac}) | [AppImage](${urls.App}) | [Windows](${urls.Win}) |"
  } else {
    msg += " ~~MacOS~~ | ~~AppImage~~ | ~~Windows~~~ |"
  }
  gitHubNotify(msg)
}


def gitHubNotifyPRFail() {
  def d = ":small_orange_diamond:"
  def msg = "#### :x: "
  msg += "[${env.JOB_NAME}${currentBuild.displayName}](${currentBuild.absoluteUrl}) ${d} "
  msg += "${buildDuration()} ${d} ${GIT_COMMIT.take(8)} ${d} "
  msg += "[:page_facing_up: build log](${currentBuild.absoluteUrl}/consoleText)"
  //msg += "Failed in stage: ${env.STAGE_NAME}\n"
  //msg += "```${currentBuild.rawBuild.getLog(5)}```"
  gitHubNotify(msg)
}

def gitHubNotifyPRSuccess() {
  def d = ":small_blue_diamond:"
  def msg = "#### :heavy_check_mark: "
  def type = getBuildType() == 'e2e' ? ' e2e' : ''
  msg += "[${env.JOB_NAME}${currentBuild.displayName}](${currentBuild.absoluteUrl}) ${d} "
  msg += "${buildDuration()} ${d} ${GIT_COMMIT.take(8)} ${d} "
  msg += "[:package: ${env.BUILD_PLATFORM}${type} package](${env.PKG_URL})"
  gitHubNotify(msg)
}

def getEnv(build, envvar) {
  return build.getBuildVariables().get(envvar)
}

def pkgUrl(build) {
  return getEnv(build, 'PKG_URL')
}

def pkgFind(glob) {
  def fullGlob =  "pkg/*${glob}"
  def found = findFiles(glob: fullGlob)
  if (found.size() == 0) {
    sh 'ls -l pkg/'
    error("File not found via glob: ${fullGlob} ${found.size()}")
  }
  return found[0].path
}

def setBuildDesc(Map links) {
  def desc = 'Links: \n'
  links.each { type, url ->
    if (url != null) {
      desc += "<a href=\"${url}\">${type}</a>  \n"
    }
  }
  currentBuild.description = desc
}

def updateLatestNightlies(urls) {
  /* latest.json has slightly different key names */
  def latest = [
    APK: urls.Apk, IOS: urls.iOS,
    APP: urls.App, MAC: urls.Mac,
    WIN: urls.Win, SHA: urls.SHA
  ]
  def latestFile = pwd() + '/' + 'pkg/latest.json'
  /* it might not exist */
  sh 'mkdir -p pkg'
  def latestJson = new JsonBuilder(latest).toPrettyString()
  println("latest.json:\n${latestJson}")
  new File(latestFile).write(latestJson)
  return uploadArtifact(latestFile)
}

def getParentRunEnv(name) {
  def c = currentBuild.rawBuild.getCause(hudson.model.Cause$UpstreamCause)
  if (c == null) { return null }
  return c.getUpstreamRun().getEnvironment()[name]
}

def runLint() {
  sh 'lein cljfmt check'
}

def runTests() {
  sh 'lein test-cljs'
}

return this

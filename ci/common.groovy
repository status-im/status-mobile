import groovy.json.JsonBuilder

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
  def errroCode = 0
  /* prepare environment for specific platform build */
  sh "scripts/prepare-for-platform.sh ${platform}"
  while (!installed && attempt <= maxAttempts) {
    println("#${attempt} attempt to install npm deps")

    errorCode = sh('scripts/locked-npm-install.sh', returnStatus: true)
    installed = fileExists('node_modules/web3/index.js')
    attemp = attempt + 1
  }

  if(!installed || errorCode != 0) {
    error "node dependencies installation failed (installed: ${installed}, errorCode: ${errorCode})"
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


def githubNotify(Map urls) {
  def githubIssuesUrl = 'https://api.github.com/repos/status-im/status-react/issues'
  withCredentials([string(credentialsId: 'GIT_HUB_TOKEN', variable: 'githubToken')]) {
    def message = "#### :white_check_mark: [${currentBuild.displayName}](${currentBuild.absoluteUrl}) "
    message += "CI BUILD SUCCESSFUL in ${currentBuild.durationString} (${GIT_COMMIT})\n"
    message += '| | | | | |\n'
    message += '|-|-|-|-|-|\n'
    message += "| [Android](${urls.apk})([e2e](${urls.e2e})) | [iOS](${urls.ipa}) |"
    if (dmgUrl != null) {
      message += " [MacOS](${urls.dmg}) | [AppImage](${urls.app}) | [Windows](${urls.win}) |"
    } else {
      message += " ~~MacOS~~ | ~~AppImage~~ | ~~Windows~~~ |"
    }
    def msgObj = [body: message]
    def msgJson = new JsonBuilder(msgObj).toPrettyString()
    sh """
      curl --silent \
        -u status-im:${githubToken} \
        -H "Content-Type: application/json" \
        --data '${msgJson}' \
        "${githubIssuesUrl}/${env.CHANGE_ID}/comments"
    """.trim()
  }
}

def pkgFind(glob) {
  return findFiles(glob: "pkg/*${glob}")[0].path
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

def updateLatestNightlies(Map links) {
  def latestFile = pwd() + '/' + 'pkg/latest.json'
  /* it might not exist */
  sh 'mkdir -p pkg'
  def latestJson = new JsonBuilder(links).toPrettyString()
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

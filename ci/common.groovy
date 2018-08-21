def version() {
  return readFile("${env.WORKSPACE}/VERSION").trim()
}

def buildBranch(name = null, buildType = params.BUILD_TYPE) {
  /* always pass the BRANCH and BUILD_TYPE params with current branch */
  return build(
    job: name,
    parameters: [
      [name: 'BRANCH',     value: env.GIT_BRANCH, $class: 'StringParameterValue'],
      [name: 'BUILD_TYPE', value: buildType,      $class: 'StringParameterValue'],
  ])
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

def tagBuild() {
  withCredentials([[
    $class: 'UsernamePasswordMultiBinding',
    credentialsId: 'status-im-auto',
    usernameVariable: 'GIT_USER',
    passwordVariable: 'GIT_PASS'
  ]]) {
    return sh(
      returnStdout: true,
      script: './scripts/build_no.sh --increment'
    ).trim()
  }
}

def uploadArtifact(path, filename) {
  def domain = 'ams3.digitaloceanspaces.com'
  def bucket = 'status-im-desktop'
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
        put ${path}/${filename} s3://${bucket}/
    """
  }
  def url = "https://${bucket}.${domain}/${filename}"
  return url
}

return this

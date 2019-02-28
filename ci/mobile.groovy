cmn = load 'ci/common.groovy'
ios = load 'ci/ios.groovy'
android = load 'ci/android.groovy'

def prep(type = 'nightly') {
  if (type != 'release') {
    cmn.doGitRebase()
  }
  /* ensure that we start from a known state */
  cmn.clean()
  /* Run at start to void mismatched numbers */
  cmn.genBuildNumber()
  /* select type of build */
  switch (type) {
    case 'nightly':
      sh 'cp .env.nightly .env'; break
    case 'release':
      sh 'cp .env.prod .env'; break
    case 'e2e':
      sh 'cp .env.e2e .env'; break
    default:
      sh 'cp .env.jenkins .env'; break
  }
  /* install ruby dependencies */
  sh 'bundle install --quiet'
  /* node deps, pods, and status-go download */
  sh "make prepare-${env.BUILD_PLATFORM}"
}

def leinBuild(platform) {
  sh "lein prod-build-${platform}"
}

return this

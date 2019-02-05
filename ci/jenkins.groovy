import jenkins.model.CauseOfInterruption.UserInterruption
import hudson.model.Result
import hudson.model.Run

utils = load 'ci/utils.groovy'

@NonCPS
def abortPreviousRunningBuilds() {
  /* Aborting makes sense only for PR builds, since devs start so many of them */
  if (!env.JOB_NAME.contains('status-react/prs')) {
    println ">> Not aborting any previous jobs. Not a PR build."
    return
  }
  Run previousBuild = currentBuild.rawBuild.getPreviousBuildInProgress()

  while (previousBuild != null) {
    if (previousBuild.isInProgress()) {
      def executor = previousBuild.getExecutor()
      if (executor != null) {
        println ">> Aborting older build #${previousBuild.number}"
        executor.interrupt(Result.ABORTED, new UserInterruption(
          "newer build #${currentBuild.number}"
        ))
      }
    }

    previousBuild = previousBuild.getPreviousBuildInProgress()
  }
}

def Build(name = null, buildType = null) {
  /* default to current build type */
  buildType = buildType ? buildType : utils.getBuildType()
  /* need to drop origin/ to match definitions of child jobs */
  def branchName = utils.branchName()
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
  println "Build: ${b.getAbsoluteUrl()} (${b.result})"
  if (b.result != 'SUCCESS') {
    error("Build Failed")
  }
  return b
}

def copyArts(build) {
  /**
   * The build argument is of class RunWrapper.
   * https://javadoc.jenkins.io/plugin/workflow-support/org/jenkinsci/plugins/workflow/support/steps/build/RunWrapper.html
   **/
  copyArtifacts(
    projectName: build.fullProjectName,
    target: 'pkg',
    flatten: true,
    selector: specific("${build.number}")
  )
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

return this

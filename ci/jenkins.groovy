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

def strParam(key, value) {
  /* just a helper for making string params */
  return [
    name: key,
    value: value,
    $class: 'StringParameterValue',
  ]
}

def Build(name = null) {
  /**
   * Generate parameters to pass from current params
   * This allows utils.updateEnv() to work in sub-jobs
   **/
  parameters = params.keySet().collectEntries { key ->
      [(key): strParam(key, params.get(key))]
  }
  /* default to current build type */
  parameters['BUILD_TYPE'] = strParam('BUILD_TYPE', utils.getBuildType())
  /* need to drop origin/ to match definitions of child jobs */
  parameters['BRANCH'] = strParam('BRANCH', utils.branchName())
  /* necessary for updating GitHub PRs */
  parameters['CHANGE_ID'] = strParam('CHANGE_ID', env.CHANGE_ID)
  /* always pass the BRANCH and BUILD_TYPE params with current branch */
  def b = build(
    job: name,
    /* this allows us to analize the job even after failure */
    propagate: false,
    parameters: parameters.values()
  )
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

/**
 * Arguments:
 *  - pure - Use --pure mode with Nix for more deterministic behaviour
 *  - keep - List of env variables to keep even in pure mode
 *  - args - Map of arguments to provide to --argstr
 **/
def shell(Map opts = [:], String cmd) {
  def defaults = [
    pure: true,
    args: ['target-os': env.TARGET_OS ? env.TARGET_OS : 'none'],
    keep: ['LOCALE_ARCHIVE_2_27', 'IN_CI_ENVIRONMENT'],
  ]
  /* merge defaults with received opts */
  opts = defaults + opts
  /* Previous merge overwrites the array */
  opts.keep = (opts.keep + defaults.keep).unique()

  if (env.TARGET_OS in ['windows', 'ios']) {
    opts.pure = false
  }
  def stdOut = sh(
    script: """
      set +x
      . ~/.nix-profile/etc/profile.d/nix.sh
      set -x
      IN_CI_ENVIRONMENT=1 \\
      nix-shell --run \'${cmd}\' ${_getNixCommandArgs(opts, true)}
    """,
    returnStdout: true
  )
  return stdOut.trim()
}

/**
 * Arguments:
 *  - pure - Use --pure mode with Nix for more deterministic behaviour
 *  - link - Bu default build creates a `result` directory, you can turn that off
 *  - keep - List of env variables to pass through to Nix build
 *  - args - Map of arguments to provide to --argstr
 *  - attr - Name of attribute to use with --attr flag
 *  - sbox - List of host file paths to pass to the Nix expression
 *  - safeEnv - Name of env variables to pass securely through to Nix build (they won't get captured in Nix derivation file)
 **/
def build(Map opts = [:]) {
  env.IN_CI_ENVIRONMENT = '1'

  def defaults = [
    pure: true,
    link: true,
    args: ['target-os': env.TARGET_OS],
    keep: ['IN_CI_ENVIRONMENT'],
    attr: null,
    sbox: [],
    safeEnv: [],
  ]
  /* merge defaults with received opts */
  opts = defaults + opts
  /* Previous merge overwrites the array */
  opts.args = defaults.args + opts.args
  opts.keep = (opts.keep + defaults.keep).unique()

  def resultPath = sh(
    returnStdout: true,
    script: """
      set +x
      . ~/.nix-profile/etc/profile.d/nix.sh
      set -x
      nix-build ${_getNixCommandArgs(opts, false)}
    """
  ).trim()
  if (!opts.link) { /* if not linking, copy results */
    sh "cp ${resultPath}/* ${env.WORKSPACE}/result/"
  }
  return resultPath
}

private makeNixBuildEnvFile(Map opts = [:]) {
  File envFile = File.createTempFile("nix-env", ".tmp")
  if (!opts.safeEnv.isEmpty()) {
    // Export the environment variables we want to keep into a temporary script we can pass to Nix and source it from the build script
    def exportCommandList = opts.safeEnv.collect { envVarName -> """
      echo \"export ${envVarName}=\\\"\$(printenv ${envVarName})\\\"\" >> ${envFile.absolutePath}
    """ }
    def exportCommands = exportCommandList.join("")
    sh """
      ${exportCommands}
      chmod u+x ${envFile.absolutePath}
    """

    opts.args = opts.args + [ 'secrets-file': envFile.absolutePath ]
    opts.sbox = opts.sbox + envFile.absolutePath
  }

  return envFile
}

private def _getNixCommandArgs(Map opts = [:], boolean isShell) {
  def keepFlags = []
  def entryPoint = "\'${env.WORKSPACE}/shell.nix\'"
  if (!isShell || opts.attr != null) {
    entryPoint = "\'${env.WORKSPACE}/default.nix\'"
  }
  def extraSandboxPathsFlag = ''

  if (isShell) {
    keepFlags = opts.keep.collect { var -> "--keep ${var} " }
  } else {
    def envVarsList = opts.keep.collect { var -> "${var}=\"${env[var]}\";" }
    keepFlags = ["--arg env \'{${envVarsList.join("")}}\'"]

    /* Export the environment variables we want to keep into
     * a Nix attribute set we can pass to Nix and source it from the build script */
    def envFile = makeNixBuildEnvFile(opts)
    envFile.deleteOnExit()
  }

  def argsFlags = opts.args.collect { key,val -> "--argstr ${key} \'${val}\'" }
  def attrFlag = ''
  if (opts.attr != null) {
    attrFlag = "--attr '${opts.attr}'"
  }
  if (opts.sbox != null && !opts.sbox.isEmpty()) {
    extraSandboxPathsFlag = "--option extra-sandbox-paths \"${opts.sbox.join(' ')}\""
  }

  return [
    opts.pure ? "--pure" : "",
    opts.link ? "" : "--no-out-link",
    keepFlags.join(" "),
    argsFlags.join(" "),
    extraSandboxPathsFlag,
    attrFlag,
    entryPoint,
  ].join(" ")
}

def prepEnv() {
  if (env.TARGET_OS in ['linux', 'windows', 'android']) {
    def glibcLocales = sh(
      returnStdout: true,
      script: """
        . ~/.nix-profile/etc/profile.d/nix.sh && \\
        nix-build --no-out-link '<nixpkgs>' -A glibcLocales
      """
    ).trim()
    /**
     * This is a hack to fix missing locale errors.
     * See:
     * - https://github.com/NixOS/nixpkgs/issues/38991
     * - https://qiita.com/kimagure/items/4449ceb0bda5c10ca50f
     **/
    env.LOCALE_ARCHIVE_2_27 = "${glibcLocales}/lib/locale/locale-archive"
  }
}

return this

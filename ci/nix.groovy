/**
 * Arguments:
 *  - pure - Use --pure mode with Nix for more deterministic behaviour
 *  - keep - List of env variables to keep even in pure mode
 *  - args - Map of arguments to provide to --argstr
 **/
def shell(Map opts = [:], String cmd) {
  def defaults = [
    pure: true,
    args: ['target-os': env.TARGET_OS],
    keep: ['LOCALE_ARCHIVE_2_27'],
  ]
  /* merge defaults with received opts */
  opts = defaults + opts
  /* Previous merge overwrites the array */
  opts.keep = (opts.keep + defaults.keep).unique()

  def isPure = opts.pure && env.TARGET_OS != 'windows' && env.TARGET_OS != 'ios'
  def keepFlags = opts.keep.collect { var -> "--keep ${var} " }
  def argsFlags = opts.args.collect { key,val -> "--argstr ${key} \'${val}\'" }
  sh """
    set +x
    . ~/.nix-profile/etc/profile.d/nix.sh
    set -x
    nix-shell \\
        ${isPure ? "--pure" : ""} \\
        ${keepFlags.join(" ")} \\
        ${argsFlags.join(" ")} \\
        --run \'${cmd}\' \\
        \'${env.WORKSPACE}/shell.nix\'
  """
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

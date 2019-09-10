{ stdenv, runCommand, makeWrapper,
  watchman }:

let
  patchedWatchmanFactory = watchmanSockPath:
    assert (builtins.stringLength watchmanSockPath) > 0 -> stdenv.isDarwin;
    if stdenv.isDarwin then
        assert (builtins.stringLength watchmanSockPath) > 0;
        # Create a wrapper to watchman that includes the --sockname flag
        runCommand "patched-watchman" { nativeBuildInputs = [ makeWrapper ]; } ''
        mkdir -p $out/bin

        makeWrapper ${watchman}/bin/watchman \
                    $out/bin/watchman \
                    --add-flags "--sockname=${watchmanSockPath}"
        '' else watchman;

in patchedWatchmanFactory

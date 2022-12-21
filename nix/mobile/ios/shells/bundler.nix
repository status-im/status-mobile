{ mkShell, bundler }:

mkShell {
  buildInputs = [ bundler ];
  shellHook = ''
    bundle install --quiet --gemfile=fastlane/Gemfile
  '';
}

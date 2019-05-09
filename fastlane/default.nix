{ stdenv, bundlerEnv, ruby, makeWrapper }:

let
  fastlane = stdenv.mkDerivation rec {
    name = "${pname}-${version}";
    pname = "fastlane";
    version = (import ./gemset.nix).fastlane.version;

    nativeBuildInputs = [ makeWrapper ];

    env = bundlerEnv {
      name = "${name}-gems";
      inherit pname ruby;
      gemdir = ./.;
    };

    phases = [ "installPhase" ];

    installPhase = ''
      mkdir -p $out/bin
      makeWrapper ${env}/bin/fastlane $out/bin/fastlane \
      --set FASTLANE_SKIP_UPDATE_CHECK 1
    '';

    meta = with stdenv.lib; {
      description     = "A tool to automate building and releasing iOS and Android apps";
      longDescription = "fastlane is a tool for iOS and Android developers to automate tedious tasks like generating screenshots, dealing with provisioning profiles, and releasing your application.";
      homepage        = https://github.com/fastlane/fastlane;
      license         = licenses.mit;
      maintainers     = with maintainers; [
        peterromfeldhk
      ];
    };
  };

in fastlane // {
  shellHook = ''
    export FASTLANE_PLUGINFILE_PATH=$PWD/fastlane/Pluginfile
  '';
}

## This nix file cannot currently be used for iOS due to an error in BUILD TARGET ReactNativeConfig OF PROJECT ReactNativeConfig WITH CONFIGURATION Release
## The log shows:
## PhaseScriptExecution Run\ Script /Users/jenkins/Library/Developer/Xcode/DerivedData/StatusIm-gzbepwnrlqbukxbmmpfskimspfch/Build/Intermediates.noindex/ArchiveIntermediates/StatusIm/IntermediateBuildFilesPath/ReactNativeConfig.build/Release-iphoneos/ReactNativeConfig.build/Script-EBE4E8281C7BF689000F8875.sh
##     /bin/sh -c /Users/jenkins/Library/Developer/Xcode/DerivedData/StatusIm-gzbepwnrlqbukxbmmpfskimspfch/Build/Intermediates.noindex/ArchiveIntermediates/StatusIm/IntermediateBuildFilesPath/ReactNativeConfig.build/Release-iphoneos/ReactNativeConfig.build/Script-EBE4E8281C7BF689000F8875.sh
## which calls node_modules/react-native-config/ios/ReactNativeConfig/BuildDotenvConfig.ruby. The log further shows:
## Ignoring json-2.2.0 because its extensions are not built.  Try: gem pristine json --version 2.2.0
## Ignoring unf_ext-0.0.7.6 because its extensions are not built.  Try: gem pristine unf_ext --version 0.0.7.6
## /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/spec_set.rb:91:in `block in materialize': Could not find unf_ext-0.0.7.6 in any of the sources (Bundler::GemNotFound)
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/spec_set.rb:85:in `map!'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/spec_set.rb:85:in `materialize'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/definition.rb:170:in `specs'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/definition.rb:237:in `specs_for'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/definition.rb:226:in `requested_specs'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/runtime.rb:108:in `block in definition_method'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/runtime.rb:20:in `setup'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler.rb:107:in `setup'
## 	from /nix/store/w7rysr83rd8jga24mp3a2m3vx50n4nva-bundler-1.17.2/lib/ruby/gems/2.5.0/gems/bundler-1.17.2/lib/bundler/setup.rb:20:in `<top (required)>'
## 	from /System/Library/Frameworks/Ruby.framework/Versions/2.3/usr/lib/ruby/2.3.0/rubygems/core_ext/kernel_require.rb:55:in `require'
## 	from /System/Library/Frameworks/Ruby.framework/Versions/2.3/usr/lib/ruby/2.3.0/rubygems/core_ext/kernel_require.rb:55:in `require'

{ stdenv,
  localMavenRepoBuilder, zip, unzip }:

let
  mavenLocalRepo = (localMavenRepoBuilder "status-react-maven-deps" mavenSourceFiles).overrideDerivation (oldAttrs: {
    # Add the zip and unzip tools that we'll use in the postCopy step
    nativeBuildInputs = oldAttrs.nativeBuildInputs ++ (stdenv.lib.optionals stdenv.isLinux [ zip unzip ]);
  });
  mavenSourceFiles =
    let
      srcs = import ./maven-sources.nix { };
      system = if stdenv.isDarwin then "osx" else "linux";
      # Warning: This must be the same as gradlePluginVersion android/gradle.properties
      version = "3.5.3-5435860";
      aapt2NativePkg = "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/${version}/aapt2-${version}-${system}";
    in srcs // (if !stdenv.isLinux then { } else {
      # On Linux, we need to patch the interpreter in Java packages that contain native executables to use Nix's interpreter instead
      "${aapt2NativePkg}" = srcs."${aapt2NativePkg}" // {
        postCopy = ''
          [ -n "$NIX_CC" ] || exit 1 # We need an stdenv with a compiler

          prevSet=$-
          set -e

          # Patch executables from maven dependency to use Nix's interpreter
          tmpDir=$(mktemp -d)
          unzip $depPath.jar -d $tmpDir
          for exe in `find $tmpDir/ -type f -executable`; do
            patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $exe
          done

          # Rebuild the .jar file with patched binaries
          pushd $tmpDir > /dev/null
          chmod u+w $depPath.jar
          zip -fr $depPath.jar
          chmod $depPath.jar --reference=$depPath.jar.sha1
          popd > /dev/null
          rm -rf $tmpDir

          set $prevSet
        '';
      };
    });

in mavenLocalRepo

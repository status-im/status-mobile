{ mkShell, cocoapods }:

let
  # source of what pod should install
  podfileLock = "ios/Podfile.lock";
  # current state of pods installed by pod
  manifestLock = "ios/Pods/Manifest.lock";
in {
  shell = mkShell {
    buildInputs = [ cocoapods ];
    shellHook = ''
      pushd "$STATUS_REACT_HOME" > /dev/null
      { 
        echo "Checking for modifications in ios/Pods..."
        if diff -q ${podfileLock} ${manifestLock}; then
          echo "No modifications detected."
        else
          # CocoaPods are trash and can't handle other pod instances running
          ./scripts/wait-for.sh 240 'pod install'
          (cd ios && pod install)
        fi
      }
      popd > /dev/null
    '';
  };
}

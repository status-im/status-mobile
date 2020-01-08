{ mkShell, mergeSh, status-go }:

let
  shell = mkShell {
    shellHook = ''
      if [ -z "$STATUS_GO_IOS_LIBDIR" ]; then
        echo "STATUS_GO_IOS_LIBDIR is not defined! Aborting."
        exit 1
      fi

      RCTSTATUS_DIR="$STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus"
      targetBasename='Statusgo.framework'

      # Compare target folder with source to see if copying is required
      if [ -d "$RCTSTATUS_DIR/$targetBasename" ] && [ -d $STATUS_REACT_HOME/ios/Pods/ ] && \
        diff -q --no-dereference --recursive $RCTSTATUS_DIR/$targetBasename/ $STATUS_GO_IOS_LIBDIR/ > /dev/null; then
        echo "$RCTSTATUS_DIR/$targetBasename already in place"
      else
        sourceBasename="$(basename $STATUS_GO_IOS_LIBDIR)"
        echo "Copying $sourceBasename from Nix store to $RCTSTATUS_DIR"
        rm -rf "$RCTSTATUS_DIR/$targetBasename/"
        cp -a $STATUS_GO_IOS_LIBDIR $RCTSTATUS_DIR
        chmod -R 755 "$RCTSTATUS_DIR/$targetBasename"
        if [ "$sourceBasename" != "$targetBasename" ]; then
          mv "$RCTSTATUS_DIR/$sourceBasename" "$RCTSTATUS_DIR/$targetBasename"
        fi
      fi
    '';
  };
in
  mergeSh status-go.shell [ shell ]

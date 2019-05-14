{ baseGo }:

let
  go = baseGo.overrideDerivation(oldAttrs: {
    # Patch the Go compiler so that we can have a say (using a NIX_GOWORKDIR environment variable) as to the temporary directory it uses for linking,
    # since that directory path ends up in the string table and .gnu.version_d ELF header
    postPatch = (oldAttrs.postPatch or "") + ''
      substituteInPlace "src/cmd/go/internal/work/action.go" --replace \
        'tmp, err := ioutil.TempDir(os.Getenv("GOTMPDIR"), "go-build")' \
        'var err error
		tmp := os.Getenv("NIX_GOWORKDIR")
		if tmp == "" {
			tmp, err = ioutil.TempDir(os.Getenv("GOTMPDIR"), "go-build")
		}'
      # Disable chown tests, they fail with 'invalid argument'
      rm src/os/os_unix_test.go
    '';
  });

in go

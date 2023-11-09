# Using custom `status-go` version

## Building mobile with custom `status-go` branch
If you need to use a branch of a `status-go` as a dependency of `status-mobile`, you specify it using an update script in `status-mobile` repo:
```bash
scripts/update-status-go.sh <rev>
```

Where `rev` is a branch name, tag, or commit SHA1 you want to build. The script will save the indicated commit hash along with other information in the `status-go-version.json` file.

If you are using a GitHub fork of `status-go` repo, export the `STATUS_GO_OWNER` environment variable when running the script.

## Building mobile with `status-go` from local repository
If instead you need to use a locally checked-out `status-go` repository as a dependency of `status-mobile`, you can achieve that by defining the `STATUS_GO_SRC_OVERRIDE`
environment variable:

```bash
export STATUS_GO_SRC_OVERRIDE=$GOPATH/src/github.com/status-im/status-go
# Any command that you run from now on will use the specified status-go location.
make run-ios
```

or for a one-off build:

```bash
make run-ios STATUS_GO_SRC_OVERRIDE=$GOPATH/src/github.com/status-im/status-go
```

# How to make changes to status-go


## Overview

To make changes to status-go, the best approach is to first identify the issue/feature and the changes to be made directly in status-go.
For any bugs we want to most likely write a failing test in status-go, and make sure that it passes, before we test on mobile.

Once you are quite confident that the status-go code is ready to be tested on mobile, you can test it locally by running the following command:

```
env STATUS_GO_SRC_OVERRIDE={your-status-go-directory} make run-{android/ios}
```

This will recompile status-go and run status-mobile on your device with that updated status-go version.

Once you have tested the status-go changes locally and you think it's ready for a PR:

1) Create a PR in status-go

2) Point your local status-mobile branch to that PR by running the script:

```
scripts/update-status-go.sh {status-go-branch-name}
```

This will update `status-go-version.json`

Commit updated status-go-version.json and create a status-mobile PR

Once both PRs have been approved and are good to merge, you can follow the [merge guidelines](./merging-pr-process.md)

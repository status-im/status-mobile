## PR process

1) Create a PR in status-mobile
2) Add some reviewers to the PR and wait for feedback
3) Address feedback
4) Make sure builds and tests are green (run `make test` locally, `make lint-fix` to fix any indentation issue and `make lint`)
5) Once the PR has been reviewed by the dev team you can run e2e tests on it by going to https://github.com/status-im/status-mobile/projects/7 and move the pr under the column E2E tests. This will trigger tests.
6) Once e2e tests have run, they will report the result on the PR, if it's less than 100%, ask QA to take a look to make sure everything is in order (some might fail for legitimate reasons)
7) Ask QA for manual testing if the PR requires it
8) Once it has been tested successfully, squash everything into one commit. rebase and merge. The commands we use:
```
  git checkout develop
  git pull develop
  git checkout your-feature-branch
  git rebase develop
  git checkout develop
  git rebase your-feature-branch
  git push
```


## Status-go changes

If you are introducing status-go changes, the PR process is pretty similar, with some differences.

The most important thing is that

status-mobile code that makes it to the `develop` branch, should always point to a tagged version of status-go in the `develop` branch of status-go.

In practice this means:


1) Create a PR in status-go, get it reviewed by status-go devs
2) Create a PR in status-mobile, get it reviewed by devs, then go through manual testing (if necessary)
3) Once ready to merge, merge status-go PR first, make sure you bump the `VERSION` file in status-go
4) Once merged, tag the version with the new version and push the tag:
```
  git checkout develop
  git pull develop
  git tag vx.y.z
  git push origin vx.y.z
```
5) Update status-mobile with the new status-go version, using the new tag `scripts/update-status-go.sh "vx.y.z"`
6) Push, make sure it's rebased and go through the merge process as above.
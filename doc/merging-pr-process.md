## PR process

1) Create a PR in status-mobile
2) Add some reviewers to the PR and wait for feedback
3) Address feedback
4) Make sure builds and tests are green (run `make test` locally, `make lint-fix` to fix any indentation issue and `make lint`)
5) Once the PR has been reviewed by the dev team, run e2e tests on it by going to https://github.com/status-im/status-mobile/projects/7 and move the pr under the column E2E tests. This will trigger tests. **Running E2E tests is required for all PRs.**
   1) Read our [PR Review Policy](pr-review-policy.md) for more details on our approach to PR reviews.
   2) Learn more about our [Pipeline for QA](pipeline_process.md) process
6) Once e2e tests have run, they will report the result on the PR, if it's less than 100%, ask QA to take a look to make sure everything is in order (some might fail for legitimate reasons)
7) Ask QA for manual testing if the PR requires it (more details [here](pipeline_process.md))
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

In practice, this means that sometimes they need to be merged in lockstep.
1) Create a PR in status-go and status-mobile. Update the status-go version to the PR revision `scripts/update-status-go.sh $git_revision`.
2) Get both PRs reviewed and approved. Once status-mobile PR has been approved, go through manual QA testing if necessary. Don't merge status-mobile PR just yet.
3) Now that you know the integration between client & server is working, bump the `VERSION` in status-go and merge it.
4) Once merged, tag the version with the new version and push the tag:
```
  git checkout develop
  git pull develop
  git tag vx.y.z
  git push origin vx.y.z
```
5) Update status-mobile with the new status-go version, using the new tag `scripts/update-status-go.sh "vx.y.z"`
6) If you had to rebase status-go to include new changes, e2e test MUST be re-run. If there's any issue you will
   have to fix in status-go with another PR and follow the same process.
7) In status-mobile, push, rebase against `develop` and merge it 🚀


important note : make sure your status-go PRs get a tested-ok by QA before merging them in. 

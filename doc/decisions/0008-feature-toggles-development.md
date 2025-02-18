# 0007. Feature toggles development

| Date | Tags |
|---|---|
| 2018-05-25 | development |


## Status

Proposed

## Context

This change describes a methodology of development which we believe will help
us increase the confidence and speed in the development process.

### What problem are we trying to solve

Currently the time between a PR is sent and the code gets merged is between 1 day
to in some case 4 to 5 days.

This has some disadvantages:

1) This workflow incentivises developers to make large PRs, as building 
incrementally but having to wait 2 to 3 days for the code to be merged at each
step is not desirable and results in loss of context.

2) If a bug is introduced it is harder to fix, as the longer it takes to integrate code 
the more likely the developer has lost familiarity with the code.

3) Constant rebases introduce an overhead for the developer and can introduce
subtle bugs, as rebased code is not thoroughly reviewed.

4) Code review is only performed once, but during the manual QA step significant
changes might be added, which are not reviewed anymore.

### How are we going to address it

We believe feature toggles development https://martinfowler.com/articles/feature-toggles.html
is a step in the right direction.

The way it would work is:

A developer starts working on a new feature.
Any new code will be scoped behind a feature flag and:

1) no significant changes are made to the existing codebase
2) no changes are visible to the user when the feature flag is off

In this case the manual QA step can be skipped, and the code can be integrated
as soon as the review step is done.

Clojure reviewers can object if they think the changes are so significant that
manual QA is required.

This process should ease some workload on the QA team and move some of the
responsibilities on clojure developers.

### What needs to change

To improve no bugs are introduced, there needs to be a concerted effort in
improving the current test suites, both e2e and clojure, and ensure new code is 
sufficiently covered.
The shape/type of tests needed is open to discussion, but generally light 
integration tests + light e2e tests are a good approach for this kind of workflow.

Also manual smoke testing of the images generated is necessary, until we are 
more confident on our test suites.

Once the feature is to be toggled, a PR will be sent changing the config,
at which point manual QA will be required before merging.

If fixes are to be applied, they can be made in a separate PR, which can skip manual QA, 
but will not skip code review, ensuring that subsequent changes are reviewed. Once those are 
merged into develop, the PR that changes the feature toggle will resume testing.


### How far we want to go

At the most extreme, this workflow allows push-to-master, with multiple commits
a day being integrated, but this would hardly fit with the code-review process.

The idea is to add some rigour to the process so that the review step is meaningful,
essentially at any point it could be toggled on and it should provide **value** to the 
end user.

This means that PRs that will skip manual QA need to be deliverables and 
presentable to an end-user (no incomplete implementations), i.e. features 
need to be split into meaningful chunks.

Not all code contributions will want to use this workflow, it is up to the developer
to decide.

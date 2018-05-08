## Preamble

    Idea: 63-refactor-geth-packages
    Title: Refactor status-go geth packages
    Status: Done
    Created: 2017-12-20

## Summary
`geth/` packages require refactoring as they are tightly coupled, hard to maintain and extend. In this idea, we would like to fix all these issues and make status-go architecture easier to work with for new core members and external contributors.

## Swarm Participants
- Lead Contributor: @adambabik
- Contributors:
  - @dshulyak (20h/week)
  - @adriacidre
- Evaluator: @adambabik

## Product Overview
We have a bunch of problems related to the `geth/` packages:
* `NodeManager` contains a lot of complexity, especially related to starting and stopping a node,
* `geth/account` unnecessarily depends on packages like `geth/rpc`,
* decouple `geth/transactions` from `geth/account`,
* remove huge, unnecessary interfaces,
* get rid of `geth/common` package.

In this idea, our goal is to come up with a better architecture design for these packages, removing unnecessary dependencies and huge interfaces. Overall, the whole repo will be easier to browse through for audit companies and external contributors.

### Product Description
It does not add any new features from the user perspective but it's a part of making status-go better tested, easier to extend and reason about, also by external contributors.

### Requirements & Dependencies
All existing tests should pass. If we need to change the interface in `library.go`, we need to communicate that and work with status-react team to figure out the best way to do it.

### Minimum Viable Product
Goal Date: 2018-02-02

Description: Minimal implementation of `Node` that can be started and stopped and an RPC client can be connected to it. It should be preceded with code examples how the new interface of `Node` will look like.

### Iteration 1
Goal Date: 2018-02-09

Description: The rest of the `Node` features is implemented.

### Iteration 2: [AccountManager cleanup](https://github.com/status-im/status-go/pull/753)
Goal Date: 2018-03-23

Description:
`AccountManager` should know nothing about Whisper or `node.NodeManager`, so on this iteration we will remove these dependencies.

### Iteration 3: [TransactionManager cleanup](https://github.com/status-im/status-go/issues/772)
Goal Date: 2018-03-30

Description:
`TransactionManager` shouldn't directly depend on `AccountManager` and `NodeManager`. In this iteration we want to remove these direct dependencies.

### Iteration 4: [common.NodeManager removal](https://github.com/status-im/status-go/issues/779)
Goal Date: 2018-03-30

Description:
`common.NodeManager` interface and all its mocks have to be removed from status-go. This interface is too large and is not useful at all.

### Iteration 5: `geth/common` package removal
Goal Date: 2018-04-06

Description:
`get/common` package contains everything. In this iteration, the goal is to remove it complete or at least move most of the functions to appropriate packages.
  - [x] [common.Nodemanager removal](https://github.com/status-im/status-go/issues/779)
  - [x] [Extract lib specific code from geth/common](https://github.com/status-im/status-go/issues/785)
  - [x] [geth/common package removal](https://github.com/status-im/status-go/issues/795)

### Iteration 6: [Simplify `node.NodeManager` API](https://github.com/status-im/status-go/issues/797)
Goal Date: 2018-04-13

Description:
1. [x] [Change its name to something more appropriate as it does not manage multiple nodes](https://github.com/status-im/status-go/pull/803)
2. [x] [Rename methods indicating there is only support for one node: `StartNode -> Start`, `StopNode -> Stop` etc.](https://github.com/status-im/status-go/pull/803)
3. [x] [Unify EnsureSync on t/utils/utils.go and StatusManager](https://github.com/status-im/status-go/pull/806)

### Iteration 6: [A PR in status-react](https://github.com/status-im/status-react/pull/3826)
Goal Date: 2018-04-13

Description:
A PR in status-react is created with updated status-go version.

## Success Metrics
1. A tree of module dependencies is hugely simplified,
1. All tests pass,
1. There are no race conditions in tests.

## Exit criteria
1. All iterations are done. There is no threat that they can't be finished.
1. New version of status-go is merged into `status-react` develop branch.

## Summary after finish

The first and second success metrics have been achieved. We managed to reduce dependencies between packages and get rid of `common` package. No change did negatively impact tests reliability and all tests pass properly. With regard to the third metric, we haven't implemented automated race condition checks yet so it's hard to tell but there are no race condition issues reported so far.

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

# Tests

## Introduction

This document provides a general overview of the types of tests we use and when
to use them. It is not meant to be a tutorial or a detailed documentation about
testing in software development.

## Types of tests

Tests in `status-mobile` are comprised of:

- Unit tests
  - Subscription tests
  - Event tests
  - Tests for various utilities
- [Component tests](./component-tests-overview.md)
- Integration/contract tests
- [End-to-end tests](./how-to-launch-e2e.md)

We apply the [test
pyramid](https://en.wikipedia.org/wiki/Test_automation#Testing_at_different_levels)
strategy, which means we want the majority of tests at the bottom of the
pyramid. Those should be fast and deterministic and support REPL-Driven
development (RDD). Slightly above them, we have component tests, then
integration/contract tests and finally end-to-end tests. The closer to the top
of the pyramid, the more valuable a test can be, but also more difficult to
pinpoint why it failed and harder to make it dependable.

*Note*: there are literally dozens of [types of
tests](https://en.wikipedia.org/wiki/Software_testing), each with its strengths
and weaknesses.

We tend not to stub or mock implementations in our tests, which means our tests
are [sociable](https://martinfowler.com/bliki/UnitTest.html).

## What to test?

The UI is driven by global & local state changes caused by events. Global state
is managed by re-frame and local state by Reagent atoms or React hooks. Except
for component and end-to-end tests, we test only non-UI code in `status-mobile`.
Given that the UI is greatly derived from global state, by guaranteeing the
state is correct we can prevent bugs and, more importantly, reduce the [cost of
change](https://www.pmi.org/disciplined-agile/agile/costofchange).

We strive to minimize the amount of _business logic_ in views (UI code). We
achieve this by moving capabilities to status-go and also by adhering to
re-frame's architecture.

Whenever appropriate (see section `When to test?`), we _may_ test:

- Re-frame events.
- Re-frame subscriptions.
- Utility functions.
- User journeys through integration/contract tests.

Interestingly, we don't test re-frame _effects_ in isolation.

### What are status-mobile integration and contract tests?

The mobile _integration tests_ can be used to "simulate" user interactions and
make actual calls to status-go via the RPC layer and actually receive signals.
We can also use these tests to verify the app-db and multiple subscriptions are
correct. We use the word _simulate_ because there is no UI. Basically, any flow
that can be driven by re-frame events is possible to automatically test. There
is no way to change or inspect local state managed by React.

A _contract test_ has the same capabilities as an integration test, but we want
to draw the line that they should focus more on a particupar RPC endpoint or
signal, and not on a user journey (e.g. create a wallet account). In the future,
we may consider running them automatically in status-go.

**Note:** integration tests and contract tests are currently overlapping in
their responsibilities and still require a clearer distinction.

## When to test?

(Automated) tests basically exist to support rapid software changes, but not
every piece of code should be tested. The following are general recommendations,
not rules.

- What would be the consequences to the user of a bug in the implementation you
  are working on?
- Can a QA exercise all the branches in the code you changed? Not surprisingly,
  usually QAs can't test many code paths (it may be nearly impossible), and
  because PRs are not often tested by reviewers, many PRs can get into `develop`
  without the necessary quality assurance.
- How costly was it for you to verify a function/event/etc was correct? Now
  consider that this cost will be dispersed to every developer who needs to
  change the implementation if there are no tests.
- Check the number of conditionals, and if they nest as well. Every conditional
  may require two different assertions, and the number of assertions can grow
  exponentially.
- How complicated are the arguments to the function? If they contain nested maps
  or data that went through a few transformations, it may be tricky to decipher
  what they are, unless you are familiar with the code. A test would be able to
  capture the data, however complex they are.

### When to unit-test subscriptions?

Only test [layer-3
subscriptions](https://day8.github.io/re-frame/subscriptions/#the-four-layers),
i.e. don't bother testing extractor subscriptions (check the related
[guideline](https://github.com/status-im/status-mobile/blob/7774c4eac16fdee950a17bf5d07630c45a980f41/doc/new-guidelines.md#subscription-tests)).
Some layer-3 subscriptions can still be straightforward and may not be worth
testing.

- Check the number of _inputs_ to the sub (from the graph). The higher this
  number, the greater the chance the subscription can break if any of the
  input's implementation changes.

**Note**: if a tested subscription changes inadvertently, even if its own tests
still pass, other subscriptions that depend on it and have tests may still fail.
This is why we don't directly test the subscription handler, but instead, use
the macro `test-helpers.unit/deftest-sub`.

### When to unit-test events?

A good hint is to ask if you and other CCs need to rely on re-frisk, UI, REPL,
or FlowStorm to understand the event. If the answer is yes or probably, then a
test would be prudent.

- Many events only receive arguments and pass them along without much or any
  transformation to an RPC call. These are straightforward and usually don't
  need tests ([example](https://github.com/status-im/status-mobile/blob/7774c4eac16fdee950a17bf5d07630c45a980f41/src/status_im/contexts/contact/blocking/events.cljs#L79-L85)).
- Overall, every event basically returns two effects at most, `:fx` and/or
  `:db`. Usually, the complicated part lies in the computation to return the new
  app-db. If the event doesn't perform transformations in the app-db or just
  does a trivial `assoc`, for example, it may not be worth testing.

For reference, the re-frame author particularly [suggests testing events and
subscriptions](https://github.com/day8/re-frame/blob/09e2d7132c479aa43f2a64164e54e42bf8511902/docs/Testing.md#what-to-test).

### When to unit-test utility functions?

Most utility functions in `status-mobile` are pure and can be readily and
cheaply tested.

- If the utility is used in an event/subscription and if the event/subscription
  has tests, you may prefer to test the event/subscription and not the utility,
  or the other way around sometimes.
- If the utility is tricky to verify, such as functions manipulating time, write
  tests ([example](https://github.com/status-im/status-mobile/blob/7774c4eac16fdee950a17bf5d07630c45a980f41/src/utils/datetime.cljs#L1)).
- Utilities can be particularly hard to verify by QAs because they can be lower
  level and require very particular inputs. In such cases, consider writing
  tests.

### When to write integration/contract tests?

- You want to make real calls to status-go because you think the unit tests are
  not enough (test pyramid strategy).
- You constantly need to retest the same things on the UI, sometimes over
  multiple screens.
- The flow is too important to rely only on manual QA, which can't always be
  done due to resource limits, so an integration/contract test fills this gap.
- You want to rely less on end-to-end tests, which can be more unreliable and
  slower to change.
- You want automatic verifications for some area of the mobile app whenever
  status-go is upgraded.

**Note**: the feedback cycle to write integration tests is longer than unit
tests because they are slower and harder to debug. Using the REPL with them is
difficult due to their stateful nature.

### When to test Quo components?

This is covered in [quo/README.md#component-tests](https://github.com/status-im/status-mobile/blob/7774c4eac16fdee950a17bf5d07630c45a980f41/src/quo/README.md#component-tests).

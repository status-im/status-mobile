# 0007. Masking Sensitive Data

| Date | Tags |
|---|---|
| 2018-05-22 | e.g: architecture, security |


## Status

Proposed

## Context

We have some data that we don't want to appear in the logs (user passwords are
a good example). Currently, they are passed around as strings, that could be
printed out by mistake in a log entry (see https://github.com/status-im/status-react/issues/4053)

## Decision

To minimize the risk of leaking passwords through logs, we should not pass
passwords as strings in our codebase. We introduced a new type `MaskedData` in
`status-im.utils.security`.
We use `(security/mask-data <data to hide>` to wrap sensitive data into this
type and then use `(security/unmask <masked-data>)` to get the plaintext back.

It is important to keep that sensitive data masked as much as possible, until
you need the plaintext to pass to the extenral APIs.

Example:
```clojure
(println (security/mask-data "my-plaintext-password")) ;; Outputs "******"
(println (security/unmask (security/mask-data "my-plaintext-password"))) ;; Outputs "my-plaintext-password"
```

## Consequences

Tradeoffs:
- developers need to be aware of this type and have a clear separation where do
we use plaintext and where do we use masked datak

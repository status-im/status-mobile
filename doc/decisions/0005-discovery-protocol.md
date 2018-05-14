# 0005. Use Discovery v5 protocol

| Date | Tags |
|---|---|
| 2018-05-14 | architecture, p2p, ethereum |


## Status

proposed

## Context

Currently peers for messaging are hardcoded in the app. This means we can't
easily scale, as some of these nodes might fill up when we have more than N
active users.

Discovery v5 protocol (https://ideas.status.im/ideas/092-disc-v5-research,
https://github.com/fjl/p2p-drafts/blob/master/discv5-eip.md) allow us to
discover new peers from the app and scale server peers in our cluster
independently. This is vital for high availability/reliability and security.

## Decision

- Use and enable Discovery v5 for all networks
- Do this before beta is released

This means we have capacity to achieve 5000+ DAU / 500 concurrent connection. It
seems unlikely we can do so with reliable messaging in the current setup.

Trade-off for this is:
- Uncertainty in introducing a new protocol leading to possible delays
- Possible increase in CPU/Network (we can disable protocol once peers are found though)

## Consequences

- This is a top priority for P2P team, lead by @dshulyak and assisted by @adambabik and @jacob
- We need this battle tested to get it into app as soon as possible
- Chat, mobile team and QA team should be aware of this protocol change

# 0004. Disable group chat

| Date | Tags |
|---|---|
| 2018-05-14 | scope, chat |


## Status

proposed

## Context

We want to launch our beta in Q2. This requires us to keep a tight scope and stay on the critical path.

Group chat is not as vital as 1-1 chat for the chat experience. It is also
likely not as useful for an MVP as public chat is, since this gives people a
chance to talk to strangers and it is open by default.

Whether group chats are in scope or not has been a recurring discussing and a
lot of effort has been wasted related to this.

There are currently a lot of outstanding regressions related to group chat: https://github.com/status-im/status-react/labels/group-chat Additionally, since group chats are private the encryption and security of those are generally harder than 1-1 and public chat.

## Decision

Disable group chat for beta and don't work on bugs related to it until after. See https://github.com/status-im/status-react/issues/3995

This ensures we can release beta without blockers, and then we can take some
time to fix group chats properly.

## Consequences

Group chats disabled in beta. People won't be able to chat in private groups.

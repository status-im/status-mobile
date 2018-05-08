## Preamble

    Idea: <to be assigned>
    Title: Group Chat
    Status: Draft
    Created: 2017-11-23
    Related: https://github.com/status-im/ideas/issues/15

## Summary

Provide a powerful group chat with flexible moderation features

## Swarm Participants

- Lead Contributor: @james
- Testing & Evaluation: (TBD)
- Contributor: @themue
- Contributor:
- PM: (TBD)
- UX (if relevant): (TBD)

## Product Overview

Users want to have a convenient and powerful group chat comparable to the market competitors. But beside these standard operations they want to have greater control over the content in their chat feeds ensuring a positive experience. We can give users that control by providing them with a suite of moderation tools. Our moderation tools will enable users to define their own content rules or subscribe to a moderation provider to do it for them. A marketplace will ultimately exist where users can choose from a range of moderation providers.

### Minimum Viable Product

- Goal Date: (TBD)
- Description: The MVP provides users with basic moderation tools so that they can block users, filter posts with certain phrases, and reduce spam.

#### User Stories 

- [US 1: Blocking a user](./user-stories/us-1-blocking-a-user.md)
- [US 2: Reducing SPAM](./user-stories/us-2-reducing-spam.md)
- [US 3: Filter unwanted content](./user-stories/us-3-filter-unwanted-content.md)
- [US 4: Advanced filtering with smart contracts](./user-stories/us-4-advanced-filtering-with-smart-contracts.md)
- [US 5: Subscribe to moderation providers](./user-stories/us-5-subscribe-to-moderation-providers.md)

#### Requirements

- A format / schema for moderation rules must be defined.
- A standard rules interface for smart contracts must be defined.
- A smart contract that must:
  - Allow a caller to burn any ERC20 token.
  - Allow a caller to label burn amounts.
  - Return burn amounts given a token address, user address, and an optional label.
- A moderation settings UI must exist where the user may configure:
  - Blocked users.
  - Filtered phrases.
  - The address of a smart contract to call.
  - Burn amount required.
- UserA must be able to block a UserB from UserB's profile.
- Messages are evaluated against the moderation rules before they are rendered, aborting render where necessary.
- Moderation rules must persist between sessions.
- Chat group settings UI must:
  - Display the user's burn amount for that channel.
  - Allow users to burn tokens for that channel.

## Dates

- Goal Date: (TBD)
- Description: (TBD)
- Testing Days required: (TBD)

## Success Metrics

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

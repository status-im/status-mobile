# Requirement 1: Blocking Menu

## User Stories

- [1: Blocking a User](https://github.com/status-im/ideas/blob/progress/39-init-reqeng/active/39-chat-moderation/user-stories/us-1-blocking-a-user.md)

## Description

The context menu for all participants of a chat must contain a blocking menu point.

## Rationale

Selecting this menu point by *User A* for a *User B* leads to the blocking of the
selected user for all feeds of *User A*.

## Fit Criteria

- Selecting this menu point leads to a security question.
- Confirming the menu point leads to a blinding out of all messages of
  *User B* in all common feeds of *User A*.

## Dependencies

None.

## Conflicts

None.

## Links

None.

## Remarks

- Question: Does the list of chat members also shows the own profile? Here the
  context menu must contain no blocking menu point.
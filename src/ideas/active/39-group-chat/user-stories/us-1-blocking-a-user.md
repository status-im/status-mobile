# User Story 1: Blocking a User

## Description

*User A* enters a public group chat. *User B* sends a message that *User A* dislikes. *User A* 
blocks *User B*. All messages from *User B* no longer show up in any chat feeds of *User A*.

## Trigger

Any reason *User A* doesn't want to see posts of *User B* anymore.

## Actors

- *User A*
- *User B*
- *User C*

## Pre-Condition

In a chat room with *User A*, *User B*, and possible more more *User A* can read all
messages of *User B*.

## Post-Condition

The same chat room with the same visitors, but *User A* doesn't see any post of *User B*.
Also in other chat rooms with both users *User A* doesn't see any post of *User B*.

## Flow

- *User A* selects account of *User B*.
- Menu with multiple options is display, one of those is `Block ...`.
- After selecting `Block ...` and confirming a security question posts of *User B* aren't
  visible anymore to *User A*.

## Remarks

- Question: How to remove this blocking? Is *User B* still visible in the list of chat
  participants and can be selected to open a menu like for blocking? Or does the chat
  room menu an option to list all blocked users and those can be unblocked there?
- Question: How is quoting, mentioning, or forwarding of *User B* messages by *User C*
  handled? Are those visible to *User A*?

# User Story 3: Filter unwanted Content

## Description

*User A* does not want to view any messages that contain the phrase x. *User A*
updates their moderation rules to ignore any messages that contain the phrase x.
Messages that contain the phrase x no longer appear in any chat feeds of *User A*.

## Trigger

Feeds of *User A* contain a phrase the user doesn't want to see.

## Actors

- *User A*
- *User B*

## Pre-Condition

The disliked phrase is not inside the already added phrases of *User A*.

## Post-Condition

After adding a phrase to ignore no more messages containing this phrase are
in the feed of *User A*.

## Flow

- *User A* discovers unwanted phrase.
- *User A* decides to filter this phrase out.
- *User A* opens a (global|chat room) menu and selects the menu point
  for filtering.
- *User A* adds a phrase to filter.

## Remarks

- Question: Does a phrase ban of a user effects only one chat room or all
  chats of *User A*?
- Question: Does the *User A* get a kind of notification of an unwanted phrase
  by *User B*, e.g. to allow her to react on it?
- Question: Which options for filters? Exact match, full words, wildcards,
  regular expressions?
- Todo: Add user stories for the changing and the removal of filter phrases.

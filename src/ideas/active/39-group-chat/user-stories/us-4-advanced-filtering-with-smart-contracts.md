# User Story 4: Advanced Filtering with Smart Contracts

## Description

*User A* is a power user. *User A* configures her moderation rules to include
the address of a smart contract that implements a standard rules interface.
The smart contract will be called to evaluate new messages as they are received.

## Trigger

*User A* want non-standard actions to be performed for each received message.

## Actors

- *User A*

## Pre-Condition

The actions perfomed on arriving messages do not contain the wanted action.

## Post-Condition

After adding the address of a smart contract implementing the standard rule
interface this contract will be called for each received message.

## Flow

- *User A* opens a (global|chat room) menu and selects the menu point
  for adding smart contracts.
- After entering the address of a smart contract and confirming the
  adding the smart contract will be checked for implementing the standard
  rule interface.
- In case of a valid implementation the smart contract will be added.

## Remarks

- Question: Is the only one global list for all messages or also individual
  lists per chat room?
- Question: Only one contract (global or per chat room) or multiple ones?
- Question: In case of multiple contracts, can the order of their execution
  be defined?
- Question: Can contracts filter messages or send new messages? And the latter
  to *User A* or also to other users?
- Todo: Add user stories for the manipulation of the contract set.
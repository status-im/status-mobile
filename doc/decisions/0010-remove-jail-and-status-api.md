# 0010. Remove jail and Status API

| Date | Tags |
|---|---|
| Thu Aug 16  | jail, 3rd-party-code, extensions |

## Status

accepted

## Context

Originally, we supported 3rd party status extensions in form of javascript jailed execution environment (eq Jail) + small js library exposing chat commands API to js code.

While neat in theory, it has some serious downsides:

- Design of the API was quite poor, preferring mutable semantics over stateless API leveraging strength of the host application (cljs/re-frame).
  As a result of that, more dynamic/state requiring things (like the live tx detail in `/send` command messages) were very hard to do,
  so instead of "eating our own dogfood", we decided to side-step the API and implement such things as hard-coded logic in the app, while partly
  retaining the js code for "easier" things (like parameter declaration).
  Needles to say, such efforts produced code of very poor quality, riddling our app with hard-coded "magic" everywhere in the codebase, completly
  defeating the point of "dogfooding" while still requiring more effort and being much more error prone (no way to unit test jail logic) because 
  of the need to asynchronously communicate with jail for leftover logic in command messages (the parts not hardcoded in app).
  
- We were in a state where there was not even one command defined completely in jail, with all of them requiring custom hardcoded logic in app to
  actually work.
  Due to numerous changes and rush to get things working, half of the API was obsolete and was not working/working differently then described in
  the API documentation.
  
- We are quite tight stretched regarding performance and the Jail didn't particularly help with that - whenever some command message was defined in
  Jail, it required constant RN Bridge "ping-pong" whenever sending the message or receiving the message, eating more resources + breaking otherwise
  quite simple logic into harder to reason asynchronous call chains (execute method defined in Jail, wait for the result, execute next method in Jail...).
  
- Till now, there was no real interest in 3rd party bots anyway - the only "real" DAPPs submitted to status were "normal" DAPPs utilising just the `web3.js`
  API and built on top of regular web technologies, so they could be ran in any Ethereum client like Metamask or Mist, besides Status.
  
- There is a very promising new concept of extensions (project pluto), which will enable to extend status with much nicer and declarative extension
  definitions.
  Such extensions will not be able to only hook-in into the command messages logic, but also other application (host) hooks, like wallet assets/collectibles,
  chat contacts, etc.
  
## Decision

In the light of points above, we decided to remove Jail and `Status.js` API from the application and re-work command messages in set of nicely
encapsulated app level protocols, so command related logic will be defined in one place, commands will be able to leverage strength of our platform
and development of any new functionality related to them will be much faster and more pleasant. 
We will address the 3rd party code issue separately by extensions.

## Consequences

`Status.js` is deprecated and there is no way to define custom commands using javascript only.

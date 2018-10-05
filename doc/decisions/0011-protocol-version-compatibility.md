# 0011. Protocol compatibility between App versions

| Date | Tags |
|---|---|
| Fri Oct 05  | protocol, messenger |

## Status

draft

## Context

This document enumerates the rules we have to adhere to when doing any protocol modifications and summarizes best practises to do so.
In the light of recent issues (code landed in develop branch, producing "invisible" messages for previous app versions), it's best
to capture it in the ADR for future reference.

## Decision

Rules for making any changes related to message payload (transit fields defined in `status-im.transport.message.transit` ns):

- Every new change must be backward compatible back to released App version `0.9.20` and higher. That means that whenever for example
  new feature (like message replies recently) is added, we can't of course do nothing with the fact that older versions can't properly
  display new feature, but we must ensure that at least functionality existing in the older version is not affected at all.
  So in the example above, even if the older version can't display the fact that some message is reply to other message, it still has to
  be able to receive and display it as a standalone message.
  
- This means we **can't** under any circumstances increment the tag of the existing message type handler (like `c4` -> `c5`) because older
  version will simply discard and ignore message with unknown tag. 
  This means we effectively lost formal message versioning (it can be derived from payload fields itself), but that's what we have to do
  in order to maintain full backward compatibility.
  In the future we can revisit our implementation of the `deserialize` function in the `status-im.transport.message.transit` ns and regain
  our ability to match any payload change with tag increment while still retaining the ability of older versions to understand the payload
  keys from last known handler for particular message kind.
  
- If for any reason there absolutely has to be breaking change with no way to maintain compatibility for previous versions, this has to be
  publicly brought up for discussion among #core devs and majority of devs have to agree with the decision.
  
- Any PR changing the way how protocol messages are encoded has to be labeled with `protocol` tag on github and reviewes should apply all
  the rules listed above when reviewing such code.
  
Rules for testing protocol changes

- Given that even nightly builds directly from develop branch are used for day to day communication with wide community, protocol
  changes should be treated with at least the same scrutiny and rigorous testing as release branch testing.
  When producing a new message and publishing it on a network, it's effectively (almost) the same as pushing new piece of code to
  receivers.

- You can join public channel like `#status` with PR build, but never post anything there unless you are sure that everything is working
  flawless in 1-1 and public channels dedicated to testing, including compatibility between PR build and older versions.
  Of course as a last step, testing in busy public channel like `#status` is advised, because if despite all the effort, something goes
  bad, it's better when it's discovered when "dangerous" version is in possesion of just one tester and not already released to the wild,
  even if only as a nightly build for eager desktop users.
  
## Consequences

Greater understanding and clarity for requirements about protocol changes.

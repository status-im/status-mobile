---
id: 143-seamless-login
title: Seamless Login
status: Active
created: 2018-05-04
category: core
contributors:
    - alwx
    - asemiankevich
    - chadyj
    - rasom
    - andmironov
    - jpbowen
    - hesterbruikman
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
okrs:
   - "[P1]: Objective: Beta is launched successfully"
   - "[P0]: KR: 5k daily active users"
---

## Preamble

    Idea: 143
    Title: Seamless Login
    Status: In Progress
    Created: 2018-05-04


## Summary

When a user opens Status or taps on a push notification they should be able to start using the app securely, quickly and without any interruptions.

## Swarm Participants
- Lead Contributor: @alwx
- Testing & Evaluation: @asemiankevich
- PM: @chadyj
- UX: @andmironov
- UXR: @jpbowen @hesterbruikman


## Product Overview

Status frequently logs out users when the app is in the background and forces users to log in again when opening the app. Being repeatedly and unexpectedly logged out has led to frustration and complaints (see Instabug and internal reports) which negatively impact the user experience and potentially contributes to churn. Also, users are encouraged to have complex passwords but this can be cumbersome to enter.

By reducing friction around the critical point of returning to the app users can spend more time in Status which will help DAU and retention (Q2 OKR).

This idea seeks to make logging in seamless through both technical and UX methods.


### Product Description

The Status login process should smooth as possible, and this can be achieved with the (optional) use of device security features such as Touch ID or a pin code. In addition to the password prompt, a user can choose to securely use their fingerprint. This will be set up during onboarding and toggled in settings.

Note, all transactions require authentication, so even if the phone was lost or stolen a third party couldn’t transfer funds. This idea does not cover the transaction flow.

For future iterations, this swarm will research the security implications and explore enhancements around logging in, logging out, and recovering from crashes.

### Security and Privacy Implications

Potentially, both Touch ID ("fingerprint authentication") and Face ID can improve security by allowing users to have stronger passwords (the current experience encourages to have a weak password). Of course, it has some disadvantages, but we're going to require passwords anyway for important operations, so the goal is to improve security anyway.
Moreover, options like "Require authentication every time I open the application" will improve both security and privacy for sure – right now we ask password only in the case user was accidentally logged out.  

Of course, there are some open questions:
1) We should discuss options like requiring passwords to log in once in a while even if biometrical authentication is used;
2) Screens like "Settings" or "Backup your Seed Phrase" should be protected by a password?;
3) We need to discuss the "level of freedom" – what should be the options? 
4) How can we encrypt/decrypt Realm in the situation when we don't have a password?

Answers to all these question will affect the possible security and privacy implications, but the first iteration will be dedicated to discussions about this topic.

### Iteration 1

Perform some research:
1) Think more of security and privacy implications; dicuss what exactly needs to be implemented and how;
2) Discuss different technical aspects like encryption of Realm (look at react-native-keychain and PR [#3829](https://github.com/status-im/status-react/pulls/3829)) and libraries for biometric authentication;
3) Think of possible restrictions of biometric authentication;
4) Think of UX: Do we need any special onboarding screen or not? What exactly needs to be added to Settings and how should it look like?

### Iteration 2

1) Implement biometric authentication;
2) Make all required changes to onboarding flow and Settings;
3) Perform usability tests;
4) Perform security analysis.

## Iteration 3

1) Research crashes, logouts, and other related issues. Some exploration has started [here#2869](https://github.com/status-im/status-react/issues/2869#issuecomment-376098196).
2) Propose fixes that improve both security and usability.


## Success Metrics

- UXR validates workflow on real devices with real users
- 90% of surveyed users are satisfied with loggging in
- 0 password related issues in Instabug


## Exit criteria

When iterations are complete the success metrics should be evaluated. If the results are successful this swarm can be closed. If work is needed an additional iteration can be done to address shortcomings.

## Supporting Role Communication

- Core
- Marketing
- Test team

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

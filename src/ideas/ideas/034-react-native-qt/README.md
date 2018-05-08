---
id: 34-react-native-qt
title: Extend react-native-desktop to support status-react Desktop functionality
status: Active
created: 2017-11-22
category: core
lead-contributor: MaxRis
contributors:
    - MaxRis
    - vkjr
    - flexsurfer
exit-criteria: yes
success-metrics: yes
clear-roles: yes
future-iterations: yes
roles-needed:
    - PM
okrs:
    - "[P3] Objective: Status is used everyday internally"
    - "[P0] KR: 80% of core contributors use Status (mobile or desktop) every workday"
    - "[P3] KR: 10% more usage of Status Desktop than Slack"
---

## Preamble

    Idea: #34-react-native-qt
    Title: Extend react-native-desktop to support status-react Desktop functionality
    Status: In-Progress
    Created: 2017-11-22


## Summary
Status React cross-platform (Linux, OS X and Windows) desktop version based on [react-native-desktop](https://github.com/status-im/react-native-desktop/) port of react-native.

## Vision
React-Native-Desktop is the desktop extension of origin react-native framework developed by Status. With direct access to underlying OS's APIs (via Qt Framework APIs mainly) we should be able to have almost full control on software stack and provide better users experience.

Status IM application should has range of unique screens optimized for the best desktop users experience.

Desktops version of Status IM is supposed to re-use backend implementation of Status IM Mobile, but with unique UI screens and based on react-native-desktop extension of original react-native.

## Swarm Participants
- Lead Contributor: @MaxRis
- Testing & Evaluation:
- Contributor: @vkjr
- Contributor: @flexsurfer
- UX (if relevant):

## Requirements


## Goals & Implementation Plan

Goal Date: 2017-12-11 (Done)
Description: Enhance development cycle to get acceptable performance in code development (adopt figwheel + live-reload, find the way to compile ClojureScript to JS without code optimization (lein has known issues with that)

Goal Date: 2017-12-15 (Done)
Description: Login screen rendering and functionality

### Iteration 1
Goal Date: 2018-04-09
Description:
1. Upgrade react-native-desktop to react-native v.0.53.3 (since latest status-react expects  v.0.53.3 as well) (2018-04-09 update: Done)
2. Investigate and implement possibility to not rely on Node.js installed system wide, but bundle it with react-native-desktop app distribution (2018-04-09 update: In-Progress)
3. Create runnable react-native-desktop app distribution (status-electron MVP based) for Ububntu, OS X and Windows. (2018-04-09 update: Not started)

### Iteration 2
Goal Date: 2018-04-23
1. react-native-desktop running with integrated JavaScriptCore engine instead Node.js on Ubuntu (continuation of Goal 2 from Iteration 1) (2018-04-23 update: Done)
2. Porting latest status-react to run on top of react-native-desktop. Work-in-progress. Resolve messages receiving and sending issues. (2018-04-23 update: Done)
3. Create runnable react-native-desktop app distribution (status-electron MVP based) for Ububntu, OS X and Windows. (2018-04-23 update: Not started)

### Iteration 3
Goal Date: 2018-05-07
1. Build status-react Desktop in production
2. OS X bundle distribution for status-react Desktop
3. Realm native module support by r-n-d and JavaScriptCore
4. Ubuntu distribution build of old status-electron for Desktop based on react-native-desktop and JavaScriptCore

Goal Date: TODO
Description: Integrate react-native-desktop into latest status-react sources with desktops screens available.

Goal Date: TODO
Description: Resolve react-native-desktop related UI issues (missed support of some specific props or components, etc)

Goal Date: TODO
Description: Smoothly working figwheel for dev builds

Goal Date: TODO
Description: Stable Status Desktop application with screens specific for desktop users experience.

Goal Date: TODO
Description: Support by react-native-desktop of [all react-native components](https://github.com/status-im/status-electron/wiki/Status-Components are supported by react-native-desktop) used in Status Desktop.

Goal Date: TODO
Description: Automated process of Status Desktop distributions creations for Linux, OS X and Windows desktop platforms.

### Minimum Viable Product (completed)

1. Application shows 'chat' screen
2. User can login
3. User can exchange 1-1 messages

### Minimum Viable Product 2

Status Desktop application with screens specific for desktop users experience based on Status-React (Status IM Mobile) source code base with possibility to chat without interruption.

## Success Metrics:

- Successful internal usage of Status Desktop application by Status Team on daily basis (Additional metrics should be defined).
- All react-native components from the list of used components in Status Desktop https://github.com/status-im/status-electron/wiki/Status-Components are supported by react-native-desktop
- Existing UI for status-react desktop runs smoothly with `react-native-desktop`
- Automated creation of binary distributions for Linux, OS X, Windows desktop platforms.

## Exit criterias
- Stable Status Desktop application based on react-native-desktop (Additional criterias should be defined).

## Post-Mortem

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

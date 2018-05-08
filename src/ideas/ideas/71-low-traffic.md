## Preamble
    Idea: 71-low-traffic
    Title: Low traffic consumption(technical)
    Status: Aborted
    Created: 2018-01-25

## Summary
1) Issues related to statusgo network overhead

## Swarm Participants
- Lead Contributor: @JekaMas  (40h/w)
- Testing & Evaluation: @annadanchenko (?)
- Go Contributor: @b00ris (40h/w)
- Go Contributor: @PombeirP  (30h/w)
- Go Contributor: @feuGeneA (30h/w)
- Clojure Contributor: @yenda (20h/w)

## Product Overview
Measure network overhead for main `statusgo` usage scenarios.
Identify and fix the bottlenecks.

### Product Description
Main use cases to track:
1) node was started in background(sync was ended)
1.1) node was started in background(sync was ended) + whisper
2) node was started in background(sync is running)
2) node was started in background(upstream)
2.1) node was started in background(upstream) + whisper
3) one chat 1-1.  sending messages(60rpm)
4) 60 chats 1-1. receiving messages(10rpm per chat)
5) 10 group chats(60 rpm per chat)
6) 100 chats 1-1. 30 group chats. receiving historic messages for last day(100 messages per chat)


Many topics problem https://github.com/status-im/status-react/issues/2964
Whisper network overhead https://github.com/status-im/status-react/issues/2931

### Requirements & Dependencies
Part of https://github.com/status-im/ideas/issues/55
Related to ultralight client

## [Iterations](#iterations)
### Research to define further steps and related metrics. Iteration 1.
Goal Date: 2018-02-13
Description: 
* Define reasonable steps to reduce network overhead
* We have tools to measure CPU usage and network of statusgo for main use cases to track progress(part of https://github.com/status-im/ideas/issues/22)(status-go tasks TBD)
* Success metrics for the issue was defined 

### Minimum Viable Product. Iteration 2.
Goal Date: 2018-02-26
* Reduce Status mean traffic to 50-100Mb/day (2-4 Mb/hour) from current 10-20Gb/day. This relates only to online chatting without offline mailboxing
* Make WhisperV6 production ready and switch to it https://github.com/status-im/status-go/issues/634

### Acceptable traffic consumption. Iteration 3.
Goal Date: 2018-03-05
* Reduce Status mean traffic to 10-40Mb/day(0.5-2 Mb/hour)
* Many topics problem resolved https://github.com/status-im/status-react/issues/2964
* Many topics used for topics subscription
* Setup maximum Whesper's peers

### Acceptable traffic consumption at the start of Status. Iteration 4.
Goal Date: 2018-03-26
* Reduce traffic overhead on a Status start
* Many topics used for offline mailboxing
* Offline mailboxing sends only messages by given period of time (from-to timestamps)
* Update mail server to send only messages that used doesn't receive yet https://github.com/status-im/status-go/issues/690

### Minimal offline mailboxing overhead. Iteration 5.
Goal Date: 2018-04-09
* Further traffic overhead decrease on a Status start
* Use new offline mailboxing API on Clojure script side.

## Success Metrics
5 Mb/day of traffic

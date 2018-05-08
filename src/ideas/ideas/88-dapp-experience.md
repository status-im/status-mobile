## Preamble

    Idea: 88
    Title: DApp Experience
    Status: Draft
    Created: 2018-03-12

## Summary

Interacting with DApps is in the very core of Status. In fact, the shortest description we have for Status is *Mobile Ethereum OS (or Browser)*.

The problem is that new users do not really get a chance to see this aspect of Status, simply because there are very few DApps accessible directly through Status. The only way to find new DApps is on external web sites or search engines. Having to use an external app to access a core feature of our app is bad UX. Furthermore, users who are completely new to these technologies and generally unfamiliar with the concept of DApps might not even think to look for them and would only use Status as a messenger or possibly a wallet.

Briefly put, the minimum we need to provide is a nice selection of DApps so that our users could use our Browser feature.

We had two brainstorming sessions during the Bangkok offsite. The notes from there are on the link below, please do not hesitate to comment:

 https://docs.google.com/document/d/11MRsAO8GdIKVB0bk-osld-cYMMWDOVxB012QHPkQ6_k/edit?usp=sharing

## Swarm Participants

- Lead Contributor: @goranjovic
- Testing & Evaluation: @asemiankevich
- Contributor: @lukaszfryc
- Contributor: <!-- @username -->
- PM: @rachelhamlin
- UX (if relevant): <!-- @username -->

## Product Overview

Objectives:

1. Provide a definition of decentralized apps
2. Provide clear inclusion criteria that dapps need to conform with in order to be listed in our catalog
3. Perform some research o existing dapps for better understanding. For ex https://dappradar.com/
4. Create the initial catalog
5. Provide a transparent and simple process for inclusion of new entries

Key results:

1. Users are able to easily use Status as a browser, having access to a nice and representative list of dapps.
2. New users are introduced to the concept o dapps and why decentralization matters
3. Developers who want to list their dapp in our catalog have a simple and fair process to do so and clear criteria their dapp needs to meet.

Additional notes:

- We may want to add some kind of user rating to allow users to help each other in dapp selection. This is completely independent from the review criteria that a dapp needs to meet in order to be included in the catalog.
- Inclusion criteria should not contain any subjective criteria such as UX or matters of taste. These concerns should be left to end users to decide.
- We may use Status Open Bounty for the review process. The catalog could be a repository with a list of dapps, new entries could be submitted as pull requests and then reviewers would allow or decline based on the criteria. The exact incentivization strategy remains to be decided (e.g. dapp authors could stake some amount of money, reviewers would be rewarded, etc.)
- We can use a separate repo for the catalog. In fact, DApp Catalog itself could be a dapp.



### Product Description



### Requirements & Dependencies


### Minimum Viable Product

MVP for this is a fixed initial DApp Catalog

Description: Currently we have 4 Ropsten dapp suggestions in the hardcoded list under Open DApp. This list should be expanded to include 12 DApps total.
These dapps should be verified to work reasonably well (i.e. at all) and the selection should depend on currently selected network (only
Mainnet and Ropsten in scope for this MVP).

## Dates
### Goal Date: 2018-04-18

Description: Initial DApp Catalog

Exit Criteria:
- Define criteria for inclusion of DApps in initial catalog
- Update Selected DApps to include 12 DApps that meet these criteria
- Include a description for each

## Success Metrics

- N dapps available out of the box in Status dapp catalog
- X users opening a dapp from the catalog per day
- Y users opening a dapp by URL
- M new dapps submitted, reviewed and added to the catalog each month

## Supporting Role Communication
<!-- Once Requirements and Goals are fleshed out, then it should be communicated to supporting organelles if required -->

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

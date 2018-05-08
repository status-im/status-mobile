Idea code: DEV#003
Status: Done
Created: 2017-11-08
Started: 2017-11-14
Ended: 2018-01-04

## Summary

Add ERC20 support

## Idea proposal

As a user I want my [ERC20](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md) tokens to be first class citizen.

## Swarm Participants

* Lead Contributor: @jeluard
* Contributor: @goranjovic
* Support: @3esmit
* Tester: @asemiankevich

Swarm channel: **3-erc20**

## Requirements

A user should be able to manage its ERC20 tokens pretty much the same way as ETH.
In particular `wallet` and `send` command must support ERC20 tokens.

As a first step considered ERC20 tokens will be based on a static list embedded in the application. This might be revisited later (see #18).

ERC20 extensions might also be considered: [ERC223](https://github.com/ethereum/EIPs/issues/223), [Minime](https://github.com/Giveth/minime) ([SNT](https://etherscan.io/token/StatusNetwork) itself is a Minime token).

## Zeplin references

* Assets https://zpl.io/blwD8ka
* wallet main screen (with list of assets shown + managin assets) https://zpl.io/b6d1eNV

## Board
https://github.com/status-im/status-react/projects/4

## Goals & Implementation Plan

Following features should be considered:

* list wallet tokens
* provide default token
* send tokens to a contact
* request token from a contact
* get current price
* get historical price
* get token details (historical price, market cap, # tokens, volume, crowdsale)
* list historical transactions
* add gas and data support when sending transaction
* chart graphs

Can be split in 4 categories:

* wallet token details (access and management)
* token details (static and dynamic data)
* token price details
* token transactions

Token details will require some metadata currently not available in ethereum.
Token price details can be provided by `cryptocompare`.

### Minimum Viable Product

List user tokens and display token details

Goal Date: 2017-12-01
Started:  2017-11-15
Completed: 2017-12-05
Description: token list and details

### Iteration 1

Allow sending and requesting tokens.
Show token historical data and update percentage info

Goal Date: 2017-12-15
Started: 2017-12-06
Completed: 2018-01-04
Description: send/request tokens

### Post Mortem

#### What could be improved

[App structure refinement](#61) was defined during the first iteration and significantly impacted the scope of this iteration. Specifically @goranjovic work around asset details had to be removed.

A number of technical elements has been discovered and added to the todo list during development. It appears primordial to spend some time during the first days to have a detailed understanding of what's to be done and technical impacts. It appears our decentralized nature makes some usually simple things harder to implement.

Finally we discovered pretty late some technical limitations that prevented us to implement some features (e.g. token transaction history).

#### What went well

Overall implementation went smoothly and we had fast test cycle thanks to @asemiankevich .

## Appendix: archive of future work

### Known limitations

Due to technical constraints some features have been left out and will be addressed after prerequisites have been implemented.

* no token transaction history (etherscan API doesn't support that)
* send command: no token support (requires upgrade to API, support for deep linking)
* request disabled for tokens (requires upgrade to API)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
  
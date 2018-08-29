# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.9.26 - Unreleased]
### Added
- Allow user to remove custom avatar
### Fixed

### Changed
- Improved validation in account recovery. We now show a warning if any words are not from our mnemonic dictionary.

## [0.9.25 - Unreleased]
### Added
- Add MOKSHA token on Rinkeby

### Changed
- Update REP contract address
- Update mailservers on Rinkeby network
- Update url of Bounties Network DApp

## [0.9.24 - 2018-08-01]
### Fixed
- Security patch for DApp browser

## [0.9.23 - 2018-07-23]
### Added
- Added dismiss button to "Add to contacts" bar

### Fixed
- Partially fixed issue with 0 fiat in main Wallet screen. We are now explicitly showing an error when app can't fetch 
asset prices, instead of silently failing.

### Changed

## 0.9.23 - 2018-07-23
### Added
- iPad support. Status is now displayed at full native resolution on iPad's
- Deep links support
- Persist browser history
- Added refresh button
- Added more dapps
- Allow for setting custom network id
- Sound for Push Notifications, tap on PN opens corresponding chat

### Fixed
- Fixed Sign in: Cannot paste text within password field [#3931]
- Fixed chat message layout for right-to-left languages
- Fixed parsing of messages containing multiple dots (elipsis)
- Fixed Webview: Screen cut off when using ERC dEX DApp [#3131]
- Fixed links targeting new tabs [#4413]
- Fixed blinking token amounts in transaction history
- Fixed/corrected fiat values in /send and /request messages
- Automatically converts recovery phrase to lowercase during account recovery
- Fixed unread messages counter
- Fixed incoming messages timestamp
- Fixed gas validation: Showing message when total amount being sent plus the max gas cost exceed the balance [#3441]

### Changed
- Removed Mixpanel integration

## [0.9.22] - 2018-07-09
### Added
- Added Farsi public #status channel
- Spam moderation
- Collectibles support (CryptoKitties, CryptoStrikers and Etheremon)
- Added more dapps
- Universal and deep links for public chats, browsing dapps, viewing profiles

### Fixed
- Fixed mailservers connectivity issue
- Clear chat action correctly clear the unread messages counter
- Gracefully handle realm decryption failures by showing a pop up asking the user to reset the data

### Changed
- Downgraded React Native to 0.53.3 for improved performance and decreased battery consumption
- When joining a chat only download one day of history

## [0.9.21] - 2018-06-25
### Added
- Mainnet is enabled by default on new installations. Upgrades will need to manually switch.
- Chat now supports sending SNT and other tokens in 1-1 chats
- New chat UI for /send and /receive commands
- Updated chat message styling
- Updated toolbar style
- Updated DApp list
- New help links on the Profile tab. Read our Beta FAQ, file a bug, or suggest and vote on features.
- Performance improvements when fetching old messages
- Added 5 new default public chats for our friends speaking Korean, Chinese, Japanese, Russian and Spanish.

### Fixed
- Prevented empty commands from being sent in chat
- Fixed minimum amounts in transactions
- Fixed an issue where contacts names may not be shown
- Removed log files from release builds
- Fixed an issue with transactions when navigating back

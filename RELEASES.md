## 1.13

### For iOS and Android
### Features

- New activity manager https://github.com/status-im/status-react/issues/11958 
New chats and group chat invitation will appear in a new section, the activity center, where a user will be able to accept or decline the request.
https://www.figma.com/file/r59kCQxl6tlC0dHkcAJOpP/Action-center-MVP---Mobile?node-id=2%3A8012 (not fully implemented)

- Receive messages from contacts only option
Under privacy in settings a user will be able to select whether they want to receive messages and group chat invitations from contacts only.

- Added delivered confirmation for private groups
Messages sent to private groups will now be marked as confirmed if at least one of the members received it.

- New keycard functionalities: reset a keycard, changing PUK of a card, changing pairing code, create a new keycard backup card.


### Improvements

- Improved UI for starting a new private chat


### Bugfixes

- Fixed issue with crypto on ramp and requiring camera access
- Removed Wyre from list of services in buy crypto feature
- Added support for SOCKS token



## 1.12
### iOS
#### Excerpt
V1.12 Release - Keycard on iOS, Crypto Onramps, and More

Keycard can now be used on iOS devices for hardware enforced security to your keys and accounts! Status v1.12 also introduces simpler access to obtaining crypto via onramps providers as well as some key updates to the messaging experience

##### Added
Keycard iOS integration 
Link previews for our.status.im and medium.om
List of Crypto onramps
Add “delivered” message confirmations to 1:1 messages 
Keycard banner on login screen

#### Fixed
UI issue showing up a chat on a paired device when it should not
UI issue was showing messages as pending if no one is part of the group chat*
Push notification is generated on paired account when you send message to 1-1
UI issue for wrong notification when using two paired devices with same accounts
Improve keycard login time

### Android
#### Excerpt
V1.12 Release - Crypto Onramps and Improved Messaging

Status v1.12  introduces simpler access to obtaining crypto via onramp providers as well as some key updates to the messaging experience including delivered message confirmations in 1:1 private chats

##### Added
Link previews for our.status.im and medium.om
List of Crypto onramps
Add “delivered” message confirmations to 1:1 messages 
Keycard banner on login screen
Local notifications for Mentions

#### Fixed
UI issue showing up a chat on a paired device when it should not
UI issue was showing messages as pending if no one is part of the group chat*
Push notification is generated on paired account when you send message to 1-1
UI issue for wrong notification when using two paired devices with same accounts
Improve keycard login time

## 1.11
### iOS
#### Excerpt
V1.11 also makes some improvements to the overall Status experience. Improved compatibility with DApps creates a more seamless experience with your favorite DeFi, NFT, and other decentralized applications. Improvements to Status Nodes and fixes to Android notifications have been made for better performance with private, group chats and public chats.

Update in the App Store or Google Play if you do not have auto updates enabled.

For the full changelog, see Github https://github.com/status-im/status-react/milestone/49?closed=1

#### Added
- Add Giphy url support in chat
- The Graph (GRT) erc-20 token added to the default list

#### Changed
- Improved compatibility with DApps
- Sync with Status Nodes improvements

#### Fixed
- Fixes to Android notifications
- UI fixes
- Sluggish performance on private group chats bug

### Android
#### Excerpt
V1.11 also makes some improvements to the overall Status experience. Improved compatibility with DApps creates a more seamless experience with your favorite DeFi, NFT, and other decentralized applications. Improvements to Status Nodes and fixes to Android notifications have been made for better performance with private, group chats and public chats.

Update in the App Store or Google Play if you do not have auto updates enabled.

For the full changelog, see Github https://github.com/status-im/status-react/milestone/49?closed=1

#### Added
- Migrate existing account to Keycard 
- Add Giphy url support in chat
- The Graph (GRT) erc-20 token added to the default list

#### Changed
- Improved compatibility with DApps
- Sync with Status Nodes improvements

#### Fixed
- Fixes to Android notifications
- UI fixes
- Sluggish performance on private group chats bug

## 1.10
### iOS
#### Excerpt
Status version 1.10 is here and it is a perfect way to kick off 2021! Another feature rich update that makes the overall messaging, browsing, and transacting experience a whole lot better. Save your most frequently used DApps as favorites for one tap access to DeFi, NFTs, and more. Set a profile image to express yourself in chat. Speed up and cancel pending transactions and pull down to check for new transactions.

#### Added
- Pull to refresh on individual account view
- Save dapps as favorite 
- Switch between dapps in multiple tabs
- Banner to find dapps to buy crypto
- Sent message confirmation icon

#### Changed
- Manage dapp permissions without leaving the browser
- Keycard pairing code updated to readible 5 symbol code
- Profile and settings back to most right position in tab bar

#### Fixed
- Visibility of tab bar in dark mode
- Watching accounts for transaction in chat messages (update status)
- Multiple notifications for a single erc20 transfer 
- Change group chat name when '/' character is included
- Show transaction in chat for small amounts
- Correction to transaction status in chat
- Rotate mailservers on error return
- Remove unused transport security entries 
- Send chat messages in order so that text is always below image
- Performance of list rendering
- Alignment and image flickering issues in status updates
- Disallow multiple pop ups to be shown at the same time
- Add multiple custom history nodes without relogin


### Android
#### Excerpt
Status version 1.10 is here and it is a perfect way to kick off 2021! Another feature rich update that makes the overall messaging, browsing, and transacting experience a whole lot better. Bookmark your most frequently used DApps for one tap access to DeFi, NFTs, and more. Set a profile image to express yourself in chat. Speed up and cancel pending transactions and pull down to check for new transactions.

#### Added
- Pull to refresh on individual account view
- Save dapps as favorite 
- Switch between dapps in multiple tabs
- Banner to find dapps to buy crypto
- Sent message confirmations

#### Changed
- Install with deeplink to public chat (Join to fetch messages)
- Manage dapp permissions without leaving the browser
- Keycard pairing code updated to readible 5 symbol code
- Profile and settings back to most right position in tab bar

#### Fixed
- Visibility of tab bar in dark mode
- Watching accounts for transaction in chat messages (update status)
- Multiple notifications for a single erc20 transfer 
- Change group chat name when '/' character is included
- Show transaction in chat for small amounts
- Correction to transaction status in chat
- Rotate mailservers on error return
- Remove unused transport security entries 
- Send chat messages in order so that text is always below image
- Performance of list rendering
- Alignment and image flickering issues in status updates
- Disallow multiple pop ups to be shown at the same time
- Add multiple custom history nodes without relogin


## 1.9
### iOS
#### Excerpt
With release 1.9, Status gives you the tools to better connect and engage with your friends and communities. You'll find a whole new tab for Status updates. Share what's on your mind and follow what your contacts are up to.

Status updates, and any chat, are now more colorful with opt in link previews.

#### Full

#### Added
* Profile status updates
* Add unfurling of URLs in chats (YouTube)
* Onboarding into public chats

#### Changed
* Add backwards compatibility for ENS Usernames
* Account cards design to accomodate high value accounts
* Rename Status account to Ethereum account for first account on onboarding
* Remove manual fetch 24h in bottom sheet
* Pinch to zoom on images

#### Fixed
* Personal sign method
* Slow asset list
* Loading of older transactions (< April)
* Loading of message history after rejoining a public chat
* Unread message counter update when other user replies to own message
* App crash on navigation from and to generate keys in onboarding


### Android
#### Excerpt
With release 1.9, Status gives you the tools to better connect and engage with your friends and communities. You'll find a whole new tab for Status updates. Share what's on your mind and follow what your contacts are up to.

Status updates, and any chat, are now more colorful with opt in link previews.

#### Full

#### Added
* Profile status updates
* Add unfurling of URLs in chats (YouTube)
* In-app local notifications for transactions(Beta)
* Onboarding into public chats


#### Changed
* Add backwards compatibility for ENS Usernames
* Account cards design to accomodate high value accounts
* Rename Status account to Ethereum account for first account on onboarding
* Remove manual fetch 24h in bottom sheet
* Pinch to zoom on images

#### Fixed
* Personal sign method
* Slow asset list
* Loading of older transactions (< April)
* Loading of message history after rejoining a public chat
* Unread message counter update when other user replies to own message
* App crash on navigation from and to generate keys in onboarding



## 1.8
see https://notes.status.im/release-1.8-notes#
### Android + iOS
#### Excerpt

#### Full
##### V1.8 Release - More people, more private & bug fixes

This release includes updates that further improve private communication and your control over it. Group chats can now include up to 20 members (previously 10). With this increase, group chat preserves it's confidentiality, e2e encryption, and perfect forward secrecy that come with 1:1 private chats; Now usable with larger groups, without negatively impacting the network. Truly private group chats. 

Additionally, you can now opt out of 'History nodes' under Sync settings. With this option you control if you are ok sharing your IP address with a server in order to collect messages send while you were offline. In case you didn't know, you can also [run your own history node](https://status.im/technical/run_status_node.html) to connect to.

Finally, when you receive a new ERC-20 token you can now add it to your wallet by tapping 'Scan tokens'. 



#### Added
* Increased private group chat size to 20
* Allow users not to connect to history nodes
* Added scan button to fetch ERC-20 tokens

#### Fixed
* Fix browser issues with logging in on github.com
* Fix bug with mentions not resolved in messages with markdown
* Fix blank view when open yearn.finance
* Fix entering “0x” in recipient field

Update in the [App Store](https://apps.apple.com/us/app/status-private-communication/id1178893006) or [Google Play](https://play.google.com/store/apps/details?id=im.status.ethereum) if you do not have auto updates enabled.

The APK available is [here](https://status-im-files.ams3.cdn.digitaloceanspaces.com/StatusIm-Mobile-v1.8.0.apk).

For the full changelog, see our [Github](https://github.com/status-im/status-react/commits/release/1.8.x).
> [Verify APK link before publishing blog post]
> 

## 1.7.2

This release fixes an issue with fetching ERC20 balance. It was due to a contract that has been migrated to a different address which cause the whole request to fail.


## 1.7.1
Full release commits: https://github.com/status-im/status-react/commits/release/1.7.1
### Android + iOS
#### Excerpt
Several users have reported an issue in upgrading from release 1.6.1 to release 1.7. This update fixes the issue.

#### Full
Several users have reported an issue in upgrading from release 1.6.1 to release 1.7. This update fixes the issue.

The issue occurs when the application is killed during a migration that is part of the update. Killing the app causes the migration to fail and leaves a persistent database error. This error prevents users from unlocking their profile; Showing an error on password entry. The fix provided above resolves this issue by restarting the migration. It also prevents the issue from occuring if you haven’t updated yet.

So far we’ve only received reports on this issue occurring on Android. We’re monitoring closely to see if further action is required and will take action to prevent this issue from occuring in future updates. Many thanks to our ambassador tbenr and others who reported this issue.

#### Fixed
Retry failed migration


## 1.7  
Full release commits: https://github.com/status-im/status-react/commits/release/1.7.x

### Android
#### Excerpt (446 characters including spaces)
Introducing the ability to mention and be mentioned (Beta). A host of other updates make it easier to recognize people and addresses. Status uses the infamous animal random names by default to provide pseudo-anonymity. You can now give 'Trusting Honeydew Panda' a nickname to recognize them. When sending transactions you can save and select favorite addresses. A safeguard will warn you when sending funds to a contract to prevent loss of funds. 

#### Full
Introducing the ability to mention and be mentioned. Note that this feature is in Beta, your feedback is much appreciated. Aside from Mentions, this release includes a host of other updates that are all about making it easier for you to recognize people and addresses. Status uses the infamous animal random names by default to provide pseudo-anonymity. While your best friend might be the 'Trusting Honeydew Panda' you can now give them a nickname to recognize them. Sending transactions got easier with the new feature to save and select favorite addresses. A safeguard will warn you when sending funds to a contract to prevent loss of funds.


#### Added
* Mentions (Beta)
* Local nicknames
* Wallet address selection (favorites, contacts, recent)
* Private group chat invite links
* Warning when attempting to send a transaction to a contract
* New tokens: UNI, COMP, BAL, AKRO, OXT, aUSDC 
* Collapse long messages for more readible threads

#### Changed
* Removed the ability to enable using Whisper instead of Waku

#### Fixed
* Upgrade phishing detection library
* Decimal rounding when using 'Set max'
* ENS resolving for IPNS names
* SUPR support for old and new token contract




### iOS
#### Excerpt (496 characters incl spaces)
Introducing privacy preserving notifications on iOS. Paired with the ability mention and be mentioned (Beta). A host of other updates make it easier to recognize people and addresses. Status uses the infamous animal random names by default to provide pseudo-anonymity. You can now give 'Trusting Honeydew Panda' a nickname to recognize them. When sending transactions you can save and select favorite addresses. A safeguard will warn you when sending funds to a contract to prevent loss of funds. 

#### Full
Introducing privacy preserving, remote notifications on iOS. Paired with the ability mention and be mentioned. Note that this feature is in Beta, your feedback is much appreciated. Aside from Mentions, this release includes a host of other updates that are all about making it easier for you to recognize people and addresses. Status uses the infamous animal random names by default to provide pseudo-anonymity. While your best friend might be the 'Trusting Honeydew Panda' you can now give them a nickname to recognize them. Sending transactions got easier with the new feature to save and select favorite addresses. A safeguard will warn you when sending funds to a contract to prevent loss of funds.

#### Added
* (Remote) Notifications on iOS
* Mentions (Beta)
* Local nicknames
* Wallet address selection (favorites, contacts, recent)
* Private group chat invite links
* Warning when attempting to send a transaction to a contract
* New tokens: UNI, COMP, BAL, AKRO, OXT, aUSDC 
* Collapse long messages for more readible threads
#### Changed
* Removed the ability to enable using Whisper instead of Waku

#### Fixed
* Upgrade phishing detection library
* Decimal rounding when using 'Set max'
* ENS resolving for IPNS names
* SUPR support for old and new token contract


## 1.6.1
### Android + iOS
#### Excerpt
Introducing a spam mitigation
#### Full
Introducing a spam mitigation
##### Fixed
- Restrict message size on input
- Drop messages based on character length


## 1.6 
### Android
#### Excerpt
#### Full
This release includes a small, but important change to existing functionality: Onboard using a 'Referral URL'. You can confirm that you discovered the app through a partner of Status (Opt-in only). It also includes a fix to a persistant bug that resulted in messages not being marked as seen.

##### Changed 
- Attribute a referrer when installing and onboarding to the app using a Referral URL

##### Fixed
- Mark 1:1 messages as seen (Badge kept reappearing when tapping 'Add as contact')

### iOS
#### Excerpt
#### Full
This release includes a fix to a persistant bug that resulted in messages not being marked as seen as well as an issue that caused the app to crash.
##### Fixed
- Mark 1:1 messages as seen (Badge kept reappearing when tapping 'Add as contact')
- Crash on iOS when device passcode is switched off and 'Save password' is enabled inside the app


## 1.5 
### Excerpt (493 characters excl url)
Now including those features that make you feel connected while being miles apart. If there was a time you wondered if you could chat with friends and family, without giving up your privacy or security, wonder no more. Send images, audio messages, and show your love with emoji reactions ❤️; Not a word is sent to the cloud. #appdown is not a thing. E2E encryption is. If you have Internet you can send and receive messages. Securely. Privately. Always.
For the full changelog, see our Github

### Full
Now including those features that make you feel connected while being miles apart. If there was a time you wondered if you could chat with friends and family, without giving up your privacy or security, wonder no more. Send images, audio messages, and show your love with emoji reactions ❤️; Not a word is sent to the cloud. #appdown is not a thing. E2E encryption is. If you have Internet you can send and receive messages. Securely. Privately. Always.

Making new friends on open communities is now easier as well. We took the liberty of surfacing a few active public chats by category. 

For the full changelog, see our Github


#### Added
* Send images in 1:1 and group chats
* React with emojis to any message
* Send voice messages in 1:1 and group chats
* Discover public chats by category
* Ability to delete an account

#### Changed
* Major interface improvements throughout, introducing our Quo component framework
* Auto-forwarding of the onboarding carousel 
* Clarify and move 'Access existing keys' to import accounts
* Showing password entry and confirmation on the same screen
* Add contacts or invite friends from Contacts
* Searchbar includes search for ENS names


#### Fixed



## 1.4.1 - Round of improvements
### Excerpt
This intermediate release, 1.4.1, includes bug fixes as well as UX and accessibility improvements. Most visibly, a gorgeous set of new icons, accessible input fields and toggling password entry visibility. Next to that the release includes updates to Notifications on Android, like being able to open Status, as well as close the notification service, directly from the notification banner.

For the full changelog, see our [Github](https://github.com/status-im/status-react/commits/release/1.4.x)


### Full
While focused on building new features there are a few improvements we did not want to hold off on sharing. This intermediate release, 1.4.1, includes bug fixes as well as UX and accessibility improvements. Most visibly, a gorgeous set of new icons, accessible input fields and toggling password entry visibility. Next to that the release includes updates to Notifications on Android, like being able to open Status, as well as close the notification service, directly from the notification banner.

For the full changelog, see our [Github](https://github.com/status-im/status-react/commits/release/1.4.x).



#### Added
- Language support for Keycard use with Status (see our supported languages in [FAQ](https://status.im/faq/))

#### Changed
- Open the app and close the notification service from the notification banner
- More informative error messages for transaction overview contract and network fees
- Added a check for encoded characters in urls to increase browser security
- More accessible text input
- Replace icons with a more unified, carefully crafted set
- Update connectivity snackbar to explain messages can still be send and received when offline; only history does not sync

#### Fixed
- ENS name is no longer infinitely pending when transaction has failed
- Added explainer that restart of the app is required to Disable logs
- Changed notification icon styling to follow convention of other apps
- Fixed critical issue that caused occasional app crashes
- Now giving feedback when blocking users is in progress
- Apply dark mode/light mode changes to System panel
- Update balance of custom tokens to be visible upon adding the token
- Show chat view after chat search gives no results (chat list showed up empty)
- Bring back numpad to sign with Keycard after dismissing an error (e.g. having tapped an unpaired card)
- Keyboard changes (include decimals for amount input and auto-capitalization)





## 1.4 - Keycard, notifications DRAFT
In version 1.4, Status introduces a much anticipated integration with Keycard - the secure, contactless hardware wallet, also designed and developed by The Status Network. This integration enables Keycard holders to store their private keys offline on the Keycard device, add hardware-enforced authorizations to all of their transactions, and introduce two-factor authentication to log into their Status account. 

Another highly anticipated feature is the introduction of notifications. These notifications are [ANDRE to add detail]....

The final addition to v1.4 is the introduction of  browser support for EIP 1139 – A JavaScript Ethereum Provider API for consistency across clients and applications.

Beyond that, version 1.4 includes some updates to the FAQ, extended translation support, and some fixes to ENS username displays, dark mode contrast, QR code scanner and more. 

For the full changelog, see our [Github](https://github.com/status-im/status-react/commits/release/1.4.x).

#### Added
- Keycard integration on Android
- Browser support for eip-1193 (?)
- Notifications (Android)
- Content type for images in preparation of 'Images in chat' release

#### Changed
- Extended and consolidated FAQ
- Extended translation support 
- Animation in header (Profile + Wallet)


#### Fixed
- ENS in chat only when transaction is confirmed
- Dark mode contrast (increased)
- Updated and expanded token icons
- Resolved error on scrolling account view
- Parse markdown in subheader chat list
- Banner to back up seed phrase tappable
- [Chat performance improvements](https://github.com/status-im/status-react/pull/10711)
- QR code scanning issue when scanning from join.status.im


## 1.3 - Dark Mode, Group Chat

In version 1.3, Status catches up to the most important design trend in recent history with the introduction of a dark mode.

Hyperbolic? Perhaps, but we've heard your requests and we made it happen. Your app's appearance will now default to match your system settings, but if you'd like, you can head to `Profile` > `Appearance` to override it. Dark mode sure is easy on the eyes.

Beyond that, we're bringing back private groups, another highly requested feature. Private groups allow you to add up to 9 friends to an invite-only chat. At the protocol level, group chats in Status function similarly to 1:1 chats. They're encrypted end-to-end and utilise perfect forward secrecy to protect your privacy. 

In the future, we'd like to expand these to host more than 10 members. We'll embark on further testing to determine the upper limit that our current protocol can support.  

Lastly, we've changed our webview implementation over to the react-native-community package, to remain in sync with the React Native community and benefit from upstream improvements.

As always, we've also included a few nice fixes in this release. For the full changelog, [see our Github](https://github.com/status-im/status-react/commits/release/1.3.x). 


#### Added
- Dark mode
- Group chat

#### Changed
- Browser implementation now using react-native-community webview package

#### Fixed
- Updates to back navigation
- Persistant wallet value
- Update to all text input, increasing pressable areas
- Improved seed phrase text input on small screens
- Easier to find user's public key for sharing (https://github.com/status-im/status-react/pull/10207#event-3163431514)
- Shortened universal links (https://github.com/status-im/status-react/issues/10083)

## 1.2

### Short Version

The change log is small, but significant! We've enabled mailservers that use our forked version of Whisper, known as Waku. Waku servers reduces bandwidth consumption by the app and enable Status to serve more concurrent users.

A few bug fixes and optimizations are included, too.

### Blog Post

This release is one of the most important in Status' history. The change log is small, but significant. Today we're rolling out new mailservers that use our fork of the Whisper messaging protocol, known as Waku. 

Waku mailservers prevent Status from using excessive mobile bandwidth. With Waku, Status can support 10 times as many users as with Whisper servers alone.

Regular Whisper mailservers use topic-based filters to surface only the chats that you want to see, but your device still receives all messages sent over the network. Filtration happens after the fact. 

To save on resources, Waku mailservers simply tell other nodes in the network which topics your device would like to receive content for, filtering before the messages reach your device.

Heavy message traffic can be incredibly resource intensive, depending on how many topics you follow. For a heavy user, the difference can amount to several hundred kilobytes of data per minute. 

There is a potential privacy tradeoff in making your followed topics apparent to other nodes—it is easier to know if a given IP address belongs to a certain chat. 

In v1.2, we offer the legacy Whisper servers as an option under `Advanced` settings. You can disable Waku by toggling `Waku enabled`. Waku mailservers talk to existing mailservers without any disruption. 

With further improvements to Waku in the pipeline, Status becomes accessible to even more users and can deliver peer-to-peer messaging without draining unnecessary data. 



### Changelog

#### Changed
- Waku mailservers 
- Updating wallet fiat prices even more frequently (awaiting clarification)

#### Fixed
- App crashing when webview is closed
- Messages not showing when received while on a different tab
- Token balances displayed as 0 after relogin  

## 1.1

Biggest changes:
- Status now support importing private key and seed phrase inside multiaccount!
(https://github.com/status-im/status-react/pull/10100)
- all chat dialogs moved to bottom sheet
- added "Mark all as read" option
- unread badge is changed for public chat

More detailed:

ENS name improvements:

- https://github.com/status-im/status-react/pull/9952 - fixes ENS to new registry 
- https://github.com/status-im/status-react/pull/9997 - Check for empty public key (for ENS names)
- https://github.com/status-im/status-react/pull/10007 - improve ENS  display in Public chats
- https://github.com/status-im/status-react/pull/10022 - fix crash on long ENS names


General improvements:

- https://github.com/status-im/status-react/pull/10027 - deep link handling (rename get.status.im to join.status.im)
- https://github.com/status-im/status-react/pull/9940 - changed navigation on logout
- https://github.com/status-im/status-react/pull/9917 - added search to currencies and assets
- https://github.com/status-im/status-react/pull/9941 - improvement on snackbar by considering login time (less "Offline", "Connected" after login)
- https://github.com/status-im/status-react/pull/9898 - added qr code reader for scanning QR when add watch-only address
- https://github.com/status-im/status-react/pull/9882 - Respond to FaceID failure callback
- https://github.com/status-im/status-react/pull/9915 - Make bottom sheet height dynamical
- https://github.com/status-im/status-react/pull/9931 - 2 lines for user name on intro screen

Chat improvements:
- https://github.com/status-im/status-react/pull/10056 - added "Mark all as read" feature
- https://github.com/status-im/status-react/pull/9993, https://github.com/status-im/status-react/pull/9999 - better handling of ENS names / chat keys when starting new chat
- https://github.com/status-im/status-react/pull/10117 - reworked unread message indicator in public chats
- https://github.com/status-im/status-react/pull/9868 - all chat dialogs are moved to bottom sheets

Performance improvements: 
- https://github.com/status-im/status-react/pull/9995 - Offload chat messages
- https://github.com/status-im/status-react/pull/10120 - Android app UI is slow after background

Issues fixed:
- https://github.com/status-im/status-react/issues/10002
- https://github.com/status-im/status-react/issues/8797
- https://github.com/status-im/status-react/issues/6839
- https://github.com/status-im/status-react/issues/9754
- https://github.com/status-im/status-react/issues/10044
- https://github.com/status-im/status-react/issues/10041
- https://github.com/status-im/status-react/issues/9886

## 1.0

### Blog Post

Status version 1 is live!  

After nearly a year of quiet and focused development, we've restructured the core application, built out the promises of the white paper and prepared the app for a future in which the Status Network seeks to deliver on the promises of Ethereum at large.

While the core mobile application is no longer the sole focus of the Status Network, it is integral to our dream of a decentralized web. With Status, we're building not just a tool for private and secure communication, but a gateway into an emerging ecosystem of distributed applications that embodies what is possible with decentralized technologies today. We believe apps like Status represent the future of the web—enabling an internet that can uphold human rights, and providing a path towards more open and equitable systems.

Today, the underpinninngs of the core application are still tethered to the Ethereum stack. Whisper is still the communication layer of Status, and Status is still the only production use case of Whisper.

The scalability of Whisper is not significantly improved from what it was roughly three years ago, when we first embarked on this project. We face immense challenge in building on this stack every day.

But we also feel ready to share our progress with the world. Status version 1 launches with the Ethereum wallet, web3-enabled browser and peer-to-peer messenger that our community knows well. It includes new SNT utility features, like a sticker marketplace and a decentralized DApp directory (https://dap.ps). Other white paper features, such as the fiat-to-crypto Teller Network and incentivized messaging with Tribute to Talk, are well underway.

Most pressingly, we're working to remedy the limitations of Whisper. In a forthcoming release, we'll introduce Waku mailservers, which will reduce the amount of bandwidth Status consumes—an important point for accessibility—and extend the capabilities of our nodes so that Status can support more concurrent users.


[Link to user guide]
[Link to roadmap]
[Shoutout contributors & team]
Huge shout out to our contributors:
Enrico
Yalu (user GH name)
Acolytec3

-----

**Support comms** 
- How to submit stickers for sticker marketplace ([google form](https://docs.google.com/forms/d/19itZB_V5bVW4cyV8a2ifDWHuQSr9V_dkVMCl6ARxOag/edit))
- How to share your feedback with us (#status, primarily)
- Learn more about Status - Glossary, [FAQs up-to-date](https://status.im/docs/FAQs.html), [user guide by Jonny](https://docs.google.com/presentation/d/11o53KGcuzKouDU5VBeTB01mLCm41hKKuraz-AaudsEg/edit#slide=id.p)
- How to release your ENS name ([here](https://hackmd.io/QrfTRP6sQ9aQzjFyb_IfRQ?both))
- How to set up your existing ENS name ([here](https://hackmd.io/QrfTRP6sQ9aQzjFyb_IfRQ?both))

-----

**v1.0 Change Log**
**Added**
• Support for multiple Ethereum wallets within one Status account
• Generate unlimited additional BIP 44 wallets in Status
• Add watch-only addresses as well
• Brand new onboarding and key encryption flow
• Choose your random name and public chat key during onboarding
• You also can recover a seed phrase from any mnemonic compatible wallet
• Improvements to wallet transaction flow UI
• Sticker market and two initial sticker packs
• Choice of wallet to use when interacting with Dapps
• [Proper markdown support](https://github.com/status-im/status-react/pull/9409#issue-338045622)
• ENS username setting option for use in chat
• Improved chat intro screen and public chat list
• Updated chat command flow, with additional privacy layer to be compatible with multiaccount

**Removed**
• Custom display names
• Custom profile photos, temporarily—photos will become a feature of ENS
• Push notifications—these will be improved and reintroduced for Android
• Group chat, temporarily—fixes to be prioritized for v1.1
• Google Firebase database and Realm.js DB—performance gains, and more in line with our principles  (fewer third party services)

**Changed**
• Algorithm for deriving public chat key changed, performance. (andrey review: pubkey or 3 word name?, not sure if Algorithm for deriving pubkey can be changed)
• Storage of app state moved to Status-go—performance improvements in chat load times
• Browser privacy mode enabled by default, option removed (EIP1102)
• Updates to EIP1102 for compatibility (https://github.com/status-im/status-react/pull/9629)
• Protocol breaking changes: removed transit encoding in favor of protobuf, for better performance and interoperability with other languages
• Roughly 45% improvement in how quickly chats load
• Profile screen cleaned up


----

> Names and picture have changed as we use a different algorithm to generate it (linear > > feedback shift register instead of Mersienne Twister).
> There are breaking changing between protocol, as agreed before, so no compatibility at all. Previous version won't be able to receive/send messages to v1.

> We confirmed the performance improvement with the test-team, roughly 45% improvement in how quickly chats load (maybe greater).
>indeed, true, good move to make together.
> but I would suggest to also add in the release changelog that the algorithm for deriving a > pubkey -> 3 word name changed, just because a standard was changed
> this is also good to say because there is performance gains.


## 0.13.2
- V1 notification
- Bug fixes:
    - Neverending loading indicator (#8550)
    - Last message jumps after chat is opened (#8556)
- Sticker Market with two packs launched on mainnet
- Account explorer in preparation for multi-account
- Onboarding face lift (preparing for multiaccount/keycard)

## 0.13.0

This release comes with a long change log and one popularly requested new feature: custom, user-added ERC20 token support. 

If you'd like to see a token that the Status wallet does not display by default, you can now add it yourself. From the wallet screen, open `Manage Assets` from the `...` menu and select `Add custom token`. Fill in the token's contract address, and the relevant information, and you will then be able to view this asset in your wallet. 

We've also added and altered our privacy settings:

1) New `Preview privacy mode`: toggling this option in your profile will ensure that Status displays a blank screen when switching apps on your phone, to protect you from accidentally exposing your information while doing other things. Thanks to contributor [bitsikka](https://github.com/bitsikka) for this feature!
2) `Browser privacy mode` has been relocated from the profile to the browser, and reconfigured into a two option setting. Selecting `Require my permission` for this setting ensures that privacy mode is on, but still means that some DApps may be incompatible with Status.

![](https://notes.status.im/uploads/upload_c9ea844cdcdc1ce5a786373ac08521bc.png)

There are a few more important changes:

- `Fetch more messages` was introduced in 0.12.0 to fill gaps in your message history; it now backfills gaps of up to 30 days in public chats, increased from 24 hours.
- We've removed the swipe functionality from the chat screen. To remove a chat, you can now tap on it and hold, pulling up a bottom menu. Another shout out to [bitsikka](https://github.com/bitsikka) for this change!
- We've reintroduced bug reporting on both iOS and Android: shake your phone to pull up an email report, or enable `Development mode` from your profile screen to `Report a bug` from there.

Lastly, we've had some great contributions from the community for this release. They've added new features, refactored and even merged some changes to our developer tools.

Special thanks to [tbenr](https://github.com/tbenr), [bitsikka](https://github.com/bitsikka), [m0ar](https://github.com/m0ar) and [alexanmtz](https://github.com/alexanmtz) for your recent [commits](https://notes.status.im/CgruSsRzQVGhsagaLxXGqQ?view#0130-mobile-release)! 

To get involved, you can find our current bounty issues on Gitcoin [LINK]. 



**Added**
- Custom, user-added ERC20 support 
- New bug reporting options: shake device to send a report, with logs attached
- Bloom Protocol token - thanks to contributor [m0ar](https://github.com/m0ar)!
- Preview privacy mode to display blank Status screen when switching apps - thanks to contributor [bitsikka](https://github.com/bitsikka)! 
- URLs for transactions open in Status (partial [EIP681](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-681.md) support) 

**Changed**
- Moved `Browser privacy Mode` toggle to the browser screen as new preference setting
- `Fetch more messages` button now retrieves up to 30 days of history in public chats 
- Long press a chat for more options; no more swiping
- Fewer messages rendered before a chat is scrolled for better performance ([8191](https://github.com/status-im/status-react/pull/8191))
- Using checksummed addresses in wallet (EIP55) ([8121](https://github.com/status-im/status-react/issues/8121) and [4959](https://github.com/status-im/status-react/issues/4959)) 
- Scanning QR code from a DApp does not cause redirect

**Fixed**
- Browser crash when incorrect domain entered ([8239](https://github.com/status-im/status-react/issues/8239))
- ENS registration bug ([8249](https://github.com/status-im/status-react/issues/8249))
- Wallet balance updates correctly after Tx received ([8107](https://github.com/status-im/status-react/issues/8107))
- Support for EIP1577/`contenthash` field for ENS
- Numeric validation for `send`/`receive` chat commands ([1442](https://github.com/status-im/status-react/issues/1442))
-  Allow to select chat item below plus button ([7859](https://github.com/status-im/status-react/issues/7859))


## 0.12.0

In this release, we're announcing the launch of Discover!

Discover is our new portal for exploring the decentralized web. It has long been our vision for Status to act as a gateway to the new web, just like Chrome or Safari do for web2.

Until this point, however, we've prioritized a curated list of DApps over an open-ended experience. 

With the launch of Discover, our browser becomes just that—a browser, with special capabilities for web3.

Discover is its own web app and the new go-to for finding DApps. For now, we've ported over our existing list, with a few upgrades to the UI.

But in the near future, we're decentralizing Discover, enabling anyone to add a DApp and giving the community control over curation through an SNT voting mechanism. More on that [here](https://our.status.im/discover-preparing-for-launch/).

Discover can be accessed from any browser at https://dap.ps, so you can participate on desktop as well.

This release also comes with a noteworthy chat improvement. Previously, a gap in connection to the mail servers—which happens if Status is closed for more than 24 hours or otherwise loses connection—meant that some messages might be lost.

We now detect gaps where messages are missing and offer you the option to fetch the missing history. By tapping `Fetch messages` when it appears within a chat, you can backfill missing history. This is a big step forward for reliability in our peer-to-peer service.

**Added**
- `Fetch messages` option to get missing message history
- Discover is live on https://dap.ps

**Changed**
- `DApps` screen is now fully browser-centric with address bar, history and a link to Discover 


**Fixed** 
- Contact syncing between 0.11.0 and newer builds
- 503 error when accessing DApps
- More reliable history for token transactions

## 0.11.0

Download 0.11.0 and you'll notice one thing right away: we have a new navigation!

The home screen has been split into two tabs: Chats & DApps. From the chat view, you'll find the option to start or join a new chat has moved from the top right corner to bottom center.

The DApp list also looks slightly different. We've done away with info screens for each DApp. Once you navigate to a URL, you'll find that your browser history appears under a `Recent` section at the top.

This is phase one of bigger improvements to come, so we'd love to hear your feedback.

We've also made preparations for the impending [Chaos Unicorn Day](https://chaos-unicorn-day.org/) in this release. 

Reminder: On April 1st, we'll experiment with eliminating all centralized, third party services from Status, including our own node clusters. To safeguard from the chaos, we've prepared [a guide for you](https://our.status.im/bulletproofing-against-chaos-unicorns-with-status-on-arm/).  



**Added** 
- New DApps added 
- New extensions features integrated within Status
- Chaos mode and preparation for Chaos Unicorn Day

**Changed**
- New bottom navigation: chats & DApps are separated
- Improved UI performance (https://www.pivotaltracker.com/story/show/164245989)

**Fixed** 
- Universal link to public chat bug (https://github.com/status-im/status-react/issues/7549)
- Beta warning popup (https://github.com/status-im/status-react/issues/7771)
- DApp offline error (https://github.com/status-im/status-react/issues/7188#issuecomment-474308724)
- Bug with QR code scanner for custom bootnodes (https://github.com/status-im/status-react/pull/7782)
- Error message when entering an incorrect custom mailserver (https://github.com/status-im/status-react/issues/7752)


## 0.10.1 - Hot Fix

**Added** 
- Option for automatic mail server selection to optimize connection

**Fixed**
- Copy errors corrected
- "Fetching messages" indicator appears on home screen

## 0.10.0

A handful of exciting new features and fixes coming to you fresh in 0.10.0.

First, a word about our new version numbers. Until now, our build versions have followed a convention of minor increments: 0.9.31, 0.9.32, 0.9.33. To better reflect the scope of our releases, we've [decided to level up](https://discuss.status.im/t/new-status-versioning-scheme/1066) to 0.10.0 and increment by 0.1.0 for typical releases, and 0.0.N for bug fixes. Our routine is business as usual otherwise.

Onto the highlights from 0.10.0.

The option to block a user will come in handy for those who participate in popular public chats. If someone is spamming a chat or otherwise bugging you, simply tap on their icon and hit `Block contact` from their profile. You'll no longer see any of their messages.

From _your_ profile screen, you can undo this action via a new contact list feature. Open  `Contacts` to find a list of users who are able to see your info—namely, photo and display name—and from there, `Blocked users` to see and unblock users as you wish.

To better find users you _do_ want to hear from, we've added a search bar to the home screen. This allows you to filter through chats and browser history items. Text search within a chat is still to come.

Back on your profile screen, you'll find another helpful new option to control message syncing. If on a mobile network, you can require Status to ask before syncing conversations, in order to protect against heavy data usage. You can also enable mobile network syncing by default, or turn these options off completely. 

You'll also find a new DApp permissions section here. DApps that adhere to browser privacy mode [ask permission](https://our.status.im/0-9-33/) to access your Ethereum account. You can now revoke access for a given DApp using these settings.

And that concludes the most significant changes of this release! For more, check out the change log below.

**Added**
- Ability to block specific users from profile view
- Contacts and blocked users list
- Search bar for chats and browser history 
- Option to turn off syncing for chat on cell connection 
- DApp web3 permissions management 
- New DApps and WBTC token

**Changed**
- Push notifications include sender name
- Contact updates pushed periodically to better maintain user data
- Public chats show first letter instead of #
- Minor tweaks to chat UI

**Fixed**
- Status now works on restricted [ports](https://github.com/status-im/status-react/pull/7382)
- Overlap on small screens during log-in
- False mailserver connection error [removed](https://github.com/status-im/status-react/issues/7531)
- Premature sending of contact requests on Profile > Send message
- DApps on same host (i.e. IPFS) no longer share user-granted permissions

Ready to update to 0.10.0?

iOS
Within TestFlight, make sure you see version 0.10.0 and tap ‘INSTALL.’
If you don’t currently have Status installed, access via TestFlight here: https://testflight.apple.com/join/J8EuJmey.

Android
If you are not automatically asked to update, go to Status.im in the PlayStore and click ‘Update.’
For more information, please join us in Status.
We're more than happy to answer your questions and help you out.

---
Social copy:
Release 0.10.0 is here! A whole bunch of exciting features, blocking contacts, searching, & more. Check out the blog for more details and update Status. our.status.im/0-10-0/



## 0.9.33

We're back in action! After a pause for the holidays and a transition back that saw us release 4 small updates to v.0.9.32, we've got a brand new mobile version for you.

This release is a big one.

First, we've made significant improvements to log-in, yielding a 10x increase in log-in speed. Android users will also be pleased to find a "Save password until logout" toggle to match iOS. This will save you significant time on launch of the app.

Push notifications are improved by a number of fixes, including opening to the appropriate chat and no longer exposing your language settings to recipients using a different language than your own.

Because of these fixes, anyone using an older version of the app **_will not receive notifications_** from users on v.0.9.33. Furthermore, iOS users will not receive push notifications while the app is closed, nor if they are on certain iOS versions (11.X). This is a known and temporary issue.

Browser privacy mode is now enabled by default. This means that DApps will be required to ask permission before connecting to your wallet, and it may cause some DApps to break. If you find a DApp isn't working, you can go to your profile screen and toggle privacy mode off. In many cases, this should fix the issue. 

We're hopeful that more and more DApp developers will be up to speed with this new convention soon. 

Device pairing is no longer hidden under development mode. Visit the device section on your profile screen to "Pair this device" and sync messages and contacts from your phone to desktop. You can also name your devices to track which you've paired.

For the full list of changes, see below.

**Added**
- Device pairing and naming
- "Save password until logout" for Android
- New DApps including Aragon and Dragonereum 
- BUFF token for ETHDenver
- Titles and favicons in browser
- Support for EIP1577, multihash interface for ENS names

**Changed**
- Privacy mode enabled by default
- Log-in time improved 10x
- Private group chats limited to 10 members
- Connectivity loading indicator no longer appears as "Fetching messages"
- Chat bubble width increased
- Ethereum provider consistent with EIP1193
- Upgraded styling for group chats

**Fixed**
- Metadata leakage of language settings in push notifications
- Push notifications failing to open chat
- Loading indicator in browser
- Results displaying properly in Etheroll
- Issues with extensions installation
- Wrong password handling

-------

_Notes to call out_
- Privacy mode by default instructions
- Breaking change: no PNs for old versions after next mobile release - https://github.com/status-im/status-react/pull/6893
- Setting up device pairing


## 0.9.32
Introducing beta support for Private group chats! 
In this release you’ll find the ability to start private group chats with your friends. These group chats are e2e encrypted and utilize Signal's [double ratchet algorithm](https://en.wikipedia.org/wiki/Double_Ratchet_Algorithm) for guaranteed privacy.  To start chatting go to the Home tab, tap "+", then Start group chat.

There is also beta support for device pairing between all devices. To begin syncing messages and contacts across devices, go to Profile, tap advanced and turn on development mode, now go to devices and tap pair this device.



Added
- Private group chats with increases security using Signal's double ratchet algorithm (In beta. Still being tested. Feedback welcome!)
- Device pairing and syncing with perfect forward secrecy (in dev mode)
- Added DApp Decentraland


Changed
- Support multi-extensions store
- Restyled wallet onboarding flow, main wallet screen, and sent-transaction screen
- Faster sign-in

Fixed
- Transaction history fixes 
- Updating enabled tokens before navigating to wallet 

[All changes](https://github.com/status-im/status-react/compare/release/0.9.31...release/0.9.32)

## 0.9.31

Version 0.9.31 brings Status up to speed with the latest privacy standard for Ethereum browsers, introduced by EIP1102. 

You'll find a new privacy setting on your profile screen called "Browser privacy mode."

If you toggle this setting ON, DApps will be required to ask your permission before accessing your Status wallet. DApps that do not currently follow this standard may not work!

If you run into trouble, you can always toggle this setting OFF. With Browser privacy mode OFF, DApps can access your Status wallet without your permission. 

We're encouraging DApp builders to adopt this new standard and make web3 safer for users as soon as possible. Check the Status blog for details on how to comply.



Added
- Browser privacy mode to toggle new web3 security setting on or off
- New DApps: Crypto Takeovers, Cryptographics, blockimmo and SNT Voting DApp
- ST to assets list

Changed
- Changing mailserver no longer requires a logout
- Send button is inactive when there isn't an active internet or mailserver connection

Fixed
- ENS addresses with unregistered chat IDs no longer resolve in chat
- CryptoKitties log-in issue
- Restored missing transaction error messages
- Granting profile access applies per DApp rather than per host

[All changes](https://github.com/status-im/status-react/compare/release/0.9.30...release/0.9.31)

## 0.9.30

Added

- ENS registration link on profile screen
- Kudos tokens for the Status #CryptoLife Hackathon
- Reply and quote a message in chat. Long tap a message and tap Reply.
- Chat now resolves all .eth address
- New Farsi and Latin American Spanish translations
- Every Dapp gets its own built in chat room.  Tap the chat icon from the Dapp browser to start chatting
- Added Kickback Dapp
- New custom RPC networks for POA Network and xDAI Chain


Changed
- Anonymous names now shown in chats and profile
- Disabled "sign transaction" button when offline
- Renamed default international public chats to ensure universal links would work. For example “status 中文” was changed to “status-chinese”. 
- Removed Instabug for bug reports. In-app help is now available in #status

Fixed
- Fixed an issue where messages are not saved to the database in some circumstances
- Fixed an error when fetching history on certain accounts
- Fixed logging in after logout when opening app from a notification
- Fixed profile QR code
- iPhone XS Max improvements

Known Issues

- POA and xDAI networks are only available for new accounts, not upgrades
- For more POA and xDAI network considerations see this issue post.

## 0.9.29

This releases improves message validation to prevent potential crashes or malformed messages. We recommend all users upgrade.

The fix makes these improvements:  
- add validate method to StatusMessage protocol 
- spec all message types for use in validate method 
- use valid method after transit/decode step to reject invalid messages


## Older Versions

- [Google docs](https://docs.google.com/document/d/1Z0mFlJJkNbRgv6qm80ob5g4PEvsI8cTmbwZpcRPWPho/edit#)


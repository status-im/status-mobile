(ns status-im.translations.en)

(def translations
  {
   ;;common
   :members-title                         "Members"
   :not-implemented                       "!not implemented"
   :chat-name                             "Chat name"
   :notifications-title                   "Notifications and sounds"
   :offline                               "Offline"
   :search-for                            "Search for..."
   :cancel                                "Cancel"
   :next                                  "Next"
   :open                                  "Open"
   :description                           "Description"
   :enter-url                             "Enter URL"
   :open-dapp                             "Open ÐApp"
   :url                                   "URL"
   :type-a-message                        "Type a message..."
   :type-a-command                        "Start typing a command..."
   :error                                 "Error"
   :unknown-status-go-error               "Unknown status-go error"
   :node-unavailable                      "No ethereum node running"
   :yes                                   "Yes"
   :no                                    "No"

   :camera-access-error                   "To grant the required camera permission, please go to your system settings and make sure that Status > Camera is selected."
   :photos-access-error                   "To grant the required photos permission, please go to your system settings and make sure that Status > Photos is selected."

   ;;drawer
   :switch-users                          "Switch users"
   :logout-title                          "Log out?"
   :logout-are-you-sure                   "Are you sure you want\nto log out?"
   :logout                                "Log out"
   :current-network                       "Current network"

   ;;home
   :home                                  "Home"

   ;;chat
   :is-typing                             "is typing"
   :and-you                               "and you"
   :search-chat                           "Search chat"
   :members                               {:one   "1 member"
                                           :other "{{count}} members"
                                           :zero  "no members"}
   :members-active                        {:one   "1 member"
                                           :other "{{count}} members"
                                           :zero  "no members"}
   :public-group-status                   "Public"
   :active-online                         "Online"
   :active-unknown                        "Unknown"
   :available                             "Available"
   :no-messages                           "No messages"
   :suggestions-requests                  "Requests"
   :suggestions-commands                  "Commands"
   :faucet-success                        "Faucet request has been received"
   :faucet-error                          "Faucet request error"

   ;;sync
   :sync-in-progress                      "Syncing..."
   :sync-synced                           "In sync"

   ;;messages
   :status-sending                        "Sending..."
   :status-pending                        "Pending"
   :status-sent                           "Sent"
   :status-seen-by-everyone               "Seen by everyone"
   :status-seen                           "Seen"
   :status-delivered                      "Delivered"
   :status-failed                         "Failed"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "second"
                                           :other "seconds"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "hour"
                                           :other "hours"}
   :datetime-day                          {:one   "day"
                                           :other "days"}
   :datetime-ago                          "ago"
   :datetime-yesterday                    "yesterday"
   :datetime-today                        "today"

   ;;profile
   :profile                               "Profile"
   :edit-profile                          "Edit profile"
   :main-currency                         "Main currency"
   :message                               "Message"
   :notifications                         "Notifications"
   :not-specified                         "Not specified"
   :public-key                            "Public key"
   :phone-number                          "Phone number"
   :share-contact-code                    "Share my contact code"
   :update-status                         "Update your status..."
   :add-a-status                          "Add a status..."
   :status-prompt                         "Set your status. Using #hastags will help others discover you and talk about what's on your mind"
   :add-to-contacts                       "Add to contacts"
   :in-contacts                           "In contacts"
   :remove-from-contacts                  "Remove from contacts"
   :start-conversation                    "Start conversation"
   :testnet-text                          "You’re on the {{testnet}} Testnet. Do not send real ETH or SNT to your address"
   :mainnet-text                          "You’re on the Mainnet. Real ETH will be sent"

   ;;make_photo
   :image-source-title                    "Edit picture"
   :image-source-make-photo               "Capture"
   :image-source-gallery                  "Select from gallery"

   ;;sharing
   :sharing-copy-to-clipboard             "Copy to clipboard"
   :sharing-share                         "Share..."
   :sharing-cancel                        "Cancel"

   :browsing-title                        "Browse"
   :browsing-open-in-web-browser          "Open in web browser"
   :browsing-open-in-browser              "Open in browser"
   :browsing-cancel                       "Cancel"

   ;;sign-up
   :contacts-syncronized                  "Your contacts have been synchronized"
   :confirmation-code                     (str "Thanks! We've sent you a text message with a confirmation "
                                               "code. Please provide that code to confirm your phone number")
   :incorrect-code                        (str "Sorry the code was incorrect, please enter it again")
   :phew-here-is-your-passphrase          "Phew, that was hard. Here is your passphrase, *write this down and keep it safe!* You will need it to recover your account."
   :here-is-your-passphrase               "Here is your passphrase, *write this down and keep this safe!* You will need it to recover your account."
   :here-is-your-signing-phrase           "Here is your signing phrase. You will use it to verify your transactions. *Write it down and keep it safe!*"
   :phone-number-required                 "Tap here to validate your phone number & I'll find your friends."
   :shake-your-phone                      "Found a bug or have a suggestion? Just ~shake~ your phone!"
   :intro-status                          "Chat with me to setup your account and change your settings."
   :intro-message1                        "Welcome to Status!\nTap this message to set your password and get started."
   :account-generation-message            "Gimmie a sec, I gotta do some crazy math to generate your account!"
   :move-to-internal-failure-message      "We need to move some important files from external to internal storage. To do this, we need your permission. We won't be using external storage in future versions."
   :debug-enabled                         "Debug server has been launched! You can now execute *status-dev-cli scan* to find the server from your computer on the same network."

   ;;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significant"

   ;;chats
   :new                                   "New"
   :new-chat                              "New chat"
   :start-new-chat                        "Start new chat"
   :start-group-chat                      "Start group chat"
   :invite-friends                        "Invite friends"
   :chats                                 "Chats"
   :delete-chat                           "Delete chat"
   :new-group-chat                        "New group chat"
   :new-public-group-chat                 "Join public chat"
   :edit-chats                            "Edit chats"
   :search-chats                          "Search chats"
   :empty-topic                           "Empty topic"
   :topic-format                          "Wrong format [a-z0-9\\-]+"
   :public-group-topic                    "Topic"

   ;;discover
   :discover                              "Discover"
   :none                                  "None"
   :search-tags                           "Type your search tags here"
   :popular-tags                          "Popular #hashtags"
   :recent                                "Recent statuses"
   :no-statuses-found                     "No statuses found"
   :chat                                  "Chat"
   :all                                   "All"
   :public-chats                          "Public chats"
   :soon                                  "Soon"
   :public-chat-user-count                "{{count}} people"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp profile"
   :no-statuses-discovered                "No statuses discovered"
   :no-statuses-discovered-body           "When somebody posts\na status you will see it here."
   :no-hashtags-discovered-title          "No #hashtags discovered"
   :no-hashtags-discovered-body           "When a #hashtag becomes\npopular you will see it here."

   ;;settings
   :settings                              "Settings"

   ;;contacts
   :contacts                              "Contacts"
   :new-contact                           "New contact"
   :delete-contact                        "Delete contact"
   :delete-contact-confirmation           "This contact will be removed from your contacts"
   :remove-from-group                     "Remove from group"
   :edit-contacts                         "Edit contacts"
   :search-contacts                       "Search contacts"
   :contacts-group-new-chat               "Start new chat"
   :choose-from-contacts                  "Choose from contacts"
   :no-contacts                           "No contacts yet"
   :show-qr                               "Show QR code"
   :qr-code-public-key-hint               "Share this code to \nstart chatting"
   :enter-address                         "Enter address"
   :enter-contact-code                    "Enter contact code"
   :more                                  "more"

   ;;group-settings
   :remove                                "Remove"
   :save                                  "Save"
   :delete                                "Delete"
   :clear-history                         "Clear history"
   :mute-notifications                    "Mute notifications"
   :leave-chat                            "Leave chat"
   :chat-settings                         "Chat settings"
   :edit                                  "Edit"
   :add-members                           "Add members"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "New group"
   :reorder-groups                        "Reorder groups"
   :edit-group                            "Edit group"
   :delete-group                          "Delete group"
   :delete-group-confirmation             "This group will be removed from your groups. This will not affect your contacts"
   :delete-group-prompt                   "This will not affect your contacts"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}

   ;;protocol
   :received-invitation                   "received chat invitation"
   :removed-from-chat                     "removed you from group chat"
   :left                                  "left"
   :invited                               "invited"
   :removed                               "removed"
   :You                                   "You"

   ;;new-contact
   :add-new-contact                       "Add new contact"
   :scan-qr                               "Scan QR code"
   :name                                  "Name"
   :address-explication                   "Your public key is used to generate your address on Ethereum and is a series of numbers and letters. You can find it easily in your profile"
   :use-valid-contact-code                "Please enter a valid contact code"
   :enter-valid-public-key                "Please enter a valid public key or scan a QR code"
   :contact-already-added                 "The contact has already been added"
   :can-not-add-yourself                  "You can't add yourself"
   :unknown-address                       "Unknown address"

   ;;login
   :connect                               "Connect"
   :address                               "Address"
   :password                              "Password"
   :sign-in-to-status                     "Sign in to Status"
   :sign-in                               "Sign in"
   :wrong-password                        "Wrong password"
   :enter-password                        "Enter password"

   ;;recover
   :passphrase                            "Passphrase"
   :recover                               "Recover"
   :twelve-words-in-correct-order         "12 words in correct order"

   ;;accounts
   :recover-access                        "Recover access"
   :create-new-account                    "Create new account"

   ;;wallet-qr-code
   :done                                  "Done"

   ;;validation
   :invalid-phone                         "Invalid phone number"
   :amount                                "Amount"

   ;;transactions
   :confirm                               "Confirm"
   :transaction                           "Transaction"
   :unsigned-transaction-expired          "Unsigned transaction expired"
   :status                                "Status"
   :recipient                             "Recipient"
   :specify-recipient                     "Specify recipient..."
   :recipient-code                        "Enter recipient address"
   :recent-recipients                     "Recent recipients"
   :to                                    "To"
   :from                                  "From"
   :data                                  "Data"
   :got-it                                "Got it"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "Gas limit"
   :gas-price                             "Gas price"
   :gas-used                              "Gas used"
   :cost-fee                              "Cost/Fee"
   :nonce                                 "Nonce"
   :confirmations                         "Confirmations"
   :confirmations-helper-text             "Please wait for at least 12 confirmations to make sure your transaction is processed securely"
   :copy-transaction-hash                 "Copy transaction hash"
   :open-on-etherscan                     "Open on Etherscan.io"
   :incoming                              "Incoming"
   :outgoing                              "Outgoing"
   :pending                               "Pending"
   :postponed                             "Postponed"

   ;;webview
   :web-view-error                        "oops, error"

   ;;testfairy warning
   :testfairy-title                       "Warning!"
   :testfairy-message                     "You are using an app installed from a nightly build. For testing purposes this build includes session recording if wifi connection is used, so all your interactions with this app is saved (as video and logs) and might be used by our development team to investigate possible issues. Saved video/logs do not include your passwords. Recording is done only if the app is installed from a nightly build. Nothing is recorded if the app is installed from PlayStore or TestFlight."

   ;; wallet
   :wallet                                "Wallet"
   :wallets                               "Wallets"
   :your-wallets                          "Your wallets"
   :main-wallet                           "Main Wallet"
   :wallet-error                          "Error loading data"
   :wallet-send                           "Send"
   :wallet-send-token                     "Send {{symbol}}"
   :wallet-request                        "Request"
   :wallet-exchange                       "Exchange"
   :wallet-asset                          "Asset"
   :wallet-assets                         "Assets"
   :wallet-add-asset                      "Add asset"
   :wallet-total-value                    "Total value"
   :wallet-settings                       "Wallet settings"
   :wallet-manage-assets                  "Manage Assets"
   :signing-phrase-description            "Sign the transaction by entering your password. Make sure that the words above match your secret signing phrase"
   :wallet-insufficient-funds             "Insufficient funds"
   :receive                               "Receive"
   :request-qr-legend                     "Share this code to receive assets"
   :send-request                          "Send request"
   :send-transaction-request              "Send a transaction request"
   :share                                 "Share"
   :eth                                   "ETH"
   :gwei                                  "Gwei"
   :currency                              "Currency"
   :usd-currency                          "USD"
   :amount-placeholder                    "Specify amount..."
   :transactions                          "Transactions"
   :transaction-details                   "Transaction details"
   :transaction-failed                    "Transaction failed"
   :transactions-sign                     "Sign"
   :transactions-sign-all                 "Sign all"
   :transactions-sign-transaction         "Sign transaction"
   :transactions-sign-later               "Sign later"
   :transactions-delete                   "Delete transaction"
   :transactions-delete-content           "Transaction will be removed from 'Unsigned' list"
   :transactions-history                  "History"
   :transactions-unsigned                 "Unsigned"
   :transactions-history-empty            "No transactions in your history yet"
   :transactions-unsigned-empty           "You don't have any unsigned transactions"
   :transactions-filter-title             "Filter history"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Type"
   :transactions-filter-select-all        "Select all"
   :view-transaction-details              "View transaction details"
   :transaction-description               "Please wait for at least 12 confirmations to make sure your transaction is processed securely"
   :transaction-sent                      "Transaction sent"
   :transaction-moved-text                "The transaction will remain in the 'Unsigned' list for the next 5 mins"
   :transaction-moved-title               "Transaction moved"
   :sign-later-title                      "Sign transaction later?"
   :sign-later-text                       "Check the transaction history to sign this transaction"
   :not-applicable                        "Not applicable for unsigned transactions"
   :send-transaction                      "Send transaction"
   :new-request                           "New request"
   :request-transaction                   "Request transaction"
   :receive-transaction                   "Receive transaction"
   :new-transaction                       "New Transaction"
   :transaction-history                   "Transaction History"

   ;; Wallet Send
   :wallet-choose-recipient               "Choose Recipient"
   :wallet-choose-from-contacts           "Choose From Contacts"
   :wallet-address-from-clipboard         "Use Address From Clipboard"
   :wallet-invalid-address                "Invalid address: \n {{data}}"
   :wallet-invalid-chain-id               "Network does not match: \n {{data}} but current chain is {{chain}}"
   :wallet-browse-photos                  "Browse Photos"
   :wallet-advanced                       "Advanced"
   :wallet-transaction-fee                "Transaction Fee"
   :wallet-transaction-fee-details        "Gas limit is the amount of gas to send with your transaction. Increasing this number will not get your transaction processed faster"
   :wallet-transaction-total-fee          "Total Fee"
   :validation-amount-invalid-number      "Amount is not a valid number"
   :validation-amount-is-too-precise      "Amount is too precise. The smallest unit you can send is 1 Wei (1x10^-18 ETH)"
   :scan-qr-code                          "Scan a QR code with a wallet address"
   :reset-default                         "Reset to default"



   ;; network settings
   :new-network                           "New network"
   :add-network                           "Add network"
   :add-new-network                       "Add new network"
   :add-wnode                             "Add mailserver"
   :existing-networks                     "Existing networks"
   ;; TODO(dmitryn): come up with better description/naming. Suggested namings: Mailbox and Master Node
   :existing-wnodes                       "Existing mailservers"
   :add-json-file                         "Add a JSON file"
   :paste-json-as-text                    "Paste JSON as text"
   :paste-json                            "Paste JSON"
   :specify-rpc-url                       "Specify a RPC URL"
   :edit-network-config                   "Edit network config"
   :connected                             "Connected"
   :process-json                          "Process JSON"
   :error-processing-json                 "Error processing JSON"
   :rpc-url                               "RPC URL"
   :network                               "Network"
   :remove-network                        "Remove network"
   :network-settings                      "Network settings"
   :offline-messaging-settings            "Offline messages settings"
   :edit-network-warning                  "Be careful, editing the network data may disable this network for you"
   :connecting-requires-login             "Connecting to another network requires login"
   :close-app-title                       "Warning!"
   :close-app-content                     "The app will stop and close. When you reopen it, the selected network will be used"
   :close-app-button                      "Confirm"
   :connect-wnode-content                 "Connect to {{name}}?"

   ;; browser
   :browser                               "Browser"
   :enter-dapp-url                        "Enter a ÐApp URL"
   :dapp                                  "ÐApp"
   :selected-dapps                        "Selected ÐApps"})

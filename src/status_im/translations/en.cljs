(ns status-im.translations.en)

(def translations
  {
   ;common
   :members-title                         "Members"
   :not-implemented                       "!not implemented"
   :chat-name                             "Chat name"
   :notifications-title                   "Notifications and sounds"
   :offline                               "Offline"
   :search-for                            "Search for..."
   :cancel                                "Cancel"

   ;drawer
   :invite-friends                        "Invite friends"
   :faq                                   "FAQ"
   :switch-users                          "Switch users"
   :feedback                              "Got Feedback?\nShake your phone!"

   ;chat
   :is-typing                             "is typing"
   :and-you                               "and you"
   :search-chat                           "Search chat"
   :members                               {:one   "1 member"
                                           :other "{{count}} members"
                                           :zero  "no members"}
   :members-active                        {:one   "1 member, 1 active"
                                           :other "{{count}} members, {{count}} active"
                                           :zero  "no members"}
   :active-online                         "Online"
   :active-unknown                        "Unknown"
   :available                             "Available"
   :no-messages                           "No messages"
   :suggestions-requests                  "Requests"
   :suggestions-commands                  "Commands"
   :faucet-success                        "Faucet request has been received"
   :faucet-error                          "Faucet request error"

   ;sync
   :sync-in-progress                      "Syncing..."
   :sync-synced                           "In sync"

   ;messages
   :status-sending                        "Sending"
   :status-pending                        "Pending"
   :status-sent                           "Sent"
   :status-seen-by-everyone               "Seen by everyone"
   :status-seen                           "Seen"
   :status-delivered                      "Delivered"
   :status-failed                         "Failed"

   ;datetime
   :datetime-second                       {:one   "second"
                                           :other "seconds"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "hour"
                                           :other "hours"}
   :datetime-day                          {:one   "day"
                                           :other "days"}
   :datetime-multiple                     "s"
   :datetime-ago                          "ago"
   :datetime-yesterday                    "yesterday"
   :datetime-today                        "today"

   ;profile
   :profile                               "Profile"
   :report-user                           "REPORT USER"
   :message                               "Message"
   :username                              "Username"
   :not-specified                         "Not specified"
   :public-key                            "Public Key"
   :phone-number                          "Phone number"
   :email                                 "Email"
   :profile-no-status                     "No status"
   :add-to-contacts                       "Add to contacts"
   :error-incorrect-name                  "Please select another name"
   :error-incorrect-email                 "Incorrect e-mail"

   ;;make_photo
   :image-source-title                    "Profile image"
   :image-source-make-photo               "Capture"
   :image-source-gallery                  "Select from gallery"
   :image-source-cancel                   "Cancel"

   ;;sharing
   :sharing-copy-to-clipboard             "Copy to clipboard"
   :sharing-share                         "Share..."
   :sharing-cancel                        "Cancel"

   ;sign-up
   :contacts-syncronized                  "Your contacts have been synchronized"
   :confirmation-code                     (str "Thanks! We've sent you a text message with a confirmation "
                                               "code. Please provide that code to confirm your phone number")
   :incorrect-code                        (str "Sorry the code was incorrect, please enter again")
   :generate-passphrase                   (str "I'll generate a passphrase for you so you can restore your "
                                               "access or log in from another device")
   :phew-here-is-your-passphrase          "*Phew* that was hard, here is your passphrase, *write this down and keep this safe!* You will need it to recover your account."
   :here-is-your-passphrase               "Here is your passphrase, *write this down and keep this safe!* You will need it to recover your account."
   :written-down                          "Make sure you had securely written it down"
   :phone-number-required                 "Tap here to enter your phone number & I'll find your friends"
   :shake-your-phone                      "Found a bug or have a suggestion? Just ~shake~ your phone!"
   :intro-status                          "Chat with me to setup your account and change your settings!"
   :intro-message1                        "Welcome to Status\nTap this message to set your password & get started!"
   :account-generation-message            "Gimmie a sec, I gotta do some crazy math to generate your account!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "New chat"
   :new-group-chat                        "New group chat"

   ;discover
   :discover                              "Discover"
   :none                                  "None"
   :search-tags                           "Type your search tags here"
   :popular-tags                          "Popular tags"
   :recent                                "Recent"
   :no-statuses-discovered                "No statuses discovered"
   :no-statuses-found                     "No statuses found"

   ;settings
   :settings                              "Settings"

   ;contacts
   :contacts                              "Contacts"
   :new-contact                           "New Contact"
   :remove-contact                        "Remove contact"
   :show-all                              "SHOW ALL"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "People"
   :contacts-group-new-chat               "Start new chat"
   :no-contacts                           "No contacts yet"
   :show-qr                               "Show QR"
   :enter-address                         "Enter address"

   ;group-settings
   :remove                                "Remove"
   :save                                  "Save"
   :change-color                          "Change color"
   :clear-history                         "Clear history"
   :delete-and-leave                      "Delete and leave"
   :chat-settings                         "Chat settings"
   :edit                                  "Edit"
   :add-members                           "Add Members"
   :blue                                  "Blue"
   :purple                                "Purple"
   :green                                 "Green"
   :red                                   "Red"

   ;commands
   :money-command-description             "Send money"
   :location-command-description          "Send location"
   :phone-command-description             "Send phone number"
   :phone-request-text                    "Phone number request"
   :confirmation-code-command-description "Send confirmation code"
   :confirmation-code-request-text        "Confirmation code request"
   :send-command-description              "Send location"
   :request-command-description           "Send request"
   :keypair-password-command-description  ""
   :help-command-description              "Help"
   :request                               "Request"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH to {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH from {{chat-name}}"
   :command-text-location                 "Location: {{address}}"
   :command-text-browse                   "Browsing webpage: {{webpage}}"
   :command-text-send                     "Transaction: {{amount}} ETH"
   :command-text-help                     "Help"
   :command-text-faucet                   "Faucet: {{url}}"

   ;new-group
   :group-chat-name                       "Chat name"
   :empty-group-chat-name                 "Please enter a name"
   :illegal-group-chat-name               "Please select another name"

   ;participants
   :add-participants                      "Add Participants"
   :remove-participants                   "Remove Participants"

   ;protocol
   :received-invitation                   "received chat invitation"
   :removed-from-chat                     "removed you from group chat"
   :left                                  "left"
   :invited                               "invited"
   :removed                               "removed"
   :You                                   "You"

   ;new-contact
   :add-new-contact                       "Add new contact"
   :import-qr                             "Import"
   :scan-qr                               "Scan QR"
   :swow-qr                               "Show QR"
   :name                                  "Name"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "Please enter a valid address or scan a QR code"
   :enter-valid-public-key                "Please enter a valid public key or scan a QR code"
   :contact-already-added                 "The contact has already been added"
   :can-not-add-yourself                  "You can't add yourself"
   :unknown-address                       "Unknown address"


   ;login
   :connect                               "Connect"
   :address                               "Address"
   :password                              "Password"
   :login                                 "Login"
   :wrong-password                        "Wrong password"

   ;recover
   :recover-from-passphrase               "Recover from passphrase"
   :recover-explain                       "Please enter the passphrase for your password to recover access"
   :passphrase                            "Passphrase"
   :recover                               "Recover"
   :enter-valid-passphrase                "Please enter a passphrase"
   :enter-valid-password                  "Please enter a password"

   ;accounts
   :recover-access                        "Recover access"
   :add-account                           "Add account"

   ;wallet-qr-code
   :done                                  "Done"
   :main-wallet                           "Main Wallet"

   ;validation
   :invalid-phone                         "Invalid phone number"
   :amount                                "Amount"
   :not-enough-eth                        (str "Not enough ETH on balance "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirm transaction"
                                           :other "Confirm {{count}} transactions"
                                           :zero  "No transactions"}
   :status                                "Status"
   :pending-confirmation                  "Pending confirmation"
   :recipient                             "Recipient"
   :one-more-item                         "One more item"
   :fee                                   "Fee"
   :value                                 "Value"

   ;:webview
   :web-view-error                        "oops, error"})

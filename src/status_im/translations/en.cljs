(ns status-im.translations.en)

(def translations
  {
   ;common
   :members-title                         "Members"
   :not-implemented                       "!not implemented"
   :chat-name                             "Chat name"
   :notifications-title                   "Notifications and sounds"

   ;drawer
   :invite-friends                        "Invite friends"
   :faq                                   "FAQ"
   :switch-users                          "Switch users"

   ;chat
   :is-typing                             "is typing"
   :and-you                               "and you"
   :search-chat                           "Search chat"
   :members                               {:one   "1 member, 1 active"
                                           :other "{{count}} members, {{count}} active"
                                           :zero  "no members"}
   :active-online                         "online"
   :active-unknown                        "unknown"

   ;messages
   :status-sending                        "Sending"
   :status-pending                        "Sending"
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

   ;profile
   :profile                               "Profile"
   :report-user                           "REPORT USER"
   :message                               "Message"
   :username                              "Username"
   :user-anonymous                        "Anonymous"
   :not-specified                         "Not specified"
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

   ;sign-up
   :contacts-syncronized                  "Your contacts have been synchronized"
   :confirmation-code                     (str "Thanks! We've sent you a text message with a confirmation "
                                               "code. Please provide that code to confirm your phone number")
   :incorrect-code                        (str "Sorry the code was incorrect, please enter again")
   :password-saved                        (str "OK great! Your password has been saved. Just to let you "
                                               "know you can always change it in the Console by the way "
                                               "it's me the Console nice to meet you!")
   :generate-passphrase                   (str "I'll generate a passphrase for you so you can restore your "
                                               "access or log in from another device")
   :here-is-your-passphrase               "Here's your passphrase:"
   :written-down                          "Make sure you had securely written it down"
   :phone-number-required                 (str "Your phone number is also required to use the app. Type the "
                                               "exclamation mark or hit the icon to open the command list "
                                               "and choose the !phone command")
   :intro-status                          (str "The brash businessman’s braggadocio "
                                               "and public exchange with candidates "
                                               "in the US presidential election")
   :intro-message1                        "Hello there! It's Status a Dapp browser in your phone."
   :intro-message2                        (str "Status uses  a highly secure key-pair authentication type "
                                               "to provide you a reliable way to access your account")
   :keypair-generated                     (str "A key pair has been generated and saved to your device. "
                                               "Create a password to secure your key")

   ;chats
   :chats                                 "Chats"
   :new-chat                              "New Chat"
   :new-group-chat                        "New Group Chat"

   ;discover
   :discovery                             "Discovery"
   :none                                  "None"
   :search-tags                           "Type your search tags here"
   :popular-tags                          "Popular tags"
   :recent                                "Recent"

   ;settings
   :settings                              "Settings"

   ;contacts
   :contacts                              "Contacts"
   :new-contact                           "New Contact"
   :show-all                              "SHOW ALL"
   :contacs-group-dapps                   "Dapps"
   :contacs-group-people                  "People"
   :no-contacts                           "No contacts yet"

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
   :name                                  "Name"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "Please enter a valid address or scan a QR code"
   :contact-already-added                 "The contact has already been added"
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

   })

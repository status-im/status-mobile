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
   :last-active                           "Active a minute ago"

   ;profile
   :profile                               "Profile"
   :report-user                           "REPORT USER"
   :message                               "Message"
   :username                              "Username"
   :phone-number                          "Phone number"
   :email                                 "Email"

   ;sign-up
   :contacts-syncronized                  "Your contacts have been synchronized"
   :confirmation-code                     (str "Thanks! We've sent you a text message with a confirmation "
                                               "code. Please provide that code to confirm your phone number")
   :password-saved                        (str "OK great! Your password has been saved. Just to let you "
                                               "know you can always change it in the Console by the way "
                                               "it's me the Console nice to meet you!")
   :generate-passphrase                   (str "I'll generate a passphrase for you so you can restore your "
                                               "access or log in from another device")
   :passphrase                            "Here's your passphrase:"
   :written-down                          "Make sure you had securely written it down"
   :phone-number-required                 (str "Your phone number is also required to use the app. Type the "
                                               "exclamation mark or hit the icon to open the command list "
                                               "and choose the !phone command")
   :intro-status                          (str "The brash businessman’s braggadocio "
                                               "and public exchange with candidates "
                                               "in the US presidential election")
   :intro-message1                        "Hello there! It's Status a Dapp browser in your phone."
   :intro-message2                        (str "Status1 uses  a highly secure key-pair authentication type "
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
   :no-name                               "Noname"
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
   :group-name                            "Group Name"

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

   ;login
   :recover-from-passphrase               "Recover from passphrase"
   :connect                               "Connect"
   :address                               "Address"
   :password                              "Password"
   :login                                 "Login"

   ;users
   :add-account                           "Add account"

   })

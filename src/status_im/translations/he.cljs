(ns status-im.translations.he)

(def translations
  {
   ;common
   :members-title                         "חבר"
   :not-implemented                       "!עדיין לא יושם"
   :chat-name                             "שם הצ'אט"
   :notifications-title                   "התראות וצלילים"
   :offline                               "לא מחובר"
   :search-for                            "...חפש"
   :cancel                                "בטל"
   :next                                  "הבא"
   :type-a-message                        "...הקלד הודעה"
   :type-a-command                        "...הקלד פעולה"
   :error                                 "שגיאה"

   :camera-access-error                   ".כדי להעניק את הרשאת המצלמה הנדרשת, בבקשה, תלכו להגדרות המערכת ותוודאו שהאופציה שנבחרה היא סטטוס > מצלמה"
   :photos-access-error                   ".כדי להעניק את הרשאת התמונות הנדרשת, בבקשה, תלכו להגדרות המערכת ותוודאו שהאופציה שנבחרה היא סטטוס > תמונות"

   ;drawer
   :invite-friends                        "הזמן חברים"
   :faq                                   "שאלות נפוצות"
   :switch-users                          "שנה משתמש"
   :feedback                              "Got feedback?\nShake your phone!"
   :view-all                              "View all"
   :current-network                       "Current network"

   ;chat
   :is-typing                             "מקליד"
   :and-you                               "גם אתה"
   :search-chat                           "חפש בצ'אט"
   :members                               {:one   "חבר אחד"
                                           :other "{{count}} חברים"
                                           :zero  "אין חברים"}
   :members-active                        {:one   "חבר אחד"
                                           :other "{{count}} חברים"
                                           :zero  "אין חברים"}
   :public-group-status                   "פומבי"
   :active-online                         "מחובר"
   :active-unknown                        "לא ידוע"
   :available                             "זמין"
   :no-messages                           "אין הודעות"
   :suggestions-requests                  "בקשות"
   :suggestions-commands                  "פקודות"
   :faucet-success                        "בקשת הברז התקבלה"
   :faucet-error                          "שגיאה בבקשת הברז"

   ;sync
   :sync-in-progress                      "...מסנכרן"
   :sync-synced                           "מסונכרן"

   ;messages
   :status-sending                        "שולח"
   :status-pending                        "ממתין"
   :status-sent                           "נשלחה"
   :status-seen-by-everyone               "פומבי"
   :status-seen                           "נראה"
   :status-delivered                      "התקבל"
   :status-failed                         "נכשל"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "שניה"
                                           :other "שניות"}
   :datetime-minute                       {:one   "דקה"
                                           :other "דקות"}
   :datetime-hour                         {:one   "שעה"
                                           :other "שעות"}
   :datetime-day                          {:one   "יום"
                                           :other "ימים"}
   :datetime-multiple                     "s"
   :datetime-ago                          "לפני"
   :datetime-yesterday                    "אתמול"
   :datetime-today                        "היום"

    ;profile
   :profile                               "פרופיל"
   :edit-profile                          "ערוך פרופיל"
   :report-user                           "דווח על המשתמש"
   :message                               "הודעה"
   :username                              "שם משתמש"
   :not-specified                         "לא מוגדר"
   :public-key                            "מפתח פומבי"
   :phone-number                          "מספר טלפון"
   :email                                 "אי-מייל"
   :update-status                         "...עדכן סטטוס"
   :add-a-status                          "...הוסף סטטוס"
   :status-prompt                         ".#hashtags צור סטטוס כדי ליידע אחרים בקשר לדברים אותם אתה מציע. אתה יכול גם להשתמש ב"

   :add-to-contacts                       "הוסף לאנשי קשר"
   :in-contacts                           "באנשי קשר"
   :remove-from-contacts                  "הסר מאנשי קשר"
   :start-conversation                    "התחל שיחה"
   :send-transaction                      "שלח עיסקה"
   :share-qr                              "QR שתף קוד"
   :error-incorrect-name                  "אנא בחר שם אחר"
   :error-incorrect-email                 "כתובת מייל שגויה"

   ;;make_photo
   :image-source-title                    "תמונת פרופיל"
   :image-source-make-photo               "צלם"
   :image-source-gallery                  "בחר מהגלריה"
   :image-source-cancel                   "בטל"

   ;;sharing
   :sharing-copy-to-clipboard             "העתק לשולחן העבודה"
   :sharing-share                         "...שתף"
   :sharing-cancel                        "בטל"

   :browsing-title                        "דפדף"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "פתח בדפדפן"
   :browsing-cancel                       "בטל"

   ;sign-up
   :contacts-syncronized                  "אנשי הקשר שלך סונכרנו"
   :confirmation-code                     (str "תודה! שלחנו לך הודעה עם אישור"
                                               "קוד. אנא ספק את הקוד כדי לאשר את מספר הטלפון שלך")
   :incorrect-code                        (str "סליחה הקוד שגוי, אנא נסה שנית")
   :generate-passphrase                   (str "אני אצור משפט קוד כדי שתוכל לשחזר את הגישה שלך או להתחבר ממכשיר אחר")                                               
   :phew-here-is-your-passphrase          "פוו זה היה קשה, הנה משפט הקוד שלך, *תרשום אותו ותשמור עליו!* אתה תצטרך אותו כדי לשחזר את המשתמש שלך"
   :here-is-your-passphrase               "Here is your passphrase, *write this down and keep this safe!* You will need it to recover your account."
   :written-down                          "Make sure you had securely written it down"
   :phone-number-required                 "Tap here to enter your phone number & I'll find your friends"
   :shake-your-phone                      "Find a bug or have a suggestion? Just ~shake~ your phone!"
   :intro-status                          "Chat with me to setup your account and change your settings!"
   :intro-message1                        "Welcome to Status\nTap this message to set your password & get started!"
   :account-generation-message            "Gimmie a sec, I gotta do some crazy math to generate your account!"
   :move-to-internal-failure-message      "We need to move some important files from external to internal storage. To do this, we need your permission. We won't be using external storage in future versions."
   :debug-enabled                         "Debug server has been launched! You can now execute *status-dev-cli scan* to find the server from your computer on the same network."

   ;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significant"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "New chat"
   :delete-chat                           "Delete chat"
   :new-group-chat                        "New group chat"
   :new-public-group-chat                 "Join public chat"
   :edit-chats                            "Edit chats"
   :search-chats                          "Search chats"
   :empty-topic                           "Empty topic"
   :topic-format                          "Wrong format [a-z0-9\\-]+"
   :public-group-topic                    "Topic"

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
   :new-contact                           "New contact"
   :delete-contact                        "Delete contact"
   :delete-contact-confirmation           "This contact will be removed from your contacts"
   :remove-from-group                     "Remove from group"
   :edit-contacts                         "Edit contacts"
   :search-contacts                       "Search contacts"
   :show-all                              "SHOW ALL"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "People"
   :contacts-group-new-chat               "Start new chat"
   :choose-from-contacts                  "Choose from contacts"
   :no-contacts                           "No contacts yet"
   :show-qr                               "Show QR"
   :enter-address                         "Enter address"
   :more                                  "more"

   ;group-settings
   :remove                                "Remove"
   :save                                  "Save"
   :delete                                "Delete"
   :change-color                          "Change color"
   :clear-history                         "Clear history"
   :mute-notifications                    "Mute notifications"
   :leave-chat                            "Leave chat"
   :delete-and-leave                      "Delete and leave"
   :chat-settings                         "Chat settings"
   :edit                                  "Edit"
   :add-members                           "Add members"
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

   ;new-group
   :group-chat-name                       "Chat name"
   :empty-group-chat-name                 "Please enter a name"
   :illegal-group-chat-name               "Please select another name"
   :new-group                             "New group"
   :reorder-groups                        "Reorder groups"
   :group-name                            "Group name"
   :edit-group                            "Edit group"
   :delete-group                          "Delete group"
   :delete-group-confirmation             "This group will be removed from your groups. This will not affect contacts"
   :delete-group-prompt                   "This will not affect contacts"
   :group-members                         "Group members"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}
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
   :sign-in-to-status                     "Sign in to Status"
   :sign-in                               "Sign in"
   :wrong-password                        "Wrong password"

   ;recover
   :recover-from-passphrase               "Recover from passphrase"
   :recover-explain                       "Please enter the passphrase for your password to recover access"
   :passphrase                            "Passphrase"
   :recover                               "Recover"
   :enter-valid-passphrase                "Please enter a passphrase"
   :enter-valid-password                  "Please enter a password"
   :twelve-words-in-correct-order         "12 words in correct order"

   ;accounts
   :recover-access                        "Recover access"
   :add-account                           "Add account"
   :create-new-account                    "Create new account"

   ;wallet-qr-code
   :done                                  "Done"
   :main-wallet                           "Main Wallet"

   ;validation
   :invalid-phone                         "Invalid phone number"
   :amount                                "Amount"
   :not-enough-eth                        (str "Not enough ETH on balance "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Confirm"
   :confirm-transactions                  {:one   "Confirm transaction"
                                           :other "Confirm {{count}} transactions"
                                           :zero  "No transactions"}
   :transactions-confirmed                {:one   "Transaction confirmed"
                                           :other "{{count}} transactions confirmed"
                                           :zero  "No transactions confirmed"}
   :transaction                           "Transaction"
   :unsigned-transactions                 "Unsigned transactions"
   :no-unsigned-transactions              "No unsigned transactions"
   :enter-password-transactions           {:one   "Confirm transaction by entering your password"
                                           :other "Confirm transactions by entering your password"}
   :status                                "Status"
   :pending-confirmation                  "Pending confirmation"
   :recipient                             "Recipient"
   :one-more-item                         "One more item"
   :fee                                   "Fee"
   :estimated-fee                         "Est. fee"
   :value                                 "Value"
   :to                                    "To"
   :from                                  "From"
   :data                                  "Data"
   :got-it                                "Got it"
   :contract-creation                     "Contract Creation"

   ;:webview
   :web-view-error                        "oops, error"})

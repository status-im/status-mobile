(ns status-im.translations.en)

(def translations
  {:chat            {:is-typing   "is typing"
                     :and-you     "and you"

                     :search-chat "Search chat"

                     :members     {:one   "1 member, 1 active"
                                   :other "{{count}} members, {{count}} active"}
                     :last-active "Active a minute ago"}
   :sign-up         {:contacts-syncronized  "Your contacts have been synchronized"
                     :confirmation-code     (str "Thanks! We've sent you a text message with a confirmation "
                                                 "code. Please provide that code to confirm your phone number")
                     :password-saved        (str "OK great! Your password has been saved. Just to let you "
                                                 "know you can always change it in the Console by the way "
                                                 "it's me the Console nice to meet you!")
                     :generate-passphrase   (str "I'll generate a passphrase for you so you can restore your "
                                                 "access or log in from another device")
                     :passphrase            "Here's your passphrase:"
                     :written-down          "Make sure you had securely written it down"
                     :phone-number-required (str "Your phone number is also required to use the app. Type the "
                                                 "exclamation mark or hit the icon to open the command list "
                                                 "and choose the !phone command")
                     :intro-status          (str "The brash businessman’s braggadocio "
                                                 "and public exchange with candidates "
                                                 "in the US presidential election")
                     :intro-message1        "Hello there! It's Status a Dapp browser in your phone."
                     :intro-message2        (str "Status1 uses  a highly secure key-pair authentication type "
                                                 "to provide you a reliable way to access your account")
                     :keypair-generated     (str "A key pair has been generated and saved to your device. "
                                                 "Create a password to secure your key")}
   :chats           {:title          "Chats"
                     :new-chat       "New Chat"
                     :new-group-chat "New Group Chat"}
   :profile         {:title        "Profile"
                     :message      "Message"
                     :username     "Username"
                     :phone-number "Phone number"
                     :email        "Email"
                     :report-user  "REPORT USER"}
   :settings        {:title "Settings"}
   :discovery       {:title        "Discovery"
                     :none         "None"
                     :search-tags  "Type your search tags here"
                     :popular-tags "Popular tags"
                     :recent       "Recent"}
   :contacts        {:title   "Contacts"
                     :no-name "Noname"}
   :invite-friends  {:title "Invite friends"}
   :faq             {:title "FAQ"}
   :drawer          {:switch-users "Switch users"}
   :group-settings  {:no-name          "Noname"
                     :remove           "Remove"
                     :save             "Save"
                     :change-color     "Change color"
                     :clear-history    "Clear history"
                     :delete-and-leave "Delete and leave"
                     :chat-settings    "Chat settings"
                     :edit             "Edit"
                     :add-members      "Add Members"}
   :new-group       {:title      "New Group Chat"
                     :group-name "Group Name"}
   :participants    {:add "Add Participants"
                     :remove "Remove Participants"}
   :commands        {:money             {:description "Send money"}
                     :location          {:description "Send location"}
                     :phone             {:description  "Send phone number"
                                         :request-text "Phone number request"}
                     :confirmation-code {:description  "Send confirmation code"
                                         :request-text "Confirmation code request"}
                     :send              {:description "Send location"}
                     :request           {:description "Send request"}
                     :keypair-password  {:text:description ""}
                     :help              {:description "Help"}}
   :protocol        {:received-invitation "received chat invitation"
                     :removed-from-chat   "removed you from group chat"}
   :colors          {:blue   "Blue"
                     :purple "Purple"
                     :green  "Green"
                     :red    "Red"}
   :notifications   {:title "Notifications and sounds"}
   :not-implemented "!not implemented"
   :chat-name       "Chat name"
   :members-title   "Members"
   :left            "left"
   :invited         "invited"
   :removed         "removed"
   :You             "You"})
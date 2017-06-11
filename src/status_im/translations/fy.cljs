(ns status-im.translations.fy)

(def translations
  {
   ;common
   :members-title                         "Leden"
   :not-implemented                       "!net ymplemintearre"
   :chat-name                             "Chatnamme"
   :notifications-title                   "Notifikaasjes en gelûden "
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Freonen útnoegje"
   :faq                                   "FAQ"
   :switch-users                          "Skeakel tusken brûkers "

   ;chat
   :is-typing                             "typt"
   :and-you                               "en do"
   :search-chat                           "Sykje yn de chat"
   :members                               {:one   "1 lid"
                                           :other "{{count}} leden"
                                           :zero  "gjin leden"}
   :members-active                        {:one   "1 lid, 1 aktive"
                                           :other "{{count}} leden, {{count}} aktive"
                                           :zero  "gjin leden"}
   :active-online                         "Online"
   :active-unknown                        "Ûnbekend"
   :available                             "Beskikber"
   :no-messages                           "Gjin berjochten"
   :suggestions-requests                  "Oanfreegje"
   :suggestions-commands                  "Kommando’s"

   ;sync
   :sync-in-progress                      "Wurdt syngronisearre ..."
   :sync-synced                           "Syngronisearre"

   ;messages
   :status-sending                        "Wurdt ferstjoerd "
   :status-pending                        "Yn behanneling"
   :status-sent                           "Ferstjoerd"
   :status-seen-by-everyone               "Troch eltsenien sjoen"
   :status-seen                           "Sjoen"
   :status-delivered                      "Besoarge"
   :status-failed                         "Mislearre"

   ;datetime
   :datetime-second                       {:one   "sekonde "
                                           :other " sekonden"}
   :datetime-minute                       {:one   "minút"
                                           :other "minuten"}
   :datetime-hour                         {:one   "oere"
                                           :other "oeren"}
   :datetime-day                          {:one   "dei"
                                           :other "dagen"}
   :datetime-multiple                     "s"
   :datetime-ago                          "lyn"
   :datetime-yesterday                    "juster"
   :datetime-today                        "hjoed"

   ;profile
   :profile                               "Profyl"
   :report-user                           "Meld brûker"
   :message                               "Berjocht"
   :username                              "Brûkersnamme"
   :not-specified                         "Net opjûn"
   :public-key                            "Iepenbiere kaai"
   :phone-number                          "Telefoannûmer"
   :email                                 "E-mailadres"
   :profile-no-status                     "Gjin status"
   :add-to-contacts                       "Oan kontaktpersoanen tafoegje"
   :error-incorrect-name                  "Kies in oare namme"
   :error-incorrect-email                 "ferkeard e-mailadres"

   ;;make_photo
   :image-source-title                    "Profylfoto"
   :image-source-make-photo               "Foto nimme"
   :image-source-gallery                  "Kies út galerij"
   :image-source-cancel                   "Annulearje"

   ;sign-up
   :contacts-syncronized                  "Dyn kontaktpersoanen binne syngroniseard"
   :confirmation-code                     (str "Tiige tank! Wy ha dy in sms stjoerd mei in befêstigingskoade"
                                               ". Jou dyn koade op om dyn telefoannûmer te befêstigje")
   :incorrect-code                        (str "Sorry, de koade wie ferkeard, fier hem opnei yn")
   :generate-passphrase                   (str "Ik sil in wachtsin meitsje, sadatsto dyn"
                                               "tagong kist kreie of fanôf in oar apparaat kist ynlogge")
   :phew-here-is-your-passphrase          "*Poah* dat wie dreech, hjir is dyn wachtsin, *skriuw dizze op en bewarje him goed!* Do silst him nedich ha om dyn account te herstelle."
   :here-is-your-passphrase               "Dat wie dreech, hjir is dyn wachtsin, *skriuw dizze op en bewarje him goed!* Do silst him nedich ha om dyn account te herstelle."
   :written-down                          "Soarch derfoar datsto him feilich hast opskreaun"
   :phone-number-required                 "Tik hjir om dyn telefoannûmer yn te fieren, dan sykje ik dyn freonen"
   :intro-status                          "Chat mei my om dyn account yn te stellen en dyn ynstellingen te wizigjen!"
   :intro-message1                        "Wolkom by Status\nTik op dit berjocht om dyn wachtwurd yn te stellen en oan de slach te gean!"
   :account-generation-message            "Jou my in momintsje, ik moat wat yngewikkelde berekkeningen dwaan om dyn account oan te meitsjen !"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nije chat"
   :new-group-chat                        "Nije groepchat"

   ;discover
   :discover                             "Ûntdekking"
   :none                                  "Gjin"
   :search-tags                           "Typ hjir dyn syktags"
   :popular-tags                          "Populêre tags"
   :recent                                "Koartlyn"
   :no-statuses-discovered                "Gjin statussen ûntdutsen"

   ;settings
   :settings                              "Ynstellingen"

   ;contacts
   :contacts                              "Kontaktpersoanen"
   :new-contact                           "Nije kontaktpersoanen"
   :show-all                              "Toan alles"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Minsken"
   :contacts-group-new-chat               "Start neie chat"
   :no-contacts                           "Noch gjin kontaktpersoanen"
   :show-qr                               "Toan QR"

   ;group-settings
   :remove                                "Fuortsmite"
   :save                                  "Opslaan"
   :change-color                          "Wizigje kleur"
   :clear-history                         "Wis skiednis"
   :delete-and-leave                      "Fuortsmite en ôfslute"
   :chat-settings                         "Chatynstellingen"
   :edit                                  "Bewurkje"
   :add-members                           "Foegje leden ta"
   :blue                                  "Blau"
   :purple                                "Pears"
   :green                                 "Grien"
   :red                                   "Read"

   ;commands
   :money-command-description             "Stjoer jild"
   :location-command-description          "Stjoer lokaasje"
   :phone-command-description             "Stjoer telefoannûmer"
   :phone-request-text                    "Telefoannûmer oanfraach"
   :confirmation-code-command-description "Telefoannûmer oanfraach"
   :confirmation-code-request-text        "Befêstigingskoade oanfraach"
   :send-command-description              "Stjoer lokaasje"
   :request-command-description           "Stjoer oanfraach"
   :help-command-description              "Help"
   :request                               "Oanfraach"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH nei {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH fan {{chat-name}}"

   ;new-group
   :group-chat-name                       "Chatnamme"
   :empty-group-chat-name                 "Fear in namme yn"
   :illegal-group-chat-name               "Kies in oare namme"

   ;participants
   :add-participants                      "Foegje dielnimmers ta"
   :remove-participants                   "Ferwiderje dielnimmers"

   ;protocol
   :received-invitation                   "Chatútnoeging ûntfange"
   :removed-from-chat                     "ferwidere dy út de chat"
   :left                                  "gie fuort"
   :invited                               "útnoege"
   :removed                               "ferwidere"
   :You                                   "Do"

   ;new-contact
   :add-new-contact                       "Foegje nij kontaktpersoan ta "
   :import-qr                             "QR ymportearje"
   :scan-qr                               "QR scanne"
   :name                                  "Namme"
   :whisper-identity                      "Flúster identiteit"
   :enter-valid-address                   "Fear in jildich adres yn of scan in QR-koade"
   :contact-already-added                 "De kontaktpersoan is ol tafoege"
   :can-not-add-yourself                  "Do kinst dysels net tafoegje"
   :unknown-address                       "Ûnbekend adres"


   ;login
   :connect                               "Ferbine"
   :address                               "Adres"
   :password                              "Wachtwurd"
   :login                                 "Ynlogge"
   :wrong-password                        "Ferkeard wachtwurd"

   ;recover
   :recover-from-passphrase               "Herstelle mei wachtsin"
   :recover-explain                       "Fear dyn wachtsin yn fan dyn account om tagong te herstelle"
   :passphrase                            "Wachtsin"
   :recover                               "Herstelle"
   :enter-valid-passphrase                "Fear in wachtsin yn"
   :enter-valid-password                  "Fear in wachtwurd yn"

   ;accounts
   :recover-access                        "Tagong herstelle"
   :add-account                           "Foegje account ta"

   ;wallet-qr-code
   :done                                  "Klear"
   :main-wallet                           "Haadbeurs"

   ;validation
   :invalid-phone                         "Ûnjildich telefoannûmer"
   :amount                                "Bedrach"
   :not-enough-eth                        (str "Net genôch ETH op saldo"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Befestigje transaksje"
                                           :other "Befestigje {{count}} transaksjes"
                                           :zero  "Gjin transaksjes"}
   :status                                "Status"
   :pending-confirmation                  "Yn ôfwachting fan bevestiging"
   :recipient                             "Ûntfanger"
   :one-more-item                         "Noch in item"
   :fee                                   "Kosten"
   :value                                 "Wearde"

   ;:webview
   :web-view-error                        "oeps, flater"})

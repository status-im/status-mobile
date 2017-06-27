(ns status-im.translations.fy)

(def translations
  {
   ;common
   :members-title                         "Leden"
   :not-implemented                       "!net ymplemintearre"
   :chat-name                             "Chatnamme"
   :notifications-title                   "Notifikaasjes en gelûden "
   :offline                               "Offline"
   :search-for                            "Sykje om..."
   :cancel                                "Ôfbrekke"
   :next                                  "Folgjende"
   :type-a-message                        "Typ in berjocht..."
   :type-a-command                        "Begjin mei in kommando te typen..."
   :error                                 "Flater"

   :camera-access-error                   "Om de kamera tastimming te jaan, gea nei dyn systeem ynstellings en wêz wis dat Status > Camera selektearre is."
   :photos-access-error                   "Om ús tastimming te jaan oan dyn foto's, gea nei dyn systeem ynstellings en wêz wis dat Status > Foto's selektearre is."
    
   ;drawer
   :invite-friends                        "Freonen útnoegje"
   :faq                                   "FAQ"
   :switch-users                          "Skeakel tusken brûkers "
   :feedback                              "Hast do feedback?\nSkodzje dyn telefoan!"
   :view-all                              "Alles besjen"
   :current-network                       "Aktuele netwurk"

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
   :public-group-status                   "Iepenbier"
   :active-online                         "Online"
   :active-unknown                        "Ûnbekend"
   :available                             "Beskikber"
   :no-messages                           "Gjin berjochten"
   :suggestions-requests                  "Oanfreegje"
   :suggestions-commands                  "Kommando’s"
   :faucet-success                        "Faucet fersyk is ûntfongen"
   :faucet-error                          "Faucet fersyk flater"

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
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
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
   :edit-profile                          "Profyl oanpasse"
   :report-user                           "MELD BRÛKER"
   :message                               "Berjocht"
   :username                              "Brûkersnamme"
   :not-specified                         "Net opjûn"
   :public-key                            "Iepenbiere kaai"
   :phone-number                          "Telefoannûmer"
   :email                                 "E-mailadres"
   :update-status                         "Fernij dyn status..."
   :add-a-status                          "Foegje in status ta..."
   :status-prompt                         "Foeg in status ta om oare minsken te litte witte wast do oanbiedest, do kinst ek #hashtags gebroeke."
   :add-to-contacts                       "Oan kontaktpersoanen tafoegje"
   :in-contacts                           "Yn kontakten"
   :remove-from-contacts                  "Fuortsmite út kontakten"
   :start-conversation                    "Start konversaasje"
   :send-transaction                      "Stjoer transaksje"
   :share-qr                              "Deel QR"
   :error-incorrect-name                  "Kies in oare namme"
   :error-incorrect-email                 "ferkeard e-mailadres"

   ;;make_photo
   :image-source-title                    "Profylfoto"
   :image-source-make-photo               "Foto nimme"
   :image-source-gallery                  "Kies út galerij"
   :image-source-cancel                   "Annulearje"

   ;;sharing
   :sharing-copy-to-clipboard             "Nei it klamboerd ta kopiearje"
   :sharing-share                         "Diele..."
   :sharing-cancel                        "Ôfbrekke"

   :browsing-title                        "Blêdzje"
   :browsing-browse                       "@blêdzje"
   :browsing-open-in-web-browser          "Iepenje yn web blêder"
   :browsing-cancel                       "Ôfbrekke"
    
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
   :shake-your-phone                      "Hast in bug fûn of hast in suggestje? ~Skodzje~ dyn telefoan!"
   :intro-status                          "Chat mei my om dyn account yn te stellen en dyn ynstellingen te wizigjen!"
   :intro-message1                        "Wolkom by Status\nTik op dit berjocht om dyn wachtwurd yn te stellen en oan de slach te gean!"
   :account-generation-message            "Jou my in momintsje, ik moat wat yngewikkelde berekkeningen dwaan om dyn account oan te meitsjen !"
   :move-to-internal-failure-message      "We need to move some important files from external to internal storage. To do this, we need your permission. We won't be using external storage in future versions."
   :debug-enabled                         "Debug server has been launched! You can now execute *status-dev-cli scan* to find the server from your computer on the same network."
    
   ;phone types
   :phone-e164                            "Ynternasjonaal 1"
   :phone-international                   "Ynternasjonaal 2"
   :phone-national                        "Lanlik"
   :phone-significant                     "Significant"
    
   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nije chat"
   :delete-chat                           "Chat fuortsmite"
   :new-group-chat                        "Nije groepchat"
   :new-public-group-chat                 "Doch mei yn iepenbare chat"
   :edit-chats                            "Bewurkje chats"
   :search-chats                          "Sykje chats"
   :empty-topic                           "Leech ûnderwerp"
   :topic-format                          "Ferkearde yndieling [a-z0-9\\-]+"
   :public-group-topic                    "Ûnderwerp"

   ;discover
   :discover                              "Ûntdekking"
   :none                                  "Gjin"
   :search-tags                           "Typ hjir dyn syktags"
   :popular-tags                          "Populêre tags"
   :recent                                "Koartlyn"
   :no-statuses-discovered                "Gjin statussen ûntdutsen"
   :no-statuses-found                     "Gjin statussen fûn"

   ;settings
   :settings                              "Ynstellingen"

   ;contacts
   :contacts                              "Kontaktpersoanen"
   :new-contact                           "Nije kontaktpersoanen"
   :delete-contact                        "Wiskje kontakt"
   :delete-contact-confirmation           "Dit kontakt sil fuortsmiten wurde fan jo kontakten"
   :remove-from-group                     "Fuortsmite út groep"
   :edit-contacts                         "Bewurkje kontakten"
   :search-contacts                       "Sykje kontakten"
   :show-all                              "Toan alles"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Minsken"
   :contacts-group-new-chat               "Start neie chat"
   :choose-from-contacts                  "Kies út de kontakten"
   :no-contacts                           "Noch gjin kontaktpersoanen"
   :show-qr                               "Toan QR"
   :enter-address                         "Fier adres yn"
   :more                                  "mear"

   ;group-settings
   :remove                                "Fuortsmite"
   :save                                  "Opslaan"
   :delete                                "Wiskje"
   :change-color                          "Wizigje kleur"
   :clear-history                         "Wis skiednis"
   :mute-notifications                    "Mute notifikaasjes"
   :leave-chat                            "Ferlit chat"
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
   :keypair-password-command-description  ""
   :help-command-description              "Help"
   :request                               "Oanfraach"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH nei {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH fan {{chat-name}}"

   ;new-group
   :group-chat-name                       "Chatnamme"
   :empty-group-chat-name                 "Fear in namme yn"
   :illegal-group-chat-name               "Kies in oare namme"
   :new-group                             "Nije groep"
   :reorder-groups                        "Reorder groepen"
   :group-name                            "Groepsnamme"
   :edit-group                            "Bewurkje group"
   :delete-group                          "Wiskje groep"
   :delete-group-confirmation             "Dizze groep sille fuortsmiten wurde fan dyn groepen. Dit sil gjin ynfloed ha mei jo kontakten"
   :delete-group-prompt                   "Dit sil gjin ynfloed ha mei jo kontakten"
   :group-members                         "Groep leden"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakten"}
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
   :swow-qr                               "Lit QR sjin"
   :name                                  "Namme"
   :whisper-identity                      "Flúster identiteit"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "Fear in jildich adres yn of scan in QR-koade"
   :enter-valid-public-key                "Fear in jildich adres public kaai yn of scan in QR koade"
   :contact-already-added                 "De kontaktpersoan is ol tafoege"
   :can-not-add-yourself                  "Do kinst dysels net tafoegje"
   :unknown-address                       "Ûnbekend adres"


   ;login
   :connect                               "Ferbine"
   :address                               "Adres"
   :password                              "Wachtwurd"
   :login                                 "Ynlogge"
   :sign-in-to-status                     "Oanmelde by Status"
   :sign-in                               "Ynlogge"
   :wrong-password                        "Ferkeard wachtwurd"

   ;recover
   :recover-from-passphrase               "Herstelle mei wachtsin"
   :recover-explain                       "Fear dyn wachtsin yn fan dyn account om tagong te herstelle"
   :passphrase                            "Wachtsin"
   :recover                               "Herstelle"
   :enter-valid-passphrase                "Fear in wachtsin yn"
   :enter-valid-password                  "Fear in wachtwurd yn"
   :twelve-words-in-correct-order         "12 wurden yn de goeie folchoarder"

   ;accounts
   :recover-access                        "Tagong herstelle"
   :add-account                           "Foegje account ta"
   :create-new-account                    "Nij akkount oanmeitsje"

   ;wallet-qr-code
   :done                                  "Klear"
   :main-wallet                           "Haadbeurs"

   ;validation
   :invalid-phone                         "Ûnjildich telefoannûmer"
   :amount                                "Bedrach"
   :not-enough-eth                        (str "Net genôch ETH op saldo"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Befestigje"
   :confirm-transactions                  {:one   "Befestigje transaksje"
                                           :other "Befestigje {{count}} transaksjes"
                                           :zero  "Gjin transaksjes"}
   :transactions-confirmed                {:one   "Transaksje befêstige"
                                           :other "{{count}} Transaksjes befêstige"
                                           :zero "Gjin transaksjes befêstige"}
   :transaction                           "Transaksje"
   :unsigned-transactions                 "Net-ûndertekene transaksjes"
   :no-unsigned-transactions              "Gjin unsigned transaksjes"
   :enter-password-transactions           {:one   "Befêstigje transaksje mei jo wachtwurd"
                                           :other "Befêstigje transaksjes mei jo wachtwurd"}
   :status                                "Status"
   :pending-confirmation                  "Yn ôfwachting fan bevestiging"
   :recipient                             "Ûntfanger"
   :one-more-item                         "Noch in item"
   :fee                                   "Kosten"
   :estimated-fee                         "Est. kosten"
   :value                                 "Wearde"
   :to                                    "Ta"
   :from                                  "Fan"
   :data                                  "Data"
   :got-it                                "Befetsje ik"
   :contract-creation                     "Kontrakt kreaasje"

   ;:webview
   :web-view-error                        "oeps, flater"})

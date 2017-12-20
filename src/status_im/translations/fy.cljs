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
   :switch-users                          "Skeakel tusken brûkers "
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
   :datetime-ago                          "lyn"
   :datetime-yesterday                    "juster"
   :datetime-today                        "hjoed"

   ;profile
   :profile                               "Profyl"
   :edit-profile                          "Profyl oanpasse"
   :message                               "Berjocht"
   :not-specified                         "Net opjûn"
   :public-key                            "Iepenbiere kaai"
   :phone-number                          "Telefoannûmer"
   :update-status                         "Fernij dyn status..."
   :add-a-status                          "Foegje in status ta..."
   :status-prompt                         "Foeg in status ta om oare minsken te litte witte wast do oanbiedest, do kinst ek #hashtags gebroeke."
   :add-to-contacts                       "Oan kontaktpersoanen tafoegje"
   :in-contacts                           "Yn kontakten"
   :remove-from-contacts                  "Fuortsmite út kontakten"
   :start-conversation                    "Start konversaasje"
   :send-transaction                      "Stjoer transaksje"

   ;;make_photo
   :image-source-title                    "Profylfoto"
   :image-source-make-photo               "Foto nimme"
   :image-source-gallery                  "Kies út galerij"

   ;;sharing
   :sharing-copy-to-clipboard             "Nei it klamboerd ta kopiearje"
   :sharing-share                         "Diele..."
   :sharing-cancel                        "Ôfbrekke"

   :browsing-title                        "Blêdzje"
   :browsing-open-in-web-browser          "Iepenje yn web blêder"
   :browsing-cancel                       "Ôfbrekke"
    
   ;sign-up
   :contacts-syncronized                  "Dyn kontaktpersoanen binne syngroniseard"
   :confirmation-code                     (str "Tiige tank! Wy ha dy in sms stjoerd mei in befêstigingskoade"
                                               ". Jou dyn koade op om dyn telefoannûmer te befêstigje")
   :incorrect-code                        (str "Sorry, de koade wie ferkeard, fier hem opnei yn")
   :phew-here-is-your-passphrase          "*Poah* dat wie dreech, hjir is dyn wachtsin, *skriuw dizze op en bewarje him goed!* Do silst him nedich ha om dyn account te herstelle."
   :here-is-your-passphrase               "Dat wie dreech, hjir is dyn wachtsin, *skriuw dizze op en bewarje him goed!* Do silst him nedich ha om dyn account te herstelle."
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
   :clear-history                         "Wis skiednis"
   :mute-notifications                    "Mute notifikaasjes"
   :leave-chat                            "Ferlit chat"
   :chat-settings                         "Chatynstellingen"
   :edit                                  "Bewurkje"
   :add-members                           "Foegje leden ta"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Nije groep"
   :reorder-groups                        "Reorder groepen"
   :edit-group                            "Bewurkje group"
   :delete-group                          "Wiskje groep"
   :delete-group-confirmation             "Dizze groep sille fuortsmiten wurde fan dyn groepen. Dit sil gjin ynfloed ha mei jo kontakten"
   :delete-group-prompt                   "Dit sil gjin ynfloed ha mei jo kontakten"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakten"}
   ;participants

   ;protocol
   :received-invitation                   "Chatútnoeging ûntfange"
   :removed-from-chat                     "ferwidere dy út de chat"
   :left                                  "gie fuort"
   :invited                               "útnoege"
   :removed                               "ferwidere"
   :You                                   "Do"

   ;new-contact
   :add-new-contact                       "Foegje nij kontaktpersoan ta "
   :scan-qr                               "QR scanne"
   :name                                  "Namme"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-public-key                "Fear in jildich adres public kaai yn of scan in QR koade"
   :contact-already-added                 "De kontaktpersoan is ol tafoege"
   :can-not-add-yourself                  "Do kinst dysels net tafoegje"
   :unknown-address                       "Ûnbekend adres"


   ;login
   :connect                               "Ferbine"
   :address                               "Adres"
   :password                              "Wachtwurd"
   :sign-in-to-status                     "Oanmelde by Status"
   :sign-in                               "Ynlogge"
   :wrong-password                        "Ferkeard wachtwurd"

   ;recover
   :passphrase                            "Wachtsin"
   :recover                               "Herstelle"
   :twelve-words-in-correct-order         "12 wurden yn de goeie folchoarder"

   ;accounts
   :recover-access                        "Tagong herstelle"
   :create-new-account                    "Nij akkount oanmeitsje"

   ;wallet-qr-code
   :done                                  "Klear"
   :main-wallet                           "Haadbeurs"

   ;validation
   :invalid-phone                         "Ûnjildich telefoannûmer"
   :amount                                "Bedrach"
   ;transactions
   :confirm                               "Befestigje"
   :transaction                           "Transaksje"
   :status                                "Status"
   :recipient                             "Ûntfanger"
   :to                                    "Ta"
   :from                                  "Fan"
   :data                                  "Data"
   :got-it                                "Befetsje ik"

   ;:webview
   :web-view-error                        "oeps, flater"})

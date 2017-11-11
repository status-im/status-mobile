(ns status-im.translations.lv)

(def translations
  {
   ;common
   :members-title                         "Dalībnieki"
   :not-implemented                       "!tādas funkcijas pagaidām nav"
   :chat-name                             "Čata nosaukums"
   :notifications-title                   "Paziņojumi un Skaņas"
   :offline                               "Offline"
   :search-for                            "Meklēt..."
   :cancel                                "Atcelt"
   :next                                  "Tālāk"
   :type-a-message                        "Raksti īsziņu..."
   :type-a-command                        "Ievadi komandu..."
   :error                                 "Kļūda"

   :camera-access-error                   "Kļūda, nav atļaujas piekļūt kamerai. Lūdzu, iestatījumos pārliecinies, ka  Status > Camera ir izvēlēts."
   :photos-access-error                   "Kļūda, nav atļaujas piekļūt fotogrāfijām. Lūdzu, iestatījumos pārliecinies, ka Status > Photos ir izvēlēts."

   ;drawer
   :invite-friends                        "Uzaicināt draugus"
   :faq                                   "FAQ un biežāk uzdotie jautājumi"
   :switch-users                          "Mainīt lietotāju"
   :feedback                              "Gribi atstāt atsauksmi? Krati telefonu!"
   :view-all                              "Skatīt visu"
   :current-network                       "Tīkls"

   ;chat
   :is-typing                             "raksta"
   :and-you                               "un tu"
   :search-chat                           "Meklēt čatu"
   :members                               {:one   "1 dalībnieks"
                                           :other "{{count}} dalībnieki"
                                           :zero  "nav dalībnieku"}
   :members-active                        {:one   "1 dalībnieks"
                                           :other "{{count}} dalībnieki"
                                           :zero  "nav dalībnieku"}
   :public-group-status                   "Publisks"
   :active-online                         "Online"
   :active-unknown                        "Nezināms"
   :available                             "Pieejams"
   :no-messages                           "Nav īsziņu"
   :suggestions-requests                  "Pieprasījums"
   :suggestions-commands                  "Komandas"
   :faucet-success                        "Faucet pieprasījums saņemts"
   :faucet-error                          "Faucet pieprasījuma kļūda"

   ;sync
   :sync-in-progress                      "Sinhronizē..."
   :sync-synced                           "Sinhronizēts"

   ;messages
   :status-sending                        "Sūta"
   :status-pending                        "Neizlemts"
   :status-sent                           "Aizsūtīts"
   :status-seen-by-everyone               "Apskatīts"
   :status-seen                           "Apskatīts"
   :status-delivered                      "Piegādāts"
   :status-failed                         "Neizdevās"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "sekunde"
                                           :other "sekundes"}
   :datetime-minute                       {:one   "minūte"
                                           :other "minūtes"}
   :datetime-hour                         {:one   "stunda"
                                           :other "stundas"}
   :datetime-day                          {:one   "diena"
                                           :other "dienas"}
   :datetime-multiple                     "s"
   :datetime-ago                          "pirms"
   :datetime-yesterday                    "vakar"
   :datetime-today                        "šodien"

   ;profile
   :profile                               "Profils"
   :edit-profile                          "Rediģēt profilu"
   :report-user                           "Ziņot"
   :message                               "Īsziņa"
   :username                              "Lietotājvārds"
   :not-specified                         "Nav norādīts"
   :public-key                            "Public key"
   :phone-number                          "Telefona numurs"
   :email                                 "E-pasts"
   :update-status                         "Rediģēt aprakstu..."
   :add-a-status                          "Pievienot aprakstu..."
   :status-prompt                         "Pievieno aprakstu lai cilvēki zinātu ko tu piedāvā. Tu vari arī izlietot #hashtagus."
   :add-to-contacts                       "Pievieno kontaktu"
   :in-contacts                           "Kontaktos"
   :remove-from-contacts                  "Izdzēst kontaktu"
   :start-conversation                    "Sākt sarunu"
   :send-transaction                      "Sūtīt transakciju"
   :share-qr                              "Dalīties ar QR kodu"
   :error-incorrect-name                  "Lūdzu izmanto citu vārdu"
   :error-incorrect-email                 "Nepareizs e-pasts"

   
   ;;make_photo
   :image-source-title                    "Profila bilde"
   :image-source-make-photo               "Fotografēt"
   :image-source-gallery                  "Izvēlēties no galerijas"
   :image-source-cancel                   "Atcelt"

   ;;sharing
   :sharing-copy-to-clipboard             "Copy to clipboard"
   :sharing-share                         "Dalīties..."
   :sharing-cancel                        "Atcelt"

   :browsing-title                        "Browse"
   :browsing-open-in-web-browser          "Atvert pārlūkprogrammā"
   :browsing-cancel                       "Atcelt"

   ;sign-up
   :contacts-syncronized                  "Jūsu kontakti ir sinhronizēti"
   :confirmation-code                     (str "Paldies! Mēs nosūtijām tev īsziņu ar apstiprinājuma kodu"
                                               "code. Lūdzu ievadi apstiprinājuma kodu lai verificētu savu telefona numuru")
   :incorrect-code                        (str "Nepareizs kods, mēģiniet vēlreiz")
   :generate-passphrase                   (str "Tu saņemsi jaunu passphrase lai"
                                               "tu varētu ieiet no cita telefona")
   :phew-here-is-your-passphrase          "*Fuh* tas bija grūti, re, te ir tavs jauns passphrase, *pieraksti un sargā to!* Tev viņš būs vajadzīgs lai atgūtu pieju."
   :here-is-your-passphrase               "Te ir tavs jauns passphrase, *pieraksti un sargā to!* Tev viņš būs vajadzīgs lai atgūtu pieju savam kontam."
   :written-down                          "Pārliecinies, ka pareizi pierakstīji to."
   :phone-number-required                 "Ievadi savu telefonu un es atradīšu tavus draugus."
   :shake-your-phone                      "Atradi kļūdu? Pastāsti mums par to! Krati telefonu!"
   :intro-status                          "Čato ar mani, ja gribi mainīt ustatījumus!"
   :intro-message1                        "Sveicināti! Uzspied lai sāktu!"
   :account-generation-message            "Lūdzu uzgaidi..."
   :move-to-internal-failure-message      "Mums vajag pārvietot dažus failus no eksternās uz interno glabātuvi. Lai mēs to varētu izdarīt mums vajag jūsu atļauju."
   :debug-enabled                         "Debug serveris startēja! Tu vari izlietot *status-dev-cli scan* lai atrastu serveri."

   ;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significant"

   ;chats
   :chats                                 "Čati"
   :new-chat                              "Jaunais čāts"
   :delete-chat                           "Dzēst čatu"
   :new-group-chat                        "Jauna grupa"
   :new-public-group-chat                 "Pievienoties publiskajam čatam"
   :edit-chats                            "Rediģēt čatu"
   :search-chats                          "Meklēt čatu"
   :empty-topic                           "Nav temata"
   :topic-format                          "Nepareiz formāts [a-z0-9\\-]+"
   :public-group-topic                    "Temats"

   ;discover
   :discover                              "Discover"
   :none                                  "None"
   :search-tags                           "Type your search tags here"
   :popular-tags                          "Popular tags"
   :recent                                "Recent"
   :no-statuses-discovered                "No statuses discovered"
   :no-statuses-found                     "No statuses found"

   ;settings
   :settings                              "Iestatījumi"

   ;contacts
   :contacts                              "Kontakti"
   :new-contact                           "Jauns kontakts"
   :delete-contact                        "Dzēst kontaktu"
   :delete-contact-confirmation           "Kontakts tiks dzēsts"
   :remove-from-group                     "Dzēst no grupas"
   :edit-contacts                         "Rediģēt kontakts"
   :search-contacts                       "Meklēt kontaktus"
   :show-all                              "PARĀDĪT VISUS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Cilvēki"
   :contacts-group-new-chat               "Sākt jaunu čatu"
   :choose-from-contacts                  "Izvēlēties no kontaktiem"
   :no-contacts                           "Kontaktu nav"
   :show-qr                               "Parādīt QR"
   :enter-address                         "Ieraksti adresi"
   :more                                  "vairāk"

   ;group-settings
   :remove                                "Noņemt"
   :save                                  "Saglabāt"
   :delete                                "Dzēst"
   :change-color                          "Mainīt krāsu"
   :clear-history                         "Dzēst vēsturi"
   :mute-notifications                    "Mute"
   :leave-chat                            "Iziet no čata"
   :delete-and-leave                      "Dzēst un iziet"
   :chat-settings                         "Čata iestatījumi"
   :edit                                  "Rediģēt"
   :add-members                           "Pievienot biedrus"
   :blue                                  "Blue"
   :purple                                "Purple"
   :green                                 "Green"
   :red                                   "Red"

   ;commands
   :money-command-description             "Sūtīt naudu"
   :location-command-description          "Sūtīt lokāciju"
   :phone-command-description             "Sūtīt numuru"
   :phone-request-text                    "Telefona numura pieprasījums"
   :confirmation-code-command-description "Sūtīt apstiprinājuma kodu"
   :confirmation-code-request-text        "Apstiprinājuma koda pieprasījums"
   :send-command-description              "Sūtīt lokāciju"
   :request-command-description           "Sūtīt pieprasījumu"
   :keypair-password-command-description  ""
   :help-command-description              "Palīdzība"
   :request                               "Pieprasījums"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH kam {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH no {{chat-name}}"

   ;new-group
   :group-chat-name                       "Čata nosaukums"
   :empty-group-chat-name                 "Ievadi vārdu"
   :illegal-group-chat-name               "Lūdzu, izvēlaties citu vārdu"
   :new-group                             "Jauna grupa"
   :reorder-groups                        "Reorder groups"
   :group-name                            "Grupas nosaukums"
   :edit-group                            "Rediģēt grupu"
   :delete-group                          "Dzēst grupu"
   :delete-group-confirmation             "Grupa tiks noņēmta. Tas neietikmēs tavus kontaktus"
   :delete-group-prompt                   "Tas neietikmēs kontaktus"
   :group-members                         "Grupas biedri"
   :contact-s                             {:one   "kontakts"
                                           :other "kontakti"}
   ;participants
   :add-participants                      "Pievienot biedrus"
   :remove-participants                   "Noņemt biedrus"

   ;protocol
   :received-invitation                   "čata uzaicinājums"
   :removed-from-chat                     "noņema jūs no grupas čata"
   :left                                  "atstāja grupu"
   :invited                               "uzaicināts"
   :removed                               "noņemts"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Pievienot jaunu kontaktu"
   :import-qr                             "Importēt"
   :scan-qr                               "Skanēt QR"
   :swow-qr                               "Parādīt QR"
   :name                                  "Vārds"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "Lūdzu ievadi adresi jeb skanē QR kodu"
   :enter-valid-public-key                "Lūdzu ievadi publisko atslēgu, jeb skanē QR kodu"
   :contact-already-added                 "Kontakts jau bija pievienots"
   :can-not-add-yourself                  "Tu nevari pievienot sevi"
   :unknown-address                       "Nezināmā adrese"


   ;login
   :connect                               "Savienoties"
   :address                               "Adrese"
   :password                              "Parole"
   :login                                 "Lietotājvārds"
   :sign-in-to-status                     "Ieiet Status"
   :sign-in                               "Ieiet"
   :wrong-password                        "Parole ievadīta nepareizi"

   ;recover
   :recover-from-passphrase               "Atgūt no passphrase"
   :recover-explain                       "Ievadi passphrase lai atgūtu pieeju"
   :passphrase                            "Passphrase"
   :recover                               "Atgūt"
   :enter-valid-passphrase                "Ievadi passphrase"
   :enter-valid-password                  "Ievadi paroli"
   :twelve-words-in-correct-order         "12 vārdi"

   ;accounts
   :recover-access                        "Atgūt pieeju"
   :add-account                           "Pievienot kontu"
   :create-new-account                    "Izveidot jaunu kontu"

   ;wallet-qr-code
   :done                                  "Darīts"
   :main-wallet                           "Galvenais maks"

   ;validation
   :invalid-phone                         "Nepareizs telefona numurs"
   :amount                                "Summa"
   :not-enough-eth                        (str "Kontā nepietiek ETH "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Apstiprināt"
   :confirm-transactions                  {:one   "Apstiprināt transakciju"
                                           :other "Apstiprināt {{count}} transakcijas"
                                           :zero  "Nav transakciju"}
   :transactions-confirmed                {:one   "Transakcija apstiprināta"
                                           :other "{{count}} transakcija apstiprinātas"
                                           :zero  "Nav apstiprinātas transakcijas"}
   :transaction                           "Transakcija"
   :unsigned-transactions                 "Neparakstītas transakcijas"
   :no-unsigned-transactions              "Nav neparakstītas transakcijas"
   :enter-password-transactions           {:one   "Apstiprināt transakciju (ievadi savu paroli)"
                                           :other "Apstiprināt transakcija (ievadi savu paroli)"}
   :status                                "Status"
   :pending-confirmation                  "Neizlemts apstiprinājums"
   :recipient                             "Saņēmējs"
   :one-more-item                         "Vel viena lieta"
   :fee                                   "Maksa"
   :estimated-fee                         "~Maksa"
   :value                                 "Vērtība"
   :to                                    "Kam"
   :from                                  "No"
   :data                                  "Dati"
   :got-it                                "Got it"
   :contract-creation                     "Līguma izveidošana"

   ;:webview
   :web-view-error                        "ups, kļūda"})

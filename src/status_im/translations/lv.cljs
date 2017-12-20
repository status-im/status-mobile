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
   :switch-users                          "Mainīt lietotāju"
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
   :datetime-ago                          "pirms"
   :datetime-yesterday                    "vakar"
   :datetime-today                        "šodien"

   ;profile
   :profile                               "Profils"
   :edit-profile                          "Rediģēt profilu"
   :message                               "Īsziņa"
   :not-specified                         "Nav norādīts"
   :public-key                            "Public key"
   :phone-number                          "Telefona numurs"
   :update-status                         "Rediģēt aprakstu..."
   :add-a-status                          "Pievienot aprakstu..."
   :status-prompt                         "Pievieno aprakstu lai cilvēki zinātu ko tu piedāvā. Tu vari arī izlietot #hashtagus."
   :add-to-contacts                       "Pievieno kontaktu"
   :in-contacts                           "Kontaktos"
   :remove-from-contacts                  "Izdzēst kontaktu"
   :start-conversation                    "Sākt sarunu"
   :send-transaction                      "Sūtīt transakciju"
   ;;make_photo
   :image-source-title                    "Profila bilde"
   :image-source-make-photo               "Fotografēt"
   :image-source-gallery                  "Izvēlēties no galerijas"

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
   :phew-here-is-your-passphrase          "*Fuh* tas bija grūti, re, te ir tavs jauns passphrase, *pieraksti un sargā to!* Tev viņš būs vajadzīgs lai atgūtu pieju."
   :here-is-your-passphrase               "Te ir tavs jauns passphrase, *pieraksti un sargā to!* Tev viņš būs vajadzīgs lai atgūtu pieju savam kontam."
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
   :clear-history                         "Dzēst vēsturi"
   :mute-notifications                    "Mute"
   :leave-chat                            "Iziet no čata"
   :chat-settings                         "Čata iestatījumi"
   :edit                                  "Rediģēt"
   :add-members                           "Pievienot biedrus"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Jauna grupa"
   :reorder-groups                        "Reorder groups"
   :edit-group                            "Rediģēt grupu"
   :delete-group                          "Dzēst grupu"
   :delete-group-confirmation             "Grupa tiks noņēmta. Tas neietikmēs tavus kontaktus"
   :delete-group-prompt                   "Tas neietikmēs kontaktus"
   :contact-s                             {:one   "kontakts"
                                           :other "kontakti"}
   ;participants

   ;protocol
   :received-invitation                   "čata uzaicinājums"
   :removed-from-chat                     "noņema jūs no grupas čata"
   :left                                  "atstāja grupu"
   :invited                               "uzaicināts"
   :removed                               "noņemts"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Pievienot jaunu kontaktu"
   :scan-qr                               "Skanēt QR"
   :name                                  "Vārds"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-public-key                "Lūdzu ievadi publisko atslēgu, jeb skanē QR kodu"
   :contact-already-added                 "Kontakts jau bija pievienots"
   :can-not-add-yourself                  "Tu nevari pievienot sevi"
   :unknown-address                       "Nezināmā adrese"


   ;login
   :connect                               "Savienoties"
   :address                               "Adrese"
   :password                              "Parole"
   :sign-in-to-status                     "Ieiet Status"
   :sign-in                               "Ieiet"
   :wrong-password                        "Parole ievadīta nepareizi"

   ;recover
   :passphrase                            "Passphrase"
   :recover                               "Atgūt"
   :twelve-words-in-correct-order         "12 vārdi"

   ;accounts
   :recover-access                        "Atgūt pieeju"
   :create-new-account                    "Izveidot jaunu kontu"

   ;wallet-qr-code
   :done                                  "Darīts"
   :main-wallet                           "Galvenais maks"

   ;validation
   :invalid-phone                         "Nepareizs telefona numurs"
   :amount                                "Summa"
   ;transactions
   :confirm                               "Apstiprināt"
   :transaction                           "Transakcija"
   :status                                "Status"
   :recipient                             "Saņēmējs"
   :to                                    "Kam"
   :from                                  "No"
   :data                                  "Dati"
   :got-it                                "Got it"

   ;:webview
   :web-view-error                        "ups, kļūda"})

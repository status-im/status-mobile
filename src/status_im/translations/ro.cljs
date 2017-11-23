(ns status-im.translations.ro)

(def translations
  {
   ;common
   :members-title                         "Membri"
   :not-implemented                       "!nu a fost implementat"
   :chat-name                             "Nume chat"
   :notifications-title                   "Notificări și sunete"
   :offline                               "Offline"

   ;drawer
   :switch-users                          "Schimbă utilizatori"

   ;chat
   :is-typing                             "tastează"
   :and-you                               "Și cu tine"
   :search-chat                           "Caută în chat"
   :members                               {:one   "1 membru"
                                           :other "{{count}} membri"
                                           :zero  "nici un membru"}
   :members-active                        {:one   "1 membru, 1 activ"
                                           :other "{{count}} membri, {{count}} activi"
                                           :zero  "nici un membru"}
   :active-online                         "Online"
   :active-unknown                        "Necunoscut"
   :available                             "Disponibil"
   :no-messages                           "Nici un mesaj"
   :suggestions-requests                  "Solicitări"
   :suggestions-commands                  "Comenzi"

   ;sync
   :sync-in-progress                      "Se sincronizează…"
   :sync-synced                           "Sincronizat"

   ;messages
   :status-sending                        "Se trimite"
   :status-pending                        "În așteptare"
   :status-sent                           "Trimis"
   :status-seen-by-everyone               "Văzut de toată lumea"
   :status-seen                           "Văzut"
   :status-delivered                      "Livrat"
   :status-failed                         "Eșuat"

   ;datetime
   :datetime-second                       {:one   "secundă"
                                           :other "secunde"}
   :datetime-minute                       {:one   "minut"
                                           :other "minute"}
   :datetime-hour                         {:one   "oră"
                                           :other "ore"}
   :datetime-day                          {:one   "zi"
                                           :other "zile"}
   :datetime-ago                          "în urmă"
   :datetime-yesterday                    "ieri"
   :datetime-today                        "azi"

   ;profile
   :profile                               "Profil"
   :message                               "Mesaj"
   :not-specified                         "Nu este specificat"
   :public-key                            "Cheie publică"
   :phone-number                          "Număr de telefon"
   :add-to-contacts                       "Adaugă la contacte"

   ;;make_photo
   :image-source-title                    "Imagine de profil"
   :image-source-make-photo               "Captează"
   :image-source-gallery                  "Selectează din galerie"

   ;sign-up
   :contacts-syncronized                  "Contactele tale au fost sincronizate"
   :confirmation-code                     (str "Mulțumim! Ți-am trims un mesaj text cu un cod "
                                               "de confirmare. Te rugăm să ne transmiți acel cod pentru a-ți confirma numărul de telefon")
   :incorrect-code                        (str "Ne pare rău, dar codul este greșit, te rugăm să-l introduci încă o dată")
   :phew-here-is-your-passphrase          "*Pfui* a fost greu, iată fraza ta de acces, *noteaz-o și păstreaz-o în siguranță!* Vei avea nevoie de ea pentru a-ți redobândi accesul la cont."
   :here-is-your-passphrase               "Iată fraza ta de acces, *noteaz-o și păstreaz-o în siguranță!* Vei avea nevoie de ea pentru a-ți redobândi accesul la cont."
   :phone-number-required                 "Apasă aici ca să-ți introduci numărul de telefon și eu îți voi invita prietenii"
   :intro-status                          "Hai să vorbim pe chat pentru a-ți seta contul și modifica setările!"
   :intro-message1                        "Bine ai vent în Status\nApasă pe acest mesaj pentru a-ți seta parola și a începe!"
   :account-generation-message            "O secundă, trebuie să fac niște calcule complicate ca să-ți generez contul!"

   ;chats
   :chats                                 "Discuții pe chat"
   :new-group-chat                        "Grup nou de chat"

   ;discover
   :discover                              "Descoperire"
   :none                                  "Niciuna"
   :search-tags                           "Tastează aici etichetele de căutat"
   :popular-tags                          "Etichete populare"
   :recent                                "Recente"
   :no-statuses-discovered                "Nici un status găsit"

   ;settings
   :settings                              "Setări"

   ;contacts
   :contacts                              "Contacte"
   :new-contact                           "Contact nou"
   :contacts-group-new-chat               "Începe discuție nouă"
   :no-contacts                           "Nici un contact deocamdată"
   :show-qr                               "Afișează QR"

   ;group-settings
   :remove                                "Elimină"
   :save                                  "Salvează"
   :clear-history                         "Șterge istoricul"
   :chat-settings                         "Setări chat"
   :edit                                  "Editare"
   :add-members                           "Adăugare membri"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "A primit invitația de chat"
   :removed-from-chat                     "te-a elimination din grupul de chat"
   :left                                  "a plecat"
   :invited                               "invitat"
   :removed                               "eliminat"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Adaugă contact nou"
   :scan-qr                               "Scanează QR"
   :name                                  "Nume"
   :address-explication                   "Poate că aici ar trebui să fie un text care să explice ce este o adresă și unde să o cauți"
   :contact-already-added                 "Contactul a fost adăugat deja"
   :can-not-add-yourself                  "Nu te poți adăuga pe tine"
   :unknown-address                       "Adresă necunoscută"


   ;login
   :connect                               "Conectare"
   :address                               "Adresă"
   :password                              "Parolă"
   :wrong-password                        "Parola greșită"

   ;recover
   :passphrase                            "Fraza de acces"
   :recover                               "Recuperează"

   ;accounts
   :recover-access                        "Recuperează acces"

   ;wallet-qr-code
   :done                                  "Gata"
   :main-wallet                           "Portofelul principal"

   ;validation
   :invalid-phone                         "Număr de telefon nevalid"
   :amount                                "Sumă"
   ;transactions
   :status                                "Status"
   :recipient                             "Beneficiar"

   ;:webview
   :web-view-error                        "ups, eroare"

   :confirm                               "Confirmare"
   :phone-national                        "Național"
   :public-group-topic                    "Subiect"
   :debug-enabled                         "Serverul de curățare a fost lansat! Adresa dvs. Acum vă puteți adăuga DApp rulând *status-dev-cli scan* de pe computerul dvs."
   :new-public-group-chat                 "Alăturați-vă chat-ului public"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Anulare"
   :twelve-words-in-correct-order         "12 de cuvinte în ordinea corectă"
   :remove-from-contacts                  "Eliminare din contacte"
   :delete-chat                           "Ștergere chat"
   :edit-chats                            "Editare discuții"
   :sign-in                               "Conectare"
   :create-new-account                    "Creați un cont nou"
   :sign-in-to-status                     "Conectați-vă cu statusul"
   :got-it                                "Am înțeles"
   :move-to-internal-failure-message      "Trebuie să mutăm anumite fișiere importante din mediul de stocare extern în mediul de stocare intern. Pentru a face asta, avem nevoie de permisiunea dvs. În versiunile viitoare, nu vom folosi stocarea externă."
   :edit-group                            "Editare grup"
   :delete-group                          "Ștergere grup"
   :browsing-title                        "Navigare"
   :reorder-groups                        "Reorganizare grupuri"
   :browsing-cancel                       "Anulare"
   :faucet-success                        "Solicitarea a fost primită"
   :choose-from-contacts                  "Alegeți dintre contacte"
   :new-group                             "Grup nou"
   :phone-e164                            "Internațional 1"
   :remove-from-group                     "Eliminare din grup"
   :search-contacts                       "Căutare contacte"
   :transaction                           "Tranzacție"
   :public-group-status                   "Public"
   :leave-chat                            "Părăsire chat"
   :start-conversation                    "Începeți o conversație"
   :topic-format                          "Format greșit [a-z0-9\\-]+"
   :enter-valid-public-key                "Vă rugăm să introduceți o cheie publică validă sau să scanați un cod QR"
   :faucet-error                          "Eroare solicitare"
   :phone-significant                     "Important"
   :search-for                            "Căutare după..."
   :sharing-copy-to-clipboard             "Copiere în clipboard"
   :phone-international                   "Internațional 2"
   :enter-address                         "Introduceți adresa"
   :send-transaction                      "Trimitere tranzacție"
   :delete-contact                        "Ștergere contact"
   :mute-notifications                    "Notificări silențioase"


   :contact-s                             {:one   "contact"
                                           :other "contacte"}
   :next                                  "Mai departe"
   :from                                  "De la"
   :search-chats                          "Căutare discuții"
   :in-contacts                           "În contacte"

   :sharing-share                         "Partajare..."
   :type-a-message                        "Tastați un mesaj..."
   :type-a-command                        "Începeți să tastați o comandă..."
   :shake-your-phone                      "Ați găsit o eroare sau aveți o sugestie? E suficient ~să scuturați~ telefonul!"
   :status-prompt                         "Creați un status pentru a-i ajuta pe ceilalți să afle ce anume oferiți. Puteți folosi și #hashtag-uri."
   :add-a-status                          "Adăugați un status..."
   :error                                 "Eroare"
   :edit-contacts                         "Editare contacte"
   :more                                  "mai mult"
   :cancel                                "Anulare"
   :no-statuses-found                     "Niciun status găsit"
   :browsing-open-in-web-browser          "Deschide în browser web"
   :delete-group-prompt                   "Aceasta nu va afecta contactele"
   :edit-profile                          "Editare profil"


   :empty-topic                           "Subiect gol"
   :to                                    "Către"
   :data                                  "Date"})

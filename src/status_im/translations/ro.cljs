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
   :invite-friends                        "Invită prieteni"
   :faq                                   "Întrebări frecvente"
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
   :datetime-multiple                     "s"
   :datetime-ago                          "în urmă"
   :datetime-yesterday                    "ieri"
   :datetime-today                        "azi"

   ;profile
   :profile                               "Profil"
   :report-user                           "RAPORTEAZĂ UTILIZATOR"
   :message                               "Mesaj"
   :username                              "Nume de utilizator"
   :not-specified                         "Nu este specificat"
   :public-key                            "Cheie publică"
   :phone-number                          "Număr de telefon"
   :email                                 "E-mail"
   :profile-no-status                     "Nici un status"
   :add-to-contacts                       "Adaugă la contacte"
   :error-incorrect-name                  "Te rugăm să alegi un alt nume"
   :error-incorrect-email                 "E-mail greșit"

   ;;make_photo
   :image-source-title                    "Imagine de profil"
   :image-source-make-photo               "Captează"
   :image-source-gallery                  "Selectează din galerie"
   :image-source-cancel                   "Anulează"

   ;sign-up
   :contacts-syncronized                  "Contactele tale au fost sincronizate"
   :confirmation-code                     (str "Mulțumim! Ți-am trims un mesaj text cu un cod "
                                               "de confirmare. Te rugăm să ne transmiți acel cod pentru a-ți confirma numărul de telefon")
   :incorrect-code                        (str "Ne pare rău, dar codul este greșit, te rugăm să-l introduci încă o dată")
   :generate-passphrase                   (str "Voi genera o frază de acces pentru tine, ca să poți redobândi "
                                               "accesul sau să te poți conecta de pe alt dispozitiv")
   :phew-here-is-your-passphrase          "*Pfui* a fost greu, iată fraza ta de acces, *noteaz-o și păstreaz-o în siguranță!* Vei avea nevoie de ea pentru a-ți redobândi accesul la cont."
   :here-is-your-passphrase               "Iată fraza ta de acces, *noteaz-o și păstreaz-o în siguranță!* Vei avea nevoie de ea pentru a-ți redobândi accesul la cont."
   :written-down                          "Ai grijă să o notezi în condiții de siguranță"
   :phone-number-required                 "Apasă aici ca să-ți introduci numărul de telefon și eu îți voi invita prietenii"
   :intro-status                          "Hai să vorbim pe chat pentru a-ți seta contul și modifica setările!"
   :intro-message1                        "Bine ai vent în Status\nApasă pe acest mesaj pentru a-ți seta parola și a începe!"
   :account-generation-message            "O secundă, trebuie să fac niște calcule complicate ca să-ți generez contul!"

   ;chats
   :chats                                 "Discuții pe chat"
   :new-chat                              "Discuție nouă"
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
   :show-all                              "AFIȘEAZĂ TOATE"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Oameni"
   :contacts-group-new-chat               "Începe discuție nouă"
   :no-contacts                           "Nici un contact deocamdată"
   :show-qr                               "Afișează QR"

   ;group-settings
   :remove                                "Elimină"
   :save                                  "Salvează"
   :change-color                          "Schimbă culoarea"
   :clear-history                         "Șterge istoricul"
   :delete-and-leave                      "Șterge și ieși"
   :chat-settings                         "Setări chat"
   :edit                                  "Editare"
   :add-members                           "Adăugare membri"
   :blue                                  "Albastru"
   :purple                                "Violet"
   :green                                 "Verde"
   :red                                   "Roșu"

   ;commands
   :money-command-description             "Trimite bani"
   :location-command-description          "Trimite locație"
   :phone-command-description             "Trimite număr de telefon"
   :phone-request-text                    "Solicitare număr de telefon"
   :confirmation-code-command-description "Trimite cod de confirmare"
   :confirmation-code-request-text        "Solicitare cod de confirmare"
   :send-command-description              "Trimite locația"
   :request-command-description           "Trimite solicitarea"
   :keypair-password-command-description  ""
   :help-command-description              "Ajutor"
   :request                               "Solicitare"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH pentru {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de la {{chat-name}}"

   ;new-group
   :group-chat-name                       "Nume chat"
   :empty-group-chat-name                 "Te rugăm să introduci un nume"
   :illegal-group-chat-name               "Te rugăm să selectezi un alt nume"

   ;participants
   :add-participants                      "Adaugă participanți"
   :remove-participants                   "Elimină participanți"

   ;protocol
   :received-invitation                   "A primit invitația de chat"
   :removed-from-chat                     "te-a elimination din grupul de chat"
   :left                                  "a plecat"
   :invited                               "invitat"
   :removed                               "eliminat"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Adaugă contact nou"
   :import-qr                             "Importă"
   :scan-qr                               "Scanează QR"
   :name                                  "Nume"
   :whisper-identity                      "Identitatea"
   :address-explication                   "Poate că aici ar trebui să fie un text care să explice ce este o adresă și unde să o cauți"
   :enter-valid-address                   "Te rugăm să introduci o adresă validă sau să scanezi un cod QR"
   :contact-already-added                 "Contactul a fost adăugat deja"
   :can-not-add-yourself                  "Nu te poți adăuga pe tine"
   :unknown-address                       "Adresă necunoscută"


   ;login
   :connect                               "Conectare"
   :address                               "Adresă"
   :password                              "Parolă"
   :login                                 "Conectează-te"
   :wrong-password                        "Parola greșită"

   ;recover
   :recover-from-passphrase               "Recuperează folosind fraza de acces"
   :recover-explain                       "Te rugăm să introduci fraza de acces si parola ta pentru a redobandi accesul"
   :passphrase                            "Fraza de acces"
   :recover                               "Recuperează"
   :enter-valid-passphrase                "Te rugăm să introduci o frază de acces"
   :enter-valid-password                  "Te rugăm să introduci o parolă"

   ;accounts
   :recover-access                        "Recuperează acces"
   :add-account                           "Adaugă cont"

   ;wallet-qr-code
   :done                                  "Gata"
   :main-wallet                           "Portofelul principal"

   ;validation
   :invalid-phone                         "Număr de telefon nevalid"
   :amount                                "Sumă"
   :not-enough-eth                        (str "Sold ETH insuficient "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmare tranzacție"
                                           :other "Confirmare {{count}} tranzacții"
                                           :zero  "Nicio tranzacție"}
   :status                                "Status"
   :pending-confirmation                  "Se așteaptă confirmarea"
   :recipient                             "Beneficiar"
   :one-more-item                         "Încă un articol"
   :fee                                   "Comision"
   :value                                 "Valoare"

   ;:webview
   :web-view-error                        "ups, eroare"

   :confirm                               "Confirmare"
   :phone-national                        "Național"
   :transactions-confirmed                {:one   "Tranzacție confirmată"
                                           :other "{{count}} (de) tranzacții confirmate"
                                           :zero  "Nicio tranzacție confirmată"}
   :public-group-topic                    "Subiect"
   :debug-enabled                         "Serverul de curățare a fost lansat! Adresa dvs. Acum vă puteți adăuga DApp rulând *status-dev-cli scan* de pe computerul dvs."
   :new-public-group-chat                 "Alăturați-vă chat-ului public"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Anulare"
   :share-qr                              "Partajare QR"
   :feedback                              "Aveți feedback?\nScuturați telefonul!"
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
   :group-name                            "Nume grup"
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
   :swow-qr                               "Afișare QR"
   :browsing-open-in-web-browser          "Deschide în browser web"
   :delete-group-prompt                   "Aceasta nu va afecta contactele"
   :edit-profile                          "Editare profil"


   :enter-password-transactions           {:one   "Confirmați tranzacția introducând parola"
                                           :other "Confirmați tranzacțiile introducând parola"}
   :unsigned-transactions                 "Tranzacții nesemnate"
   :empty-topic                           "Subiect gol"
   :to                                    "Către"
   :group-members                         "Membrii grupului"
   :estimated-fee                         "Comision est."
   :data                                  "Date"})

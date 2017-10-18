(ns status-im.translations.sl)

(def translations
  {
   ;common
   :members-title                         "Člani"
   :not-implemented                       "!ni implementirano"
   :chat-name                             "Ime za klepet"
   :notifications-title                   "Obvestila in zvoki"
   :offline                               "Nedosegljiv/-a"

   ;drawer
   :invite-friends                        "Povabi prijatelje"
   :faq                                   "Pogosta vprašanja"
   :switch-users                          "Preklopi med uporabniki"

   ;chat
   :is-typing                             "piše"
   :and-you                               "in ti"
   :search-chat                           "Iskanje po klepetu"
   :members                               {:one   "1 član"
                                           :other "{{count}} članov"
                                           :zero  "ni članov"}
   :members-active                        {:one   "1 član, 1 aktiven"
                                           :other "{{count}} članov, {{count}} aktivnih"
                                           :zero  "ni članov"}
   :active-online                         "Dosegljiv/-a"
   :active-unknown                        "Neznano"
   :available                             "Na voljo"
   :no-messages                           "Ni sporočil"
   :suggestions-requests                  "Prošnje"
   :suggestions-commands                  "Ukazi"

   ;sync
   :sync-in-progress                      "Sinhronizacija..."
   :sync-synced                           "Sinhronizirano"

   ;messages
   :status-sending                        "Pošiljanje"
   :status-pending                        "V teku"
   :status-sent                           "Poslano"
   :status-seen-by-everyone               "Vsi so videli"
   :status-seen                           "Videno"
   :status-delivered                      "Dostavljeno"
   :status-failed                         "Ni uspelo"

   ;datetime
   :datetime-second                       {:one   "sekunda"
                                           :other "sekund"}
   :datetime-minute                       {:one   "minuta"
                                           :other "minut"}
   :datetime-hour                         {:one   "ura"
                                           :other "ur"}
   :datetime-day                          {:one   "dan"
                                           :other "dni"}
   :datetime-multiple                     "s"
   :datetime-ago                          "nazaj"
   :datetime-yesterday                    "včeraj"
   :datetime-today                        "danes"

   ;profile
   :profile                               "Profil"
   :report-user                           "PRIJAVI UPORABNIKA"
   :message                               "Sporočilo"
   :username                              "Uporabniško ime"
   :not-specified                         "Ni navedeno"
   :public-key                            "Javni ključ"
   :phone-number                          "Telefonska številka"
   :email                                 "E-pošta"
   :profile-no-status                     "Brez statusa"
   :add-to-contacts                       "Dodaj med stike"
   :error-incorrect-name                  "Prosimo, izberi drugo ime"
   :error-incorrect-email                 "Nepravilna e-pošta"

   ;;make_photo
   :image-source-title                    "Fotografija profila"
   :image-source-make-photo               "Zajemi"
   :image-source-gallery                  "Izberi iz galerije"
   :image-source-cancel                   "Prekliči"

   ;sign-up
   :contacts-syncronized                  "Tvoji stiki so bili sinhronizirani"
   :confirmation-code                     (str "Hvala! Poslali smo ti sporočilo s potrditveno "
                                               "kodo. Prosimo, vnesi to kodo in potrdi svojo telefonsko številko")
   :incorrect-code                        (str "Koda na žalost ni bila pravilna, prosimo, da jo ponovno vneseš")
   :generate-passphrase                   (str "Zate bom ustvaril šifrirno geslo, tako da boš lahko obnovil/-a svoj "
                                               "dostop, ali se prijavil/-a iz druge naprave")
   :phew-here-is-your-passphrase          "*Pfuu* to pa je bilo težko, tukaj je tvoje šifrirno geslo, *zapiši si ga in shrani na varno mesto!* Potreboval/-a ga boš, da obnoviš svoj račun."
   :here-is-your-passphrase               "Tukaj je tvoje geslo, *zapiši si ga in shrani na varno mesto!* Potreboval/-a ga boš, da obnoviš svoj račun."
   :written-down                          "Poskrbi, da si ga pazljivo zapišeš"
   :phone-number-required                 "Pritisni tukaj in vnesi svojo telefonsko številko ter poiskal bom tvoje prijatelje"
   :intro-status                          "Klepetaj z mano in nastavi svoj račun ter spremeni svoje nastavitve!"
   :intro-message1                        "Dobrodošel/-la v status\nPritisni to sporočilo in nastavi svoje geslo ter začni!"
   :account-generation-message            "Počakaj sekundo, opraviti moram noro računico, da ustvarim tvoj račun!"

   ;chats
   :chats                                 "Klepeti"
   :new-chat                              "Nov klepet"
   :new-group-chat                        "Nov skupinski klepet"

   ;discover
   :discover                              "Odkrivanje"
   :none                                  "Brez"
   :search-tags                           "Sem vnesi svoje priljubljene oznake"
   :popular-tags                          "Priljubljene oznake"
   :recent                                "Nedavno"
   :no-statuses-discovered                "Ni odkritih statusov"

   ;settings
   :settings                              "Nastavitve"

   ;contacts
   :contacts                              "Stiki"
   :new-contact                           "Nov stik"
   :show-all                              "POKAŽI VSE"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Osebe"
   :contacts-group-new-chat               "Začni nov klepet"
   :no-contacts                           "Zaenkrat še ni stikov"
   :show-qr                               "Prikaži QR"

   ;group-settings
   :remove                                "Odstrani"
   :save                                  "Shrani"
   :change-color                          "Spremeni barvo"
   :clear-history                         "Počisti zgodovino"
   :delete-and-leave                      "Izbriši in zapri"
   :chat-settings                         "Nastavitve klepeta"
   :edit                                  "Uredi"
   :add-members                           "Dodaj člane"
   :blue                                  "Modra"
   :purple                                "Vijolična"
   :green                                 "Zelena"
   :red                                   "Rdeča"

   ;commands
   :money-command-description             "Pošlji denar"
   :location-command-description          "Pošlji lokacijo"
   :phone-command-description             "Pošlji telefonsko številko"
   :phone-request-text                    "Prošnja za telefonsko številko"
   :confirmation-code-command-description "Pošlji potrditveno kodo"
   :confirmation-code-request-text        "Prošnja za potrditveno kodo"
   :send-command-description              "Pošlji lokacijo"
   :request-command-description           "Pošlji prošnjo"
   :keypair-password-command-description  ""
   :help-command-description              "Pomoč"
   :request                               "Prošnja"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH osebi {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH od osebe {{chat-name}}"

   ;new-group
   :group-chat-name                       "Ime za klepet"
   :empty-group-chat-name                 "Prosimo, vnesi ime"
   :illegal-group-chat-name               "Prosimo, izberi drugo ime"

   ;participants
   :add-participants                      "Dodaj udeležence"
   :remove-participants                   "Odstrani udeležence"

   ;protocol
   :received-invitation                   "je prejel/-a povabilo za klepet"
   :removed-from-chat                     "te je odstranil/-a iz skupinskega klepeta"
   :left                                  "preostane"
   :invited                               "povabil/-a"
   :removed                               "odstranil/-a"
   :You                                   "Ti"

   ;new-contact
   :add-new-contact                       "Dodaj nov stik"
   :import-qr                             "Uvozi"
   :scan-qr                               "Skeniraj QR"
   :name                                  "Ime"
   :whisper-identity                      "Skrivna identiteta"
   :address-explication                   "Sem morda sodi besedilo, ki razlaga, kaj je naslov ter kje ga najti"
   :enter-valid-address                   "Prosimo, vnesi veljaven naslov ali skeniraj QR kodo"
   :contact-already-added                 "Stik je bil že dodan"
   :can-not-add-yourself                  "Sebe ni mogoče dodati"
   :unknown-address                       "Neznan naslov"


   ;login
   :connect                               "Poveži"
   :address                               "Naslov"
   :password                              "Geslo"
   :login                                 "Prijava"
   :wrong-password                        "Napačno geslo"

   ;recover
   :recover-from-passphrase               "Povrni prek šifrirnega gesla"
   :recover-explain                       "Prosimo, vnesi šifrirno geslo svojega gesla za povrnitev dostopa"
   :passphrase                            "Šifrirno geslo"
   :recover                               "Povrni"
   :enter-valid-passphrase                "Prosimo, vnesi šifrirno geslo"
   :enter-valid-password                  "Prosimo, vnesi geslo"

   ;accounts
   :recover-access                        "Povrni dostop"
   :add-account                           "Dodaj račun"

   ;wallet-qr-code
   :done                                  "Končano"
   :main-wallet                           "Glavna denarnica"

   ;validation
   :invalid-phone                         "Neveljavna telefonska številka"
   :amount                                "Vsota"
   :not-enough-eth                        (str "Stanje ETH na računu je prenizko "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Potrdi transakcijo"
                                           :other "Potrdi {{count}} transakcij"
                                           :zero  "Ni transakcij"}
   :status                                "Status"
   :pending-confirmation                  "Potrditev v teku"
   :recipient                             "Prejemnik"
   :one-more-item                         "Še en predmet"
   :fee                                   "Plačilo"
   :value                                 "Vrednost"

   ;:webview
   :web-view-error                        "ups, napaka"

   :confirm                               "Potrdi"
   :phone-national                        "Državno"
   :transactions-confirmed                {:one   "Transakcija potrjena"
                                           :other "Št. potrjenih transakcij: {{count}}"
                                           :zero  "Ni potrjenih transakcij"}
   :public-group-topic                    "Tema"
   :debug-enabled                         "Strežnik za odpravljanje napak je bil zagnan! Sedaj lahko aplikacijo DApp dodate tako, da zaženete *status-dev-cli scan* na vašem računalniku"
   :new-public-group-chat                 "Pridruži se javnemu klepetu"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Prekliči"
   :share-qr                              "Deli QR kodo"
   :feedback                              "Imate povratne informacije?\nStresite vaš telefon!"
   :twelve-words-in-correct-order         "12 besed v pravilnem vrstnem redu"
   :remove-from-contacts                  "Odstrani iz stikov"
   :delete-chat                           "Izbriši klepet"
   :edit-chats                            "Uredi klepete"
   :sign-in                               "Prijava"
   :create-new-account                    "Ustvari nov račun"
   :sign-in-to-status                     "Prijava v Status"
   :got-it                                "Razumem"
   :move-to-internal-failure-message      "Nekatere pomembne datoteke moramo premakniti iz zunanjega na notranji pomnilnik. Za to potrebujemo vaše dovoljenje. V prihajajočih različicah ne bomo uporabljali zunanjega pomnilnika."
   :edit-group                            "Uredi skupino"
   :delete-group                          "Izbriši skupino"
   :browsing-title                        "Brskaj"
   :reorder-groups                        "Preuredi skupine"
   :browsing-cancel                       "Prekliči"
   :faucet-success                        "Zahteva za dovod je bila prejeta"
   :choose-from-contacts                  "Izberi iz stikov"
   :new-group                             "Nova skupina"
   :phone-e164                            "Mednarodno 1"
   :remove-from-group                     "Odstrani iz skupine"
   :search-contacts                       "Iskanje stikov"
   :transaction                           "Transakcija"
   :public-group-status                   "Javno"
   :leave-chat                            "Zapusti klepet"
   :start-conversation                    "Začni pogovor"
   :topic-format                          "Napačna oblika [a-z0-9\\-]+"
   :enter-valid-public-key                "Prosimo, vnesite veljavno javno kodo, ali skenirajte QR kodo"
   :faucet-error                          "Napaka v zahtevi za dovod"
   :phone-significant                     "Pomembno"
   :search-for                            "Išči..."
   :sharing-copy-to-clipboard             "Kopiraj v odložišče"
   :phone-international                   "Mednarodno 2"
   :enter-address                         "Vnesi naslov"
   :send-transaction                      "Pošlji transakcijo"
   :delete-contact                        "Izbriši stik"
   :mute-notifications                    "Utišaj obvestila"


   :contact-s                             {:one   "stik"
                                           :other "stiki"}
   :group-name                            "Ime skupine"
   :next                                  "Naprej"
   :from                                  "Od"
   :search-chats                          "Išči klepete"
   :in-contacts                           "Med stiki"

   :sharing-share                         "Deli..."
   :type-a-message                        "Napiši sporočilo..."
   :type-a-command                        "Začni pisati ukaz..."
   :shake-your-phone                      "Ste naleteli na napako ali imate predlog? Preprosto ~stresite~ vaš telefon!"
   :status-prompt                         "Ustvarite status, ki bo drugim nudil več informacij o stvareh, ki jih nudite. Uporabljate lahko tudi #hashtage."
   :add-a-status                          "Dodaj status..."
   :error                                 "Napaka"
   :edit-contacts                         "Urejanje stikov"
   :more                                  "več"
   :cancel                                "Prekliči"
   :no-statuses-found                     "Ni statusov"
   :swow-qr                               "Prikaži QR kodo"
   :browsing-open-in-web-browser          "Odpri v spletnem brskalniku"
   :delete-group-prompt                   "To ne bo vplivalo na stike"
   :edit-profile                          "Uredi profil"


   :enter-password-transactions           {:one   "Potrdite transakcijo z vnosom vašega gesla"
                                           :other "Potrdite transakcije z vnosom vašega gesla"}
   :unsigned-transactions                 "Nepodpisane transakcije"
   :empty-topic                           "Prazna tema"
   :to                                    "Za"
   :group-members                         "Člani skupine"
   :estimated-fee                         "Predvideno plačilo"
   :data                                  "Podatki"})

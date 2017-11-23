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
   :datetime-ago                          "nazaj"
   :datetime-yesterday                    "včeraj"
   :datetime-today                        "danes"

   ;profile
   :profile                               "Profil"
   :message                               "Sporočilo"
   :not-specified                         "Ni navedeno"
   :public-key                            "Javni ključ"
   :phone-number                          "Telefonska številka"
   :add-to-contacts                       "Dodaj med stike"

   ;;make_photo
   :image-source-title                    "Fotografija profila"
   :image-source-make-photo               "Zajemi"
   :image-source-gallery                  "Izberi iz galerije"

   ;sign-up
   :contacts-syncronized                  "Tvoji stiki so bili sinhronizirani"
   :confirmation-code                     (str "Hvala! Poslali smo ti sporočilo s potrditveno "
                                               "kodo. Prosimo, vnesi to kodo in potrdi svojo telefonsko številko")
   :incorrect-code                        (str "Koda na žalost ni bila pravilna, prosimo, da jo ponovno vneseš")
   :phew-here-is-your-passphrase          "*Pfuu* to pa je bilo težko, tukaj je tvoje šifrirno geslo, *zapiši si ga in shrani na varno mesto!* Potreboval/-a ga boš, da obnoviš svoj račun."
   :here-is-your-passphrase               "Tukaj je tvoje geslo, *zapiši si ga in shrani na varno mesto!* Potreboval/-a ga boš, da obnoviš svoj račun."
   :phone-number-required                 "Pritisni tukaj in vnesi svojo telefonsko številko ter poiskal bom tvoje prijatelje"
   :intro-status                          "Klepetaj z mano in nastavi svoj račun ter spremeni svoje nastavitve!"
   :intro-message1                        "Dobrodošel/-la v status\nPritisni to sporočilo in nastavi svoje geslo ter začni!"
   :account-generation-message            "Počakaj sekundo, opraviti moram noro računico, da ustvarim tvoj račun!"

   ;chats
   :chats                                 "Klepeti"
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
   :contacts-group-new-chat               "Začni nov klepet"
   :no-contacts                           "Zaenkrat še ni stikov"
   :show-qr                               "Prikaži QR"

   ;group-settings
   :remove                                "Odstrani"
   :save                                  "Shrani"
   :clear-history                         "Počisti zgodovino"
   :chat-settings                         "Nastavitve klepeta"
   :edit                                  "Uredi"
   :add-members                           "Dodaj člane"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "je prejel/-a povabilo za klepet"
   :removed-from-chat                     "te je odstranil/-a iz skupinskega klepeta"
   :left                                  "preostane"
   :invited                               "povabil/-a"
   :removed                               "odstranil/-a"
   :You                                   "Ti"

   ;new-contact
   :add-new-contact                       "Dodaj nov stik"
   :scan-qr                               "Skeniraj QR"
   :name                                  "Ime"
   :address-explication                   "Sem morda sodi besedilo, ki razlaga, kaj je naslov ter kje ga najti"
   :contact-already-added                 "Stik je bil že dodan"
   :can-not-add-yourself                  "Sebe ni mogoče dodati"
   :unknown-address                       "Neznan naslov"


   ;login
   :connect                               "Poveži"
   :address                               "Naslov"
   :password                              "Geslo"
   :wrong-password                        "Napačno geslo"

   ;recover
   :passphrase                            "Šifrirno geslo"
   :recover                               "Povrni"

   ;accounts
   :recover-access                        "Povrni dostop"

   ;wallet-qr-code
   :done                                  "Končano"
   :main-wallet                           "Glavna denarnica"

   ;validation
   :invalid-phone                         "Neveljavna telefonska številka"
   :amount                                "Vsota"
   ;transactions
   :status                                "Status"
   :recipient                             "Prejemnik"

   ;:webview
   :web-view-error                        "ups, napaka"

   :confirm                               "Potrdi"
   :phone-national                        "Državno"
   :public-group-topic                    "Tema"
   :debug-enabled                         "Strežnik za odpravljanje napak je bil zagnan! Sedaj lahko aplikacijo DApp dodate tako, da zaženete *status-dev-cli scan* na vašem računalniku"
   :new-public-group-chat                 "Pridruži se javnemu klepetu"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Prekliči"
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
   :browsing-open-in-web-browser          "Odpri v spletnem brskalniku"
   :delete-group-prompt                   "To ne bo vplivalo na stike"
   :edit-profile                          "Uredi profil"


   :empty-topic                           "Prazna tema"
   :to                                    "Za"
   :data                                  "Podatki"})

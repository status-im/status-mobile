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
   :discover                             "Odkrivanje"
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
   :command-text-location                 "Lokacija: {{address}}"
   :command-text-browse                   "Brskanje po spletni strani: {{webpage}}"
   :command-text-send                     "Transakcija: {{amount}} ETH"
   :command-text-help                     "Pomoč"

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
   :web-view-error                        "ups, napaka"})

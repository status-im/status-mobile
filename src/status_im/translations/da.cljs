(ns status-im.translations.da)

(def translations
  {
   ;common
   :members-title                         "Medlemmere"
   :not-implemented                       "Ikke implementeret!"
   :chat-name                             "Chatnavn"
   :notifications-title                   "Notifikationer"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Inviter venner"
   :faq                                   "FAQ"
   :switch-users                          "Skift brugere"

   ;chat
   :is-typing                             "skriver"
   :and-you                               "og du"
   :search-chat                           "Søg i chat"
   :members                               {:one   "1 medlem"
                                           :other "{{count}} medlemmer"
                                           :zero  "ingen medlemmer"}
   :members-active                        {:one   "1 medlem, 1 aktiv"
                                           :other "{{count}} medlemmer, {{count}} aktive"
                                           :zero  "ingen medlemmer"}
   :active-online                         "Online"
   :active-unknown                        "Ukendt"
   :available                             "Tilgængelig"
   :no-messages                           "Ingen beskeder"
   :suggestions-requests                  "Anmodninger"
   :suggestions-commands                  "kommandoer"

   ;sync
   :sync-in-progress                      "Synkroniserer..."
   :sync-synced                           "Synkroniseret"

   ;messages
   :status-sending                        "Sender"
   :status-pending                        "Under behandling"
   :status-sent                           "Sendt"
   :status-seen-by-everyone               "Set af alle"
   :status-seen                           "Set"
   :status-delivered                      "Leveret"
   :status-failed                         "Mislykket"

   ;datetime
   :datetime-second                       {:one   "sekund"
                                           :other "sekunder"}
   :datetime-minute                       {:one   "minut"
                                           :other "minutter"}
   :datetime-hour                         {:one   "time"
                                           :other "timer"}
   :datetime-day                          {:one   "dag"
                                           :other "dage"}
   :datetime-multiple                     "s"
   :datetime-ago                          "siden"
   :datetime-yesterday                    "i går"
   :datetime-today                        "i dag"

   ;profile
   :profile                               "Profil"
   :report-user                           "Anmeld bruger"
   :message                               "Meddelelser"
   :username                              "Brugrenavn"
   :not-specified                         "Ikke angivet"
   :public-key                            "Offentlig nøgle"
   :phone-number                          "Telefonnummer"
   :email                                 "Email"
   :profile-no-status                     "Ingen status"
   :add-to-contacts                       "Tilføg til kontakter"
   :error-incorrect-name                  "Fejl: Forkert navn"
   :error-incorrect-email                 "Fejl: Forkert email"

   ;;make_photo
   :image-source-title                    "Profilbillede"
   :image-source-make-photo               "Tag billede"
   :image-source-gallery                  "Vælg fra galleri"
   :image-source-cancel                   "Afbryd"

   ;sign-up
   :contacts-syncronized                  "Dine kontakter er synkroniseret"
   :confirmation-code                     (str "Tak! Vi har sendt dig en sms med en bekræftelseskode. "
                                               "Vær venlig at indtaste koden for at bekræfte dit telefonnummer")
   :incorrect-code                        (str "Koden var desværre forkert. Prøv venligst igen")
   :generate-passphrase                   (str "Jeg vil generere en kodesætning (passphrase) til dig, så du kan gendanne din adgang eller logge ind fra en anden enhed")
   :phew-here-is-your-passphrase          "*Pust* det var svært, her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du behøver den for at gendanne din konto."
   :here-is-your-passphrase               "Her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du behøver den for at gendanne din konto."
   :written-down                          "Sørg for at du har skrevet den sikkert ned"
   :phone-number-required                 "Klik her for at indtaste dit telefonnummer, så vil jeg finde dine venner"
   :intro-status                          "Chat med mig for at opsætte din konto og ændre dine indstillinger!!"
   :intro-message1                        "Velkommen til Status\n Tryk på denne meddelelse for at angive din adgangskode og komme i gang!"
   :account-generation-message            "Vent et øjeblik, jeg er nødt til at gøre nogle skøre beregninger for at generere din konto!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Ny chat"
   :new-group-chat                        "Ny gruppchat"

   ;discover
   :discover                              "Opdag"
   :none                                  "Ingen"
   :search-tags                           "Skrive de tags du vil søge efter her"
   :popular-tags                          "Populære tags"
   :recent                                "Seneste"
   :no-statuses-discovered                "Ingen statusser opdaget"

   ;settings
   :settings                              "Indstillinger"

   ;contacts
   :contacts                              "Kontakter"
   :new-contact                           "Ny kontakt"
   :show-all                              "Vis alle"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mennesker"
   :contacts-group-new-chat               "Start en ny chat"
   :no-contacts                           "Ingen kontakter endnu"
   :show-qr                               "Vis QR-kode"

   ;group-settings
   :remove                                "Fjern"
   :save                                  "Gem"
   :change-color                          "Ændre farve"
   :clear-history                         "Fjern historik"
   :delete-and-leave                      "Slet og forlad"
   :chat-settings                         "Chatindstillinger"
   :edit                                  "Rediger"
   :add-members                           "Tilføj medlemmere"
   :blue                                  "Blå"
   :purple                                "Lilla"
   :green                                 "Grøn"
   :red                                   "Rød"

   ;commands
   :money-command-description             "Send penge"
   :location-command-description          "Send placering"
   :phone-command-description             "Send telefonnummer"
   :phone-request-text                    "Telefonnummeranmodning"
   :confirmation-code-command-description "Send bekræftelseskode"
   :confirmation-code-request-text        "Bekræftelseskodeanmodning"
   :send-command-description              "Send placering"
   :request-command-description           "Send anmodning"
   :keypair-password-command-description  ""
   :help-command-description              "Hjælp"
   :request                               "Anmod"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH til {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH fra {{chat-name}}"

   ;new-group
   :group-chat-name                       "Gruppechat navn"
   :empty-group-chat-name                 "Angiv et navn"
   :illegal-group-chat-name               "Vælg venligst et andet navn"

   ;participants
   :add-participants                      "Tilføj deltagere"
   :remove-participants                   "Fjern deltagere"

   ;protocol
   :received-invitation                   "Accepterede chatinvitaion"
   :removed-from-chat                     "Fjernede dig fra gruppechatten"
   :left                                  "Forlod"
   :invited                               "Inviteret"
   :removed                               "Fjernet"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Tilføj ny kontakt"
   :import-qr                             "Importer QR-kode"
   :scan-qr                               "Skan QR-kode"
   :name                                  "Navn"
   :whisper-identity                      "Whisper identitet"
   :address-explication                   ""
   :enter-valid-address                   "Indtast en gyldig adresse eller scan en QR-kode"
   :contact-already-added                 "Kontakten er allerede tilføjet"
   :can-not-add-yourself                  "Du kan ikke tilføje dig selv"
   :unknown-address                       "Ukendt adresse"


   ;login
   :connect                               "Tilslut"
   :address                               "adresse"
   :password                              "Kodeord"
   :login                                 "Login"
   :wrong-password                        "Forkert kodeord"

   ;recover
   :recover-from-passphrase               "Gendan fra kodesætning (passphrase)"
   :recover-explain                       "Indtast kodesætningen (passphrase) til dit kodeord for at genoprette adgangen"
   :passphrase                            "Kodesætning (passphrase)"
   :recover                               "Gendan"
   :enter-valid-passphrase                "Angiv korrekt kodesætning (passphrase)"
   :enter-valid-password                  "Angiv korrekt kodeord"

   ;accounts
   :recover-access                        "Gendan adgang"
   :add-account                           "Tilføj konto"

   ;wallet-qr-code
   :done                                  "Klar"
   :main-wallet                           "Hovedpung"

   ;validation
   :invalid-phone                         "Ugyldigt telefonnummer"
   :amount                                "Beløb"
   :not-enough-eth                        (str "'Ikke tilstrækkeligt ETH på kontoen "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Bekræft transaktionen"
                                           :other "Bekræft {{count}} transaktioner"
                                           :zero  "Ingen transaktioner"}
   :status                                "Status"
   :pending-confirmation                  "Venter på bekræftelse"
   :recipient                             "Modtager"
   :one-more-item                         "En ting til"
   :fee                                   "Gebyr"
   :value                                 "Værdi"

   ;:webview
   :web-view-error                        "hovsa, fejl"})

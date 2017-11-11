(ns status-im.translations.da)

(def translations
  {
   ;common
   :members-title                         "Medlemmere"
   :not-implemented                       "Ikke implementeret!"
   :chat-name                             "Chatnavn"
   :notifications-title                   "Notifikationer"
   :offline                               "Offline"
   :search-for                            "Søg efter..."
   :cancel                                "Annullér"
   :next                                  "Næste"
   :type-a-message                        "Skriv en besked..."
   :type-a-command                        "Skriv en kommando..."
   :error                                 "Fejl"
   
   :camera-access-error                   "Gå venligst til dine systemindstillinger og sørg  for at du Status > Kamera er tilladt."
   :photos-access-error                   "Gå venligst til dine systemindstillinger og sørg  for at du Status > Billeder er tilladt."

   ;drawer
   :invite-friends                        "Inviter venner"
   :faq                                   "FAQ"
   :switch-users                          "Skift bruger"
   :feedback                              "Ryst din telefon for at give os feedback!"
   :view-all                              "Vis alle"
   :current-network                       "Nuværende netværk"

   ;chat
   :is-typing                             "skriver"
   :and-you                               "og du"
   :search-chat                           "Søg i chat"
   :members                               {:one   "1 medlem"
                                           :other "{{count}} medlemmer"
                                           :zero  "ingen medlemmer"}
   :members-active                        {:one   "1 medlem"
                                           :other "{{count}} medlemmer"
                                           :zero  "ingen medlemmer"}
   :public-group-status                   "Offentlig"
   :active-online                         "Online"
   :active-unknown                        "Ukendt"
   :available                             "Aktiv"
   :no-messages                           "Ingen beskeder"
   :suggestions-requests                  "Anmodninger"
   :suggestions-commands                  "Kommandoer"
   :faucet-success                        "Faucet request er modtaget"
   :faucet-error                          "Faucet request fejl"

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
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
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
   :edit-profile                          "Rediger profil"
   :report-user                           "Anmeld bruger"
   :message                               "Meddelelser"
   :username                              "Brugrenavn"
   :not-specified                         "Ikke angivet"
   :public-key                            "Offentlig nøgle"
   :phone-number                          "Telefonnummer"
   :email                                 "Email"
   :update-status                         "Opdater din status..."
   :add-a-status                          "Tilføj en status..."
   :status-prompt                         "Lav en status for at lade folk vide hvad du tilbyder. Du kan også bruge #hashtags."
   :profile-no-status                     "Ingen status"
   :add-to-contacts                       "Tilføj til kontakter"
   :in-contacts                           "I kontakter"
   :remove-from-contacts                  "Fjern fra kontakter"
   :start-conversation                    "Begynd samtale"
   :send-transaction                      "Send transaktion"
   :share-qr                              "Del QR-kode"
   :error-incorrect-name                  "Fejl: Forkert navn"
   :error-incorrect-email                 "Fejl: Forkert email"

   ;;make_photo
   :image-source-title                    "Profilbillede"
   :image-source-make-photo               "Tag billede"
   :image-source-gallery                  "Vælg fra galleri"
   :image-source-cancel                   "Afbryd"
   
   ;;sharing
   :sharing-copy-to-clipboard             "Kopier til udklipsholderen"
   :sharing-share                         "Del..."
   :sharing-cancel                        "Annullér"

   :browsing-title                        "Browse"
   :browsing-open-in-web-browser          "Åben i web browser"
   :browsing-cancel                       "Annullér"

   ;sign-up
   :contacts-syncronized                  "Dine kontakter er synkroniseret"
   :confirmation-code                     (str "Tak! Vi har sendt dig en sms med en bekræftelseskode. "
                                               "Vær venlig at indtaste koden for at bekræfte dit telefonnummer")
   :incorrect-code                        (str "Koden var desværre forkert. Prøv venligst igen")
   :generate-passphrase                   (str "Jeg vil generere en kodesætning (passphrase) til dig, så du kan gendanne din adgang eller logge ind fra en anden enhed")
   :phew-here-is-your-passphrase          "*Puha* det var hårdt, her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du skal bruge den for at kunne gendanne din konto."
   :here-is-your-passphrase               "Her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du behøver den for at gendanne din konto."
   :written-down                          "Sørg for at du har skrevet den sikkert ned"
   :phone-number-required                 "Klik her for at indtaste dit telefonnummer, så vil jeg finde dine venner"
   :shake-your-phone                      "Fundet en bug eller har du et foreslag. Blot ryst din telefon!"
   :intro-status                          "Chat med mig for at opsætte din konto og ændre dine indstillinger!!"
   :intro-message1                        "Velkommen til Status\n Tryk på denne meddelelse for at angive din adgangskode og komme i gang!"
   :account-generation-message            "Vent et øjeblik, jeg er nødt til at gøre nogle skøre beregninger for at generere din konto!"
   :move-to-internal-failure-message      "Vi skal flyyte nogle vigtige filer fra din eksterne til din interne hukommelse. For at gøre dette har vi brug for din tilladelse. Vi vil ikke bruge din eksterne hukkomelse i kommende versioner."
   :debug-enabled                         "Debug server has been launched! You can now execute *status-dev-cli scan* to find the server from your computer on the same network."

   ;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significant"

   ;chats
   :chats                                 "Samtaler"
   :new-chat                              "Ny samtale"
   :delete-chat                           "Fjern samtale"
   :new-group-chat                        "Ny gruppsamtale"
   :new-public-group-chat                  "Deltag i offentlig samtale"
   :edit-chats                            "Rediger samtaler"
   :search-chats                          "Søg samtaler"
   :empty-topic                           "Tomt emne"
   :topic-format                          "Forkert format [a-z0-9\\-]+"
   :public-group-topic                    "Emne"

   ;discover
   :discover                              "Opdag"
   :none                                  "Ingen"
   :search-tags                           "Skrive de tags du vil søge efter her"
   :popular-tags                          "Populære tags"
   :recent                                "Seneste"
   :no-statuses-discovered                "Ingen statusser opdaget"
   :no-statuses-found                     "Inger statusser fundet"

   ;settings
   :settings                              "Indstillinger"

   ;contacts
   :contacts                              "Kontakter"
   :new-contact                           "Ny kontakt"
   :delete-contact                        "Fjern kontakt"
   :delete-contact-confirmation           "Denne kontakt vil blive fjernet fra dine kontakter"
   :remove-from-group                     "Fjern fra gruppe"
   :edit-contacts                         "Rediger kontakter"
   :search-contacts                       "Søg efter kontakter"
   :show-all                              "Vis alle"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mennesker"
   :contacts-group-new-chat               "Start en ny samtale"
   :choose-from-contacts                  "Vælg fra kontakter"
   :no-contacts                           "Ingen kontakter endnu"
   :show-qr                               "Vis QR-kode"
   :enter-address                         "Indtast adresse"
   :more                                  "mere"

   ;group-settings
   :remove                                "Fjern"
   :save                                  "Gem"
   :delete                                "Slet"
   :change-color                          "Ændre farve"
   :clear-history                         "Fjern historik"
   :mute-notifications                    "Sluk for notifikationer"
   :leave-chat                            "Forlad samtale"
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
   :new-group                             "Ny gruppe"
   :reorder-groups                        "Arranger grupper"
   :group-name                            "Gruppenavn"
   :edit-group                            "Rediger gruppe"
   :delete-group                          "Slet gruppe"
   :delete-group-confirmation             "Denne gruppe vil blivve fjernet fra dine grupper. Dette vil ikke påvirke dine kontakter."
   :delete-group-prompt                   "Dette vil ikke påvirke dine kontakter"
   :group-members                         "Gruppemedlemmer"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakter"}
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
   :swow-qr                               "Vis QR"
   :name                                  "Navn"
   :whisper-identity                      "Whisper identitet"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "Indtast venligst en gyldig adresse eller scan en QR-kode"
   :enter-valid-public-key                "Indtast venligst en gyldig offentlig nøgle eller scan en QR-kode"
   :contact-already-added                 "Kontakten er allerede tilføjet"
   :can-not-add-yourself                  "Du kan ikke tilføje dig selv fjolle"
   :unknown-address                       "Ukendt adresse"


   ;login
   :connect                               "Tilslut"
   :address                               "Adresse"
   :password                              "Kodeord"
   :login                                 "Login"
   :sign-in-to-status                     "Log på Status"
   :sign-in                               "Log på"
   :wrong-password                        "Forkert kodeord"

   ;recover
   :recover-from-passphrase               "Gendan fra kodesætning (passphrase)"
   :recover-explain                       "Indtast kodesætningen (passphrase) til dit kodeord for at genoprette adgangen"
   :passphrase                            "Kodesætning (passphrase)"
   :recover                               "Gendan"
   :enter-valid-passphrase                "Angiv korrekt kodesætning (passphrase)"
   :enter-valid-password                  "Angiv korrekt kodeord"
   :twelve-words-in-correct-order         "12 ord i den korrekte rækkefølge"

   ;accounts
   :recover-access                        "Gendan adgang"
   :add-account                           "Tilføj konto"
   :create-new-account                    "Opret en ny konto"

   ;wallet-qr-code
   :done                                  "Klar"
   :main-wallet                           "Hovedpung"

   ;validation
   :invalid-phone                         "Ugyldigt telefonnummer"
   :amount                                "Beløb"
   :not-enough-eth                        (str "'Ikke tilstrækkeligt ETH på kontoen "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Bekræft"
   :confirm-transactions                  {:one   "Bekræft transaktionen"
                                           :other "Bekræft {{count}} transaktioner"
                                           :zero  "Ingen transaktioner"}
   :transactions-confirmed                {:one   "Transaktionen bekræftet"
                                           :other "{{count}} transaktioner bekræftet"
                                           :zero  "Ingen transaktioner bekræftet"}
   :transaction                           "Transaktion"
   :unsigned-transactions                 "Usignerede transaktioner"
   :no-unsigned-transactions              "Ingen usignerede transaktioner"
   :enter-password-transactions           {:one   "Bekræft transaktionen ved at indtaste dit kodeord"
                                           :other "Bekræft transaktionerne ved at indtaste dit kodeord"}                                        
   :status                                "Status"
   :pending-confirmation                  "Venter på bekræftelse"
   :recipient                             "Modtager"
   :one-more-item                         "En ting til"
   :fee                                   "Gebyr"
   :estimated-fee                         "Forventet gebyr"
   :value                                 "Værdi"
   :to                                    "Til"
   :from                                  "Fra"
   :data                                  "Data"
   :got-it                                "Forstået"
   :contract-creation                     "Kontraktoprettelse"

   ;:webview
   :web-view-error                        "hovsa, fejl"})

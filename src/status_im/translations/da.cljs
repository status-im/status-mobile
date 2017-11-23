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
   :switch-users                          "Skift bruger"
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
   :datetime-ago                          "siden"
   :datetime-yesterday                    "i går"
   :datetime-today                        "i dag"

   ;profile
   :profile                               "Profil"
   :edit-profile                          "Rediger profil"
   :message                               "Meddelelser"
   :not-specified                         "Ikke angivet"
   :public-key                            "Offentlig nøgle"
   :phone-number                          "Telefonnummer"
   :update-status                         "Opdater din status..."
   :add-a-status                          "Tilføj en status..."
   :status-prompt                         "Lav en status for at lade folk vide hvad du tilbyder. Du kan også bruge #hashtags."
   :add-to-contacts                       "Tilføj til kontakter"
   :in-contacts                           "I kontakter"
   :remove-from-contacts                  "Fjern fra kontakter"
   :start-conversation                    "Begynd samtale"
   :send-transaction                      "Send transaktion"

   ;;make_photo
   :image-source-title                    "Profilbillede"
   :image-source-make-photo               "Tag billede"
   :image-source-gallery                  "Vælg fra galleri"
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
   :phew-here-is-your-passphrase          "*Puha* det var hårdt, her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du skal bruge den for at kunne gendanne din konto."
   :here-is-your-passphrase               "Her er din kodesætning (passphrase), *skriv den ned og gem den sikkert!* Du behøver den for at gendanne din konto."
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
   :clear-history                         "Fjern historik"
   :mute-notifications                    "Sluk for notifikationer"
   :leave-chat                            "Forlad samtale"
   :chat-settings                         "Chatindstillinger"
   :edit                                  "Rediger"
   :add-members                           "Tilføj medlemmere"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Ny gruppe"
   :reorder-groups                        "Arranger grupper"
   :edit-group                            "Rediger gruppe"
   :delete-group                          "Slet gruppe"
   :delete-group-confirmation             "Denne gruppe vil blivve fjernet fra dine grupper. Dette vil ikke påvirke dine kontakter."
   :delete-group-prompt                   "Dette vil ikke påvirke dine kontakter"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakter"}
   ;participants

   ;protocol
   :received-invitation                   "Accepterede chatinvitaion"
   :removed-from-chat                     "Fjernede dig fra gruppechatten"
   :left                                  "Forlod"
   :invited                               "Inviteret"
   :removed                               "Fjernet"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Tilføj ny kontakt"
   :scan-qr                               "Skan QR-kode"
   :name                                  "Navn"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-public-key                "Indtast venligst en gyldig offentlig nøgle eller scan en QR-kode"
   :contact-already-added                 "Kontakten er allerede tilføjet"
   :can-not-add-yourself                  "Du kan ikke tilføje dig selv fjolle"
   :unknown-address                       "Ukendt adresse"


   ;login
   :connect                               "Tilslut"
   :address                               "Adresse"
   :password                              "Kodeord"
   :sign-in-to-status                     "Log på Status"
   :sign-in                               "Log på"
   :wrong-password                        "Forkert kodeord"

   ;recover
   :passphrase                            "Kodesætning (passphrase)"
   :recover                               "Gendan"
   :twelve-words-in-correct-order         "12 ord i den korrekte rækkefølge"

   ;accounts
   :recover-access                        "Gendan adgang"
   :create-new-account                    "Opret en ny konto"

   ;wallet-qr-code
   :done                                  "Klar"
   :main-wallet                           "Hovedpung"

   ;validation
   :invalid-phone                         "Ugyldigt telefonnummer"
   :amount                                "Beløb"
   ;transactions
   :confirm                               "Bekræft"
   :transaction                           "Transaktion"
   :status                                "Status"
   :recipient                             "Modtager"
   :to                                    "Til"
   :from                                  "Fra"
   :data                                  "Data"
   :got-it                                "Forstået"

   ;:webview
   :web-view-error                        "hovsa, fejl"})

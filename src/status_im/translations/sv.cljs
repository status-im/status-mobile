(ns status-im.translations.sv)

(def translations
  {
   ;common
   :members-title                         "Medlemmar"
   :not-implemented                       "!inte implementerad"
   :chat-name                             "Chattnamn"
   :notifications-title                   "Aviseringar och ljud"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Bjud in vänner"
   :faq                                   "FAQ"
   :switch-users                          "Byt användare"

   ;chat
   :is-typing                             "skriver"
   :and-you                               "och du"
   :search-chat                           "Sök chatt"
   :members                               {:one   "1 medlem"
                                           :other "{{count}} medlemmar"
                                           :zero  "inga medlemmar"}
   :members-active                        {:one   "1 medlem, 1 aktiv"
                                           :other "{{count}} medlemmar, {{count}} aktiva"
                                           :zero  "inga medlemmar"}
   :active-online                         "Online"
   :active-unknown                        "Okänd"
   :available                             "Tillgänglig"
   :no-messages                           "Inga meddelanden"
   :suggestions-requests                  "Begäranden"
   :suggestions-commands                  "Kommandon"

   ;sync
   :sync-in-progress                      "Synkroniserar..."
   :sync-synced                           "Synkroniserad"

   ;messages
   :status-sending                        "Skickar"
   :status-pending                        "Avvaktan"
   :status-sent                           "Skickad"
   :status-seen-by-everyone               "Sedd av alla"
   :status-seen                           "Sedd"
   :status-delivered                      "Levererad"
   :status-failed                         "Misslyckad"

   ;datetime
   :datetime-second                       {:one   "sekund"
                                           :other "sekunder"}
   :datetime-minute                       {:one   "minut"
                                           :other "minuter"}
   :datetime-hour                         {:one   "timma"
                                           :other "timmar"}
   :datetime-day                          {:one   "dag"
                                           :other "dagar"}
   :datetime-multiple                     "s"
   :datetime-ago                          "sedan"
   :datetime-yesterday                    "igår"
   :datetime-today                        "idag"

   ;profile
   :profile                               "Profil"
   :report-user                           "RAPPORTERA ANVÄNDARE"
   :message                               "Meddelande"
   :username                              "Användarnamn"
   :not-specified                         "Inte angivet"
   :public-key                            "Offentlig nyckel"
   :phone-number                          "Telefonnummer"
   :email                                 "E-post"
   :profile-no-status                     "Ingen status"
   :add-to-contacts                       "Lägg till i kontakter"
   :error-incorrect-name                  "Var god välj ett annat namn"
   :error-incorrect-email                 "Inkorrekt e-post"

   ;;make_photo
   :image-source-title                    "Profilbild"
   :image-source-make-photo               "Fånga"
   :image-source-gallery                  "Välj från galleri"
   :image-source-cancel                   "Avbryt"

   ;sign-up
   :contacts-syncronized                  "Dina kontakter har synkroniserats"
   :confirmation-code                     (str "Tack! Vi har skickat dig ett textmeddelande med en bekräftelse "
                                               "kod. Var god ange den koden för att bekräfta ditt telefonnummer")
   :incorrect-code                        (str "Tyvärr var koden felaktig, var god ange den igen")
   :generate-passphrase                   (str "Jag kommer att generera en lösenordsfras för dig så att du kan återställa din "
                                               "åtkomst eller logga in från en annan enhet")
   :phew-here-is-your-passphrase          "*Pust* det var svårt, här är din lösenordsfras, *skriv ner det här och förvara det säkert!* Du kommer att behöva det för att återställa ditt konto."
   :here-is-your-passphrase               "Här är din lösenordsfras, *skriv ner det här och förvara det säkert!* Du kommer att behöva det för att återställa ditt konto."
   :written-down                          "Se till att du hade skrivit ner det säkert"
   :phone-number-required                 "Tryck här för att ange ditt telefonnummer och jag kommer att hitta dina vänner"
   :intro-status                          "Chatta med mig för att konfigurera ditt konto och ändra dina inställningar!"
   :intro-message1                        "Välkommen till Status\nTryck på detta meddelande för att ställa in ditt lösenord och komma igång!"
   :account-generation-message            "Vänta ett ögonblick, jag måste göra lite galna beräkningar för att generera ditt konto!"

   ;chats
   :chats                                 "Chattar"
   :new-chat                              "Ny chatt"
   :new-group-chat                        "Ny gruppchatt"

   ;discover
   :discover                             "Upptäckt"
   :none                                  "Inga"
   :search-tags                           "Skriv dina söktaggar här"
   :popular-tags                          "Populära taggar"
   :recent                                "Senaste"
   :no-statuses-discovered                "Inga statusar upptäckta"

   ;settings
   :settings                              "Inställningar"

   ;contacts
   :contacts                              "Kontakter"
   :new-contact                           "Ny kontakt"
   :show-all                              "VISA ALLA"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Människor"
   :contacts-group-new-chat               "Starta ny chatt"
   :no-contacts                           "Inga kontakter ännu"
   :show-qr                               "Visa QR"

   ;group-settings
   :remove                                "Ta bort"
   :save                                  "Spara"
   :change-color                          "Ändra färg"
   :clear-history                         "Rensa historik"
   :delete-and-leave                      "Radera och lämna"
   :chat-settings                         "Chattinställningar"
   :edit                                  "Redigera"
   :add-members                           "Lägg till medlemmar"
   :blue                                  "Blå"
   :purple                                "Lila"
   :green                                 "Grön"
   :red                                   "Röd"

   ;commands
   :money-command-description             "Skicka pengar"
   :location-command-description          "Skicka plats"
   :phone-command-description             "Skicka telefonnummer"
   :phone-request-text                    "Telefonnummerbegäran"
   :confirmation-code-command-description "Skicka bekräftelsekod"
   :confirmation-code-request-text        "Bekräftelsekodbegäran"
   :send-command-description              "Skicka plats"
   :request-command-description           "Skicka begäran"
   :keypair-password-command-description  ""
   :help-command-description              "Hjälp"
   :request                               "Begäran"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH till {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH från {{chat-name}}"
   :command-text-location                 "Plats: {{address}}"
   :command-text-browse                   "Läser webbsidan: {{webpage}}"
   :command-text-send                     "Transaktion: {{amount}} ETH"
   :command-text-help                     "Hjälp"

   ;new-group
   :group-chat-name                       "Chattnamn"
   :empty-group-chat-name                 "Var god ange ett namn"
   :illegal-group-chat-name               "Var god välj ett annat namn"

   ;participants
   :add-participants                      "Lägg till Deltagare"
   :remove-participants                   "Ta bort Deltagare"

   ;protocol
   :received-invitation                   "tog emot chattinbjudan"
   :removed-from-chat                     "tog bort dig från gruppchatten"
   :left                                  "lämnade"
   :invited                               "inbjudna"
   :removed                               "borttagna"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Lägg till ny kontakt"
   :import-qr                             "Importera"
   :scan-qr                               "Skanna QR"
   :name                                  "Namn"
   :whisper-identity                      "Viskningsidentitet"
   :address-explication                   "Kanske borde det finnas lite text som förklarar vad en adress är och var man hittar den"
   :enter-valid-address                   "Var god ange en giltig adress eller skanna en QR-kod"
   :contact-already-added                 "Kontakten har redan lagts till"
   :can-not-add-yourself                  "Du kan inte lägga till dig själv"
   :unknown-address                       "Okänd adress"


   ;login
   :connect                               "Anslut"
   :address                               "Adress"
   :password                              "Lösenord"
   :login                                 "Inloggning"
   :wrong-password                        "Fel lösenord"

   ;recover
   :recover-from-passphrase               "Återställ från lösenordsfras"
   :recover-explain                       "Var god ange lösenordsfrasen för ditt lösenord för att återställa åtkomsten"
   :passphrase                            "Lösenordsfras"
   :recover                               "Återställ"
   :enter-valid-passphrase                "Var god ange en lösenordsfras"
   :enter-valid-password                  "Var god ange ett lösenord"

   ;accounts
   :recover-access                        "Återställ åtkomst"
   :add-account                           "Lägg till konto"

   ;wallet-qr-code
   :done                                  "Klar"
   :main-wallet                           "Huvudplånbok"

   ;validation
   :invalid-phone                         "Ogiltigt telefonnummer"
   :amount                                "Belopp"
   :not-enough-eth                        (str "'Inte tillräcklig ETH på balansen "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Bekräfta transaktion"
                                           :other "Bekräfta {{count}} transaktioner"
                                           :zero  "Inga transaktioner"}
   :status                                "Status"
   :pending-confirmation                  "I väntan på bekräftelse"
   :recipient                             "Mottagare"
   :one-more-item                         "En artikel till"
   :fee                                   "Avgift"
   :value                                 "Värde"

   ;:webview
   :web-view-error                        "hoppsan, fel"})

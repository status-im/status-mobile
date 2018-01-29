(ns status-im.translations.sv)

(def translations
  {
   ;;common
   :members-title                         "Medlemmar"
   :not-implemented                       "!inte implementerad"
   :chat-name                             "Chattnamn"
   :notifications-title                   "Aviseringar och ljud"
   :offline                               "Offline"
   :search-for                            "Sök efter..."
   :cancel                                "Avbryt"
   :next                                  "Nästa"
   :open                                  "Öppna"
   :description                           "Beskrivning"
   :url                                   "URL"
   :type-a-message                        "Skriv ett meddelande..."
   :type-a-command                        "Skriv ett kommando..."
   :error                                 "Ett fel uppstod"
   :unknown-status-go-error               "Okänt status-go fel"
   :node-unavailable                      "Ingen ethereum-nod är igång"
   :yes                                   "Ja"
   :no                                    "Nej"

   :camera-access-error                   "För att bevilja nödvändinga kamerarättigheter, gå till systeminställningarna och kontrollera att Status > Kamera är vald."
   :photos-access-error                   "För att bevilja nödvändinga fotorättigheter,  gå till systeminställiningarna och kontrollera att Status > Foton är vald."

   ;;drawer
   :switch-users                          "Byt användare"
   :current-network                       "Nuvarande nätverk"

   ;;chat
   :is-typing                             "skriver"
   :and-you                               "och du"
   :search-chat                           "Sök chatt"
   :members                               {:one   "1 medlem"
                                           :other "{{count}} medlemmar"
                                           :zero  "inga medlemmar"}
   :members-active                        {:one   "1 medlem"
                                           :other "{{count}} medlemmar"
                                           :zero  "inga medlemmar"}
   :public-group-status                   "Offentlig"
   :active-online                         "Online"
   :active-unknown                        "Okänd"
   :available                             "Tillgänglig"
   :no-messages                           "Inga meddelanden"
   :suggestions-requests                  "Begäranden"
   :suggestions-commands                  "Kommandon"
   :faucet-success                        "Faucet-begäran mottagen"
   :faucet-error                          "Felaktig faucet-egäran"

   ;;sync
   :sync-in-progress                      "Synkroniserar..."
   :sync-synced                           "Synkroniserad"

   ;;messages
   :status-sending                        "Skickar"
   :status-pending                        "Avvaktan"
   :status-sent                           "Skickad"
   :status-seen-by-everyone               "Sedd av alla"
   :status-seen                           "Sedd"
   :status-delivered                      "Levererad"
   :status-failed                         "Misslyckad"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "sekund"
                                           :other "sekunder"}
   :datetime-minute                       {:one   "minut"
                                           :other "minuter"}
   :datetime-hour                         {:one   "timma"
                                           :other "timmar"}
   :datetime-day                          {:one   "dag"
                                           :other "dagar"}
   :datetime-ago                          "sedan"
   :datetime-yesterday                    "igår"
   :datetime-today                        "idag"

   ;;profile
   :profile                               "Profil"
   :edit-profile                          "Redigera profil"
   :message                               "Meddelande"
   :not-specified                         "Inte angivet"
   :public-key                            "Offentlig nyckel"
   :phone-number                          "Telefonnummer"
   :update-status                         "Uppdatera din status..."
   :add-a-status                          "Lägg till status..."
   :status-prompt                         "Lägg till din status. Använd #hashtags för att göra det lättare för andra att hitta dig och tala om det som intresserar dig"
   :add-to-contacts                       "Lägg till i kontakter"
   :in-contacts                           "I kontakterna"
   :remove-from-contacts                  "Ta bort från kontakter"
   :start-conversation                    "Börja en konversation"
   :send-transaction                      "Sänd en transaktion"
   :testnet-text                          "Du är på {{testnet}} Testnet. Sänd inte riktiga ETH eller SNT till din address"
   :mainnet-text                          "Du är på Mainnet. Äkta ETH kommer att sändas"

   ;;make_photo
   :image-source-title                    "Profilbild"
   :image-source-make-photo               "Fånga"
   :image-source-gallery                  "Välj från galleri"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopiera till urklipp"
   :sharing-share                         "Dela..."
   :sharing-cancel                        "Avbryt"

   :browsing-title                        "Webbläsning"
   :browsing-open-in-web-browser          "Öppna in webbläsare"
   :browsing-cancel                       "Avbryt"

   ;;sign-up
   :contacts-syncronized                  "Dina kontakter har synkroniserats"
   :confirmation-code                     (str "Tack! Vi har skickat dig ett textmeddelande med en bekräftelse "
                                               "kod. Var god ange den koden för att bekräfta ditt telefonnummer")
   :incorrect-code                        (str "Tyvärr var koden felaktig, var god ange den igen")
   :phew-here-is-your-passphrase          "*Pust* det var svårt, här är din lösenordsfras, *skriv ner den och förvara den säkert!* Du kommer att behöva den för att återställa ditt konto."
   :here-is-your-passphrase               "Här är din lösenordsfras, *skriv ner den och förvara den säkert!* Du kommer att behöva den för att återställa ditt konto."
   :here-is-your-signing-phrase           "Här är din underskriftsfras. Du kommer att använde den för att verifiera dina transaktioner. *Skriv ner den och förvara den säkert!*"
   :phone-number-required                 "Tryck här för att ange ditt telefonnummer så jag kommer att hitta dina vänner"
   :shake-your-phone                      "Hittade du ett fel eller har du ett förbättringsförslag? ~Skaka~ bara din telefon!"
   :intro-status                          "Chatta med mig för att konfigurera ditt konto och ändra dina inställningar!"
   :intro-message1                        "Välkommen till Status\nTryck på detta meddelande för att ange ett lösenord och komma igång!"
   :account-generation-message            "Vänta ett ögonblick, jag måste göra lite galna beräkningar för att generera ditt konto!"
   :move-to-internal-failure-message      "Vi måste flytta vissa viktiga filer från extern lagring till intern lagring. För att göra detta behöver vi ditt tillstånd. Vi kommer inte längre att använda extern lagring i framtida versioner."
   :debug-enabled                         "Debugservern har startats! Du kan nu köra *status-dev-cli scan* för att hitta servern på samma nätverk som din dator."

   ;;phone types
   :phone-e164                            "Internationell 1"
   :phone-international                   "Internationell 2"
   :phone-national                        "Nationell"
   :phone-significant                     "Viktig"

   ;;chats
   :chats                                 "Chattar"
   :delete-chat                           "Ta bort chatt"
   :new-group-chat                        "Ny gruppchatt"
   :new-public-group-chat                 "Gå med i offentlig chatt"
   :edit-chats                            "Redigera chattar"
   :search-chats                          "Sök chattar"
   :empty-topic                           "Tom rubrik"
   :topic-format                          "Felaktigt format [a-z0-9\\-]+"
   :public-group-topic                    "Rubrik"

   ;;discover
   :discover                              "Upptäck"
   :none                                  "Inga"
   :search-tags                           "Skriv dina söktaggar här"
   :popular-tags                          "Populära taggar"
   :recent                                "Senaste"
   :no-statuses-found                     "Inga statusar hittade"
   :chat                                  "Chatta"
   :all                                   "Alla"
   :public-chats                          "Offentliga chattar"
   :soon                                  "Snart"
   :public-chat-user-count                "{{count}} deltagare"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp profil"
   :no-statuses-discovered                "Inga statusar upptäckta"
   :no-statuses-discovered-body           "När någon har publicerat\nen status ser du den här."
   :no-hashtags-discovered-title          "Inga #hashtags upptäcktes"
   :no-hashtags-discovered-body           "När en #hashtag blir\npopulär ser du den här."

   ;;settings
   :settings                              "Inställningar"

   ;;contacts
   :contacts                              "Kontakter"
   :new-contact                           "Ny kontakt"
   :delete-contact                        "Ta bort kontakt"
   :delete-contact-confirmation           "Denna kontakt tas bort från dina kontakter"
   :remove-from-group                     "Ta bort från grupp"
   :edit-contacts                         "Redigera kontakter"
   :search-contacts                       "Sök kontakter"
   :contacts-group-new-chat               "Starta ny chatt"
   :choose-from-contacts                  "Välj från kontakter"
   :no-contacts                           "Inga kontakter ännu"
   :show-qr                               "Visa QR kod"
   :enter-address                         "Ge address"
   :more                                  "mer"

   ;;group-settings
   :remove                                "Ta bort"
   :save                                  "Spara"
   :delete                                "Ta bort"
   :clear-history                         "Rensa historik"
   :mute-notifications                    "Tysta aviseringar"
   :leave-chat                            "Lämna chatt"
   :chat-settings                         "Chattinställningar"
   :edit                                  "Redigera"
   :add-members                           "Lägg till medlemmar"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "Ny grupp"
   :reorder-groups                        "Ordna grupper"
   :edit-group                            "Redigera grupp"
   :delete-group                          "Ta bort grupp"
   :delete-group-confirmation             "Denna grupp kommer att tas bort från dina grupper. Detta påverkar inte dina kontakter"
   :delete-group-prompt                   "Detta påverkar inte dina kontakter"
   :contact-s                             {:one   "kontakt"
                                           :other "kontakter"}

   ;;protocol
   :received-invitation                   "tog emot chattinbjudan"
   :removed-from-chat                     "tog bort dig från gruppchatten"
   :left                                  "lämnade"
   :invited                               "inbjudna"
   :removed                               "borttagna"
   :You                                   "Du"

   ;;new-contact
   :add-new-contact                       "Lägg till ny kontakt"
   :scan-qr                               "Skanna QR kod"
   :name                                  "Namn"
   :address-explication                   "Din offentliga nykel används för att generera din address på Ethereum och är en serie siffror och bokstäver. Du kan lätt hitta den i din profil"
   :enter-valid-public-key                "Var vänlig och ange en giltig offentlig nyckel eller skanna en QR kod"
   :contact-already-added                 "Kontakten har redan lagts till"
   :can-not-add-yourself                  "Du kan inte lägga till dig själv"
   :unknown-address                       "Okänd adress"

   ;;login
   :connect                               "Anslut"
   :address                               "Adress"
   :password                              "Lösenord"
   :sign-in-to-status                     "Logga in in Status"
   :sign-in                               "Logga in"
   :wrong-password                        "Fel lösenord"
   :enter-password                        "Ge lösenord"

   ;;recover
   :passphrase                            "Lösenordsfras"
   :recover                               "Återställ"
   :twelve-words-in-correct-order         "12 ord i korrekt ordning"

   ;;accounts
   :recover-access                        "Återställ åtkomst"
   :create-new-account                    "Skapa nytt konto"

   ;;wallet-qr-code
   :done                                  "Klar"

   ;;validation
   :invalid-phone                         "Ogiltigt telefonnummer"
   :amount                                "Belopp"

   ;;transactions
   :confirm                               "Bekräfta"
   :transaction                           "Transaktion"
   :unsigned-transaction-expired          "Ej underskriven transaktion förföll"
   :status                                "Status"
   :recipient                             "Mottagare"
   :to                                    "Till"
   :from                                  "Från"
   :data                                  "Data"
   :got-it                                "OK"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "Gas begränsning"
   :gas-price                             "Gas pris"
   :gas-used                              "Gas använt"
   :cost-fee                              "Kostnad/Avgift"
   :nonce                                 "Nonce"
   :confirmations                         "Bekräftelser"
   :confirmations-helper-text             "Var god och vänta på minst 12 bekräftelser för att försäkra dig om att transaktionen gått igenom"
   :copy-transaction-hash                 "Kopiera transaktionshash"
   :open-on-etherscan                     "Öppna på Etherscan.io"
   :incoming                              "Inkommande"
   :outgoing                              "Utgående"
   :pending                               "Pågående"
   :postponed                             "Uppskjutna"

   ;;webview
   :web-view-error                        "hoppsan, fel"

   ;;testfairy warning
   :testfairy-title                       "Varning!"
   :testfairy-message                     "Du använder en app som installerats från en nattlig version. För att underlätta testning spelar denna version in sessionen om WiFi används. All interaktion med appen sparas (som video och loggfiler) och kan användas av utvecklarna för att undersöka möjliga fel. Sparade video/loggfiler innehåller inte lösenord. Inspelningen sker endast om appen installeras från en nattlig version. Inget spelas in om appen installeras från PlayStore eller TestFlight."

   ;; wallet
   :wallet                                "Plånbok"
   :wallets                               "Plånböcker"
   :your-wallets                          "Dina plånböcker"
   :main-wallet                           "Huvudplånbok"
   :wallet-error                          "Fel vid laddning av data"
   :wallet-send                           "Sänd"
   :wallet-send-token                     "Sänd {{symbol}}"
   :wallet-request                        "Begär"
   :wallet-exchange                       "Växla"
   :wallet-assets                         "Tillgångar"
   :wallet-add-asset                      "Lägg till tillgång"
   :wallet-total-value                    "Totalt värde"
   :wallet-settings                       "Plånboksinställningar"
   :wallet-manage-assets                  "Hantera tillgångar"
   :signing-phrase-description            "Skriv under transaktionen genom att ange ditt lösenord. Kontrollera att orden ovan motsvarar din hemliga underskriftsfras"
   :wallet-insufficient-funds             "Otillräckligt kapital"
   :request-transaction                   "Begär transaktion"
   :send-request                          "Skicka begäran"
   :share                                 "Dela"
   :eth                                   "ETH"
   :currency                              "Valuta"
   :usd-currency                          "USD"
   :transactions                          "Transaktioner"
   :transaction-details                   "Transaktiondetaljer"
   :transaction-failed                    "Transaktionen misslyckades"
   :transactions-sign                     "Skriv under"
   :transactions-sign-all                 "Skriv under alla"
   :transactions-sign-transaction         "Skriv under transaktionen"
   :transactions-sign-later               "Skriv under senare"
   :transactions-delete                   "Ta bort transaktion"
   :transactions-delete-content           "Transaktionen tas bort från listan 'Ej underskrivna'"
   :transactions-history                  "Historik"
   :transactions-unsigned                 "Ej underskrivna"
   :transactions-history-empty            "Inga transaktioner i din historik ännu"
   :transactions-unsigned-empty           "Du har inga ej underskrivna transaktioner"
   :transactions-filter-title             "Filtrera historik"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Typ"
   :transactions-filter-select-all        "Välj alla"
   :view-transaction-details              "Visa transaktionsdetaljer"
   :transaction-description               "Var god och vänta på minst 12 bekräftelser för att försäkra dig om att transaktionen gått igenom"
   :transaction-sent                      "Transaktionen skickad"
   :transaction-moved-text                "Transaktionen står kvar på listan 'Ej underskrivna' i 5 minuter"
   :transaction-moved-title               "Transaktionen flyttades"
   :sign-later-title                      "Skriv under transaktionen senare?"
   :sign-later-text                       "Kontrollera transaktionshistoriken för att skriva under denna transaktion"
   :not-applicable                        "Otillämplig på ej underskrivna transaktioner"

   ;; Wallet Send
   :wallet-choose-recipient               "Välj mottagare"
   :wallet-choose-from-contacts           "Välj från kontakter"
   :wallet-address-from-clipboard         "Använd address i urklipp"
   :wallet-invalid-address                "Ogiltig address: \n {{data}}"
   :wallet-invalid-chain-id               "Felaktigt nätverk: \n {{data}}"
   :wallet-browse-photos                  "Bläddra foton"
   :validation-amount-invalid-number      "Beloppet är inte ett giltigt tal"
   :validation-amount-is-too-precise      "Summan är för precis Den minsta summan du kan skicka är 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Nytt nätverk"
   :add-network                           "Lägg till nätverk"
   :add-new-network                       "Lägg till nytt nätverk"
   :existing-networks                     "Nuvarande nätverk"
   :add-json-file                         "Lägg till JSON fil"
   :paste-json-as-text                    "Klistra in JSON som text"
   :paste-json                            "Klistra in JSON"
   :specify-rpc-url                       "Ange RPC URL"
   :edit-network-config                   "Redigera nätverksinställningar"
   :connected                             "Ansluten"
   :process-json                          "Bearbeta JSON"
   :error-processing-json                 "Bearbetning av JSON misslyckades"
   :rpc-url                               "RPC URL"
   :remove-network                        "Ta bort nätverk"
   :network-settings                      "Nätverksinställningar"
   :edit-network-warning                  "Var försiktig. Om du redigerar nätverksinställningarna kan nätverket tas ur bruk för dig"
   :connecting-requires-login             "Du måste logga in för att kunna ansluta till ett annat nätverk"
   :close-app-title                       "Varning!"
   :close-app-content                     "Denna app kommer att avslutas. När du öppnar den på nytt kommer det valda nätverket att användas"
   :close-app-button                      "Bekräfta"})


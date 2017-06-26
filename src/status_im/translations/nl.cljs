(ns status-im.translations.nl)

(def translations
  {
   ;common
   :members-title                         "Leden"
   :not-implemented                       "!niet geïmplementeerd"
   :chat-name                             "Chatnaam"
   :notifications-title                   "Meldingen en geluiden"
   :offline                               "Offline"
   :search-for                            "Zoeken naar..."
   :cancel                                "Annuleren"
   :next                                  "Volgende"
   :type-a-message                        "Type een bericht..."
   :type-a-command                        "Type een commando..."
   :error                                 "Fout"
    
   :camera-access-error                   "Om de benodigde camera toestemming te verlenen, ga naar systeemconfiguratie en bevestig dat Status > Camera is geselecteerd."
   :photos-access-error                   "Om de benodigde foto's toestemming te verlenen, ga naar systeemconfiguratie en bevestig dat Status > Foto's is geselecteerd."
    
   ;drawer
   :invite-friends                        "Nodig vrienden uit"
   :faq                                   "FAQ"
   :switch-users                          "Schakel tussen gebruikers"
   :feedback                              "Heb je feedback?\nSchud je telefoon!"
   :view-all                              "Bekijk alles"
   :current-network                       "Huidig netwerk"
    
   ;chat
   :is-typing                             "typt"
   :and-you                               "en jij"
   :search-chat                           "Zoek in de chat"
   :members                               {:one   "1 lid"
                                           :other "{{count}} leden"
                                           :zero  "geen leden"}
   :members-active                        {:one   "1 lid, 1 actieve"
                                           :other "{{count}} leden, {{count}} actieve"
                                           :zero  "geen leden"}
   :public-group-status                   "Publiekelijk"
   :active-online                         "Online"
   :active-unknown                        "Onbekend"
   :available                             "Beschikbaar"
   :no-messages                           "Geen berichten"
   :suggestions-requests                  "Aanvragen"
   :suggestions-commands                  "Commando’s"
   :faucet-success                        "Faucet aanvraag is ontvangen"
   :faucet-error                          "Fout in faucet aanvraag"
    
   ;sync
   :sync-in-progress                      "Wordt gesynchroniseerd..."
   :sync-synced                           "Gesynchroniseerd"

   ;messages
   :status-sending                        "Wordt verstuurd"
   :status-pending                        "Hangende"
   :status-sent                           "Verstuurd"
   :status-seen-by-everyone               "Door iedereen gezien"
   :status-seen                           "Gezien"
   :status-delivered                      "Bezorgd"
   :status-failed                         "Mislukt"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "seconde"
                                           :other "seconden"}
   :datetime-minute                       {:one   "minuut"
                                           :other "minuten"}
   :datetime-hour                         {:one   "uur"
                                           :other "uren"}
   :datetime-day                          {:one   "dag"
                                           :other "dagen"}
   :datetime-multiple                     "s"
   :datetime-ago                          "geleden"
   :datetime-yesterday                    "gisteren"
   :datetime-today                        "vandaag"

   ;profile
   :profile                               "Profiel"
   :edit-profile                          "Wijzig profiel"
   :report-user                           "MELD GEBRUIKER"
   :message                               "Bericht"
   :username                              "Gebruikersnaam"
   :not-specified                         "Niet opgegeven"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnummer"
   :email                                 "E-mailadres"
   :update-status                         "Update jouw status..."
   :add-a-status                          "Voeg status toe..."
   :status-prompt                         "Creëer een status om mensen te laten weten wat je allemaal aanbiedt. Je kunt ook #hashtags gebruiken."                       
   :in-contacts                           "In contacten"
   :remove-from-contacts                  "Verwijder uit contacten"
   :start-conversation                    "Begin gesprek"
   :send-transaction                      "Verstuur transactie"
   :share-qr                              "QR delen"
   :profile-no-status                     "Geen status"
   :add-to-contacts                       "Aan contactpersonen toevoegen"
   :error-incorrect-name                  "Kies een andere naam"
   :error-incorrect-email                 "Onjuist e-mailadres"

   ;;make_photo
   :image-source-title                    "Profielfoto"
   :image-source-make-photo               "Foto nemen"
   :image-source-gallery                  "Kies uit galerij"
   :image-source-cancel                   "Annuleren"
    
   ;;sharing
   :sharing-copy-to-clipboard             "Kopieer naar klipbord"
   :sharing-share                         "Deel..."
   :sharing-cancel                        "Annuleer"
    
   :browsing-title                        "Browse"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "Open in web browser"
   :browsing-cancel                       "Annuleer"

   ;sign-up
   :contacts-syncronized                  "Jouw contactpersonen zijn gesynchroniseerd"
   :confirmation-code                     (str "Bedankt! We hebben je een sms gestuurd met een bevestigingscode"
                                               ". Geef die code op om jouw telefoonnummer te bevestigen")
   :incorrect-code                        (str "Sorry, de code was onjuist, voer hem opnieuw in")
   :generate-passphrase                   (str "Ik zal een wachtzin maken, zodat je jouw"
                                               "toegang kunt herstellen of vanaf een ander apparaat kunt inloggen")
   :phew-here-is-your-passphrase          "*Foei* dat was moeilijk, hier is jouw wachtzin, *schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
   :here-is-your-passphrase               "Hier is jouw wachtzin, * schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
   :written-down                          "Zorg ervoor dat je hem veilig hebt opgeschreven"
   :phone-number-required                 "Tik hier om je telefoonnummer in te voeren, dan zoek ik jouw vrienden"
   :shake-your-phone                      "Heb je een bug gevonden of een suggestie? ~schud~ simpelweg je telefoon!"
   :intro-status                          "Chat met me om jouw account in te stellen en jouw instellingen te wijzigen!"
   :intro-message1                        "Welkom bij Status\nTik op dit bericht om jouw wachtwoord in te stellen en aan de slag te gaan!"
   :account-generation-message            "Geef me een momentje, ik moet wat ingewikkelde berekeningen doen om jouw account aan te maken!"
   :move-to-internal-failure-message      "Wij moeten enkele belangrijke bestanden van de externe naar interne opslag overzetten. Hiervoor hebben wij jou toestemming nodig. Wij gebruiken in toekomstige versies geen externe opslag"
   :debug-enabled                         "De debug server is gelanceerd! Je kunt nu *status-dev-cli scan* uitvoeren om de server van jouw computer te vinden op hetzelfde netwerk."

   ;phone types
   :phone-e164                            "Internationaal 1"
   :phone-international                   "Internationaal 2"
   :phone-national                        "Nationaal"
   :phone-significant                     "Significant"
    
   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nieuwe chat"
   :delete-chat                           "Verwijder chat"
   :new-group-chat                        "Nieuwe groepchat"
   :new-public-group-chat                 "Deelnemen aan publieke chat"
   :edit-chats                            "Wijzig chats"
   :search-chats                          "Zoek chats"
   :empty-topic                           "Lege topic"
   :topic-format                          "Verkeerd formaat [a-z0-9\\-]+"
   :public-group-topic                    "Topic"

   ;discover
   :discover                              "Ontdek"
   :none                                  "Geen"
   :search-tags                           "Typ hier jouw zoektags"
   :popular-tags                          "Populaire tags"
   :recent                                "Recent"
   :no-statuses-discovered                "Geen statussen ontdekt"

   ;settings
   :settings                              "Instellingen"

   ;contacts
   :contacts                              "Contactpersonen"
   :new-contact                           "Nieuwe contactpersonen"
   :delete-contact                        "Verwijder contact"
   :delete-contact-confirmation           "Deze contactpersoon zal uit jouw contacten worden verwijderd"
   :remove-from-group                     "Verwijder uit groep"
   :edit-contacts                         "Wijzig contacten"
   :search-contacts                       "Zoek contacten"
   :show-all                              "TOON ALLES"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mensen"
   :contacts-group-new-chat               "Start nieuwe chat"
   :choose-from-contacts                  "Kies uit contacten"
   :no-contacts                           "Nog geen contactpersonen"
   :show-qr                               "Toon QR"
   :enter-address                         "Voer adres in"
   :more                                  "Meer"

   ;group-settings
   :remove                                "Verwijderen"
   :save                                  "Opslaan"
   :delete                                "Verwijderen"
   :change-color                          "Wijzig kleur"
   :clear-history                         "Wis geschiedenis"
   :mute-notifications                    "Meldingen uitschakelen"
   :leave-chat                            "Verlaat chat"
   :delete-and-leave                      "Verwijderen en afsluiten"
   :chat-settings                         "Chatinstellingen"
   :edit                                  "Bewerken"
   :add-members                           "Voeg leden toe"
   :blue                                  "Blauw"
   :purple                                "Paars"
   :green                                 "Groen"
   :red                                   "Rood"

   ;commands
   :money-command-description             "Stuur geld"
   :location-command-description          "Stuur locatie"
   :phone-command-description             "Stuur telefoonnummer"
   :phone-request-text                    "Telefoonnummer aanvraag"
   :confirmation-code-command-description "Stuur bevestigingscode"
   :confirmation-code-request-text        "Bevestigingscode aanvraag"
   :send-command-description              "Stuur locatie"
   :request-command-description           "Stuur aanvraag"
   :keypair-password-command-description  ""
   :help-command-description              "Help"
   :request                               "Aanvraag"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH naar {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH van {{chat-name}}"

   ;new-group
   :group-chat-name                       "Chatnaam"
   :empty-group-chat-name                 "Voer een naam in"
   :illegal-group-chat-name               "Kies een andere naam"
   :new-group                             "Nieuwe groep"
   :reorder-groups                        "Groepen rangschikken"
   :group-name                            "Naam van groep"
   :edit-group                            "Wijzig groep"
   :delete-group                          "Verwijder groep"
   :delete-group-confirmation             "Deze groep zal uit jouw groepen worden verwijderd. Dit heeft geen invloed op contacten"
   :delete-group-prompt                   "Dit heeft geen invloed op contacten"
   :group-members                         "Groepleden"
   :contact-s                             {:one   "contact"
   :other                                  "contacten"}

   ;participants
   :add-participants                      "Voeg deelnemers toe"
   :remove-participants                   "Verwijder deelnemers"

   ;protocol
   :received-invitation                   "ontving chatuitnodiging"
   :removed-from-chat                     "verwijderde jou van groepchat"
   :left                                  "vertrok"
   :invited                               "uitgenodigd"
   :removed                               "verwijderd"
   :You                                   "Jij"

   ;new-contact
   :add-new-contact                       "Voeg nieuwe contactpersoon toe"
   :import-qr                             "Importeren"
   :scan-qr                               "QR scannen"
   :swow-qr                               "Toon QR"
   :name                                  "Naam"
   :whisper-identity                      "Fluister identiteit"
   :address-explication                   "Misschien zou hier wat tekst moeten staan waarin wordt uitgelegd wat een adres is en waar je deze kunt vinden"
   :enter-valid-address                   "Voer een geldig adres in of scan een QR-code"
   :enter-valid-public-key                "Voer een geldig publiekelijk adres in of scan een QR-code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "Je kunt jezelf niet toevoegen"
   :unknown-address                       "Onbekend adres"


   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :login                                 "Inloggen"
   :sign-in-to-status                     "Log in op Status"
   :sign-in                               "Inloggen"
   :wrong-password                        "Verkeerd wachtwoord"

   ;recover
   :recover-from-passphrase               "Herstellen met wachtzin"
   :recover-explain                       "Voer de wachtzin in voor jouw wachtwoord om toegang te herstellen"
   :passphrase                            "Wachtzin"
   :recover                               "Herstellen"
   :enter-valid-passphrase                "Voer een wachtzin in"
   :enter-valid-password                  "Voer een wachtwoord in"
   :twelve-words-in-correct-order         "12 woorden in de correcte volgorde"
    
   ;accounts
   :recover-access                        "Toegang herstellen"
   :add-account                           "Voeg account toe"
   :create-new-account                    "Maak een nieuw account"

   ;wallet-qr-code
   :done                                  "Klaar"
   :main-wallet                           "Hoofdportemonnee"

   ;validation
   :invalid-phone                         "Ongeldig telefoonnummer"
   :amount                                "Bedrag"
   :not-enough-eth                        (str "Niet genoeg ETH op saldo"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Bevestig"
   :confirm-transactions                  {:one   "Bevestig transactie"
                                           :other "Bevestig {{count}} transacties"
                                           :zero  "Geen transacties"}
   :transactions-confirmed                {:one   "Transaction bevestigd"
                                           :other "{{count}} Transacties bevestigd"
                                           :zero "Geen transacties bevestigd"}
   :transaction                           "Transactie"
   :unsigned-transactions                 "Ongetekende transacties"
   :no-unsigned-transactions              "Geen ongetekende transacties"
   :enter-password-transactions           {:one   "Bevestig transactie door wachtwoord in te voeren"
                                           :other "Bevestig transacties door wachtwoord in te voeren"}
   :status                                "Status"
   :pending-confirmation                  "In afwachting van bevestiging"
   :recipient                             "Ontvanger"
   :one-more-item                         "Nog één artikel"
   :fee                                   "Kosten"
   :estimated-fee                         "Geschatte kosten"
   :value                                 "Waarde"
   :to                                    "Naar"
   :from                                  "Van"
   :data                                  "Data"
   :got-it                                "Ontvangen"
   :contract-creation                     "Contract aanmaken"
    
   ;:webview
   :web-view-error                        "oeps, fout"})

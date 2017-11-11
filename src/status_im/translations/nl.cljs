(ns status-im.translations.nl)

(def translations
  {
   ;common
   :members-title                         "Leden"
   :not-implemented                       "!niet geïmplementeerd"
   :chat-name                             "Chatnaam"
   :notifications-title                   "Meldingen en geluiden"
   :offline                               "Offline"
   :search-for                            "Zoek naar..."
   :cancel                                "Annuleren"
   :next                                  "Volgende"
   :type-a-message                        "Typ een bericht..."
   :type-a-command                        "typ een commando..."
   :error                                 "Error"

   :camera-access-error                   "Om cameratoegang te geven, ga je naar systeem instellingen en zorg je dat Status > Camera geselecteerd is."
   :photos-access-error                   "Om fototoegang te geven, ga je naar systeem instellingen en zorg je dat Status > Foto's geselecteerd is."

   ;drawer
   :invite-friends                        "Nodig vrienden uit"
   :faq                                   "FAQ"
   :switch-users                          "Schakel tussen gebruikers"
   :feedback                              "Heb je feedback?\nSchud je telefoon!"
   :view-all                              "Laat alles zien"
   :current-network                       "Huidige netwerk"

   ;chat
   :is-typing                             "is aan het typen"
   :and-you                               "en jij"
   :search-chat                           "Zoek in chat"
   :members                               {:one   "1 lid"
                                           :other "{{count}} leden"
                                           :zero  "geen leden"}
   :members-active                        {:one   "1 lid, 1 actieve"
                                           :other "{{count}} leden, {{count}} actieve"
                                           :zero  "geen leden"}
   :active-online                         "Online"
   :active-unknown                        "Onbekend"
   :available                             "Beschikbaar"
   :no-messages                           "Geen berichten"
   :suggestions-requests                  "Aanvragen"
   :suggestions-commands                  "Commando’s"
   :faucet-success                        "Faucet aanvraag is ontvangen"
   :faucet-error                          "Error bij Faucet aanvraag "

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
   :edit-profile                          "Bewerk profiel"
   :report-user                           "MELD GEBRUIKER"
   :message                               "Bericht"
   :username                              "Gebruikersnaam"
   :not-specified                         "Niet opgegeven"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnummer"
   :email                                 "E-mailadres"
   :update-status                         "Wijzig je status..."
   :add-a-status                          "Voeg een status toe..."
   :status-prompt                         "Maak een status aan om mensen te laten weten wat je te bieden hebt. Je kan ook #hashtags gebruiken."
   :add-to-contacts                       "Aan contactpersonen toevoegen"
   :in-contacts                           "In contacts"
   :remove-from-contacts                  "Remove from contacts"
   :start-conversation                    "Begin een gesprek"
   :send-transaction                      "Verstuur een transactie"
   :share-qr                              "Deel de QR"
   :error-incorrect-name                  "Kies een andere naam"
   :error-incorrect-email                 "Onjuist e-mailadres"

   ;;make_photo
   :image-source-title                    "Profielfoto"
   :image-source-make-photo               "Foto nemen"
   :image-source-gallery                  "Kies uit galerij"
   :image-source-cancel                   "Annuleren"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopieer naar klembord"
   :sharing-share                         "Deel..."
   :sharing-cancel                        "Annuleren"

   :browsing-title                        "Browse"
   :browsing-open-in-web-browser          "Open in een web browser"
   :browsing-cancel                       "Annuleren"

   ;sign-up
   :contacts-syncronized                  "Jouw contactpersonen zijn gesynchroniseerd"
   :confirmation-code                     (str "Bedankt! We hebben je een sms gestuurd met een bevestigingscode"
                                               ". Geef die code op om jouw telefoonnummer te bevestigen")
   :incorrect-code                        (str "Sorry, de code was onjuist, voer hem opnieuw in")
   :generate-passphrase                   (str "Ik zal een wachtzin maken, zodat je jouw"
                                               "toegang kunt herstellen of vanaf een ander apparaat kunt inloggen")
   :phew-here-is-your-passphrase          "*Foei* dat was moeilijk, hier is jouw wachtzin, *schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
   :here-is-your-passphrase               "Hier is jouw wachtzin, *schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
   :written-down                          "Zorg ervoor dat je hem veilig hebt opgeschreven"
   :phone-number-required                 "Tik hier om je telefoonnummer in te voeren, dan zoek ik jouw vrienden"
   :shake-your-phone                      "Bug gevonden, of heb je een suggestie? ~Schud~ je telefoon!"
   :intro-status                          "Chat met me om jouw account in te stellen en jouw instellingen te wijzigen!"
   :intro-message1                        "Welkom bij Status\nTik op dit bericht om jouw wachtwoord in te stellen en aan de slag te gaan!"
   :account-generation-message            "Geef me een moment, ik moet wat ingewikkelde berekeningen doen om jouw account aan te maken!"
   :move-to-internal-failure-message      "We moeten wat belangrijke bestanden van externe naar interne opslag verplaatsen. Om dit te doen hebben we je toestemming interne opslag niet meer gebruiken in volgende versies."
   :debug-enabled                         "Debug-server is gelanceerd! Je kan nu *status-dev-cli scan* uitvoeren om de server te vinden vanaf je computer op het zelfde netwerk."

   ;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "Nationaal"
   :phone-significant                     "Significant"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nieuwe chat"
   :delete-chat                           "Verwijder chat"
   :new-group-chat                        "Nieuwe groepchat"
   :new-public-group-chat                 "Neem deel in publieke chat"
   :edit-chats                            "Bewerk chats"
   :search-chats                          "Zoek chats"
   :empty-topic                           "Leeg onderwerp"
   :topic-format                          "Verkeert formaat [a-z0-9\\-]+"
   :public-group-topic                    "Onderwerp"

   ;discover
   :discover                              "Ontdekking"
   :none                                  "Geen"
   :search-tags                           "Typ hier jouw zoektags"
   :popular-tags                          "Populaire tags"
   :recent                                "Recent"
   :no-statuses-discovered                "Geen statussen ontdekt"
   :no-statuses-found                     "Geen statussen gevonden"

   ;settings
   :settings                              "Instellingen"

   ;contacts
   :contacts                              "Contactpersonen"
   :new-contact                           "Nieuwe contactpersonen"
   :delete-contact                        "Delete contact"
   :delete-contact-confirmation           "This contact will be removed from your contacts"
   :remove-from-group                     "Remove from group"
   :edit-contacts                         "Edit contacts"
   :search-contacts                       "Search contacts"
   :show-all                              "TOON ALLES"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mensen"
   :contacts-group-new-chat               "Start nieuwe chat"
   :choose-from-contacts                  "Choose from contacts"
   :no-contacts                           "Nog geen contactpersonen"
   :show-qr                               "Toon QR"
   :enter-address                         "Enter address"
   :more                                  "more"

   ;group-settings
   :remove                                "Verwijderen"
   :save                                  "Opslaan"
   :delete                                "Delete"
   :change-color                          "Wijzig kleur"
   :clear-history                         "Wis geschiedenis"
   :mute-notifications                    "Stille notificaties"
   :leave-chat                            "Verlaat chat"
   :delete-and-leave                      "Verwijderen en verlaten"
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
   :reorder-groups                        "Hergroepeer groep"
   :group-name                            "Groepsnaam"
   :edit-group                            "Bewerk groep"
   :delete-group                          "Verwijder groep"
   :delete-group-confirmation             "Deze groep zal worden verwijderd van jouw groepen. Dit heeft geen effect op je contacten."
   :delete-group-prompt                   "This will not affect contacts"
   :group-members                         "Groepsleden"
   :contact-s                             {:one   "contact"
                                           :other "contacten"}

   ;participants
   :add-participants                      "Voeg deelnemers toe"
   :remove-participants                   "Verwijder deelnemers"

   ;protocol
   :received-invitation                   "ontving chatuitnodiging"
   :removed-from-chat                     "verwijderde jou van groepchat"
   :left                                  "ging weg"
   :invited                               "uitgenodigd"
   :removed                               "verwijderd"
   :You                                   "Jou"

   ;new-contact
   :add-new-contact                       "Voeg nieuwe contactpersoon toe"
   :import-qr                             "Importeren"
   :scan-qr                               "QR scannen"
   :swow-qr                               "QR weergeven"
   :name                                  "Naam"
   :whisper-identity                      "Fluister identiteit"
   :address-explication                   "Misschien zou hier wat tekst moeten staan waarin wordt uitgelegd wat een adres is en waar je deze kunt vinden"
   :enter-valid-address                   "Voer een geldig adres in of scan een QR-code"
   :enter-valid-public-key                "Voer een geldig publieke sleutel in of scan een QR code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "Je kunt niet zelf toevoegen"
   :unknown-address                       "Onbekend adres"

   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :login                                 "Inloggen"
   :sign-in-to-status                     "Meld u aan bij Status"
   :sign-in                               "Meld aan"
   :wrong-password                        "Verkeerd wachtwoord"

   ;recover
   :recover-from-passphrase               "Herstellen met wachtzin"
   :recover-explain                       "Voer de wachtzin in voor jouw wachtwoord om toegang te herstellen"
   :passphrase                            "Wachtzin"
   :recover                               "Herstellen"
   :enter-valid-passphrase                "Voer een wachtzin in"
   :enter-valid-password                  "Voer een wachtwoord in"
   :twelve-words-in-correct-order         "12 woorden in goede volgorde"

   ;accounts
   :recover-access                        "Toegang herstellen"
   :add-account                           "Voeg account toe"
   :create-new-account                    "Maake een nieuwe account aan"

   ;wallet-qr-code
   :done                                  "Klaar"
   :main-wallet                           "Hoofdportemonnee"

   ;validation
   :invalid-phone                         "Ongeldig telefoonnummer"
   :amount                                "Bedrag"
   :not-enough-eth                        (str "Niet genoeg ETH op saldo"
                                               "({{balance}} ETH)")

   ;transactions
   :confirm                               "Bevestigen"
   :confirm-transactions                  {:one   "Bevestig transactie"
                                           :other "Bevestig {{count}} transacties"
                                           :zero  "Geen transacties"}
   :transactions-confirmed                {:one   "Transactie bevestigd"
                                           :other "{{count}} transacties bevestigd"
                                           :zero  "Geen transacties bevestigd"}
   :transaction                           "Transactie"
   :unsigned-transactions                 "Ongetekend transacties"
   :no-unsigned-transactions              "Geen ongetekende transacties"
   :enter-password-transactions           {:one   "Bevetig de transactie door je wachtwoord in te voeren"
                                           :other "Bevetig de transacties door je wachtwoord in te voeren"}
   :status                                "Status"
   :pending-confirmation                  "In afwachting van bevestiging"
   :recipient                             "Ontvanger"
   :one-more-item                         "Nog één item"
   :fee                                   "Kosten"
   :estimated-fee                         "Geschatte kosten"
   :value                                 "Waarde"
   :to                                    "Naar"
   :from                                  "Van"
   :data                                  "Data"
   :got-it                                "Got it"
   :contract-creation                     "Contract Aanmaak"

   ;:webview
   :web-view-error                        "oeps, fout"

   :public-group-status                   "Publiek"})
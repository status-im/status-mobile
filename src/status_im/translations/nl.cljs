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
   :cancel                                "Annuleer"
   :next                                  "Volgende"
   :type-a-message                        "Typ een bericht..."
   :type-a-command                        "Start met een commando te typen..."
   :error                                 "Fout"

   :camera-access-error                   "Om de vereiste camera toegang toe te kennen, ga naar systeem instellingen en controleer of Status > Camera is geselecteerd."
   :photos-access-error                   "Om de vereiste foto toegang toe te kennen, ga naar systeem instellingen en controleer of Status > Foto's is geselecteerd."
  
   ;drawer
   :invite-friends                        "Vrienden uitnodigen"
   :faq                                   "FAQ"
   :switch-users                          "Schakel tussen gebruikers"
   :feedback                              "Heb je feedback?\nSchud je telefoon!"
   :view-all                              "Toon alles"
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
   :public-group-status                   "Publiek"
   :active-online                         "Online"
   :active-unknown                        "Onbekend"
   :available                             "Beschikbaar"
   :no-messages                           "Geen berichten"
   :suggestions-requests                  "Aanvragen"
   :suggestions-commands                  "Commando’s"
   :faucet-success                        "Faucet aanvraag werd ontvangen"
   :faucet-error                          "Faucet aanvraag fout"

   ;sync
   :sync-in-progress                      "Wordt gesynchroniseerd..."
   :sync-synced                           "Gesynchroniseerd"

   ;messages
   :status-sending                        "Wordt verstuurd"
   :status-pending                        "In afwachting"
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
   :edit-profile                          "Bewerk profiel"
   :report-user                           "MELD GEBRUIKER"
   :message                               "Bericht"
   :username                              "Gebruikersnaam"
   :not-specified                         "Niet opgegeven"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnummer"
   :email                                 "E-mailadres"
   :update-status                         "Update je status..."
   :add-a-status                          "Voeg een status toe..."
   :status-prompt                         "Maak een status aan om mensen op de hoogte te brengen van de zaken die je aanbiedt. Je kan ook #hashtags gebruiken."
   :add-to-contacts                       "Aan contactpersonen toevoegen"
   :in-contacts                           "In contactpersonen"
   :remove-from-contacts                  "Verwijder van contactpersonen"
   :start-conversation                    "Start conversatie"
   :send-transaction                      "Zend transactie"
   :share-qr                              "Deel QR"
   :error-incorrect-name                  "Kies een andere naam"
   :error-incorrect-email                 "Ongeldig e-mailadres"

   ;;make_photo
   :image-source-title                    "Profielfoto"
   :image-source-make-photo               "Foto nemen"
   :image-source-gallery                  "Kies uit galerij"
   :image-source-cancel                   "Annuleren"

     ;;sharing
   :sharing-copy-to-clipboard             "Kopieer naar klembord."
   :sharing-share                         "Deel..."
   :sharing-cancel                        "Annuleer"

   :browsing-title                        "Doorblader"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "Openen in web browser"
   :browsing-cancel                       "Annuleer"
    
   ;sign-up
   :contacts-syncronized                  "Jouw contactpersonen zijn gesynchroniseerd"
   :confirmation-code                     (str "Bedankt! We hebben je een sms gestuurd met een bevestigingscode"
                                               ". Geef die code op om jouw telefoonnummer te bevestigen")
   :incorrect-code                        (str "Sorry, de code was onjuist, voer hem opnieuw in")
   :generate-passphrase                   (str "Ik zal een wachtzin maken, zodat je jouw"
                                               "toegang kunt herstellen of vanaf een ander apparaat kunt inloggen")
   :phew-here-is-your-passphrase          "*Phew* dat was moeilijk, hier is jouw wachtzin, *schrijf deze op en bewaar deze goed!* Je zult deze nodig hebben om jouw account te herstellen."
   :here-is-your-passphrase               "Hier is jouw wachtzin, *schrijf deze op en bewaar deze goed!* Je zult deze nodig hebben om jouw account te herstellen."
   :written-down                          "Zorg ervoor dat je deze veilig hebt opgeschreven"
   :phone-number-required                 "Tik hier om je telefoonnummer in te voeren & dan zoek ik jouw vrienden"
   :shake-your-phone                      "Een bug gevonden, of heb je een suggestie? ~Schud~ gewoon met je telefoon!"
   :intro-status                          "Chat met me om jouw account in te stellen en jouw instellingen te wijzigen!"
   :intro-message1                        "Welkom bij Status\nTik op dit bericht om jouw wachtwoord in te stellen & aan de slag te gaan!"
   :account-generation-message            "Geef me een momentje, ik moet wat ingewikkelde berekeningen doen om jouw account aan te maken!"
   :move-to-internal-failure-message      "We moeten een aantal belangrijke bestanden van je externe naar je interne geheugen verplaatsen. Om dit te doen hebben we je toestemming nodig. In toekomstige versies zullen we geen gebruik maken van externe opslag."
   :debug-enabled                         "Debug server is gestart! Je kan nu *status-dev-cli scan* uitvoeren om de server te zoeken van jouw computer op hetzelfde netwerk."
    
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
   :new-public-group-chat                 "Neem deel aan publieke chat"
   :edit-chats                            "Bewerk chats"
   :search-chats                          "Zoek in chats"
   :empty-topic                           "Ledig onderwerp"
   :topic-format                          "Foutief formaat [a-z0-9\\-]+"
   :public-group-topic                    "Onderwerp"

   ;discover
   :discover                             "Ontdekken"
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
   :delete-contact                        "Verwijder contactpersoon"
   :delete-contact-confirmation           "Deze contactpersoon zal worden verwijderd van je contactpersonen"
   :remove-from-group                     "Verwijder uit groep"
   :edit-contacts                         "Bewerk contactpersonen"
   :search-contacts                       "Zoek contactpersonen"  
   :show-all                              "TOON ALLES"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mensen"
   :contacts-group-new-chat               "Start nieuwe chat"
   :choose-from-contacts                  "Kies uit contactpersonen"
   :no-contacts                           "Nog geen contactpersonen"
   :show-qr                               "Toon QR"
   :enter-address                         "Geef adres in"
   :more                                  "meer"

   ;group-settings
   :remove                                "Verwijderen"
   :save                                  "Opslaan"
   :delete                                "Wissen"
   :change-color                          "Wijzig kleur"
   :clear-history                         "Wis geschiedenis"
   :mute-notifications                    "Demp notificaties"
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
   :reorder-groups                        "Herschik groepen"
   :group-name                            "Groepsnaam"
   :edit-group                            "Bewerk groep"
   :delete-group                          "Verwijder groep"
   :delete-group-confirmation             "Deze groep zal worden verwijderd uit je groepen. Dit heeft geen effect op je contactpersonen."
   :delete-group-prompt                   "Dit heeft geen effect op je contactpersonen"
   :group-members                         "Groepsleden"
   :contact-s                             {:one   "contactpersoon"
                                           :other "contactpersonen"}
   ;participants
   :add-participants                      "Voeg deelnemers toe"
   :remove-participants                   "Verwijder deelnemers"

   ;protocol
   :received-invitation                   "ontving chatuitnodiging"
   :removed-from-chat                     "verwijderde jou van groepchat"
   :left                                  "ging weg"
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
   :enter-valid-public-key                "Voeg een geldige publieke sleutel toe, of scan een QR code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "Je kunt jezelf niet toevoegen"
   :unknown-address                       "Onbekend adres"


   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :login                                 "Inloggen"
   :sign-in-to-status                     "Meld aan op Status"
   :sign-in                               "Meld aan"
   :wrong-password                        "Foutief wachtwoord"

   ;recover
   :recover-from-passphrase               "Herstellen met wachtzin"
   :recover-explain                       "Voer de wachtzin in voor jouw wachtwoord om toegang te herstellen"
   :passphrase                            "Wachtzin"
   :recover                               "Herstellen"
   :enter-valid-passphrase                "Voer een wachtzin in"
   :enter-valid-password                  "Voer een wachtwoord in"
   :twelve-words-in-correct-order         "12 woorden in de juiste volgorde"

   ;accounts
   :recover-access                        "Toegang herstellen"
   :add-account                           "Voeg account toe"
   :create-new-account                    "Maak een nieuw account aan"

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
   :transactions-confirmed                {:one   "Transactie bevestigd"
                                           :other "{{count}} transacties bevestigd"
                                           :zero  "Geen transacties bevestigd"}
   :transaction                           "Transactie"
   :unsigned-transactions                 "Ongetekende transactie"
   :no-unsigned-transactions              "Geen ongetekende transacties"
   :enter-password-transactions           {:one   "Bevestig transactie door het invullen van je wachtwoord"
                                           :other "Bevestig transacties door het invullen van je wachtwoord"}
   :status                                "Status"
   :pending-confirmation                  "In afwachting van bevestiging"
   :recipient                             "Ontvanger"
   :one-more-item                         "Nog één artikel"
   :fee                                   "Kosten"
   :value                                 "Waarde"
   :to                                    "Naar"
   :from                                  "Van"
   :data                                  "Data"
   :got-it                                "Begrepen"
   :contract-creation                     "Contract Creatie"


   ;:webview
   :web-view-error                        "oeps, foutje"})

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
   :switch-users                          "Schakel tussen gebruikers"
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
   :datetime-ago                          "geleden"
   :datetime-yesterday                    "gisteren"
   :datetime-today                        "vandaag"

   ;profile
   :profile                               "Profiel"
   :edit-profile                          "Bewerk profiel"
   :message                               "Bericht"
   :not-specified                         "Niet opgegeven"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnummer"
   :update-status                         "Wijzig je status..."
   :add-a-status                          "Voeg een status toe..."
   :status-prompt                         "Maak een status aan om mensen te laten weten wat je te bieden hebt. Je kan ook #hashtags gebruiken."
   :add-to-contacts                       "Aan contactpersonen toevoegen"
   :in-contacts                           "In contacts"
   :remove-from-contacts                  "Remove from contacts"
   :start-conversation                    "Begin een gesprek"
   :send-transaction                      "Verstuur een transactie"

   ;;make_photo
   :image-source-title                    "Profielfoto"
   :image-source-make-photo               "Foto nemen"
   :image-source-gallery                  "Kies uit galerij"

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
   :phew-here-is-your-passphrase          "*Foei* dat was moeilijk, hier is jouw wachtzin, *schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
   :here-is-your-passphrase               "Hier is jouw wachtzin, *schrijf deze op en bewaar hem goed!* Je zult hem nodig hebben om jouw account te herstellen."
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
   :clear-history                         "Wis geschiedenis"
   :mute-notifications                    "Stille notificaties"
   :leave-chat                            "Verlaat chat"
   :chat-settings                         "Chatinstellingen"
   :edit                                  "Bewerken"
   :add-members                           "Voeg leden toe"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Nieuwe groep"
   :reorder-groups                        "Hergroepeer groep"
   :edit-group                            "Bewerk groep"
   :delete-group                          "Verwijder groep"
   :delete-group-confirmation             "Deze groep zal worden verwijderd van jouw groepen. Dit heeft geen effect op je contacten."
   :delete-group-prompt                   "This will not affect contacts"
   :contact-s                             {:one   "contact"
                                           :other "contacten"}

   ;participants

   ;protocol
   :received-invitation                   "ontving chatuitnodiging"
   :removed-from-chat                     "verwijderde jou van groepchat"
   :left                                  "ging weg"
   :invited                               "uitgenodigd"
   :removed                               "verwijderd"
   :You                                   "Jou"

   ;new-contact
   :add-new-contact                       "Voeg nieuwe contactpersoon toe"
   :scan-qr                               "QR scannen"
   :name                                  "Naam"
   :address-explication                   "Misschien zou hier wat tekst moeten staan waarin wordt uitgelegd wat een adres is en waar je deze kunt vinden"
   :enter-valid-public-key                "Voer een geldig publieke sleutel in of scan een QR code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "Je kunt niet zelf toevoegen"
   :unknown-address                       "Onbekend adres"

   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :sign-in-to-status                     "Meld u aan bij Status"
   :sign-in                               "Meld aan"
   :wrong-password                        "Verkeerd wachtwoord"

   ;recover
   :passphrase                            "Wachtzin"
   :recover                               "Herstellen"
   :twelve-words-in-correct-order         "12 woorden in goede volgorde"

   ;accounts
   :recover-access                        "Toegang herstellen"
   :create-new-account                    "Maake een nieuwe account aan"

   ;wallet-qr-code
   :done                                  "Klaar"
   :main-wallet                           "Hoofdportemonnee"

   ;validation
   :invalid-phone                         "Ongeldig telefoonnummer"
   :amount                                "Bedrag"

   ;transactions
   :confirm                               "Bevestigen"
   :transaction                           "Transactie"
   :status                                "Status"
   :recipient                             "Ontvanger"
   :to                                    "Naar"
   :from                                  "Van"
   :data                                  "Data"
   :got-it                                "Got it"

   ;:webview
   :web-view-error                        "oeps, fout"

   :public-group-status                   "Publiek"})
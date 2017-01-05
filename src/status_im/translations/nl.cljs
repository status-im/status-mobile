(ns status-im.translations.nl)

(def translations
  {
   ;common
   :members-title                         "Leden"
   :not-implemented                       "!niet geïmplementeerd"
   :chat-name                             "Chatnaam"
   :notifications-title                   "Meldingen en geluiden"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Nodig vrienden uit"
   :faq                                   "FAQ"
   :switch-users                          "Schakel tussen gebruikers"

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
   :active-online                         "Online"
   :active-unknown                        "Onbekend"
   :available                             "Beschikbaar"
   :no-messages                           "Geen berichten"
   :suggestions-requests                  "Aanvragen"
   :suggestions-commands                  "Commando’s"

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
   :report-user                           "MELD GEBRUIKER"
   :message                               "Bericht"
   :username                              "Gebruikersnaam"
   :not-specified                         "Niet opgegeven"
   :public-key                            "Openbare sleutel"
   :phone-number                          "Telefoonnummer"
   :email                                 "E-mailadres"
   :profile-no-status                     "Geen status"
   :add-to-contacts                       "Aan contactpersonen toevoegen"
   :error-incorrect-name                  "Kies een andere naam"
   :error-incorrect-email                 "Onjuist e-mailadres"

   ;;make_photo
   :image-source-title                    "Profielfoto"
   :image-source-make-photo               "Foto nemen"
   :image-source-gallery                  "Kies uit galerij"
   :image-source-cancel                   "Annuleren"

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
   :intro-status                          "Chat met me om jouw account in te stellen en jouw instellingen te wijzigen!"
   :intro-message1                        "Welkom bij Status\nTik op dit bericht om jouw wachtwoord in te stellen en aan de slag te gaan!"
   :account-generation-message            "Geef me een momentje, ik moet wat ingewikkelde berekeningen doen om jouw account aan te maken!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nieuwe chat"
   :new-group-chat                        "Nieuwe groepchat"

   ;discover
   :discover                             "Ontdekking"
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
   :show-all                              "TOON ALLES"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Mensen"
   :contacts-group-new-chat               "Start nieuwe chat"
   :no-contacts                           "Nog geen contactpersonen"
   :show-qr                               "Toon QR"

   ;group-settings
   :remove                                "Verwijderen"
   :save                                  "Opslaan"
   :change-color                          "Wijzig kleur"
   :clear-history                         "Wis geschiedenis"
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
   :command-text-location                 "Locatie: {{address}}"
   :command-text-browse                   "Browsen op internetpagina: {{webpage}}"
   :command-text-send                     "Transactie: {{amount}} ETH"
   :command-text-help                     "Help"

   ;new-group
   :group-chat-name                       "Chatnaam"
   :empty-group-chat-name                 "Voer een naam in"
   :illegal-group-chat-name               "Kies een andere naam"

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
   :name                                  "Naam"
   :whisper-identity                      "Fluister identiteit"
   :address-explication                   "Misschien zou hier wat tekst moeten staan waarin wordt uitgelegd wat een adres is en waar je deze kunt vinden"
   :enter-valid-address                   "Voer een geldig adres in of scan een QR-code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "Je kunt niet zelf toevoegen"
   :unknown-address                       "Onbekend adres"


   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :login                                 "Inloggen"
   :wrong-password                        "Verkeerd wachtwoord"

   ;recover
   :recover-from-passphrase               "Herstellen met wachtzin"
   :recover-explain                       "Voer de wachtzin in voor jouw wachtwoord om toegang te herstellen"
   :passphrase                            "Wachtzin"
   :recover                               "Herstellen"
   :enter-valid-passphrase                "Voer een wachtzin in"
   :enter-valid-password                  "Voer een wachtwoord in"

   ;accounts
   :recover-access                        "Toegang herstellen"
   :add-account                           "Voeg account toe"

   ;wallet-qr-code
   :done                                  "Klaar"
   :main-wallet                           "Hoofdportemonnee"

   ;validation
   :invalid-phone                         "Ongeldig telefoonnummer"
   :amount                                "Bedrag"
   :not-enough-eth                        (str "Niet genoeg ETH op saldo"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Bevestig transactie"
                                           :other "Bevestig {{count}} transacties"
                                           :zero  "Geen transacties"}
   :status                                "Status"
   :pending-confirmation                  "In afwachting van bevestiging"
   :recipient                             "Ontvanger"
   :one-more-item                         "Nog één artikel"
   :fee                                   "Kosten"
   :value                                 "Waarde"

   ;:webview
   :web-view-error                        "oeps, fout"})

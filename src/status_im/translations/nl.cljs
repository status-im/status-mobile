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
   :is-typing                             "is aan het typen"
   :and-you                               "en u"
   :search-chat                           "Chat doorzoeken"
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
   :status-pending                        "In behandeling"
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
   :report-user                           "Gebruiker rapporteren"
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
   :contacts-syncronized                  "Uw contactpersonen zijn gesynchroniseerd"
   :confirmation-code                     (str "Bedankt! We hebben u een sms gestuurd met een bevestigingscode"
                                               ". Geef die code op om uw telefoonnummer te bevestigen")
   :incorrect-code                        (str "Sorry, de code was onjuist, voer hem opnieuw in")
   :generate-passphrase                   (str "Ik zal een wachtzin maken, zodat uw"
                                               "toegang herstelt kan worden of u vanaf een ander apparaat kunt inloggen")
   :phew-here-is-your-passphrase          "*Foei* dat was moeilijk, hier is uw wachtzin, *schrijf deze op en bewaar hem goed!* U zult hem nodig hebben om jouw account te herstellen."
   :here-is-your-passphrase               "Hier is uw wachtzin, * schrijf deze alstublieft op en bewaar hem goed!* U zult hem nodig hebben om uw account te herstellen."
   :written-down                          "Zorg ervoor dat u hem veilig hebt opgeschreven"
   :phone-number-required                 "Tik hier om uw telefoonnummer in te voeren, dan zoek ik uw vrienden"
   :intro-status                          "Chat met me om uw account in te stellen en uw instellingen te wijzigen!"
   :intro-message1                        "Welkom bij Status\nTik op dit bericht om uw wachtwoord in te stellen en aan de slag te gaan!"
   :account-generation-message            "Geef me een momentje aub, ik moet wat ingewikkelde berekeningen doen om uw account aan te maken!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nieuwe chat"
   :new-group-chat                        "Nieuwe groepchat"

   ;discover
   :discover                             "Ontdekking"
   :none                                  "Geen"
   :search-tags                           "Typ hier uw zoekwoorden"
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
   :show-qr                               "Toon QR-code"

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
   :phone-request-text                    "Telefoonnummer aanvragen"
   :confirmation-code-command-description "Stuur bevestigingscode"
   :confirmation-code-request-text        "Bevestigingscode aanvragen"
   :send-command-description              "Stuur locatie"
   :request-command-description           "Aanvragen"
   :keypair-password-command-description  ""
   :help-command-description              "Help"
   :request                               "Aanvraag"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH naar {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH van {{chat-name}}"

   ;new-group
   :group-chat-name                       "Groepsnaam"
   :empty-group-chat-name                 "Voer een groepsnaam in"
   :illegal-group-chat-name               "Kies een andere groepsnaam"

   ;participants
   :add-participants                      "Voeg deelnemers toe"
   :remove-participants                   "Verwijder deelnemers"

   ;protocol
   :received-invitation                   "Uitnodiging voor groepschat ontvangen"
   :removed-from-chat                     "verwijderde jou van groepchat"
   :left                                  "heeft verlaten"
   :invited                               "is uitgenodigd"
   :removed                               "verwijderd"
   :You                                   "U"

   ;new-contact
   :add-new-contact                       "Voeg nieuwe contactpersoon toe"
   :import-qr                             "Importeer QR code"
   :scan-qr                               "QR code scannen"
   :name                                  "Naam"
   :whisper-identity                      "Fluister identiteit"
   :address-explication                   "Misschien zou hier wat tekst moeten staan waarin wordt uitgelegd wat een adres is en waar je deze kunt vinden"
   :enter-valid-address                   "Voer een geldig adres in of scan een QR-code"
   :contact-already-added                 "De contactpersoon is al toegevoegd"
   :can-not-add-yourself                  "U kunt niet uzelf toevoegen"
   :unknown-address                       "Onbekend adres"


   ;login
   :connect                               "Verbinden"
   :address                               "Adres"
   :password                              "Wachtwoord"
   :login                                 "Inloggen"
   :wrong-password                        "Verkeerd wachtwoord"

   ;recover
   :recover-from-passphrase               "Herstellen met wachtzin"
   :recover-explain                       "Voer de wachtzin in voor uw wachtwoord om toegang te herstellen"
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

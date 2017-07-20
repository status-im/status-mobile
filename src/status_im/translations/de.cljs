(ns status-im.translations.de)

(def translations
  {
   ;common
   :members-title                         "Mitglieder"
   :not-implemented                       "nicht implementiert"
   :chat-name                             "Chatname"
   :notifications-title                   "Benachrichtigungen und Sounds"
   :offline                               "Offline"
   :search-for                            "Suche nach..."
   :cancel                                "Abbrechen"
   :next                                  "Weiter"
   :type-a-message                        "Schreibe eine Nachricht..."
   :type-a-command                        "Tippe ein Kommando..."
   :error                                 "Fehler"

   ;drawer
   :invite-friends                        "Freunde einladen"
   :faq                                   "FAQ"
   :switch-users                          "Benutzer wechseln"
   :feedback                              "Haben Sie Feedback? Schütteln Sie einfach Ihr Mobilgerät."
   :view-all                              "Alle ansehen"
   :current-network                       "Aktuelles Netzwerk"

   ;chat
   :is-typing                             "tippt"
   :and-you                               "und du"
   :search-chat                           "Chat durchsuchen"
   :members                               {:one   "1 Mitglied"
                                           :other "{{count}} Mitglieder"
                                           :zero  "keine Mitglieder"}
   :members-active                        {:one   "1 Mitglied, 1 aktiv"
                                           :other "{{count}} Mitglieder, {{count}} aktiv"
                                           :zero  "keine Mitglieder"}
   :active-online                         "Online"
   :active-unknown                        "Unbekannt"
   :available                             "Verfügbar"
   :no-messages                           "Keine Nachrichten"
   :suggestions-requests                  "Anfragen"
   :suggestions-commands                  "Befehle"

   ;sync
   :sync-in-progress                      "synchronisiere..."
   :sync-synced                           "Synchronisiert"

   ;messages
   :status-sending                        "sendet..."
   :status-pending                        "wird zugestellt"
   :status-sent                           "Verschickt"
   :status-seen-by-everyone               "Von Allen gesehen"
   :status-seen                           "Gesehen"
   :status-delivered                      "Zugestellt"
   :status-failed                         "Fehlgeschlagen"

   ;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "Sekunde"
                                           :other "Sekunden"}
   :datetime-minute                       {:one   "Minute"
                                           :other "Minuten"}
   :datetime-hour                         {:one   "Stunde"
                                           :other "Stunden"}
   :datetime-day                          {:one   "Tag"
                                           :other "Tage"}
   :datetime-multiple                     "mehrere"
   :datetime-ago                          "vor"
   :datetime-yesterday                    "Gestern"
   :datetime-today                        "Heute"

   ;profile
   :profile                               "Profil"
   :report-user                           "Benutzer melden"
   :message                               "Nachricht"
   :username                              "Benutzername"
   :not-specified                         "Nicht angegeben"
   :public-key                            "Öffentlicher Schlüssel"
   :phone-number                          "Telefonnumer"
   :email                                 "E-Mail"
   :profile-no-status                     "Kein Status"
   :add-to-contacts                       "Zu Kontakten hinzufügen"
   :error-incorrect-name                  "Bitte wählen Sie einen anderen Namen"
   :error-incorrect-email                 "Inkorrekte E-Mail"

   ;;make_photo
   :image-source-title                    "Profilfoto"
   :image-source-make-photo               "Foto machen"
   :image-source-gallery                  "Aus Galerie auswählen"
   :image-source-cancel                   "Abbrechen"

   ;;sharing
   :sharing-copy-to-clipboard             "In Zwischenablage kopieren"
   :sharing-share                         "Teilen..."
   :sharing-cancel                        "Abbrechen"

   :browsing-title                        "Browse"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "In Webbrowser öffnen"
   :browsing-cancel                       "Abbrechen"

   ;sign-up

   :contacts-syncronized                  "Ihre Kontakte wurden synchronisiert."
   :confirmation-code                     (str "Danke! Wir haben Ihnen eine Textnachricht mit einem Bestätigungscode "
                                               "geschickt. Bitte geben Sie den Code ein, damit wir Ihre Telefonnummer verifizieren können.")
   :incorrect-code                        (str "Es tut uns leid, der Code war nicht korrekt, bitte erneut eingeben")
   :generate-passphrase                   (str "Ich werde eine Passphrase für Sie generieren, damit Sie Ihren"
                                               "Zugriff wiederherstellen oder sich von einem anderen Gerät aus einloggen können")
   :phew-here-is-your-passphrase          "*Puh* das war schwer, hier ist Ihr Passphrase, *bitte aufschreiben und sicher aufbewahren! *Sie benötigen dies, um Ihren Account wiederherzustellen."
   :here-is-your-passphrase               "Hier ist Ihr Passphrase, *bitte aufschreiben und sicher verwahren!*Sie benötigen dies, um Ihren Account wiederherzustellen."
   :written-down                          "Stellen Sie sicher, dass Sie es sicher aufgeschrieben haben"
   :phone-number-required                 "Tippen Sie hier, um Ihre Telefonnummer einzugeben & ich werde Ihre Freunde finden"
   :intro-status                          "Chatten Sie mit mir, um Ihren Account einzurichten und Ihre Einstellungen zu ändern!"
   :intro-message1                        "Willkommen bei Status.\nTippen Sie auf diese Nachricht, um Ihr Passwort einzurichten und loszulegen!"
   :account-generation-message            "Eine Sekunde, ich muss wahnsinnig schwierige Berechnungen durchführen, um Ihr Konto zu generieren!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Neuer Chat"
   :new-group-chat                        "Neuer Gruppenchat"
   :new-public-group-chat                 "Öffentlichem Gruppenchat beitreten"
   :edit-chats                            "Chats bearbeiten"
   :search-chats                          "Chats durchsuchen"
   :empty-topic                           "Leeres Thema"
   :topic-format                          "Falsches Format[a-z0-9\\-]+"
   :public-group-topic                    "Thema"

   ;discover
   :discover                              "Entdecken"
   :none                                  "Nichts"
   :search-tags                           "Suchbegriffe"
   :popular-tags                          "Beliebte Begriffe"
   :recent                                "Kürzlich"
   :no-statuses-discovered                "Kein Status gefunden"

   ;settings
   :settings                              "Einstellungen"

   ;contacts
   :contacts                              "Kontakte"
   :new-contact                           "Neuer Kontakt"

   :show-all                              "Alle Kontakte anzeigen"

   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Leute"
   :contacts-group-new-chat               "Neuen Chat starten"
   :no-contacts                           "Noch keine Kontakte"
   :show-qr                               "QR-Code anzeigen"

   ;group-settings
   :remove                                "Entfernen"
   :save                                  "Speichern"
   :change-color                          "Farbe ändern"
   :clear-history                         "Verlauf löschen"
   :delete-and-leave                      "Löschen und verlassen"
   :chat-settings                         "Chateinstellungen"
   :edit                                  "Bearbeiten"
   :add-members                           "Mitglieder hinzufügen"
   :blue                                  "Blau"
   :purple                                "Lila"
   :green                                 "Grün"
   :red                                   "Rot"

   ;commands
   :money-command-description             "Geld senden"
   :location-command-description          "Standort senden"
   :phone-command-description             "Telefonnummer senden"
   :phone-request-text                    "Telefonnummer anfragen"
   :confirmation-code-command-description "Bestätitungscode senden"
   :confirmation-code-request-text        "Bestätigungscode anfragen"
   :send-command-description              "Senden"
   :request-command-description           "Anfrage senden"
   :keypair-password-command-description  ""
   :help-command-description              "Hilfe"
   :request                               "Anfrage"
   :chat-send-eth                         "{{amount}} ETH senden"
   :chat-send-eth-to                      "{{amount}} ETH an {{chat-name}} senden"
   :chat-send-eth-from                    "{{amount}} ETH von {{chat-name}} empfangen"

   ;new-group
   :group-chat-name                       "Gruppenname"
   :empty-group-chat-name                 "Bitte geben Sie einen Namen ein"
   :illegal-group-chat-name               "Bitte wählen Sie einen anderen Namen"

   ;participants
   :add-participants                      "Teilnehmer hinzufügen"
   :remove-participants                   "Teilnehmer entfernen"

   ;protocol
   :received-invitation                   "Gruppenchateinladung erhalten"
   :removed-from-chat                     "Aus dem Gruppenchat entfernt"
   :left                                  "Links"
   :invited                               "Eingeladen"
   :removed                               "Entfernt"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Neuen Kontakt hinzufügen"
   :import-qr                             "Importeren anhang QR-Code"
   :scan-qr                               "QR-Code scannen"
   :name                                  "Name"
   :whisper-identity                      "Identität flüstern"
   :address-explication                   "Deine Adresse findest du in deinem Benutzerprofil"
   :enter-valid-address                   "Bitte geben Sie eine gültige Adresse ein oder scannen Sie einen QR-Code"
   :contact-already-added                 "Dieser Kontakt wurde bereits hinzugefügt"
   :can-not-add-yourself                  "Du kannst dich nicht selbst hinzufügen"
   :unknown-address                       "Unbekannte Adresse"


   ;login
   :connect                               "Verbinden"
   :address                               "Adresse"
   :password                              "Passwort"
   :login                                 "Login"
   :wrong-password                        "Falsches Passwort"

   ;recover
   :recover-from-passphrase               "Mittels Passphrase wiederherstellen"
   :recover-explain                       "Bitte gib die Passphrase für deinen Account ein, um den Zugriff wiederherzustellen"
   :passphrase                            "Passphrase"
   :recover                               "Wiederherstellen"
   :enter-valid-passphrase                "Bitte geben Sie eine gültige Passphrase ein"
   :enter-valid-password                  "Bitte geben Sie ein Passwort ein"

   ;accounts
   :recover-access                        "Zugriff wiederherstellen"
   :add-account                           "Konto hinzufügen"

   ;wallet-qr-code
   :done                                  "Fertig"
   :main-wallet                           "Hauptkonto"

   ;validation
   :invalid-phone                         "Ungültige Telefonnummer"
   :amount                                "Betrag"
   :not-enough-eth                        (str "Nicht genug ETH auf dem Konto"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Transaktion bestätigen"
                                           :other "{{count}} Transaktionen bestätigen"
                                           :zero  "Keine Transaktionen"}
   :status                                "Status"
   :pending-confirmation                  "Bestätigung ausstehend"
   :recipient                             "Empfänger"

   :one-more-item                         "Noch ein Objekt"
   :fee                                   "Gebühren"

   :value                                 "Wert"

   ;:webview
   :web-view-error                        "Ups, Fehler"

   :confirm                               "Bestätigen"
   :phone-national                        "National"
   :transactions-confirmed                {:one   "Transaktion bestätigt"
                                           :other "{{count}} Transaktionen bestätigt"
                                           :zero  "Keine Transaktion bestätigt"}
   :debug-enabled                         "Debug-Server wurde eingeführt! Sie können nun Ihre DApp hinzufügen, indem Sie *status-dev-cli scan* von Ihrem Computer starten"
   :share-qr                              "QR teilen"
   :twelve-words-in-correct-order         "12 Wörter in richtiger Reihenfolge"
   :remove-from-contacts                  "Von Kontakten entfernen"
   :delete-chat                           "Chat löschen"
   :sign-in                               "Anmelden"
   :create-new-account                    "Neues Konto erstellen"
   :sign-in-to-status                     "Zum Status anmelden"
   :got-it                                "Verstanden"
   :move-to-internal-failure-message      "Wir müssen ein paar wichtige Dateien vom externen auf den internen Speicher verschieben. Um dies tun zu können, brauchen wir Ihre Genehmigung. Wir werden in zukünftige Versionen keinen externen Speicher nutzen."
   :edit-group                            "Gruppe bearbeiten"
   :delete-group                          "Gruppe löschen"
   :reorder-groups                        "Gruppen wieder ordnen"
   :faucet-success                        "Absperranfrage wurde erhalten"
   :choose-from-contacts                  "Aus Kontakten wählen"
   :new-group                             "Neue Gruppe"
   :phone-e164                            "International 1"
   :remove-from-group                     "Von Gruppe entfernen"
   :search-contacts                       "Kontakte durchsuchen"
   :transaction                           "Transaktion"
   :public-group-status                   "Öffentlich"
   :leave-chat                            "Chat verlassen"
   :start-conversation                    "Konversation beginnen"
   :enter-valid-public-key                "Bitte geben Sie einen gültigen öffentlichen Schlüssel ein oder scannen Sie einen QR-Code"
   :faucet-error                          "Absperranfrage-Fehler"
   :phone-significant                     "Signifikant"
   :phone-international                   "International 2"
   :enter-address                         "Adresse eingeben"
   :send-transaction                      "Transaktion senden"
   :delete-contact                        "Kontakt löschen"
   :mute-notifications                    "Benachrichtigungen stummschalten"


   :contact-s                             {:one   "Kontakt"
                                           :other "Kontakte"}
   :group-name                            "Gruppenname"
   :from                                  "Von"
   :in-contacts                           "In Kontakte"

   :shake-your-phone                      "Haben Sie einen Fehler gefunden oder einen Vorschlag? ~Schütteln~ Sie einfach Ihr Telefon!"
   :status-prompt                         "Erstellen Sie einen Status, um anderen Menschen zu helfen, über die Dinge zu erfahren, die Sie anbieten. Sie können auch #hashtags nutzen."
   :add-a-status                          "Einen Status hinzufügen ..."
   :edit-contacts                         "Kontakte bearbeiten"
   :more                                  "Mehr"
   :no-statuses-found                     "Keine Status gefunden"
   :swow-qr                               "QR anzeigen"
   :delete-group-prompt                   "Dies wird Kontakte nicht beeinträchtigen"
   :edit-profile                          "Profil bearbeiten"


   :enter-password-transactions           {:one   "Bestätigen Sie die Transaktion durch Eingabe Ihres Passworts"
                                           :other "Bestätigen Sie die Transaktionen durch Eingabe Ihres Passworts"}
   :unsigned-transactions                 "Unsignierte Transaktion"
   :to                                    "An"
   :group-members                         "Gruppenmitglieder"
   :estimated-fee                         "Geschätzte Gebühr"
   :data                                  "Daten"})

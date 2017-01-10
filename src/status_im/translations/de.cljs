(ns status-im.translations.de)

(def translations
  {
   ;common
   :members-title                         "Mitglieder"
   :not-implemented                       "!nicht implementiert"
   :chat-name                             "Chatname"
   :notifications-title                   "Benachrichtigungen und Sounds"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Freunde einladen"
   :faq                                   "FAQ"
   :switch-users                          "Benutzer wechseln"

   ;chat
   :is-typing                             "gibt ein"
   :and-you                               "und Sie"
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
   :sync-in-progress                      "Synchronisiere..."
   :sync-synced                           "Synchronisiert"

   ;messages
   :status-sending                        "Absenden"
   :status-pending                        "Anhängig"
   :status-sent                           "Verschickt"
   :status-seen-by-everyone               "Von allen gesehen"
   :status-seen                           "Gesehen"
   :status-delivered                      "Zugestellt"
   :status-failed                         "Fehlgeschlagen"

   ;datetime
   :datetime-second                       {:one   "Sekunde"
                                           :other "Sekunden"}
   :datetime-minute                       {:one   "Minute"
                                           :other "Minuten"}
   :datetime-hour                         {:one   "Stunde"
                                           :other "Stunden"}
   :datetime-day                          {:one   "Tag"
                                           :other "Tage"}
   :datetime-multiple                     "s"     
   :datetime-ago                          "vor"
   :datetime-yesterday                    "Gestern"
   :datetime-today                        "Heute"

   ;profile
   :profile                               "Profil"
   :report-user                           "BENUTZER MELDEN"
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
   :image-source-make-photo               "Fotografieren"
   :image-source-gallery                  "Aus Galarie auswählen"
   :image-source-cancel                   "Abbrechen"

   ;sign-up
   :contacts-syncronized                  "Ihre Kontakte wurden synchronisiert"
   :confirmation-code                     (str "Danke! Wir haben Ihnen eine Textnachricht mit einer Bestätigungscode"
                                               "geschickt. Bitte geben Sie den Code ein, damit wir Ihre Telefonnummer verifizieren können")
   :incorrect-code                        (str "Tut uns leid, der Code war nicht korrekt, bitte erneut eingeben")
   :generate-passphrase                   (str "Ich werde eine Passphrase für Sie generieren, damit Sie Ihren"
                                               "Zugriff wiederherstellen oder sich von einem anderen Gerät aus einloggen können")
   :phew-here-is-your-passphrase          "*Puh* das war schwer, hier ist Ihr Passphrase, *bitte aufschreiben und sicher aufbewahren!* Sie benötigen dies, um Ihren Account wiederherzustellen."
   :here-is-your-passphrase               "Hier ist Ihr Passphrase, *bitte aufschreiben und sicher verwahren!*Sie benötigen dies, um Ihren Account wiederherzustellen."
   :written-down                          "Stellen Sie sicher, dass Sie es sicher aufgeschrieben haben"
   :phone-number-required                 "Tippen Sie hier, um Ihre Telefonnummer einzugeben & ich werde Ihre Freunde finden"
   :shake-your-phone                      "Bug gefunden oder Verbesserungsvorschlag? Einfach das Telefon ~schütteln~!"
   :intro-status                          "Chatten Sie mit mir, um Ihren Account einzurichten und Ihre Einstellungen zu ändern!"
   :intro-message1                        "Willkommen bei Status\nTippen Sie auf diese Nachricht, um Ihr Passwort einzurichten und loszulegen!"
   :account-generation-message            "Eine Sekunde, ich muss wahnsinnig schwere Mathematik machen, um Ihr Konto zu generieren!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Neuer Chat"
   :new-group-chat                        "Neuer Gruppenchat"

   ;discover
   :discover                             "Entdeckung"
   :none                                  "Nichts"
   :search-tags                           "Geben Sie Ihre Suchbegriffe hier ein"
   :popular-tags                          "Beliebte Begriffe"
   :recent                                "Kürzlich"
   :no-statuses-discovered                "Kein Status gefunden"

   ;settings
   :settings                              "Einstellungen"

   ;contacts
   :contacts                              "Kontakte"
   :new-contact                           "Neuer Kontakt"
   :show-all                              "ALLE ANZEIGEN"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Leute"
   :contacts-group-new-chat               "Neuen Chat beginnen"
   :no-contacts                           "Noch keine Kontakte"
   :show-qr                               "QR anzeigen"

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
   :location-command-description          "Position senden"
   :phone-command-description             "Telefonnummer senden"
   :phone-request-text                    "Telefonnummer anfragen"
   :confirmation-code-command-description "Bestätitungscode senden"
   :confirmation-code-request-text        "Bestätigungscode anfragen"
   :send-command-description              "Position senden"
   :request-command-description           "Anfrage senden"
   :keypair-password-command-description  ""
   :help-command-description              "Hilfe"
   :request                               "Anfrage"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH an {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH von {{chat-name}}"
   :command-text-location                 "Position: {{address}}"
   :command-text-browse                   "Auf Webseite: {{webpage}}"
   :command-text-send                     "Transaktion: {{amount}} ETH"
   :command-text-help                     "Hilfe"

   ;new-group
   :group-chat-name                       "Chatname"
   :empty-group-chat-name                 "Bitte geben Sie einen Namen ein"
   :illegal-group-chat-name               "Bitte wählen Sie einen anderen Namen"

   ;participants
   :add-participants                      "Teilnehmer hinzufügen"
   :remove-participants                   "Teilnehmer entfernen"

   ;protocol
   :received-invitation                   "Chateinladung erhalten"
   :removed-from-chat                     "Aus dem Gruppenchat entfernt"
   :left                                  "Links"
   :invited                               "Eingeladen"
   :removed                               "Entfernt"
   :You                                   "Sie"

   ;new-contact
   :add-new-contact                       "Neuen Kontakt hinzufügen"
   :import-qr                             "Import"
   :scan-qr                               "QR-Code scannen"
   :name                                  "Name"
   :whisper-identity                      "Identität Flüstern"
   :address-explication                   "Vielleicht sollte hier ein Text stehen, der erklärt, was eine Adresse ist und wo man danach sucht"
   :enter-valid-address                   "Bitte geben Sie eine gültige Adresse ein oder scannen Sie einen QR-Code"
   :contact-already-added                 "Dieser Kontakt wurde bereits hinzugefügt"
   :can-not-add-yourself                  "Sie können sich selbst nicht hinzufügen"
   :unknown-address                       "Unbekannte Adresse"


   ;login
   :connect                               "Verbinden"
   :address                               "Adresse"
   :password                              "Passwort"
   :login                                 "Login"
   :wrong-password                        "Falsches Passwort"

   ;recover
   :recover-from-passphrase               "Via Passphrase wiederherstellen"
   :recover-explain                       "Bitte geben Sie den Passphrase für Ihr Passwort ein, um den Zugriff wiederherzustellen"
   :passphrase                            "Passphrase"
   :recover                               "Wiederherstellen"
   :enter-valid-passphrase                "Bitte geben Sie einen Passphrase ein"
   :enter-valid-password                  "Bitte geben Sie ein Passwort ein"

   ;accounts
   :recover-access                        "Zugriff wiederherstellen"
   :add-account                           "Konto hinzufügen"

   ;wallet-qr-code
   :done                                  "Fertig"
   :main-wallet                           "Haupt-Konto"

   ;validation
   :invalid-phone                         "Ungültige Telefonnummer"
   :amount                                "Betrag"
   :not-enough-eth                        (str "Nicht genug EHT auf dem Konto"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Transaktion bestätigen"
                                           :other "{{count}} Transaktionen bestätigen"
                                           :zero  "Keine Transaktionen"}
   :status                                "Status"
   :pending-confirmation                  "Bestätigung ausstehend"
   :recipient                             "Empfänger"
   :one-more-item                         "Noch ein Objekt"
   :fee                                   "Gebühr"
   :value                                 "Wert"
                                          
   ;:webview                              
   :web-view-error                        "Ups, Fehler"})

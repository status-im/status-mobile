(ns status-im.translations.de-ch)

(def translations
  {
   ;common
   :members-title                         "Mitglieder"
   :not-implemented                       "!nicht implementiert"
   :chat-name                             "Chat Name"
   :notifications-title                   "Notifikationen and Klänge"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Lade Freunde ein"
   :faq                                   "FAQ"
   :switch-users                          "Benutzer wechseln"

   ;chat
   :is-typing                             "tippt"
   :and-you                               "und du"
   :search-chat                           "Suche Chat"
   :members                               {:one   "1 Mitglied"
                                           :other "{{count}} Mitglieder"
                                           :zero  "Keine Mitglieder"}
   :members-active                        {:one   "1 Mitglied, 1 aktiv"
                                           :other "{{count}} Mitglieder, {{count}} aktiv"
                                           :zero  "Keine Mitglieder"}
   :active-online                         "Online"
   :active-unknown                        "Unbekannt"
   :available                             "Verfügbar"
   :no-messages                           "Keine Nachrichten"
   :suggestions-requests                  "Fragt an"
   :suggestions-commands                  "Kommandiert"

   ;sync
   :sync-in-progress                      "Synchronisierung..."
   :sync-synced                           "Synchron"

   ;messages
   :status-sending                        "Senden"
   :status-pending                        "Anhängig"
   :status-sent                           "Gesendet"
   :status-seen-by-everyone               "Von allen gesehen"
   :status-seen                           "Gesehen"
   :status-delivered                      "Geliefert"
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
   :datetime-yesterday                    "gestern"
   :datetime-today                        "heute"

   ;profile
   :profile                               "Profil"
   :report-user                           "BENUTZER MELDEN"
   :message                               "Nachricht"
   :username                              "Benutzername"
   :not-specified                         "Nicht spezifiziert"
   :public-key                            "Öffentlicher Schlüssel"
   :phone-number                          "Telefonnummer"
   :email                                 "Email"
   :profile-no-status                     "Kein Status"
   :add-to-contacts                       "Zu den Kontakten hinzufügen"
   :error-incorrect-name                  "Bitte wähle einen anderen Namen"
   :error-incorrect-email                 "Inkorrekte E-mail"

   ;;make_photo
   :image-source-title                    "Profilbild"
   :image-source-make-photo               "Erfassen"
   :image-source-gallery                  "Aus der Galerie auswählen"
   :image-source-cancel                   "Stornieren"

   ;sign-up
   :contacts-syncronized                  "Deine Kontakte wurden synchronisiert"
   :confirmation-code                     (str "Vielen Dank! Wir haben Dir eine SMS mit einer Bestätigung zugesandt"
                                               "code. Bitte gebe diesen Code zur Bestätigung deiner Telefonnummer an")
   :incorrect-code                        (str "Leider ist der Code nicht korrekt. Bitte versuche es nochmals")
   :generate-passphrase                   (str "Ich generiere eine Passphrase für dich, so dass du deine wiederherstellen kannst "
                                               "Zugriff oder Anmelden von einem anderen Gerät")
   :phew-here-is-your-passphrase          "*Phew* das war hart, hier ist deine Passphrase, *Schreibe diese auf und bewahre diese sicher auf!* Du benötigst diese, um dein Konto wiederherzustellen."
   :here-is-your-passphrase               "Hier ist deine Passphrase, *Schreibe diese auf und bewahre diese sicher auf!* Du benötigst diese, um dein Konto wiederherzustellen."
   :written-down                          "Stelle sicher, dass du diese aufgeschrieben hast!"
   :phone-number-required                 "Klicke hier, um deine Telefonnummer einzugeben & Ich finde deine Freunde für dich"
   :intro-status                          "Chat mit mir, damit ich dein Konto einrichten und deine Einstellungen ändern kann!"
   :intro-message1                        "Willkommen zu Status\nTap diese Nachricht um Passwort einzurichten & loszulegen!"
   :account-generation-message            "Gib mir eine Sekunde, Ich muss wie verrückt etwas berechnen um dein Konto zu generieren!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Neuer Chat"
   :new-group-chat                        "Neuer Gruppenchat"

   ;discover
   :discover                             "Entdeckung"
   :none                                  "Keine"
   :search-tags                           "Gebe hier deine Suchbegriffe ein"
   :popular-tags                          "Beliebte Suchbegriffe"
   :recent                                "Kürzlich"
   :no-statuses-discovered                "Es wurden keine Status gefunden"

   ;settings
   :settings                              "Einstellungen"

   ;contacts
   :contacts                              "Kontakte"
   :new-contact                           "Neuer Kontakt"
   :show-all                              "ZEIGE ALLES"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Leute"
   :contacts-group-new-chat               "Starte neuer Chat"
   :no-contacts                           "Noch keine Kontakte"
   :show-qr                               "Zeige QR"

   ;group-settings
   :remove                                "Entferne"
   :save                                  "Speichern"
   :change-color                          "Farbe ändern"
   :clear-history                         "Verlauf löschen"
   :delete-and-leave                      "Löschen und Verlassen"
   :chat-settings                         "Chat Einstellungen"
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
   :phone-request-text                    "Telefonnummer anfordern"
   :confirmation-code-command-description "Bestätigungscode senden"
   :confirmation-code-request-text        "Bestätigungscode anfordern"
   :send-command-description              "Standort senden"
   :request-command-description           "Anforderung senden"
   :keypair-password-command-description  ""
   :help-command-description              "Hilfe"
   :request                               "Anforderung"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH zu {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH von {{chat-name}}"
   :command-text-location                 "Standort: {{address}}"
   :command-text-browse                   "Durchsuchen der Website: {{webpage}}"
   :command-text-send                     "Transaktion: {{amount}} ETH"
   :command-text-help                     "Hilfe"

   ;new-group
   :group-chat-name                       "Chat Name"
   :empty-group-chat-name                 "Bitte gib einen Namen ein"
   :illegal-group-chat-name               "Bitte wähle einen anderen Namen aus"

   ;participants
   :add-participants                      "Teilnehmer hinzufügen"
   :remove-participants                   "Teilnehmer entfernen"

   ;protocol
   :received-invitation                   "Chat Einladung erhalten"
   :removed-from-chat                     "du wurdest vom Gruppenchat entfernt"
   :left                                  "verlassen"
   :invited                               "eingeladen"
   :removed                               "entfernt"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Teilnehmer hinzufügen"
   :import-qr                             "Importieren"
   :scan-qr                               "QR scannen"
   :name                                  "Name"
   :whisper-identity                      "Identität flüstern" 
   :address-explication                   "Vielleicht sollte hier ein Text stehen der erklärt, was eine Adresse ist und wo man suchen soll"
   :enter-valid-address                   "Bitte gebe eine gültige Adresse ein oder scannen den QR-Code"
   :contact-already-added                 "Der Kontakt wurde bereits hinzugefügt"
   :can-not-add-yourself                  "Du kannst dich nicht selbst hinzufügen"
   :unknown-address                       "Unbekannte Adresse"


   ;login
   :connect                               "Verbinden"
   :address                               "Adresse"
   :password                              "Passwort"
   :login                                 "Login"
   :wrong-password                        "Falsches Passwort"

   ;recover
   :recover-from-passphrase               "Wiederherstellung von Passphrase"
   :recover-explain                       "Bitte gebe die Passphrase für dein Passwort ein, um den Zugang wiederherzustellen"
   :passphrase                            "Passphrase"
   :recover                               "Wiederherstellen"
   :enter-valid-passphrase                "Bitte Passphrase eingeben"
   :enter-valid-password                  "Bitte Passwort eingeben"

   ;accounts
   :recover-access                        "Zugriff wiederherstellen"
   :add-account                           "Konto hinzufügen"

   ;wallet-qr-code
   :done                                  "Erledigt"
   :main-wallet                           "Haupt-Portemonnaie"

   ;validation
   :invalid-phone                         "Ungültige Telefonnummer"
   :amount                                "Betrag"
   :not-enough-eth                        (str "Nicht genug ETH zur Verfügung "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Transaktion bestätigen"
                                           :other "Bestätige {{count}} Transaktion"
                                           :zero  "Keine Transaktion"}
   :status                                "Status"
   :pending-confirmation                  "Konfirmation ausstehend"
   :recipient                             "Empfänger"
   :one-more-item                         "Noch ein Element"
   :fee                                   "Gebühr"
   :value                                 "Valuta"

   ;:webview
   :web-view-error                        "oops, Fehler"})
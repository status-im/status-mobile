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
   :datetime-ago                          "vor"
   :datetime-yesterday                    "gestern"
   :datetime-today                        "heute"

   ;profile
   :profile                               "Profil"
   :message                               "Nachricht"
   :not-specified                         "Nicht spezifiziert"
   :public-key                            "Öffentlicher Schlüssel"
   :phone-number                          "Telefonnummer"
   :add-to-contacts                       "Zu Kontakten hinzufügen"

   ;;make_photo
   :image-source-title                    "Profilbild"
   :image-source-make-photo               "Erfassen"
   :image-source-gallery                  "Aus der Galerie auswählen"

   ;sign-up
   :contacts-syncronized                  "Deine Kontakte wurden synchronisiert"
   :confirmation-code                     (str "Vielen Dank! Wir haben Dir eine SMS mit einem Bestätigungcode"
                                               "zugesandt. Bitte gebe diesen Code zur Bestätigung deiner Telefonnummer ein")
   :incorrect-code                        (str "Leider ist der Code nicht korrekt. Bitte versuche es nochmals")
   :phew-here-is-your-passphrase          "*Phew* das war hart, hier ist deine Passphrase, *Schreibe diese auf und bewahre diese sicher auf!* Du benötigst diese, um dein Konto wiederherzustellen."
   :here-is-your-passphrase               "Hier ist deine Passphrase, *Schreibe diese auf und bewahre diese sicher auf!* Du benötigst diese, um dein Konto wiederherzustellen."
   :phone-number-required                 "Klicke hier, um deine Telefonnummer einzugeben & Ich finde deine Freunde für dich"
   :intro-status                          "Chat mit mir, damit ich dein Konto einrichten und deine Einstellungen ändern kann!"
   :intro-message1                        "Willkommen zu Status\nTap diese Nachricht um dein Passwort einzurichten & loszulegen!"
   :account-generation-message            "Gib mir eine Sekunde, Ich muss wie verrückt etwas berechnen um dein Konto zu generieren!"

   ;chats
   :chats                                 "Chats"
   :new-group-chat                        "Neuer Gruppenchat"

   ;discover
   :discover                             "Entdecken"
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
   :contacts-group-new-chat               "Starte neuen Gruppenchat"
   :no-contacts                           "Noch keine Kontakte"
   :show-qr                               "QR-Code anzeigen"

   ;group-settings
   :remove                                "Entfernen"
   :save                                  "Speichern"
   :clear-history                         "Verlauf löschen"
   :chat-settings                         "Chat Einstellungen"
   :edit                                  "Bearbeiten"
   :add-members                           "Mitglieder hinzufügen"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "Chat Einladung erhalten"
   :removed-from-chat                     "du wurdest vom Gruppenchat entfernt"
   :left                                  "verlassen"
   :invited                               "eingeladen"
   :removed                               "entfernt"
   :You                                   "Du"

   ;new-contact
   :add-new-contact                       "Teilnehmer hinzufügen"
   :scan-qr                               "QR scannen"
   :name                                  "Name"
   :address-explication                   "Vielleicht sollte hier ein Text stehen der erklärt, was eine Adresse ist und wo man suchen soll"
   :contact-already-added                 "Der Kontakt wurde bereits hinzugefügt"
   :can-not-add-yourself                  "Du kannst dich nicht selbst hinzufügen"
   :unknown-address                       "Unbekannte Adresse"


   ;login
   :connect                               "Verbinden"
   :address                               "Adresse"
   :password                              "Passwort"
   :wrong-password                        "Falsches Passwort"

   ;recover
   :passphrase                            "Passphrase"
   :recover                               "Wiederherstellen"

   ;accounts
   :recover-access                        "Zugriff wiederherstellen"

   ;wallet-qr-code
   :done                                  "Erledigt"
   :main-wallet                           "Haupt-Portemonnaie"

   ;validation
   :invalid-phone                         "Ungültige Telefonnummer"
   :amount                                "Betrag"
   ;transactions
   :status                                "Status"
   :recipient                             "Empfänger"

   ;:webview
   :web-view-error                        "oops, Fehler"})

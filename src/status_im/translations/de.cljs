(ns status-im.translations.de)

(def translations
  {
   ;;common
   :members-title                         "Mitglieder"
   :not-implemented                       "!nicht implementiert"
   :chat-name                             "Chatname"
   :notifications-title                   "Benachrichtigungen und Töne"
   :offline                               "Offline"
   :search-for                            "Suche nach..."
   :cancel                                "Abbrechen"
   :next                                  "Weiter"
   :open                                  "Öffnen"
   :description                           "Beschreibung"
   :url                                   "URL"
   :type-a-message                        "Tippe eine Nachricht..."
   :type-a-command                        "Tippe einen Befehl..."
   :error                                 "Fehler"
   :unknown-status-go-error               "Unbekannter status-go Fehler"
   :node-unavailable                      "Kein laufender Ethereum-Knoten"
   :yes                                   "Ja"
   :no                                    "Nein"

   :camera-access-error                   "Um den benötigten Kamera-Zugriff zu erlauben, gehe bitte in deine Systemeinstellungen und stelle sicher, dass Status > Kamera ausgewählt ist"
   :photos-access-error                   "Um den benötigten Foto-Zugriff zu erlauben, gehe bitte in deine Systemeinstellungen und stelle sicher, dass Status > Fotos ausgewählt ist"

   ;;drawer
   :switch-users                          "Benutzer wechseln"
   :current-network                       "Aktuelles Netzwerk"

   ;;chat
   :is-typing                             "schreibt"
   :and-you                               "und du"
   :search-chat                           "Chat durchsuchen"
   :members                               {:one   "1 Mitglied"
                                           :other "{{count}} Mitglieder"
                                           :zero  "Keine Mitglieder"}
   :members-active                        {:one   "1 Mitglied"
                                           :other "{{count}} Mitglieder"
                                           :zero  "Keine Mitglieder"}
   :public-group-status                   "Öffentlich"
   :active-online                         "Online"
   :active-unknown                        "Unbekannt"
   :available                             "Verfügbar"
   :no-messages                           "Keine Nachrichten"
   :suggestions-requests                  "Anfragen"
   :suggestions-commands                  "Befehle"
   :faucet-success                        "Faucet-Anfrage wurde empfangen"
   :faucet-error                          "Faucet-Anfragefehler"

   ;;sync
   :sync-in-progress                      "Synchronisiere..."
   :sync-synced                           "Synchronisiert"

   ;;messages
   :status-sending                        "Sende..."
   :status-pending                        "Ausstehend"
   :status-sent                           "Gesendet"
   :status-seen-by-everyone               "Von allen gesehen"
   :status-seen                           "Gesehen"
   :status-delivered                      "Zugestellt"
   :status-failed                         "Fehlgeschlagen"

   ;;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "Sekunde"
                                           :other "Sekunden"}
   :datetime-minute                       {:one   "Minute"
                                           :other "Minuten"}
   :datetime-hour                         {:one   "Stunde"
                                           :other "Stunden"}
   :datetime-day                          {:one   "Tag"
                                           :other "Tage"}
   :datetime-ago                          "vor"
   :datetime-yesterday                    "Gestern"
   :datetime-today                        "Heute"

   ;;profile
   :profile                               "Profil"
   :edit-profile                          "Profil bearbeiten"
   :message                               "Nachricht"
   :not-specified                         "Nicht spezifiziert"
   :public-key                            "Öffentlicher Schlüssel"
   :phone-number                          "Telefonnummer"
   :update-status                         "Aktualisiere deinen Status..."
   :add-a-status                          "Einen Status hinzufügen..."
   :status-prompt                         "Erstelle deinen Status. Die Verwendung von #hashtags kann anderen helfen, dich zu finden und mit dir über das zu reden, was dir auf dem Herzen liegt"
   :add-to-contacts                       "Zu Kontakten hinzufügen"
   :in-contacts                           "In Kontakten"
   :remove-from-contacts                  "Aus Kontakten entfernen"
   :start-conversation                    "Beginne ein Gespräch"
   :send-transaction                      "Sende Transaktion"
   :testnet-text                          "Du befindest dich auf dem {{testnet}} Testnetzwerk
    . Sende keine echten ETH oder SNT an deine Adresse"
   :mainnet-text                          "Du befindest dich auf dem Mainnet. Echte ETH werden gesendet"

   ;;make_photo
   :image-source-title                    "Profilbild"
   :image-source-make-photo               "Aufnehmen"
   :image-source-gallery                  "Aus Gallerie auswählen"

   ;;sharing
   :sharing-copy-to-clipboard             "In die Zwischenablage kopieren"
   :sharing-share                         "Teilen..."
   :sharing-cancel                        "Abbrechen"

   :browsing-title                        "Browse"
   :browsing-open-in-web-browser          "In Webbrowser öffnen"
   :browsing-cancel                       "Abbrechen"

   ;;sign-up
   :contacts-syncronized                  "Deine Kontakte wurden synchronisiert"
   :confirmation-code                     (str "Danke! Wir haben dir eine Textnachricht mit einem Bestätigungscode "
                                               "gesendet. Bitte gib diesen Code an, um deine Telefonnummer zu verifizieren.")
   :incorrect-code                        (str "Es tut uns leid, der Code war nicht korrekt, bitte gib ihn erneut ein")
   :phew-here-is-your-passphrase          "Puh, das war anstrengend. Hier ist deine Passphrase. *Notiere sie dir und bewahre sie sicher auf!* Du wirst sie brauchen, um deinen Account wiederherzustellen."
   :here-is-your-passphrase               "Hier ist deine Passphrase. *Notiere sie dir und bewahre sie sicher auf!* Du wirst sie brauchen, um deinen Account wiederherzustellen."
   :here-is-your-signing-phrase           "Hier ist deine Signing Phrase. Du wirst sie benutzen, um deine Trtansaktionen zu verifizieren. *Notiere sie dir und bewahre sie sicher auf!*"
   :phone-number-required                 "Tippe hier, um deine Telefonnummer zu validieren und ich werde deine Freunde finden."
   :shake-your-phone                      "Du hast einen Bug gefunden oder hast einen Verbesserungsvorschlag? ~Schüttle~ einfach dein Handy!"
   :intro-status                          "Chatte mit mir, um deinen Account einzurichten und deine Einstellungen zu ändern."
   :intro-message1                        "Willkommen bei Status!\nKlicke auf diese Nachricht, um dein Passwort festzulegen und loszulegen."
   :account-generation-message            "Gib mir eine Sekunde, ich muss wahnsinnig komplizierte Berechnungen tätigen, um deinen Account zu generieren!"
   :move-to-internal-failure-message      "Wir müssen ein paar wichtige Dateien vom externen zum internen Speicher verschieben. Dafür brauchen wir deine Erlaubnis. In zukünftigen Versionen werden wir keinen externen Speicher mehr verwenden."
   :debug-enabled                         "Der Debug-Server wurde gestartet! Du kannst jetzt *status-dev-cli scan* ausführen, um den Server von deinem Computer im selben Netzwerk zu finden."

   ;;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Signifikant"

   ;;chats
   :chats                                 "Chats"
   :delete-chat                           "Chat löschen"
   :new-group-chat                        "Neuer Gruppenchat"
   :new-public-group-chat                 "Öffentlichem Chat beitreten"
   :edit-chats                            "Chats bearbeiten"
   :search-chats                          "Chats durchsuchen"
   :empty-topic                           "Leeres Thema"
   :topic-format                          "Falsches Format [a-z0-9\\-]+"
   :public-group-topic                    "Thema"

   ;;discover
   :discover                              "Entdecken"
   :none                                  "Nichts"
   :search-tags                           "Tippe deine Suchbegriffe hier"
   :popular-tags                          "Beliebte #hashtags"
   :recent                                "Kürzliche Status"
   :no-statuses-found                     "Keine Status gefunden"
   :chat                                  "Chat"
   :all                                   "Alle"
   :public-chats                          "Öffentliche Chats"
   :soon                                  "Bald"
   :public-chat-user-count                "{{count}} Personen"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp Profil"
   :no-statuses-discovered                "Keine Status gefunden"
   :no-statuses-discovered-body           "Wenn jemand einen\n Status erstellt, wirst du es hier sehen."
   :no-hashtags-discovered-title          "Keine #hashtags gefunden"
   :no-hashtags-discovered-body           "Wenn ein #hashtag beliebt\nwird, wirst du es hier sehen."

   ;;settings
   :settings                              "Einstellungen"

   ;;contacts
   :contacts                              "Kontakte"
   :new-contact                           "Neuer Kontakt"
   :delete-contact                        "Kontakt löschen"
   :delete-contact-confirmation           "Dieser Kontakt wird aus deinen Kontakten gelöscht"
   :remove-from-group                     "Aus Gruppe entfernen"
   :edit-contacts                         "Kontakte bearbeiten"
   :search-contacts                       "Kontakte durchsuchen"
   :contacts-group-new-chat               "Neuen Chat starten"
   :choose-from-contacts                  "Aus Kontakten wählen"
   :no-contacts                           "Noch keine Kontakte"
   :show-qr                               "QR-Code anzeigen"
   :enter-address                         "Andresse eingeben"
   :more                                  "mehr"

   ;;group-settings
   :remove                                "Entfernen"
   :save                                  "Speichern"
   :delete                                "Löschen"
   :clear-history                         "Verlauf leeren"
   :mute-notifications                    "Benachrichtigungen stummschalten"
   :leave-chat                            "Chat verlassen"
   :chat-settings                         "Chat-Einstellungen"
   :edit                                  "Bearbeiten"
   :add-members                           "Mitglieder hinzufügen"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Dein aktueller Standort"
   :places-nearby                         "Standorte in der Nähe"
   :search-results                        "Ergebnisse durchsuchen"
   :dropped-pin                           "Pin gesetzt"
   :location                              "Standort"
   :open-map                              "Öffne Karte"
   :sharing-copy-to-clipboard-address     "Adresse kopieren"
   :sharing-copy-to-clipboard-coordinates "Koordinaten kopieren"

   ;;new-group
   :new-group                             "Neue Gruppe"
   :reorder-groups                        "Gruppen neu ordnen"
   :edit-group                            "Gruppe bearbeiten"
   :delete-group                          "Gruppe löschen"
   :delete-group-confirmation             "Diese Gruppe wird aus deinen Gruppen gelöscht. Deine Kontakte bleiben hiervon unbeeinflusst"
   :delete-group-prompt                   "Deine Kontakte bleiben hiervon unbeeinflusst"
   :contact-s                             {:one   "Kontakt"
                                           :other "Kontakte"}

   ;;protocol
   :received-invitation                   "Chat-Einladung erhalten"
   :removed-from-chat                     "hat dich aus dem Gruppenchat entfernt"
   :left                                  "hat die Gruppe verlassen"
   :invited                               "eingeladen"
   :removed                               "entfernt"
   :You                                   "Du"

   ;;new-contact
   :add-new-contact                       "Neuen Kontakt hinzufügen"
   :scan-qr                               "Scanne QR-Code"
   :name                                  "Name"
   :address-explication                   "Dein öffentlicher Schlüssel wird verwendet, um deine Adresse auf Ethereum zu generieren und besteht aus einer Folge von Zahlen und Buchstaben. Du findest ihn leicht in deinem Profil"
   :enter-valid-public-key                "Bitte gib einen validen öffentlichen Schlüssen ein oder scanne einen QR-Code"
   :contact-already-added                 "Dieser Kontakt wurde bereits hinzugefügt"
   :can-not-add-yourself                  "Du kannst dich nicht selbst hinzufügen"
   :unknown-address                       "Unbekannte Adresse"

   ;;login
   :connect                               "Verbinden"
   :address                               "Adresse"
   :password                              "Passwort"
   :sign-in-to-status                     "Bei Status einloggen"
   :sign-in                               "Einloggen"
   :wrong-password                        "Falsches Passwort"
   :enter-password                        "Passwort eingeben"

   ;;recover
   :passphrase                            "Passphrase"
   :recover                               "Wiederherstellen"
   :twelve-words-in-correct-order         "12 Wörter in der richtigen Reihenfolge"

   ;;accounts
   :recover-access                        "Zugriff Wiederherstellen"
   :create-new-account                    "Neues Konto erstellen"

   ;;wallet-qr-code
   :done                                  "Erledigt"

   ;;validation
   :invalid-phone                         "Ungültige Telefonnummer"
   :amount                                "Betrag"

   ;;transactions
   :confirm                               "Bestätigen"
   :transaction                           "Transaktion"
   :unsigned-transaction-expired          "Unsignierte Transaktion ist ausgelaufen"
   :status                                "Status"
   :recipient                             "Empfänger"
   :to                                    "An"
   :from                                  "Von"
   :data                                  "Daten"
   :got-it                                "Erhalten"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "Gaslimit"
   :gas-price                             "Gaspreis"
   :gas-used                              "Gas verbraucht"
   :cost-fee                              "Kosten/Gebühren"
   :nonce                                 "Nonce"
   :confirmations                         "Bestätigungen"
   :confirmations-helper-text             "Bitte warte mindestens 12 Bestätigungen ab, um sicherzustellen, dass deine Transaktion sicher bearbeitet wurde"
   :copy-transaction-hash                 "Transaktions-Hash kopieren"
   :open-on-etherscan                     "Auf Etherscan.io öffnen"

   ;;webview
   :web-view-error                        "Ups, Fehler"

   ;;testfairy warning
   :testfairy-title                       "Warnung!"
   :testfairy-message                     "Du verwendest eine App Version, die aus einem Nightly Build stammt. Zu Testzwecken zeichnet diese Versions Sitzungsdaten auf, solange eine Wlan-Verbindung genutzt wird. Das bedeutet, dass all deine Interaktionen mit der App gespeichert werden (als Video und Logs) und von unseren Entwicklungsteam verwendet werden könnten, um mögliche Probleme zu untersuchen. Gespeicherte Videos/Logs enthalten keine Passwörter. Aufnahmen werden nur erstellt, wenn die App aus einem Nightly Build installiert wurde. Es wird nichts aufgezeichnet, wenn die App aus dem PlayStore oder TestFlight installiert wurde."

   ;; wallet
   :wallet                                "Wallet"
   :wallets                               "Wallets"
   :your-wallets                          "Deine Wallets"
   :main-wallet                           "Haupt-Wallet"
   :wallet-error                          "Fehler beim Laden"
   :wallet-send                           "Senden"
   :wallet-request                        "Anfragen"
   :wallet-exchange                       "Tauschen"
   :wallet-assets                         "Assets"
   :wallet-add-asset                      "Asset hinzufügen"
   :wallet-total-value                    "Gesamter Wert"
   :wallet-settings                       "Wallet-Einstellungen"
   :signing-phrase-description            "Signiere die Transaktion, indem du dein Passwort eingibst. Stelle sicher, dass die Wörter oben deiner geheimen Signing Phrase entsprechen"
   :wallet-insufficient-funds             "Unzureichendes Kapital"
   :request-transaction                   "Transaktion anfordern"
   :send-request                          "Anfrage senden"
   :share                                 "Teilen"
   :eth                                   "ETH"
   :currency                              "Währung"
   :usd-currency                          "USD"
   :transactions                          "Transaktionen"
   :transaction-details                   "Transaktionsdetails"
   :transaction-failed                    "Transaktion fehlgeschlagen"
   :transactions-sign                     "Signiere"
   :transactions-sign-all                 "Signiere alle"
   :transactions-sign-transaction         "Signiere Transaktion"
   :transactions-sign-later               "Später signieren"
   :transactions-delete                   "Transaktion löschen"
   :transactions-delete-content           "Transaktion wird von der 'Unsigniert' Liste entfernt"
   :transactions-history                  "Verlauf"
   :transactions-unsigned                 "Unsigniert"
   :transactions-history-empty            "Noch keine Transaktionen in deinem Verlauf"
   :transactions-unsigned-empty           "Du hast keine unsignierten Transaktionen"
   :transactions-filter-title             "Filterverlauf"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Typ"
   :transactions-filter-select-all        "Alle auswählen"
   :view-transaction-details              "Transaktionsdetails anzeigen"
   :transaction-description               "Bitte warte mindestens 12 Bestätigungen ab, um sicherzustellen, dass deine Transaktion sicher bearbeitet wurde"
   :transaction-sent                      "Transaktion gesendet"
   :transaction-moved-text                "Die Transaktion wird für die nächsten 5 Minuten in der 'Unsigniert' Liste bleiben"
   :transaction-moved-title               "Transaktion verschoben"
   :sign-later-title                      "Transaktion später signieren?"
   :sign-later-text                       "Gehe in den Transaktionsverlauf, um diese Transaktion zu signieren."
   :not-applicable                        "Nicht anwendbar für unsignierte Transaktionen"

   ;; Wallet Send
   :wallet-choose-recipient               "Empfänger auswählen"
   :wallet-choose-from-contacts           "Aus Kontakten auswählen"
   :wallet-address-from-clipboard         "Adresse aus Zwischenablage verwenden"
   :wallet-invalid-address                "Ungültige Adresse: \n {{data}}"
   :wallet-browse-photos                  "Fotos durchsuchen"
   :validation-amount-invalid-number      "Menge ist keine gültige Zahl"
   :validation-amount-is-too-precise      "Menge ist zu präzise. Die kleinste Einheit, die du senden kannst, ist 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Neues Netzwerk"
   :add-network                           "Netzwerk hinzufügen"
   :add-new-network                       "Neues Netzwerk hinzufügen"
   :existing-networks                     "Existierende Netzwerke"
   :add-json-file                         "JSON Datei hinzufügen"
   :paste-json-as-text                    "JSON als Text einfügen"
   :paste-json                            "JSON einfügen"
   :specify-rpc-url                       "Spezifiziere eine RPC URL"
   :edit-network-config                   "Netzwerkeinstellungen bearbeiten"
   :connected                             "Verbunden"
   :process-json                          "JSON verarbeiten"
   :error-processing-json                 "Fehler beim Verarbeiten von JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Netzwerk entfernen"
   :network-settings                      "Netzwerkeinstellungen"
   :edit-network-warning                  "Sei vorsichtig, das Bearbeiten der Netzwerkinformationen kann das Netzwerk für dich deaktivieren"
   :connecting-requires-login             "Das Verbinden mit einem anderen Netzwerk erfordert eine Anmeldung"
   :close-app-title                       "Warnung!"
   :close-app-content                     "Die App wird gestoppt und geschlossen. Wenn du sie wieder öffnest, wird das ausgewählte Netzwerk verwendet"
   :close-app-button                      "Bestätigen"})

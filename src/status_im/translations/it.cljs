(ns status-im.translations.it)

(def translations
  {
   ;common
   :members-title                         "Membri"
   :not-implemented                       "!non implementato"
   :chat-name                             "Nome chat"
   :notifications-title                   "Notifiche e suoni"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Invita amici"
   :faq                                   "FAQ"
   :switch-users                          "Cambia utente"

   ;chat
   :is-typing                             "sta scrivendo"
   :and-you                               "e tu"
   :search-chat                           "Cerca chat"
   :members                               {:one   "1 membro"
                                           :other "{{count}} membri"
                                           :zero  "nessun membro"}
   :members-active                        {:one   "1 membro, 1 attivo"
                                           :other "{{count}} membri, {{count}} attivi"
                                           :zero  "nessun membro"}
   :active-online                         "Online"
   :active-unknown                        "Sconosciuto"
   :available                             "Disponibile"
   :no-messages                           "Nessun messaggio"
   :suggestions-requests                  "Richieste"
   :suggestions-commands                  "Comandi"

   ;sync
   :sync-in-progress                      "Sincronizzazione in corso..."
   :sync-synced                           "Sincronizzato"

   ;messages
   :status-sending                        "Invio"
   :status-pending                        "In attesa di"
   :status-sent                           "Inviato"
   :status-seen-by-everyone               "Visto da tutti"
   :status-seen                           "Visto"
   :status-delivered                      "Consegnato"
   :status-failed                         "Fallito"

   ;datetime
   :datetime-second                       {:one   "secondo"
                                           :other "secondi"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minuti"}
   :datetime-hour                         {:one   "ora"
                                           :other "ore"}
   :datetime-day                          {:one   "giorno"
                                           :other "giorni"}
   :datetime-multiple                     "s"
   :datetime-ago                          "fa"
   :datetime-yesterday                    "ieri"
   :datetime-today                        "oggi"

   ;profile
   :profile                               "Profilo"
   :report-user                           "SEGNALA UTENTE"
   :message                               "Messaggio"
   :username                              "Username"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :email                                 "Email"
   :profile-no-status                     "Nessuno stato"
   :add-to-contacts                       "Aggiunto ai contatti"
   :error-incorrect-name                  "Seleziona un altro nome"
   :error-incorrect-email                 "e-mail non valida"

   ;;make_photo
   :image-source-title                    "Immagine del profilo"
   :image-source-make-photo               "Acquisisci"
   :image-source-gallery                  "Scegli dalla galleria"
   :image-source-cancel                   "Elimina"

   ;sign-up
   :contacts-syncronized                  "I tuoi contatti sono stati sincronizzati"
   :confirmation-code                     (str "Grazie! Ti abbiamo inviato un messaggio con un codice di conferma "
                                               "codice. Fornisci questo codice per confermare il tuo numero di telefono")
   :incorrect-code                        (str "Spiacenti, il codice era errato, reinseriscilo")
   :generate-passphrase                   (str "Genereremo una passphrase da utilizzare per riattivare il tuo "
                                               "accesso o accedere da un altro dispositivo")
   :phew-here-is-your-passphrase          "*Uff* è stato difficile, ecco la tua passphrase, *scrivila e tienila al sicuro!* Ti servirà per poter accedere al tuo account."
   :here-is-your-passphrase               "Questa è la tua passphrase, *scrivila e tienila al sicuro!* Ti servirà per poter accedere al tuo account."
   :written-down                          "Assicurati di averla scritta e conservata"
   :phone-number-required                 "Tocca qui per inserire il tuo numero di telefono e cercheremo i tuoi amici"
   :intro-status                          "Fai una chat con me per impostare il tuo account e cambiare le tue impostazioni!"
   :intro-message1                        "Benvenuto su Status\nTocca questo messaggio per impostare la tua password e poter cominciare!"
   :account-generation-message            "Dammi un secondo, devo fare dei calcoli complicati per generare il tuo account!"

   ;chats
   :chats                                 "Chat"
   :new-chat                              "Nuova chat"
   :new-group-chat                        "Nuova chat di gruppo"

   ;discover
   :discover                             "Scopri"
   :none                                  "Nessuno"
   :search-tags                           "Digita qui le tue etichette di ricerca"
   :popular-tags                          "Tag popolari"
   :recent                                "Recenti"
   :no-statuses-discovered                "Nessuno stato trovato"

   ;settings
   :settings                              "Impostazioni"

   ;contacts
   :contacts                              "Contatti"
   :new-contact                           "Nuovo contatto"
   :show-all                              "MOSTRA TUTTI"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Persone"
   :contacts-group-new-chat               "Comincia una nuova chat"
   :no-contacts                           "Ancora nessun contatto"
   :show-qr                               "Mostra QR"

   ;group-settings
   :remove                                "Rimuovi"
   :save                                  "Salva"
   :change-color                          "Cambia colore"
   :clear-history                         "Cancella cronologia"
   :delete-and-leave                      "Cancella e lascia"
   :chat-settings                         "Impostazioni chat"
   :edit                                  "Modifica"
   :add-members                           "Aggiungi membri"
   :blue                                  "Blu"
   :purple                                "Viola"
   :green                                 "Verde"
   :red                                   "Rosso"

   ;commands
   :money-command-description             "Invia denaro"
   :location-command-description          "Invia posizione"
   :phone-command-description             "Invia numero di telefono"
   :phone-request-text                    "Richiesta numero di telefono"
   :confirmation-code-command-description "Invia codice di conferma"
   :confirmation-code-request-text        "Codice di conferma richiesto"
   :send-command-description              "Invia posizione"
   :request-command-description           "Invia richiesta"
   :keypair-password-command-description  ""
   :help-command-description              "Aiuto"
   :request                               "Richiesta"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH per {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH da {{chat-name}}"
   :command-text-location                 "Posizione: {{address}}"
   :command-text-browse                   "Naviga la pagina web: {{webpage}}"
   :command-text-send                     "Transazione: {{amount}} ETH"
   :command-text-help                     "Aiuto"

   ;new-group
   :group-chat-name                       "Nome chat"
   :empty-group-chat-name                 "Inserisci un nome"
   :illegal-group-chat-name               "Scegli un altro nome"

   ;participants
   :add-participants                      "Aggiungi partecipanti"
   :remove-participants                   "Rimuovi partecipanti"

   ;protocol
   :received-invitation                   "invito alla chat ricevuto"
   :removed-from-chat                     "sei stato rimosso dalla chat di gruppo"
   :left                                  "lasciato"
   :invited                               "invitato"
   :removed                               "rimosso"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Aggiungi un nuovo contatto"
   :import-qr                             "Importa"
   :scan-qr                               "Scansiona codice QR"
   :name                                  "Nome"
   :whisper-identity                      "Sussurra identità"
   :address-explication                   "Forse qui bisognerebbe inserire un testo che spieghi cos'è un indirizzo e dove cercarlo"
   :enter-valid-address                   "Inserisci un indirizzo valido o scansiona un codice QR"
   :contact-already-added                 "Il contatto è stato già aggiunto"
   :can-not-add-yourself                  "Non puoi aggiungere te stesso"
   :unknown-address                       "Indirizzo sconosciuto"


   ;login
   :connect                               "Connetti"
   :address                               "Indirizzo"
   :password                              "Password"
   :login                                 "Accedi"
   :wrong-password                        "Password errata"

   ;recover
   :recover-from-passphrase               "Recupera dalla passphrase"
   :recover-explain                       "Inserisci la passphrase per poter accedere alla tua password"
   :passphrase                            "Passphrase"
   :recover                               "Recupera"
   :enter-valid-passphrase                "Inserisci una passphrase"
   :enter-valid-password                  "Inserisci una password"

   ;accounts
   :recover-access                        "Recupera accessi"
   :add-account                           "Aggiungi account"

   ;wallet-qr-code
   :done                                  "Fatto"
   :main-wallet                           "Portafogli principale"

   ;validation
   :invalid-phone                         "Numero di telefono non valido"
   :amount                                "Ammontare"
   :not-enough-eth                        (str "ETH sul bilancio insufficiente "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Conferma transazione"
                                           :other "Conferma {{count}} transazioni"
                                           :zero  "Nessuna transazione"}
   :status                                "Stato"
   :pending-confirmation                  "Conferma in attesa"
   :recipient                             "Ricevente"
   :one-more-item                         "Un altro oggetto"
   :fee                                   "Commissione"
   :value                                 "Valore"

   ;:webview
   :web-view-error                        "ops, errore"})

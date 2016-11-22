(ns status-im.translations.it-ch)

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
   :switch-users                          "Cambia utenti"

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
   :status-sending                        "Invio in corso"
   :status-pending                        "In attesa di"
   :status-sent                           "Inviato"
   :status-seen-by-everyone               "Visto da tutti"
   :status-seen                           "Visto"
   :status-delivered                      "Consegnato"
   :status-failed                         "Invio fallito"

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
   :username                              "Nome utente"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :email                                 "Email"
   :profile-no-status                     "Nessuno stato"
   :add-to-contacts                       "Aggiungi ai contatti"
   :error-incorrect-name                  "Seleziona un altro nome"
   :error-incorrect-email                 "Email errata"

   ;;make_photo
   :image-source-title                    "Immagine profilo"
   :image-source-make-photo               "Scatta"
   :image-source-gallery                  "Seleziona dalla galleria immagini"
   :image-source-cancel                   "Annulla"

   ;sign-up
   :contacts-syncronized                  "I tuoi contatti sono stati sincronizzati"
   :confirmation-code                     (str "Grazie! Ti abbiamo inviato un messaggio con un codice di "
                                               "conferma. Utilizza tale codice per confermare il tuo numero di telefono")
   :incorrect-code                        (str "Il codice inserito è errato, riprova")
   :generate-passphrase                   (str "Provvederò a generare una passphrase così potrai ripristinare il tuo "
                                               "accesso o effettuare il login da un altro dispositivo")
   :phew-here-is-your-passphrase          "*Wow* È stato difficile, ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servirà per ripristinare il tuo conto."
   :here-is-your-passphrase               "Ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servirà per ripristinare il tuo conto."
   :written-down                          "Assicurati di averla scritta correttamente"
   :phone-number-required                 "Clicca qui per inserire il tuo numero di telefono e trovare i tuoi amici"
   :intro-status                          "Avvia una conversazione con me per impostare il tuo conto e modificare le tue impostazioni!"
   :intro-message1                        "Benvenuto su Status\nTocca questo messaggio per impostare la tua password e iniziare!"
   :account-generation-message            "Dammi un secondo, devo eseguire dei calcoli matematici complessi per generare il tuo conto!"

   ;chats
   :chats                                 "Conversazioni"
   :new-chat                              "Nuova conversazione"
   :new-group-chat                        "Nuova conversazione di gruppo"

   ;discover
   :discover                             "Scoperta"
   :none                                  "Nessuna"
   :search-tags                           "Inserisci qui i tag di ricerca"
   :popular-tags                          "Tag popolari"
   :recent                                "Recente"
   :no-statuses-discovered                "Nessuno stato identificato"

   ;settings
   :settings                              "Impostazioni"

   ;contacts
   :contacts                              "Contatti"
   :new-contact                           "Nuovo contatto"
   :show-all                              "MOSTRA TUTTI"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Persone"
   :contacts-group-new-chat               "Inizia una nuova conversazione"
   :no-contacts                           "Nessun contatto registrato"
   :show-qr                               "Mostra QR"

   ;group-settings
   :remove                                "Rimuovi"
   :save                                  "Salva"
   :change-color                          "Cambia colore"
   :clear-history                         "Cancella cronologia"
   :delete-and-leave                      "Elimina ed esci"
   :chat-settings                         "Impostazioni conversazioni"
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
   :confirmation-code-request-text        "Richiesta codice di conferma"
   :send-command-description              "Invia posizione"
   :request-command-description           "Invia richiesta"
   :keypair-password-command-description  ""
   :help-command-description              "Aiuto"
   :request                               "Richiedi"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH a {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH da {{chat-name}}"
   :command-text-location                 "Posizione: {{address}}"
   :command-text-browse                   "Pagina web: {{webpage}}"
   :command-text-send                     "Transazione: {{amount}} ETH"
   :command-text-help                     "Aiuto"

   ;new-group
   :group-chat-name                       "Nome conversazione"
   :empty-group-chat-name                 "Inserire un nome"
   :illegal-group-chat-name               "Selezionare un altro nome"

   ;participants
   :add-participants                      "Aggiungi partecipanti"
   :remove-participants                   "Rimuovi partecipanti"

   ;protocol
   :received-invitation                   "ha ricevuto un invito di conversazione"
   :removed-from-chat                     "ti ha rimosso dalla conversazione di gruppo"
   :left                                  "È uscito"
   :invited                               "È stato invitato"
   :removed                               "È stato rimosso"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Aggiungi nuovo contatto"
   :import-qr                             "Importa"
   :scan-qr                               "Scansiona QR"
   :name                                  "Nome"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "Forse qui dovremmo spiegare cos'è un indirizzo e dove cercarlo"
   :enter-valid-address                   "Inserire un indirizzo valido oppure effettuare la scansione del codice QR"
   :contact-already-added                 "Il contatto è già stato aggiunto"
   :can-not-add-yourself                  "Non puoi aggiungere te stesso"
   :unknown-address                       "Indirizzo sconosciuto"


   ;login
   :connect                               "Effettua connessione"
   :address                               "Indirizzo"
   :password                              "Password"
   :login                                 "Login"
   :wrong-password                        "Password errata"

   ;recover
   :recover-from-passphrase               "Ripristina tramite passphrase"
   :recover-explain                       "Inserire la passphrase per ripristinare la password di accesso"
   :passphrase                            "Passphrase"
   :recover                               "Ripristina"
   :enter-valid-passphrase                "Inserire una passphrase"
   :enter-valid-password                  "Inserire una password"

   ;accounts
   :recover-access                        "Ripristina l'accesso"
   :add-account                           "Aggiungi conto"

   ;wallet-qr-code
   :done                                  "OK"
   :main-wallet                           "Wallet principale"

   ;validation
   :invalid-phone                         "Numero di telefono non valido"
   :amount                                "Saldo"
   :not-enough-eth                        (str "Saldo ETH non sufficiente "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Conferma transazione"
                                           :other "Conferma {{count}} transazioni"
                                           :zero  "Nessuna transazione"}
   :status                                "Stato"
   :pending-confirmation                  "Conferma pendente"
   :recipient                             "Beneficiario"
   :one-more-item                         "Ancora un elemento"
   :fee                                   "Commissione"
   :value                                 "Valore"

   ;:webview
   :web-view-error                        "Ops, si è verificato un errore"})

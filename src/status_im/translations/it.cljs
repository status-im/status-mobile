(ns status-im.translations.it)

(def translations
  {
   ;common
   :members-title                         "Membri"
   :not-implemented                       "!non implementato"
   :chat-name                             "Nome della chat"
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
   :username                              "Nome dell'utente"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :email                                 "E-mail"
   :profile-no-status                     "Nessuno stato"
   :add-to-contacts                       "Aggiungi ai contatti"
   :error-incorrect-name                  "Seleziona un altro nome"
   :error-incorrect-email                 "e-mail non valida"

   ;;make_photo
   :image-source-title                    "Immagine del profilo"
   :image-source-make-photo               "Catturare"
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
   :discover                              "Scopri"
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
   :show-all                              "Mostra tutti"
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
   :delete-and-leave                      "Elimina e lascia"
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
   :connect                               "Connettiti"
   :address                               "Indirizzo"
   :password                              "Password"
   :login                                 "Accedi"
   :wrong-password                        "Password errata"

   ;recover
   :recover-from-passphrase               "Recupera l'account usando la passphrase"
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
   :not-enough-eth                        (str "Non hai abbastanza ETH "
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
   :web-view-error                        "ops, errore"

   :confirm                               "Conferma"
   :phone-national                        "Nazionale"
   :transactions-confirmed                {:one   "Transazione confermata"
                                           :other "{{count}} transazioni confermate"
                                           :zero  "Nessuna transazione confermata"}
   :public-group-topic                    "Argomento"
   :debug-enabled                         "Il server di debug è stato avviato! Il tuo indirizzo IP è {{ip}}. Ora puoi aggiungere la tua DApp avviando *status-dev-cli add-dapp --ip {{ip}}* dal tuo computer"
   :new-public-group-chat                 "Entra nella chat pubblica"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{fa}}"
   :sharing-cancel                        "Annulla"
   :share-qr                              "Condividi QR"
   :feedback                              "Hai suggerimenti?\nScuoti il tuo telefono!"
   :twelve-words-in-correct-order         "12 parole nel corretto ordine"
   :remove-from-contacts                  "Rimuovi dai contatti"
   :delete-chat                           "Elimina chat"
   :edit-chats                            "Modifica chat"
   :sign-in                               "Accedi"
   :create-new-account                    "Crea nuovo account"
   :sign-in-to-status                     "Accedi a Stato"
   :got-it                                "Ho capito"
   :move-to-internal-failure-message      "Dobbiamo spostare alcuni file importanti dalla periferica di archiviazione esterna a quella interna. Per farlo, abbiamo bisogno del tuo permesso. Nelle versioni future non utilizzeremo periferiche di archiviazione esterna."
   :edit-group                            "Modifica gruppo"
   :delete-group                          "Elimina gruppo"
   :browsing-title                        "Sfoglia"
   :reorder-groups                        "Riordina gruppi"
   :debug-enabled-no-ip                   "Il server di debug è stato avviato! Ora puoi aggiungere la tua DApp avviando *status-dev-cli add-dapp --ip [your ip]* dal tuo computer"
   :browsing-cancel                       "Annulla"
   :faucet-success                        "La richiesta di Faucet è stata ricevuta"
   :choose-from-contacts                  "Scegli dai contatti"
   :new-group                             "Nuovo gruppo"
   :phone-e164                            "Internazionale 1"
   :remove-from-group                     "Rimuovi dal gruppo"
   :search-contacts                       "Ricerca contatti"
   :transaction                           "Transazione"
   :public-group-status                   "Pubblica"
   :leave-chat                            "Abbandona chat"
   :start-conversation                    "Avvia conversazione"
   :topic-format                          "Formato errato [a-z0-9\\-]+"
   :enter-valid-public-key                "Inserisci una chiave pubblica valida o scansiona un codice QR"
   :faucet-error                          "Errore durante richiesta di Faucet"
   :phone-significant                     "Rilevante"
   :search-for                            "Ricerca per..."
   :sharing-copy-to-clipboard             "Copia su clipboard"
   :phone-international                   "Internazionale 2"
   :enter-address                         "Inserisci indirizzo"
   :send-transaction                      "Invia transazione"
   :delete-contact                        "Elimina contatto"
   :mute-notifications                    "Disattiva suoni notifiche"


   :contact-s                             {:one   "contatto"
                                           :other "contatti"}
   :group-name                            "Nome gruppo"
   :next                                  "Avanti"
   :from                                  "Da"
   :search-chats                          "Ricerca chat"
   :in-contacts                           "Nei contatti"

   :sharing-share                         "Condividi..."
   :type-a-message                        "Scrivi un messaggio..."
   :type-a-command                        "Inizia a scrivere un messaggio..."
   :shake-your-phone                      "Hai trovato un bug o hai un suggerimento? Devi solo ~scuotere~ il tuo telefono!"
   :status-prompt                         "Crea uno stato per aiutare le persone a sapere cosa stai offrendo. Puoi usare anche gli #hashtag."
   :add-a-status                          "Aggiungi uno stato..."
   :error                                 "Errore"
   :edit-contacts                         "Modifica contatti"
   :more                                  "altro"
   :cancel                                "Annulla"
   :no-statuses-found                     "Nessuno stato trovato"
   :swow-qr                               "Mostra QR"
   :browsing-open-in-web-browser          "Apri nel browser web"
   :delete-group-prompt                   "Ciò non avrà effetto sui contatti"
   :edit-profile                          "Modifica profilo"


   :enter-password-transactions           {:one   "Conferma la transazione digitando la tua password"
                                           :other "Conferma le transazioni digitando la tua password"}
   :unsigned-transactions                 "Transazioni non firmate"
   :empty-topic                           "Argomento vuoto"
   :to                                    "A"
   :group-members                         "Membri del gruppo"
   :estimated-fee                         "Tariffa stimata"
   :data                                  "Dati"})

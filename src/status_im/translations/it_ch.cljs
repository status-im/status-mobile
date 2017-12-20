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
   :datetime-ago                          "fa"
   :datetime-yesterday                    "ieri"
   :datetime-today                        "oggi"

   ;profile
   :profile                               "Profilo"
   :message                               "Messaggio"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :add-to-contacts                       "Aggiungi ai contatti"

   ;;make_photo
   :image-source-title                    "Immagine profilo"
   :image-source-make-photo               "Scatta"
   :image-source-gallery                  "Seleziona dalla galleria immagini"

   ;sign-up
   :contacts-syncronized                  "I tuoi contatti sono stati sincronizzati"
   :confirmation-code                     (str "Grazie! Ti abbiamo inviato un messaggio con un codice di "
                                               "conferma. Utilizza tale codice per confermare il tuo numero di telefono")
   :incorrect-code                        (str "Il codice inserito è errato, riprova")
   :phew-here-is-your-passphrase          "*Wow* È stato difficile, ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servirà per ripristinare il tuo conto."
   :here-is-your-passphrase               "Ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servirà per ripristinare il tuo conto."
   :phone-number-required                 "Clicca qui per inserire il tuo numero di telefono e trovare i tuoi amici"
   :intro-status                          "Avvia una conversazione con me per impostare il tuo conto e modificare le tue impostazioni!"
   :intro-message1                        "Benvenuto su Status\nTocca questo messaggio per impostare la tua password e iniziare!"
   :account-generation-message            "Dammi un secondo, devo eseguire dei calcoli matematici complessi per generare il tuo conto!"

   ;chats
   :chats                                 "Conversazioni"
   :new-group-chat                        "Nuova conversazione di gruppo"

   ;discover
   :discover                              "Scoperta"
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
   :contacts-group-new-chat               "Inizia una nuova conversazione"
   :no-contacts                           "Nessun contatto registrato"
   :show-qr                               "Mostra QR"

   ;group-settings
   :remove                                "Rimuovi"
   :save                                  "Salva"
   :clear-history                         "Cancella cronologia"
   :chat-settings                         "Impostazioni conversazioni"
   :edit                                  "Modifica"
   :add-members                           "Aggiungi membri"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "ha ricevuto un invito di conversazione"
   :removed-from-chat                     "ti ha rimosso dalla conversazione di gruppo"
   :left                                  "È uscito"
   :invited                               "È stato invitato"
   :removed                               "È stato rimosso"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Aggiungi nuovo contatto"
   :scan-qr                               "Scansiona QR"
   :name                                  "Nome"
   :address-explication                   "Forse qui dovremmo spiegare cos'è un indirizzo e dove cercarlo"
   :contact-already-added                 "Il contatto è già stato aggiunto"
   :can-not-add-yourself                  "Non puoi aggiungere te stesso"
   :unknown-address                       "Indirizzo sconosciuto"


   ;login
   :connect                               "Effettua connessione"
   :address                               "Indirizzo"
   :password                              "Password"
   :wrong-password                        "Password errata"

   ;recover
   :passphrase                            "Passphrase"
   :recover                               "Ripristina"

   ;accounts
   :recover-access                        "Ripristina l'accesso"

   ;wallet-qr-code
   :done                                  "OK"
   :main-wallet                           "Wallet principale"

   ;validation
   :invalid-phone                         "Numero di telefono non valido"
   :amount                                "Saldo"
   ;transactions
   :status                                "Stato"
   :recipient                             "Beneficiario"

   ;:webview
   :web-view-error                        "Ops, si è verificato un errore"

   :confirm                               "Conferma"
   :phone-national                        "Nazionale"
   :public-group-topic                    "Argomento"
   :debug-enabled                         "Il server di debug è stato avviato! Ora puoi aggiungere la tua DApp eseguendo *status-dev-cli scan* dal tuo computer"
   :new-public-group-chat                 "Entra in chat pubblica"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Annulla"
   :twelve-words-in-correct-order         "12 parole in ordine corretto"
   :remove-from-contacts                  "Rimuovi dai contatti"
   :delete-chat                           "Elimina chat"
   :edit-chats                            "Modifica chat"
   :sign-in                               "Accedi"
   :create-new-account                    "Crea nuovo conto"
   :sign-in-to-status                     "Accedi a Stato"
   :got-it                                "OK"
   :move-to-internal-failure-message      "Dobbiamo spostare alcuni file importanti dal dispositivo di archiviazione esterno a quello interno. Per poter completare l'operazione abbiamo bisogno della tua autorizzazione. Non utilizzeremo lo spazio di archiviazione esterno nelle versioni future."
   :edit-group                            "Modifica gruppo"
   :delete-group                          "Elimina gruppo"
   :browsing-title                        "Sfoglia"
   :reorder-groups                        "Riordina gruppi"
   :browsing-cancel                       "Annulla"
   :faucet-success                        "Richiesta faucet ricevuta"
   :choose-from-contacts                  "Scegli dai contatti"
   :new-group                             "Nuovo gruppo"
   :phone-e164                            "Internazionale 1"
   :remove-from-group                     "Rimuovi dal gruppo"
   :search-contacts                       "Cerca tra i contatti"
   :transaction                           "Transazione"
   :public-group-status                   "Pubblico"
   :leave-chat                            "Abbandona chat"
   :start-conversation                    "Inizia conversazione"
   :topic-format                          "Formato errato [a-z0-9\\-]+"
   :enter-valid-public-key                "Inserisci una chiave pubblica valida o scansione un codice QR"
   :faucet-error                          "Errore richiesta faucet"
   :phone-significant                     "Rilevante"
   :search-for                            "Cerca…"
   :sharing-copy-to-clipboard             "Copia negli appunti"
   :phone-international                   "Internazionale 2"
   :enter-address                         "Inserisci indirizzo"
   :send-transaction                      "Invia transazione"
   :delete-contact                        "Elimina contatto"
   :mute-notifications                    "Silenzia le notifiche"


   :contact-s                             {:one   "contatto"
                                           :other "contatti"}
   :next                                  "Avanti"
   :from                                  "Da"
   :search-chats                          "Cerca nelle chat"
   :in-contacts                           "Nei contatti"

   :sharing-share                         "Condividi…"
   :type-a-message                        "Digita un messaggio…"
   :type-a-command                        "Inizia a digitare un comando…"
   :shake-your-phone                      "Hai trovato un bug o hai un suggerimento? ~Scuoti~ il telefono!"
   :status-prompt                         "Crea uno stato per consentire le persone di conoscere le cose che offri. Puoi usare anche gli #hashtag"
   :add-a-status                          "Aggiungi uno stato…"
   :error                                 "Errore"
   :edit-contacts                         "Modifica contatti"
   :more                                  "altro"
   :cancel                                "Annulla"
   :no-statuses-found                     "Nessuno stato trovato"
   :browsing-open-in-web-browser          "Apri nel browser"
   :delete-group-prompt                   "Questo non intaccherà i contatti"
   :edit-profile                          "Modifica profilo"


   :empty-topic                           "Argomento non definito"
   :to                                    "A"
   :data                                  "Dati"})
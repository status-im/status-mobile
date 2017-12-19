(ns status-im.translations.it)

(def translations
  {
   ;;common
   :members-title                         "Membri"
   :not-implemented                       "!non implementato"
   :chat-name                             "Nome della chat"
   :notifications-title                   "Notifiche e suoni"
   :offline                               "Offline"
   :search-for                            "Cercando..."
   :cancel                                "Annulla"
   :next                                  "Avanti"
   :open                                  "Apri"
   :description                           "Descrizione"
   :url                                   "URL"
   :type-a-message                        "Scrivi un messaggio..."
   :type-a-command                        "Inizia a digitare un comando..."
   :error                                 "Errore"
   :unknown-status-go-error               "Errore status-go sconosciuto"
   :node-unavailable                      "Nessun nodo ethereum disponibile"
   :yes                                   "Sì"
   :no                                    "No"

   :camera-access-error                   "Per concedere i permessi necessari per la fotocamera, per favore vai nelle impostazioni di sistema e assicurati che la voce Status > Fotocamera sia selezionata."
   :photos-access-error                   "Per concedere i permessi necessari per le foto, per favore vai nelle impostazioni di sistema e assicurati che la voce Status > Foto sia selezionata."

   ;;drawer
   :switch-users                          "Cambia utente"
   :current-network                       "Rete corrente"

   ;;chat
   :is-typing                             "sta scrivendo"
   :and-you                               "e tu"
   :search-chat                           "Cerca chat"
   :members                               {:one   "1 membro"
                                           :other "{{count}} membri"
                                           :zero  "nessun membro"}
   :members-active                        {:one   "1 membro"
                                           :other "{{count}} membri"
                                           :zero  "nessun membro"}
   :public-group-status                   "Pubblico"
   :active-online                         "Online"
   :active-unknown                        "Sconosciuto"
   :available                             "Disponibile"
   :no-messages                           "Nessun messaggio"
   :suggestions-requests                  "Richieste"
   :suggestions-commands                  "Comandi"
   :faucet-success                        "La richiesta al faucet è stata ricevuta"
   :faucet-error                          "Errore durante la richiesta al faucet"

   ;;sync
   :sync-in-progress                      "Sincronizzazione in corso..."
   :sync-synced                           "Sincronizzato"

   ;;messages
   :status-pending                        "In attesa"
   :status-sent                           "Inviato"
   :status-seen-by-everyone               "Visto da tutti"
   :status-seen                           "Visto"
   :status-delivered                      "Consegnato"
   :status-failed                         "Fallito"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
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

   ;;profile
   :profile                               "Profilo"
   :edit-profile                          "Modifica profilo"
   :message                               "Messaggio"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :update-status                         "Aggiorna il tuo stato..."
   :add-a-status                          "Aggiungi uno stato..."
   :status-prompt                         "Imposta il tuo stato. L'utilizzo di #hastags aiuterà gli altri a scoprirti e a parlare di ciò che pensi"
   :add-to-contacts                       "Aggiungi ai contatti"
   :in-contacts                           "Nei contatti"
   :remove-from-contacts                  "Rimuovi dai contatti"
   :start-conversation                    "Inizia la conversazione"
   :send-transaction                      "Invia una transazione"
   :testnet-text                          "Sei in {{testnet}} Testnet. Non mandare ETH o SNT reali al tuo indirizzo"
   :mainnet-text                          "Sei nella Mainnet. Verranno inviati ETH reali"

   ;;make_photo
   :image-source-title                    "Immagine del profilo"
   :image-source-make-photo               "Cattura"
   :image-source-gallery                  "Seleziona dalla galleria"

   ;;sharing
   :sharing-copy-to-clipboard             "Copia negli appunti"
   :sharing-share                         "Condividi..."
   :sharing-cancel                        "Annulla"

   :browsing-title                        "Naviga"
   :browsing-open-in-web-browser          "Apri nel web browser"
   :browsing-cancel                       "Annulla"

   ;;sign-up
   :contacts-syncronized                  "I tuoi contatti sono stati sincronizzati"
   :confirmation-code                     (str "Grazie! Ti abbiamo inviato un sms con un codice "
                                               "di conferma. Per favore inserisci quel codice per confermare il tuo numero di telefono")
   :incorrect-code                        (str "Il codice era errato, riprova")
   :phew-here-is-your-passphrase          "Fiù, è stato difficile. Ecco la tua passphrase, *scrivila e conservala al sicuro!* Ne avrai bisogno per recuperare il tuo account."
   :here-is-your-passphrase               "Ecco la tua passphrase, *scrivila e conservala al sicuro!* Ne avrai bisogno per recuperare il tuo account."
   :here-is-your-signing-phrase           "Ecco la tua signing phrase. La userai per verificare le tue transazioni. *Scrivila e conservala al sicuro!*"
   :phone-number-required                 "Tocca qui per convalidare il tuo numero di telefono e troverò i tuoi amici."
   :shake-your-phone                      "Hai trovato un bug o hai un suggerimento? Devi solo ~scuotere~ il tuo telefono!"
   :intro-status                          "Chatta con me per inizializzare il tuo account e cambiare le tue impostazioni."
   :intro-message1                        "Benvenuto in Status!\nTocca questo messaggio per impostare la tua password e cominciare."
   :account-generation-message            "Dammi un secondo, devo fare dei calcoli complicati per generare il tuo account!"
   :move-to-internal-failure-message      "Dobbiamo spostare alcuni file importanti dalla periferica di archiviazione esterna a quella interna. Per farlo, abbiamo bisogno del tuo permesso. Nelle versioni future non utilizzeremo periferiche di archiviazione esterne."
   :debug-enabled                         "Il server di debug è stato lanciato! Adesso puoi eseguire *status-dev-cli scan* per trovare il server dal tuo computer nella tua stessa rete."

   ;;phone types
   :phone-e164                            "Internazionale 1"
   :phone-international                   "Internazionale 2"
   :phone-national                        "Nazionale"
   :phone-significant                     "Rilevante"

   ;;chats
   :chats                                 "Chat"
   :delete-chat                           "Cancella chat"
   :new-group-chat                        "Nuova chat di gruppo"
   :new-public-group-chat                 "Entra nella chat pubblica"
   :edit-chats                            "Modifica chat"
   :search-chats                          "Cerca chat"
   :empty-topic                           "Argomento vuoto"
   :topic-format                          "Formato errato [a-z0-9\\-]+"
   :public-group-topic                    "Argomento"

   ;;discover
   :discover                              "Scopri"
   :none                                  "Nessuno"
   :search-tags                           "Digita qui i tuoi tag di ricerca"
   :popular-tags                          "#hashtags popolari"
   :recent                                "Stati recenti"
   :no-statuses-found                     "Nessuno stato trovato"
   :chat                                  "Chat"
   :all                                   "Tutte"
   :public-chats                          "Chat pubbliche"
   :soon                                  "Presto"
   :public-chat-user-count                "{{count}} persone"
   :dapps                                 "ÐApps"
   :dapp-profile                          "Profilo ÐApp"
   :no-statuses-discovered                "Nessuno stato trovato"
   :no-statuses-discovered-body           "Quando qualcuno posta\nuno stato lo vedrai qui."
   :no-hashtags-discovered-title          "Nessun #hashtags trovato"
   :no-hashtags-discovered-body           "Quando un #hashtag diventa\npopolare lo vedrai qui."

   ;;settings
   :settings                              "Impostazioni"

   ;;contacts
   :contacts                              "Contatti"
   :new-contact                           "Nuovo contatto"
   :delete-contact                        "Elimina contatto"
   :delete-contact-confirmation           "Questo contatto sarà eliminato dai tuoi contatti"
   :remove-from-group                     "Rimuovi dal gruppo"
   :edit-contacts                         "Modifica contatto"
   :search-contacts                       "Cerca contatti"
   :contacts-group-new-chat               "Inizia una nuova chat"
   :choose-from-contacts                  "Scegli dai contatti"
   :no-contacts                           "Ancora nessun contatto"
   :show-qr                               "Mostra QR"
   :enter-address                         "Inserisci indirizzo"
   :more                                  "ancora"

   ;;group-settings
   :remove                                "Rimuovi"
   :save                                  "Salva"
   :delete                                "Cancella"
   :clear-history                         "Cancella cronologia"
   :mute-notifications                    "Silenzia notifiche"
   :leave-chat                            "Lascia la chat"
   :chat-settings                         "Impostazioni della chat"
   :edit                                  "Modifica"
   :add-members                           "Aggiungi membri"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "La tua posizione corrente"
   :places-nearby                         "Posti vicini"
   :search-results                        "Cerca risultati"
   :dropped-pin                           "Pin posizionato"
   :location                              "Posizione"
   :open-map                              "Apri Mappa"
   :sharing-copy-to-clipboard-address     "Copia l'Indirizzo"
   :sharing-copy-to-clipboard-coordinates "Copia coordinate"

   ;;new-group
   :new-group                             "Nuovo gruppo"
   :reorder-groups                        "Riordina gruppi"
   :edit-group                            "Modifica gruppo"
   :delete-group                          "Elimina gruppo"
   :delete-group-confirmation             "Questo gruppo sarà cancellato dai tuoi gruppi. Questo non intaccherà i tuoi contatti"
   :delete-group-prompt                   "Questo non intaccherà i tuoi contatti"
   :contact-s                             {:one   "contatto"
                                           :other "contatti"}

   ;;protocol
   :received-invitation                   "invito alla chat ricevuto"
   :removed-from-chat                     "sei stato rimosso dalla chat di gruppo"
   :left                                  "lasciato"
   :invited                               "invitato"
   :removed                               "rimosso"
   :You                                   "Tu"

   ;;new-contact
   :add-new-contact                       "Aggiungi nuovo contatto"
   :scan-qr                               "Scansiona QR"
   :name                                  "Nome"
   :address-explication                   "La tua chiave privata è usata per generare il tuo indirizzo su Ethereum ed è una serie di numeri e lettere. Puoi trovarla facilmente nel tuo profilo"
   :enter-valid-public-key                "Per favore inserisci una chiave pubblica valida o scansiona un QR"
   :contact-already-added                 "Il contatto è già stato aggiunto"
   :can-not-add-yourself                  "Non puoi aggiungere te stesso"
   :unknown-address                       "Indirizzo sconosciuto"

   ;;login
   :connect                               "Connetti"
   :address                               "Indirizzo"
   :password                              "Password"
   :sign-in-to-status                     "Accedi a Status"
   :sign-in                               "Accedi"
   :wrong-password                        "Password sbagliata"
   :enter-password                        "Inserisci la password"

   ;;recover
   :passphrase                            "Passphrase"
   :recover                               "Recupera"
   :twelve-words-in-correct-order         "12 parole nell'ordine corretto"

   ;;accounts
   :recover-access                        "Recupera accesso"
   :create-new-account                    "Crea un nuovo account"

   ;;wallet-qr-code
   :done                                  "Fatto"

   ;;validation
   :invalid-phone                         "Numero di telefono invalido"
   :amount                                "Quantità"

   ;;transactions
   :confirm                               "Conferma"
   :transaction                           "Transazione"
   :unsigned-transaction-expired          "Transazione non firmata scaduta"
   :status                                "Stato"
   :recipient                             "Ricevente"
   :to                                    "A"
   :from                                  "Da"
   :data                                  "Dati"
   :got-it                                "Ho capito"
   :block                                 "Blocco"
   :hash                                  "Hash"
   :gas-limit                             "Gas limit"
   :gas-price                             "Gas price"
   :gas-used                              "Gas usato"
   :cost-fee                              "Costo/Fee"
   :nonce                                 "Nonce"
   :confirmations                         "Conferme"
   :confirmations-helper-text             "Per favore aspetta almeno 12 conferme per essere sicuro che la tua transazione sia stata processata in sicurezza"
   :copy-transaction-hash                 "Copia l'hash della transazione"
   :open-on-etherscan                     "Apri su Etherscan.io"

   ;;webview
   :web-view-error                        "oops, errore"

   ;;testfairy warning
   :testfairy-title                       "Attenzione!"
   :testfairy-message                     "Stai usando un app installata da una build nightly. Per scopi di testing questa build include sessioni di registrazione se è usata la connessione wifi, così tutte le tue interazioni con l'app sono salvate (sia video che log) e possono essere usate dal nostro team di sviluppo per investigare possibili problemi. I video/log salvati non includono le tue password. La registrazione è fatta solo se l'app è installata da una build nightly. Niente viene registrato se l'app è installata dal PlayStore o da TestFlight."

   ;; wallet
   :wallet                                "Portafoglio"
   :wallets                               "Portafogli"
   :your-wallets                          "I tuoi Portafogli"
   :main-wallet                           "Portafoglio principale"
   :wallet-error                          "Errore durante il caricamento dei dati"
   :wallet-send                           "Invia"
   :wallet-request                        "Richiedi"
   :wallet-exchange                       "Scambia"
   :wallet-assets                         "Assets"
   :wallet-add-asset                      "Aggiungi asset"
   :wallet-total-value                    "Valore totale"
   :wallet-settings                       "Impostazioni del portafoglio"
   :signing-phrase-description            "Firma la transazione inserendo la tua password. Assicurati che le parole sopra corrispondano alla tua signing phrase segreta"
   :wallet-insufficient-funds             "Fondi insufficienti"
   :request-transaction                   "Richiesta di transazione"
   :send-request                          "Invia richiesta"
   :share                                 "Condividi"
   :eth                                   "ETH"
   :currency                              "Valuta"
   :usd-currency                          "USD"
   :transactions                          "Transazioni"
   :transaction-details                   "Dettagli della transazione"
   :transaction-failed                    "Transazione fallita"
   :transactions-sign                     "Firma"
   :transactions-sign-all                 "Firma tutto"
   :transactions-sign-transaction         "Firma transazione"
   :transactions-sign-later               "Firma dopo"
   :transactions-delete                   "Elimina transazione"
   :transactions-delete-content           "La transazione sarà rimossa dalla lista delle tranzazioni 'Non Firmate'"
   :transactions-history                  "Cronologia"
   :transactions-unsigned                 "Non Firmate"
   :transactions-history-empty            "Non hai ancora nessuna transazione nella cronologia"
   :transactions-unsigned-empty           "Non hai nessuna transazione non firmata"
   :transactions-filter-title             "Filtra cronologia"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Tipo"
   :transactions-filter-select-all        "Seleziona tutte"
   :view-transaction-details              "Vedi i dettagli della transazione"
   :transaction-description               "Per favore aspetta almeno 12 conferme per essere sicuro che la tua transazione sia stata processata in sicurezza"
   :transaction-sent                      "Transazione inviata"
   :transaction-moved-text                "La transazione rimarrà nella lista delle transazioni 'Non Firmate' per i prossimi 5 minuti"
   :transaction-moved-title               "Transazione spostata"
   :sign-later-title                      "Firmare la transazione dopo?"
   :sign-later-text                       "Controlla la cronologia delle transazioni per firmare questa transazione"
   :not-applicable                        "Non applicabile alle transazioni non firmate"

   ;; Wallet Send
   :wallet-choose-recipient               "Scegli destinatario"
   :wallet-choose-from-contacts           "Scegli Dai Contatti"
   :wallet-address-from-clipboard         "Usa Indirizzo Dagli Appunti"
   :wallet-invalid-address                "Indirizzo non valido: \n {{data}}"
   :wallet-browse-photos                  "Sfoglia Foto"
   :validation-amount-invalid-number      "La Quantità è un numero invalido"
   :validation-amount-is-too-precise      "L'ammontare è troppo preciso. La più piccola unità che puoi inviare è 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Nuova rete"
   :add-network                           "Aggiungi rete"
   :add-new-network                       "Aggiungi nuova rete"
   :existing-networks                     "Reti esistenti"
   :add-json-file                         "Aggiugni un file JSON"
   :paste-json-as-text                    "Incolla JSON come testo"
   :paste-json                            "Incolla JSON"
   :specify-rpc-url                       "Specifica un URL RPC"
   :edit-network-config                   "Modifica configurazioni di rete"
   :connected                             "Connesso"
   :process-json                          "Processa JSON"
   :error-processing-json                 "Errore nel processamento del JSON"
   :rpc-url                               "URL RPC"
   :remove-network                        "Rimuovi rete"
   :network-settings                      "Impostazioni di rete"
   :edit-network-warning                  "Attenzione, modificando i dettagli di questa rete potresti disabilitarla per te"
   :connecting-requires-login             "La connessione ad un'altra rete richiede il login"
   :close-app-title                       "Attenzione!"
   :close-app-content                     "L'app si fermerà e si chiuderà. Quando la riaprirai, verrà utilizzata la rete selezionata"
   :close-app-button                      "Conferma"})

(ns status-im.translations.la)
 
(def translations
  {
   ;common
   :members-title                         "sodalis"
   :not-implemented                       "ne attexit"
   :chat-name                             "nomen colloquii"
   :notifications-title                   "nuntiationes"
   :offline                               "abesse"
 
   ;drawer
   :invite-friends                        "invitare amico"
   :faq                                   "FAQ"
   :switch-users                          "utens ex cambit"
 
   ;chat
   :is-typing                             "conscribit"
   :and-you                               "et tu"
   :search-chat                           "perscruta colloquii"
   :members                               {:one   "I sodalis"
                                           :other "{{count}} sodalis"
                                           :zero  "nullus sodalis"}
   :members-active                        {:one   "I soladis, I aktive"
                                           :other "{{count}} sodalis, {{count}} aktivi"
                                           :zero  "nullus sodalis"}
   :active-online                         "adesse"
   :active-unknown                        "incognitus"
   :available                             "relicus"
   :no-messages                           "ne nuntii"
   :suggestions-requests                  "precatus"
   :suggestions-commands                  "jussum"
 
   ;sync
   :sync-in-progress                      "synchronizari..."
   :sync-synced                           "synchronizatum esse"
 
   ;messages
   :status-sending                        "allegere"
   :status-pending                        "tractare"
   :status-sent                           "deditum iri"
   :status-seen-by-everyone               "adspectatum esse ab omnibus"
   :status-seen                           "adspectatum esse"
   :status-delivered                      "deditum esse"
   :status-failed                         "vitiosus"
 
   ;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "pars minuta secunda horae"
                                           :other "pars minuta secunda horae"}
   :datetime-minute                       {:one   "pars minuta horae"
                                           :other "pars minuta horae"}
   :datetime-hour                         {:one   "hora"
                                           :other "horae"}
   :datetime-day                          {:one   "dies"
                                           :other "dies"}
   :datetime-multiple                     "s"
   :datetime-ago                          "anto"
   :datetime-yesterday                    "heri"
   :datetime-today                        "hodie"
 
   ;profile
   :profile                               "biographia"
   :report-user                           "UTENS QUADRUPLARI"
   :message                               "nuntium"
   :username                              "utens nominis"
   :not-specified                         "ne adnotans"
   :public-key                            "publica clavis"
   :phone-number                          "nummerus telephonicus"
   :email                                 "smaltum"
   :profile-no-status                     "nullus status"
   :add-to-contacts                       "adicere ad contagio"
   :error-incorrect-name                  "optare alium nomen suis"
   :error-incorrect-email                 "falsum smaltum"
 
   ;;make_photo
   :image-source-title                    "photographia bigraphiae"
   :image-source-make-photo               "photographere"
   :image-source-gallery                  "eligere ad pinacothecae"
   :image-source-cancel                   "defringere"
 
   ;sign-up
   :contacts-syncronized                  "contagium sui synchronizatum esse."
   :confirmation-code                     (str "gratias tibi! misimus nuntium conscriptionis cum notae. "
                                               "transcribe notam, ut nummerum telefonicum possimus verificare.")
   :incorrect-code                        (str "nos paenite, nota falsa, perscribe denuo")
   :generate-passphrase                   (str "computo phrasis arcana pro suo, ut "
                                               "arripiens recreare aut arripere alii instrumento profiteri potest.")
   :phew-here-is-your-passphrase          "*uph* erat gravor. hic phrasis arcana sui est, *exscribe et adserva!* necesse est, ut benificium sui recreare."
   :here-is-your-passphrase               "hic phrasis arcana sui est, *exscribe et adserva!* necesse est, ut benificium sui recreare."
   :written-down                          "exscribere omnes? nisi exscribe nunc"
   :phone-number-required                 "attige ipse, et transcribe numerus telephonicus sui. deinde inveniar amici sui."
   :intro-status                          "habere colloquium cum me, ut syngraphus sui congerit et moderationes sui mutare!"
   :intro-message1                        "salve! hic Status est!.\n attige id nuntium, ut signum arcanum sui congere et incipere!"
   :account-generation-message            "mane! cumputo benificium sui."
 
   ;chats
   :chats                                 "colloquia"
   :new-chat                              "novum  colloquium"
   :new-group-chat                        "novum colloquium circli"
 
   ;discover
   :discover                              "invenite"
   :none                                  "nihil"
   :search-tags                           "perscribe hic verbum querentis"
   :popular-tags                          "favorabilis opiones"
   :recent                                "novae"
   :no-statuses-discovered                "nullus status repertum esse"
 
   ;settings
   :settings                              "moderationes"
 
   ;contacts
   :contacts                              "contagiis"
   :new-contact                           "novum contagium"
   :show-all                              "elucere omnibus"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "poplus"
   :contacts-group-new-chat               "novum colloquium circli"
   :no-contacts                           "Nulla contagio"
   :show-qr                               "QR-nota elucere"
 
   ;group-settings
   :remove                                "amovere"
   :save                                  "apothecare"
   :change-color                          "color mutare"
   :clear-history                         "processus amovere"
   :delete-and-leave                      "amovere et relinquere"
   :chat-settings                         "moderationes colloquiorum"
   :edit                                  "commutare"
   :add-members                           "sodalis adjungere"
   :blue                                  "caeruleum"
   :purple                                "austrum"
   :green                                 "viridis"
   :red                                   "rubius"
 
   ;commands
   :money-command-description             "pecunia mittere"
   :location-command-description          "locum mittere"
   :phone-command-description             "numerus telephonicus mittere"
   :phone-request-text                    "numerus telephonicus exquirere"
   :confirmation-code-command-description "nota mittere"
   :confirmation-code-request-text        "nota exquirere"
   :send-command-description              "locum mittere"
   :request-command-description           "consulatio mittere"
   :keypair-password-command-description  ""
   :help-command-description              "auxilium"
   :request                               "consulatio"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH ad {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH ab {{chat-name}}"
 
   ;new-group
   :group-chat-name                       "nomen colloquio circli"
   :empty-group-chat-name                 "perscribe nomen colloquio circli"
   :illegal-group-chat-name               "perscribe nomen aliud"
 
   ;participants
   :add-participants                      "adsecula adjungere"
   :remove-participants                   "adsecula relinquere"
 
   ;protocol
   :received-invitation                   "nancisci colloquium circli"
   :removed-from-chat                     "ex colloquium circli relinquere"
   :left                                  "desse"
   :invited                               "invitavisse"
   :removed                               "reliquisse"
   :You                                   "tu"
 
   ;new-contact
   :add-new-contact                       "nova  contagio adjungere"
   :import-qr                             "importare"
   :scan-qr                               "QR-nota photographere"
   :name                                  "nomen"
   :whisper-identity                      "identitas susurrare"
   :address-explication                   "explain what adresses are" ; TODO
   :enter-valid-address                   "perscribe ratum adloquium aut photographare QR-nota"
   :contact-already-added                 "iste contagio jam adjunctum esse"
   :can-not-add-yourself                  "ne potest adjungere ipse!"
   :unknown-address                       "ignotum adloquium"
 
 
   ;login
   :connect                               "iungere"
   :address                               "adloquium"
   :password                              "signum arcanum"
   :login                                 "profitere"
   :wrong-password                        "falsum signum acranum"
 
   ;recover
   :recover-from-passphrase               "recreare alcis auxilio phrasis arcana"
   :recover-explain                       "perscribe phrasis arcana pro signo arcano, ut recreare dicionem"
   :passphrase                            "phrasis arcana"
   :recover                               "recreare"
   :enter-valid-passphrase                "perscribe phrasis arcana"
   :enter-valid-password                  "perscribe signum arcanum"
 
   ;accounts
   :recover-access                        "restituere possessio"
   :add-account                           "benificium addere"
 
   ;wallet-qr-code
   :done                                  "paratus"
   :main-wallet                           "crumera caput"
 
   ;validation
   :invalid-phone                         "nummerus telephonicus"
   :amount                                "summa"
   :not-enough-eth                        (str "crumera haud satis EHT habet"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "authenticare transmissio"
                                           :other "{{count}} authenticare transmissiones"
                                           :zero  "nulla transmissio"}
   :status                                "status"
   :pending-confirmation                  "Confirmatio in tractatio"
   :recipient                             "acceptor"
   :one-more-item                         "etiam res"
   :fee                                   "theolonium"
   :value                                 "precium"
 
   ;:webview
   :web-view-error                        "Erratum ad adspectus"})

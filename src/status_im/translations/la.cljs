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
   :datetime-ago                          "anto"
   :datetime-yesterday                    "heri"
   :datetime-today                        "hodie"
 
   ;profile
   :profile                               "biographia"
   :message                               "nuntium"
   :not-specified                         "ne adnotans"
   :public-key                            "publica clavis"
   :phone-number                          "nummerus telephonicus"
   :add-to-contacts                       "adicere ad contagio"
   ;;make_photo
   :image-source-title                    "photographia bigraphiae"
   :image-source-make-photo               "photographere"
   :image-source-gallery                  "eligere ad pinacothecae"
   ;sign-up
   :contacts-syncronized                  "contagium sui synchronizatum esse."
   :confirmation-code                     (str "gratias tibi! misimus nuntium conscriptionis cum notae. "
                                               "transcribe notam, ut nummerum telefonicum possimus verificare.")
   :incorrect-code                        (str "nos paenite, nota falsa, perscribe denuo")
   :phew-here-is-your-passphrase          "*uph* erat gravor. hic phrasis arcana sui est, *exscribe et adserva!* necesse est, ut benificium sui recreare."
   :here-is-your-passphrase               "hic phrasis arcana sui est, *exscribe et adserva!* necesse est, ut benificium sui recreare."
   :phone-number-required                 "attige ipse, et transcribe numerus telephonicus sui. deinde inveniar amici sui."
   :intro-status                          "habere colloquium cum me, ut syngraphus sui congerit et moderationes sui mutare!"
   :intro-message1                        "salve! hic Status est!.\n attige id nuntium, ut signum arcanum sui congere et incipere!"
   :account-generation-message            "mane! cumputo benificium sui."
 
   ;chats
   :chats                                 "colloquia"
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
   :contacts-group-new-chat               "novum colloquium circli"
   :no-contacts                           "Nulla contagio"
   :show-qr                               "QR-nota elucere"
 
   ;group-settings
   :remove                                "amovere"
   :save                                  "apothecare"
   :clear-history                         "processus amovere"
   :chat-settings                         "moderationes colloquiorum"
   :edit                                  "commutare"
   :add-members                           "sodalis adjungere"
   ;commands
   :chat-send-eth                         "{{amount}} ETH"
   ;new-group
   ;participants
   ;protocol
   :received-invitation                   "nancisci colloquium circli"
   :removed-from-chat                     "ex colloquium circli relinquere"
   :left                                  "desse"
   :invited                               "invitavisse"
   :removed                               "reliquisse"
   :You                                   "tu"
 
   ;new-contact
   :add-new-contact                       "nova  contagio adjungere"
   :scan-qr                               "QR-nota photographere"
   :name                                  "nomen"
   :address-explication                   "explain what adresses are" ; TODO
   :contact-already-added                 "iste contagio jam adjunctum esse"
   :can-not-add-yourself                  "ne potest adjungere ipse!"
   :unknown-address                       "ignotum adloquium"
 
 
   ;login
   :connect                               "iungere"
   :address                               "adloquium"
   :password                              "signum arcanum"
   :wrong-password                        "falsum signum acranum"
 
   ;recover
   :passphrase                            "phrasis arcana"
   :recover                               "recreare"
   ;accounts
   :recover-access                        "restituere possessio"
   ;wallet-qr-code
   :done                                  "paratus"
   :main-wallet                           "crumera caput"
 
   ;validation
   :invalid-phone                         "nummerus telephonicus"
   :amount                                "summa"
   ;transactions
   :status                                "status"
   :recipient                             "acceptor"
   ;:webview
   :web-view-error                        "Erratum ad adspectus"})

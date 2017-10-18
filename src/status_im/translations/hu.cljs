(ns status-im.translations.hu)

(def translations
  {
   ;common
   :members-title                         "Tagok"
   :not-implemented                       "!nem végrehajtott"
   :chat-name                             "Csevegés neve"
   :notifications-title                   "Értesítések és hangok"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Barátok meghívása"
   :faq                                   "GYIK"
   :switch-users                          "Felhasználók váltása"

   ;chat
   :is-typing                             "gépel"
   :and-you                               "és te"
   :search-chat                           "Csevegés keresése"
   :members                               {:one   "1 tag"
                                           :other "{{count}} tag"
                                           :zero  "nincsenek tagok"}
   :members-active                        {:one   "1 tag, 1 aktív"
                                           :other "{{count}} tag, {{count}} aktív"
                                           :zero  "nincsenek tagok"}
   :active-online                         "Online"
   :active-unknown                        "Ismeretlen"
   :available                             "Elérhető"
   :no-messages                           "Nincsenek üzenetek"
   :suggestions-requests                  "Kérések"
   :suggestions-commands                  "Parancsok"

   ;sync
   :sync-in-progress                      "Szinkronizálás..."
   :sync-synced                           "Szinkronizálás folyamatban"

   ;messages
   :status-sending                        "Küldés"
   :status-pending                        "Függőben levő"
   :status-sent                           "Elküldve"
   :status-seen-by-everyone               "Látva mindenki által"
   :status-seen                           "Látva"
   :status-delivered                      "Kézbesítve"
   :status-failed                         "Nem sikerült"

   ;datetime
   :datetime-second                       {:one   "másodperc"
                                           :other "másodperc"}
   :datetime-minute                       {:one   "perc"
                                           :other "perc"}
   :datetime-hour                         {:one   "óra"
                                           :other "óra"}
   :datetime-day                          {:one   "nap"
                                           :other "nap"}
   :datetime-multiple                     "k"
   :datetime-ago                          "ezelőtt"
   :datetime-yesterday                    "tegnap"
   :datetime-today                        "ma"

   ;profile
   :profile                               "Profil"
   :report-user                           "FELHASZNÁLÓ JELENTÉSE"
   :message                               "Üzenet"
   :username                              "Felhasználónév"
   :not-specified                         "Nincs megadva"
   :public-key                            "Nyilvános kulcs"
   :phone-number                          "Telefonszám"
   :email                                 "E-mail"
   :profile-no-status                     "Nincs állapot"
   :add-to-contacts                       "Hozzáadás a kapcsolatokhoz"
   :error-incorrect-name                  "Kérjük, válassz ki másik nevet"
   :error-incorrect-email                 "Hibás e-mail"

   ;;make_photo
   :image-source-title                    "Profilkép"
   :image-source-make-photo               "Rögzítés"
   :image-source-gallery                  "Kiválasztás a galériából"
   :image-source-cancel                   "Mégsem"

   ;sign-up
   :contacts-syncronized                  "Kapcsolataid szinkronizálásra kerültek"
   :confirmation-code                     (str "Köszönjük! Küldtünk neked egy szöveges üzenetet megerősítési "
                                               "kóddal. Kérjük, add meg a kódot telefonszámod megerősítése érdekében")
   :incorrect-code                        (str "Sajnáljuk, hibás kód, kérjük, add meg újból")
   :generate-passphrase                   (str "Generálok neked egy jelmondatot hozzáférésed helyreállításához "
                                               "vagy egy másik eszközről történő bejelentkezéshez")
   :phew-here-is-your-passphrase          "*Hűha* ez nehéz volt, de kész a jelmondatod, *írd fel valahova és vigyázz rá!* Szükséged lesz hozzá felhasználói fiókod helyreállításához."
   :here-is-your-passphrase               "Kész a jelmondatod, *írd fel valahová és őrizd meg!* Szükséged lesz hozzá felhasználói fiókod helyreállításához."
   :written-down                          "Bizonyosodj meg arról, hogy biztonságos helyen tárolod"
   :phone-number-required                 "Érints ide telefonszámod megadásához és megtalálom a barátaidat"
   :intro-status                          "Csevegj velem felhasználói fiókod létrehozásáról és beállításaid megváltoztatásáról!"
   :intro-message1                        "Üdv az Állapotnál\nÉrints erre a üzenetre, állítsd be a jelszavad és fogj hozzá!"
   :account-generation-message            "Adj egy percet, varázsolok egy kicsit és létre is hozom a felhasználói fiókodat!"

   ;chats
   :chats                                 "Csevegések"
   :new-chat                              "Új csevegés"
   :new-group-chat                        "Új csoportos csevegés"

   ;discover
   :discover                              "Felfedezés"
   :none                                  "Semmi"
   :search-tags                           "Add meg keresési címkéidet itt"
   :popular-tags                          "Népszerű címkék"
   :recent                                "Legutóbbi"
   :no-statuses-discovered                "Nincsenek felfedezett állapotok"

   ;settings
   :settings                              "Beállítások"

   ;contacts
   :contacts                              "Kapcsolatok"
   :new-contact                           "Új kapcsolat"
   :show-all                              "ÖSSZES MUTATÁSA"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Emberek"
   :contacts-group-new-chat               "Új csevegés indítása"
   :no-contacts                           "Még nincsenek kapcsolatok"
   :show-qr                               "QR mutatása"

   ;group-settings
   :remove                                "Eltávolítás"
   :save                                  "Mentés"
   :change-color                          "Szín megváltoztatása"
   :clear-history                         "Előzmények törlése"
   :delete-and-leave                      "Törlés és kilépés"
   :chat-settings                         "Csevegés beállítások"
   :edit                                  "Szerkesztés"
   :add-members                           "Tagok hozzáadása"
   :blue                                  "Kék"
   :purple                                "Lila"
   :green                                 "Zöld"
   :red                                   "Piros"

   ;commands
   :money-command-description             "Pénz küldése"
   :location-command-description          "Halyszín küldése"
   :phone-command-description             "Telefonszám küldése"
   :phone-request-text                    "Telefonszám irénylése"
   :confirmation-code-command-description "Megerősítési kód küldése"
   :confirmation-code-request-text        "Megerősítési kód igénylése"
   :send-command-description              "Helyszín küldése"
   :request-command-description           "Küldési igény"
   :keypair-password-command-description  ""
   :help-command-description              "Segítség"
   :request                               "Kérés"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH ide {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH innen {{chat-name}}"

   ;new-group
   :group-chat-name                       "Csevegés neve"
   :empty-group-chat-name                 "Kérjük, add meg egy új nevet"
   :illegal-group-chat-name               "Kérjük, válassz egy új nevet"

   ;participants
   :add-participants                      "Résztvevők hozzáadása"
   :remove-participants                   "Résztvevők eltávolítása"

   ;protocol
   :received-invitation                   "csevegési meghívásban részesült"
   :removed-from-chat                     "eltávolítva a csoportos csevegésből"
   :left                                  "maradt"
   :invited                               "meghívott"
   :removed                               "eltávolított"
   :You                                   "Te"

   ;new-contact
   :add-new-contact                       "Új kapcsolat hozzáadása"
   :import-qr                             "Importálás"
   :scan-qr                               "QR beolvasása"
   :name                                  "Név"
   :whisper-identity                      "Whisper személyazonosság"
   :address-explication                   "Itt talán szükség lenne egy kis szövegre, ami elmagyarázná, mi is az a cím és hol lehet megtalálni"
   :enter-valid-address                   "Kérjük, adj meg egy helyes címet vagy olvass be egy QR kódot"
   :contact-already-added                 "A kapcsolat már hozzáadásra került"
   :can-not-add-yourself                  "Magadat nem adhatod hozzá"
   :unknown-address                       "Ismeretlen cím"


   ;login
   :connect                               "Kapcsolódás"
   :address                               "Cím"
   :password                              "Jelszó"
   :login                                 "Bejelentkezés"
   :wrong-password                        "Hibás jelszó"

   ;recover
   :recover-from-passphrase               "Visszaállítás jelmondatból"
   :recover-explain                       "Kérjük, adj meg egy jelmondatot a jelszavaddal történő hozzáférés helyreállításához"
   :passphrase                            "Jelmondat"
   :recover                               "Visszaállítás"
   :enter-valid-passphrase                "Kérjük, adj meg egy jelmondatote"
   :enter-valid-password                  "Kérjük, adj meg egy jelszót"

   ;accounts
   :recover-access                        "Hozzáférés helyreállítása"
   :add-account                           "Felhasználói fiók hozzáadása"

   ;wallet-qr-code
   :done                                  "Kész"
   :main-wallet                           "Fő zseb"

   ;validation
   :invalid-phone                         "Hibás telefonszám"
   :amount                                "Összeg"
   :not-enough-eth                        (str "Nincs elég ETH a számlán "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Tranzakció megerősítése"
                                           :other "{{count}} tranzakció megerősítése"
                                           :zero  "Nincsenek tranzakciók"}
   :status                                "Állapot"
   :pending-confirmation                  "Függőben lévő megerősítés"
   :recipient                             "Címzett"
   :one-more-item                         "Még egy tétel"
   :fee                                   "Díj"
   :value                                 "Érték"

   ;:webview
   :web-view-error                        "hoppá, hiba"

   :confirm                               "Megerősít"
   :phone-national                        "Nemzeti"
   :transactions-confirmed                {:one   "Tranzakció megerősítve"
                                           :other "{{count}} tranzakció megerősítve"
                                           :zero  "Nincs megerősített tranzakció"}
   :public-group-topic                    "Téma"
   :debug-enabled                         "A hibakereső szerver elindításra került! Mostantól hozzáadhatod a DAppod, ha futtatod a számítógépeden a következőt: *status-dev-cli scan*"
   :new-public-group-chat                 "Csatlakozás nyilvános csevegéshez"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Mégse"
   :share-qr                              "QR megosztása"
   :feedback                              "Visszajelzésed van?\nRázd meg a telefonod!"
   :twelve-words-in-correct-order         "12 szó helyes sorrendben"
   :remove-from-contacts                  "Eltávolítás a névjegyekből"
   :delete-chat                           "Csevegés törlése"
   :edit-chats                            "Csevegések szerkesztése"
   :sign-in                               "Bejelentkezés"
   :create-new-account                    "Új fiók létrehozása"
   :sign-in-to-status                     "Bejelentkezés Állapot megosztásához"
   :got-it                                "Értem"
   :move-to-internal-failure-message      "Át kell helyeznünk néhány fontos fájlt a külső tárhelyről a belső tárhelyre. Ehhez szükségünk van az engedélyedre. A jövőbeni verziókban nem fogjuk használni a külső tárhelyet."
   :edit-group                            "Csoport szerkesztése"
   :delete-group                          "Csoport törlése"
   :browsing-title                        "Böngészés"
   :reorder-groups                        "Csoport átrendezése"
   :browsing-cancel                       "Mégse"
   :faucet-success                        "A Faucet kérés beérkezett"
   :choose-from-contacts                  "Választás a névjegyekből"
   :new-group                             "Új csoport"
   :phone-e164                            "1. nemzetközi"
   :remove-from-group                     "Eltávolítás a csoportból"
   :search-contacts                       "Névjegyek keresése"
   :transaction                           "Tranzakció"
   :public-group-status                   "Nyilvános"
   :leave-chat                            "Kilépés a csevegésből"
   :start-conversation                    "Beszélgetés indítása"
   :topic-format                          "Rossz formátum [a-z0-9\\-]+"
   :enter-valid-public-key                "Kérjük, adj meg egy érvényes nyilvános billentyűkombinációt vagy olvasd be a QR-kódot"
   :faucet-error                          "Hiba a Faucet kérés során"
   :phone-significant                     "Jelentős"
   :search-for                            "Keresés a következőre: ..."
   :sharing-copy-to-clipboard             "Másolás vágólapra"
   :phone-international                   "2. nemzetközi"
   :enter-address                         "Cím megadása"
   :send-transaction                      "Tranzakció küldése"
   :delete-contact                        "Névjegy törlése"
   :mute-notifications                    "Értesítések elnémítása"


   :contact-s                             {:one   "névjegy"
                                           :other "névjegyek"}
   :group-name                            "Csoportnév"
   :next                                  "Következő"
   :from                                  "Feladó"
   :search-chats                          "Csevegések keresése"
   :in-contacts                           "Névjegyekben"

   :sharing-share                         "Megosztás..."
   :type-a-message                        "Írj egy üzenetet..."
   :type-a-command                        "Kezdj el begépelni egy parancsot..."
   :shake-your-phone                      "Találtál egy hibát vagy javaslatod van? Csak ~rázd~ meg a telefonod!"
   :status-prompt                         "Hozz létre egy állapotot és segíts az embereknek, hogy megismerjék az általad kínált dolgokat. Használhatsz #hashtageket is."
   :add-a-status                          "Állapot hozzáadása..."
   :error                                 "Hiba"
   :edit-contacts                         "Névjegyek szerkesztése"
   :more                                  "több"
   :cancel                                "Mégse"
   :no-statuses-found                     "Nem található állapot"
   :swow-qr                               "QR megjelenítése"
   :browsing-open-in-web-browser          "Megnyitás új böngészőben"
   :delete-group-prompt                   "Nem lesz hatással a névjegyekre"
   :edit-profile                          "Profil szerkesztése"


   :enter-password-transactions           {:one   "Erősítsd meg a tranzakciót a jelszavad megadásával"
                                           :other "Erősítsd meg a tranzakciókat a jelszavad megadásával"}
   :unsigned-transactions                 "Aláíratlan tranzakciók"
   :empty-topic                           "Üres téma"
   :to                                    "Címzett"
   :group-members                         "Csoporttagok"
   :estimated-fee                         "Becs. díj"
   :data                                  "Adatok"})

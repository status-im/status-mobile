(ns status-im.translations.lt)

(def translations
  {
   ;;common
   :members-title                         "Nariams"
   :not-implemented                       "neįgyvendinta"
   :chat-name                             "Pokalbio pavadinimas"
   :notifications-title                   "Perspėjimai ir garsai"
   :offline                               "Neprisijungęs"
   :search-for                            "Ieskoti..."
   :cancel                                "Atšaukti"
   :next                                  "Sekantis"
   :open                                  "Atidaryti"
   :description                           "Aprašymas"
   :url                                   "URL"
   :type-a-message                        "Įveskite pranešimą..."
   :type-a-command                        "Pradėkite rašyti komandą..."
   :error                                 "Klaida"
   :unknown-status-go-error               "Nežinoma status-go klaida"
   :node-unavailable                      "Joks ethereum mazgas neveikia"
   :yes                                   "Taip"
   :no                                    "Ne"

   :camera-access-error                   "Norėdami suteikti reikalingą fotoaparato leidimą, eikite į savo sistemos nustatymus ir įsitikinkite, kad pasirinktas Status > Camera."
   :photos-access-error                   "Norėdami suteikti reikalingą fotoaparato leidimą, eikite į savo sistemos nustatymus ir įsitikinkite, kad pasirinktas Status > Camera."

   ;;drawer
   :invite-friends                        "Pakviesti draugus"
   :faq                                   "DUK"
   :switch-users                          "Pasikeisti naudotoją"
   :feedback                              "Turite atsiliepimą?\nPakratykite telefoną!"
   :view-all                              "Rodyti viską"
   :current-network                       "Dabartinis tinklas"

   ;;chat
   :is-typing                             "rašo"
   :and-you                               "ir tu"
   :search-chat                           "Ieškoti pokalbyje"
   :members                               {:one   "1 narys"
                                           :other "{{count}} nariai"
                                           :zero  "jokių nariū"}
   :members-active                        {:one   "1 nrys"
                                           :other "{{count}} nariai"
                                           :zero  "jokių nariū"}
   :public-group-status                   "Viešas"
   :active-online                         "Prisijungęs"
   :active-unknown                        "Nežinomas"
   :available                             "Prieinama"
   :no-messages                           "Jokių žinučių"
   :suggestions-requests                  "Užklausimai"
   :suggestions-commands                  "Komandos"
   :faucet-success                        "Dalintuvas užklausa gauta"
   :faucet-error                          "Dalintuvas užklausos klaida"

   ;;sync
   :sync-in-progress                      "Sinchronizuojama..."
   :sync-synced                           "Sinchronizuota"

   ;;messages
   :status-sending                        "Siunčiama"
   :status-pending                        "Laukiama"
   :status-sent                           "Nusiūsta"
   :status-seen-by-everyone               "Visų matyta"
   :status-seen                           "Matyta"
   :status-delivered                      "Pristatyta"
   :status-failed                         "Nepavykęs"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "sekundė"
                                           :other "sekundės"}
   :datetime-minute                       {:one   "minutė"
                                           :other "minutės"}
   :datetime-hour                         {:one   "valanda"
                                           :other "valandos"}
   :datetime-day                          {:one   "diena"
                                           :other "dienos"}
   :datetime-multiple                     "s"
   :datetime-ago                          "prieš"
   :datetime-yesterday                    "vakar"
   :datetime-today                        "šiandien"

   ;;profile
   :profile                               "Profilis"
   :edit-profile                          "Redaguoti profilį"
   :report-user                           "Pranešti apie vartotoją"
   :message                               "Žinutė"
   :username                              "Prisijungimo vardas"
   :not-specified                         "Nenurodyta"
   :public-key                            "Viešas raktas"
   :phone-number                          "Telefono numeris"
   :email                                 "El. paštas"
   :update-status                         "Atsinaujinkite būseną..."
   :add-a-status                          "Pridėkite būseną..."
   :status-prompt                         "Sukurkite būseną, kad žmonės žinotų, ką jūs siūlote. Taip pat galite naudoti #hashtagus."
   :add-to-contacts                       "Pridėti prie kontaktų"
   :in-contacts                           "Kontaktuose"
   :remove-from-contacts                  "Ištrinti iš kontaktų"
   :start-conversation                    "Pradėti pokalbį"
   :send-transaction                      "Siųsti transakciją"
   :share-qr                              "Dalintis QR"
   :error-incorrect-name                  "Pasirinkite kita vardą"
   :error-incorrect-email                 "Neteisingas el. paštas"
   :profile-testnet-icon                  "Testnet"
   :profile-testnet-text                  "Tik Testnet. Nesiųskite ETH arba SNT į savo adresą"

   ;;make_photo
   :image-source-title                    "Profilio atvaizdai"
   :image-source-make-photo               "Fotografuoti"
   :image-source-gallery                  "Pasirinkti iš galerijos"
   :image-source-cancel                   "Atšaukti"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopijuoti į iškarpinę"
   :sharing-share                         "Dalintis..."
   :sharing-cancel                        "Atšaukti"

   :browsing-title                        "Naršyti"
   :browsing-browse                       "@naršyti"
   :browsing-open-in-web-browser          "Atverti naršyklėje"
   :browsing-cancel                       "Naršyti"

   ;;sign-up
   :contacts-syncronized                  "Jūsų kontaktai buvo sinchronizuoti"
   :confirmation-code                     (str "Dėkui! Mes išsiuntėme jums teksto pranešimą su patvirtinimo kodu. Prašau nurodyti kodą, kad patvirtintumėte savo telefono numerį")
   :incorrect-code                        (str "Atsiprašome, kodas neteisingas, prašome įvesti dar kartą")
   :generate-passphrase                   (str "Aš sukursiu slaptažodį, kad galėtumėte atkurti savo prieigą arba prisijungti iš kito įrenginio")
   :phew-here-is-your-passphrase          "* Vajė *, buvo sunkoka, čia yra jūsų slaptažodis, * išsaugokite jį! * jeigu reikės atkurti jūsų paskyrą."
   :here-is-your-passphrase               "Čia yra jūsų slaptažodis, * išsaugokite jį! * jeigu reikės atkurti jūsų paskyrą."
   :here-is-your-signing-phrase           "Čia yra jūsų pasirašymo frazė. Ją naudosite, kad patvirtintumėte savo sandorius. * Užsirašykite ir saugokite!"
   :written-down                          "Įsitikinkite, kad saugiai jį pasirašėte"
   :phone-number-required                 "Palieskite čia, jei norite įvesti savo telefono numerį, tam kad rasti jūsų draugus"
   :shake-your-phone                      "Radote klaidą arba turite pasiūlymų? Tiesiog ~pakratykite~ savo telefoną!"
   :intro-status                          "Naudokitės šiuo pokalbiu, kad nustatytumėte savo paskyrą ir pakeistumėte savo nustatymus!"
   :intro-message1                        "Sveiki atvykę į Status \nPalieskite šį pranešimą, kad nustatytumėte slaptažodį ir pradėtumėte!"
   :account-generation-message            "Sekundėlę, vyksta nemenki skaičiavimai!"
   :move-to-internal-failure-message      "Mums reikia perkelti keletą svarbių failų iš išorinės į vidinę atmintį. Norėdami tai padaryti, mums reikia jūsų leidimo. Būsimose versijose mes nenaudosime išorinės atminties."
   :debug-enabled                         "Debug serveris buvo paleistas! Dabar galite paleisti *status-dev-cli scan*, kad surastumėte serverį iš savo kompiuterio tame pačiame tinkle."

   ;;phone types
   :phone-e164                            "Tarptautinis 1"
   :phone-international                   "Tarptautinis 2"
   :phone-national                        "Vietinis"
   :phone-significant                     "Reikšmingas"

   ;;chats
   :chats                                 "Pokalbiai"
   :new-chat                              "Naujas pokalbis"
   :delete-chat                           "Trinti pokalbį"
   :new-group-chat                        "Naujas grupinis pokalbis"
   :new-public-group-chat                 "Jungtis prie grupinio pokalbio"
   :edit-chats                            "Redaguoti grupinius pokalbius"
   :search-chats                          "Ieskoti pokalbių"
   :empty-topic                           "Tuščia tema"
   :topic-format                          "Netinkamas formatas [a-z0-9\\-]+"
   :public-group-topic                    "Tema"

   ;;discover
   :discover                              "Atrasti"
   :none                                  "Jokio"
   :search-tags                           "Įveskite paieškos kriterijus"
   :popular-tags                          "Populiarūs hashtagai"
   :recent                                "Paskiausios būsenos"
   :no-statuses-found                     "Jokių busenų nerasta"
   :chat                                  "Pokalbis"
   :all                                   "Viskas"
   :public-chats                          "Vieši pokalbiai"
   :soon                                  "Greitai"
   :public-chat-user-count                "{{count}} asmenys"
   :dapps                                 "DApp'ai"
   :no-statuses-discovered                "Jokių būsenų nerasta"
   :no-statuses-discovered-body           "Kai kas nors paskelbia būsena,\nmatysite ją čia."
   :no-hashtags-discovered-title          "Jokių hashtagų nerasta"
   :no-hashtags-discovered-body           "Kai hashtagas tampa populiarus,\nmatysite jį čia."

   ;;settings
   :settings                              "Nustatymai"

   ;;contacts
   :contacts                              "Kontaktai"
   :new-contact                           "Nauji kontaktai"
   :delete-contact                        "Trinti kontakta"
   :delete-contact-confirmation           "Šis yrašas bus pasalintas iš kontaktų"
   :remove-from-group                     "Pašalinti iš grupės"
   :edit-contacts                         "Redaguoti kontaktus"
   :search-contacts                       "Ieškoti kontaktų"
   :show-all                              "RODYTI VISKĄ"
   :contacts-group-dapps                  "ÐApp'ai"
   :contacts-group-people                 "Asmenys"
   :contacts-group-new-chat               "Pradėti naują pokalbį"
   :choose-from-contacts                  "Pasirinkti iš kontaktų"
   :no-contacts                           "Jokių kontaktų nerasta"
   :show-qr                               "Rodyti QR"
   :enter-address                         "Įveskite adresą"
   :more                                  "daugiau"

   ;;group-settings
   :remove                                "Pašalinti"
   :save                                  "issaugoti"
   :delete                                "Ištrinti"
   :change-color                          "Keisti spalvą"
   :clear-history                         "Naikinti istoriją"
   :mute-notifications                    "Nutildyti pranešimus"
   :leave-chat                            "Palikti pokalbį"
   :delete-and-leave                      "Trinti ir išeiti"
   :chat-settings                         "Pokalbio nustatymai"
   :edit                                  "Redaguoti"
   :add-members                           "Pridėti narių"
   :blue                                  "Mėlyna"
   :purple                                "Purpurinis"
   :green                                 "Žalia"
   :red                                   "Rudona"

   ;;commands
   :money-command-description             "Siūsti pinigų"
   :location-command-description          "Siūsti koordinates"
   :phone-command-description             "Siūsti telefono numerį"
   :phone-request-text                    "Telefono numerio užklausa"
   :confirmation-code-command-description "Siūsti patvirtinimo kodą"
   :confirmation-code-request-text        "Konfiguravimo kodo užklausa"
   :send-command-description              "Siūsti komandą"
   :request-command-description           "Siūsti užklausą"
   :keypair-password-command-description  "Raktinės komandos aprašymas..."
   :help-command-description              "Pagalba"
   :request                               "Užklausa"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH siūsti {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH nuo {{chat-name}}"

   ;;location command
   :your-current-location                 "Jūsų dabartinė vieta"
   :places-nearby                         "Netolimos vietos"
   :search-results                        "Paieškos rezultatai"
   :dropped-pin                           "Lokacijos žymeklis"
   :location                              "Lokacija"
   :open-map                              "Atverti žemėlapį"
   :sharing-copy-to-clipboard-address     "Kopijuoti adresą"
   :sharing-copy-to-clipboard-coordinates "Kopijuoti koordinates"

   ;;new-group
   :group-chat-name                       "Pokalbio vardas"
   :empty-group-chat-name                 "Pasirinkite vardą"
   :illegal-group-chat-name               "Pasirinkite kitą vardą"
   :new-group                             "nauja grupė"
   :reorder-groups                        "Rušiuoti grupes"
   :group-name                            "Grupės vardas"
   :edit-group                            "Redaguoti grupę"
   :delete-group                          "Trinti grupę"
   :delete-group-confirmation             "Ši grupė bus pašalinta is jūsų grupių sarašo. Jūsų kontaktai nepakis."
   :delete-group-prompt                   "Tai neįtakos jūsų kontaktų"
   :group-members                         "Grupės nariai"
   :contact-s                             {:one   "kontactas"
                                           :other "kontactai"}

   ;;participants
   :add-participants                      "Pridėti dalyvių"
   :remove-participants                   "Pašalinti dalyvių"

   ;;protocol
   :received-invitation                   "Gautas pakvietimas į pokalbį"
   :removed-from-chat                     "pašalino tave iš pokalbio"
   :left                                  "išejo"
   :invited                               "pakviestas"
   :removed                               "perkeltas"
   :You                                   "tu"

   ;;new-contact
   :add-new-contact                       "Pridėti naują kontaktą"
   :import-qr                             "Importuoti"
   :scan-qr                               "Skanuoti QR"
   :swow-qr                               "Rodyti QR"
   :name                                  "Vardas"
   :whisper-identity                      "Whisper Tapatybė"
   :address-explication                   "Gal čia turėtų būti tekstas, kuriame būtų paaiškinta, kas yra adresas ir kur jo ieškoti"
   :enter-valid-address                   "Įveskite teisingą adresą arba skanuokite QR kodą"
   :enter-valid-public-key                "Įveskite teisyngą viešąjį raktą arba skanuokite QR kodą"
   :contact-already-added                 "Šis kontaktas jau pridėtas"
   :can-not-add-yourself                  "Negalima savęs pridėti"
   :unknown-address                       "Nežinomas adresas"

   ;;login
   :connect                               "Jungtis"
   :address                               "Adresas"
   :password                              "Slaptažodis"
   :login                                 "Prisijungimas"
   :sign-in-to-status                     "Jungtis į Status"
   :sign-in                               "Prisijungti"
   :wrong-password                        "Neteisingas slaptažodis"
   :enter-password                        "Įveskite slatažodį"

   ;;recover
   :recover-from-passphrase               "Atkurti iš slaptažodžio"
   :recover-explain                       "Norėdami susigrąžinti prieigą, įveskite slaptažodžio slaptafrazę"
   :passphrase                            "Slaptažodis"
   :recover                               "Atkurti"
   :enter-valid-passphrase                "Įveskite slaptažodį"
   :enter-valid-password                  "Įvestike slaptažodį"
   :twelve-words-in-correct-order         "12 žodžių teisinga tvarka"

   ;;accounts
   :recover-access                        "Atkurti prieigą"
   :add-account                           "Pridėti paskyrą"
   :create-new-account                    "Sukurti naują paskyrą"

   ;;wallet-qr-code
   :done                                  "Baigta"

   ;;validation
   :invalid-phone                         "Neteisingas telefono numeris"
   :amount                                "Paskyra"
   :not-enough-eth                        (str "Nepakankamas ETH balansas "
                                               "({{balance}} ETH)")

   ;;transactions
   :confirm                               "Patvirtinti"
   :confirm-transactions                  {:one   "Patvirtinti transakciją"
                                           :other "Patvirtinti {{count}} transakciją"
                                           :zero  "Jokios transakcijos"}
   :transactions-confirmed                {:one   "Transakcija patvirtinta"
                                           :other "{{count}} transakcijos patvirtintos"
                                           :zero  "Jokios transakcijos nepatvirtintos"}
   :transaction                           "Transakcija"
   :unsigned-transactions                 "Kepasirašytos transakcijos"
   :no-unsigned-transactions              "Jokių nepasirašytų transakcijų"
   :unsigned-transaction-expired          "Nepasirašyta transakcija baigėsi"
   :enter-password-transactions           {:one   "Patvirtinkite transakciją slaptažodžiu"
                                           :other "Patvirtinkite transakcijas slaptažodžiu"}
   :status                                "Status"
   :pending-confirmation                  "Laukiama patvirtinimo"
   :recipient                             "Gavėjas"
   :one-more-item                         "Dar viena daikta"
   :fee                                   "Mokestis"
   :estimated-fee                         "numatomas mokestis"
   :value                                 "Vertė"
   :to                                    "Į"
   :from                                  "Iš"
   :data                                  "Duomenys"
   :got-it                                "Gavo"
   :contract-creation                     "Contrakto kūrimas"
   :block                                 "Blokas"
   :hash                                  "Hash"
   :gas-limit                             "Gas limitas"
   :gas-price                             "Gas kaina"
   :gas-used                              "Gas sunaudota"
   :cost-fee                              "Kaina/Mokestis"
   :nonce                                 "Nonce"
   :confirmations                         "patvirtinimas"
   :confirmations-helper-text             "Jei norite būti tikri, kad jūsų sandoris nebus kompromituotas, palaukite, kol jis gaus mažiausiai 10 blokų patvirtinimą"
   :copy-transaction-hash                 "Copijuoti transakcijos hash"
   :open-on-etherscan                     "Atidaryti Etherscan"

   ;;webview
   :web-view-error                        "vajė, klaida"

   ;;testfairy warning
   :testfairy-title                       "Įspėjimas!"
   :testfairy-message                     "You are using app installed from a nightly build. For testing purposes this build includes session recording if wifi connection is used, so all your interaction with app is saved (as video and log) and might be used by development team to investigate possible issues. Saved video/log do not include your passwords. Recording is done only if app is installed from a nightly build. Nothing is recorded if app is installed from PlayStore or TestFlight."

   ;; wallet
   :wallet                                "Piniginė"
   :wallets                               "Piniginės"
   :your-wallets                          "Jūsų piniginės"
   :main-wallet                           "Pagrindinė piniginė"
   :wallet-error                          "Klaida užkraunant duomenis"
   :wallet-send                           "Siūsti"
   :wallet-request                        "Užklausa"
   :wallet-exchange                       "Keitykla"
   :wallet-assets                         "Aktyvai"
   :wallet-add-asset                      "Pridėti aktyvą"
   :wallet-total-value                    "Bendra vertė"
   :wallet-settings                       "Piniginės nustatymai"
   :signing-phrase-description            "Pasirašykite sandorį įvesdami savo slaptažodį. Įsitikinkite, kad aukščiau esantys žodžiai atitinka jūsų slaptą pasirašymo frazę"
   :wallet-insufficient-funds             "Nepakankamos lėšos"
   :request-transaction                   "Reikalauti transakcijos"
   :send-request                          "Siūsti užklausą"
   :share                                 "Dalintis"
   :eth                                   "ETH"
   :currency                              "Valiuta"
   :usd-currency                          "USD"
   :transactions                          "Transakcijos"
   :transaction-details                   "Transakcijų detalės"
   :transactions-sign                     "Pasirašyti"
   :transactions-sign-all                 "Pasirašyti viska"
   :transactions-sign-transaction         "Pasirašyti transakciją"
   :transactions-sign-later               "Pasirašyti"
   :transactions-sign-all-text            "Pasirašykite sandorį įvesdami savo slaptažodį.\nĮsitikinkite, kad aukščiau esantys žodžiai atitinka jūsų slaptą pasirašymo frazę"
   :transactions-sign-input-placeholder   "Įveskite slaptažodį"
   :transactions-delete                   "Trinti transakciją"
   :transactions-delete-content           "Transakcija bus pašalinta iš nepasirašyto sarašo"
   :transactions-history                  "Istorija"
   :transactions-unsigned                 "Nepasirašyta"
   :transactions-history-empty            "Tu neturi sandorių istorijos"
   :transactions-unsigned-empty           "Jūs neturite nepasirašytų transakcijų"
   :transactions-filter-title             "Filtruoti istoriją"
   :transactions-filter-tokens            "Žėtonai"
   :transactions-filter-type              "Tipas"
   :transactions-filter-select-all        "Rinktis viską"
   :view-transaction-details              "Žiūrėti transakcijų detales"
   :transaction-description               "Jei norite būti tikri, kad jūsų sandoris nebus kompromituotas, palaukite, kol jis gaus mažiausiai 10 blokų patvirtinimą"
   :transaction-sent                      "Transakcija išsiūsta"
   :transaction-moved-text                "Transakcija sėkmingai perkelta į “Nepasirašytos”"
   :transaction-moved-title               "Transakcija perkelta"
   :sign-later-title                      "Pasirašyti transakcijas vėliau?"
   :sign-later-text                       "Galėsite prisijungti prie operacijų istorijos"
   :not-applicable                        "Netaikoma nepasirašytoms transakcijoms"

   ;; Wallet Send
   :wallet-send-transaction               "Siūsti Transakciją"
   :wallet-send-step                      "Žingsnis {{step}} iš {{number}}"
   :wallet-choose-recipient               "Pasirinkti gavėją"
   :wallet-choose-from-contacts           "Pasirinkti iš kontaktų"
   :wallet-address-from-clipboard         "Įklijuoti adresą"
   :wallet-invalid-address                "Neteisingas adresas: \n {{data}}"
   :wallet-browse-photos                  "Naršyti žinutes"
   :validation-amount-invalid             "Kiekis netinkamas"
   :validation-amount-invalid-number      "Kiekio skaičius netinkamas"
   :validation-amount-is-too-precise      "Suma yra per daug tiksli. Mažiausias vienetas, kurį galite siųsti, yra 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Naujas tinklas"
   :add-network                           "Pridėti tinklą"
   :add-new-network                       "Pridėti naują tinklą"
   :existing-networks                     "Egzistuojantys tinklai"
   :add-json-file                         "pridėti JSON failą"
   :paste-json-as-text                    "Įklijuoti JSON kaip tekstą"
   :paste-json                            "Įklijuoti JSON"
   :specify-rpc-url                       "Nurodykite RPC URL"
   :edit-rpc-url                          "Redaguoti RPC URL"
   :edit-network-config                   "Redaguoti tinklo nustatymus"
   :connected                             "Jungtis"
   :process-json                          "Apdoroti JSON"
   :error-processing-json                 "Klaida apdorojant JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Nutolęs tinklas"
   :network-settings                      "Tinklo nustatymai"
   :edit-network-warning                  "Būkite atsargūs, redaguodami šio tinklo duomenis, galite padaryti jį sau neprieinamą"
   :connecting-requires-login             "Prisijungimas prie kito tinklo reikalauja prisijungimo"
   :close-app-title                       "Įspėjimas!"
   :close-app-content                     "Programa bus uždaryta. Kai iš naujo paleisite ją, pasirinktas tinklas bus naudojamas."
   :close-app-button                      "Patvirtinti"})

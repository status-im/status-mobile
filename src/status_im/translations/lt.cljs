(ns status-im.translations.lt)

(def translations
  {
   ;;common
   :members-title                         "Nariams"
   :not-implemented                       "neįgyvendinta"
   :chat-name                             "Pokalbio pavadinimas"
   :notifications-title                   "Perspėjimai ir garsai"
   :offline                               "Neprisijungęs"
   :search-for                            "Ieškoti..."
   :cancel                                "Atšaukti"
   :next                                  "Sekantis"
   :open                                  "Atidaryti"
   :description                           "Aprašymas"
   :url                                   "URL"
   :type-a-message                        "Įveskite žinutę..."
   :type-a-command                        "Pradėkite rašyti komandą..."
   :error                                 "Klaida"
   :unknown-status-go-error               "Nežinoma status-go klaida"
   :node-unavailable                      "Joks ethereum mazgas neveikia"
   :yes                                   "Taip"
   :no                                    "Ne"

   :camera-access-error                   "Norėdami suteikti reikalingą leidimą filmuoti, eikite į savo sistemos nustatymus ir įsitikinkite, kad pasirinktas Status > Camera."
   :photos-access-error                   "Norėdami suteikti reikalingą leidimą fotografuoti, eikite į savo sistemos nustatymus ir įsitikinkite, kad pasirinktas Status > Photo."

   ;;drawer
   :switch-users                          "Pasikeisti naudotoją"
   :current-network                       "Dabartinis tinklas"

   ;;chat
   :is-typing                             "rašo"
   :and-you                               "ir tu"
   :search-chat                           "Ieškoti pokalbyje"
   :members                               {:one   "1 narys"
                                           :other "{{count}} nariai"
                                           :zero  "jokių narių"}
   :members-active                        {:one   "1 narys"
                                           :other "{{count}} nariai"
                                           :zero  "jokių narių"}
   :public-group-status                   "Viešas"
   :active-online                         "Prisijungęs"
   :active-unknown                        "Nežinomas"
   :available                             "Prieinama"
   :no-messages                           "Jokių žinučių"
   :suggestions-requests                  "Užklausimai"
   :suggestions-commands                  "Komandos"
   :faucet-success                        "Dalintuvo užklausa gauta"
   :faucet-error                          "Dalintuvo užklausos klaida"

   ;;sync
   :sync-in-progress                      "Sinchronizuojama..."
   :sync-synced                           "Sinchronizuota"

   ;;messages
   :status-sending                        "Siunčiama..."
   :status-pending                        "Laukiama"
   :status-sent                           "Nusiųsta"
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
   :datetime-ago                          "prieš"
   :datetime-yesterday                    "vakar"
   :datetime-today                        "šiandien"

   ;;profile
   :profile                               "Profilis"
   :edit-profile                          "Redaguoti profilį"
   :message                               "Žinutė"
   :not-specified                         "Nenurodyta"
   :public-key                            "Viešasis raktas"
   :phone-number                          "Telefono numeris"
   :update-status                         "Atsinaujinkite būseną..."
   :add-a-status                          "Pridėkite būseną..."
   :status-prompt                         "Sukurkite būseną, kad žmonės žinotų, ką jūs siūlote. Taip pat galite naudoti #hashtagus."
   :add-to-contacts                       "Pridėti prie kontaktų"
   :in-contacts                           "Kontaktuose"
   :remove-from-contacts                  "Ištrinti iš kontaktų"
   :start-conversation                    "Pradėti pokalbį"
   :send-transaction                      "Siųsti sandėrį"
   :testnet-text                          "Jūs esate testiniame tinkle {{testnet}}. Nesiųskite tikru ETH arba SNT į jūsų adresą"
   :mainnet-text                          "Jūs esate pagrindiniame tinkle. Tikri ETH bus siunčiami"

   ;;make_photo
   :image-source-title                    "Profilio atvaizdas"
   :image-source-make-photo               "Fotografuoti"
   :image-source-gallery                  "Pasirinkti iš galerijos"

   ;;sharing
   :sharing-copy-to-clipboard             "Kopijuoti į iškarpinę"
   :sharing-share                         "Dalintis..."
   :sharing-cancel                        "Atšaukti"

   :browsing-title                        "Naršyti"
   :browsing-open-in-web-browser          "Atverti naršyklėje"
   :browsing-cancel                       "Naršyti"

   ;;sign-up
   :contacts-syncronized                  "Jūsų kontaktai buvo sinchronizuoti"
   :confirmation-code                     (str "Dėkui! Mes išsiuntėme jums teksto pranešimą su patvirtinimo kodu. "
                                               "Prašau nurodyti šį kodą, kad patvirtintumėte savo telefono numerį")
   :incorrect-code                        (str "Atsiprašome, kodas neteisingas, prašome įvesti jį dar kartą")
   :phew-here-is-your-passphrase          "Vajė, buvo sunkoka, čia yra jūsų slaptažodis, *išsaugokite jį!* jeigu reikės atkurti jūsų paskyrą."
   :here-is-your-passphrase               "Čia yra jūsų slaptafrazė, *išsaugokite ją!* jeigu reikės atkurti jūsų paskyrą."
   :here-is-your-signing-phrase           "Čia yra jūsų pasirašymo frazė. Ją naudosite, kad patvirtintumėte savo sandorius. *Užsirašykite ir saugokite!*"
   :phone-number-required                 "Palieskite čia, jei norite įvesti savo telefono numerį, tam kad rasti jūsų draugus"
   :shake-your-phone                      "Radote klaidą arba turite pasiūlymų? Tiesiog ~pakratykite~ savo telefoną!"
   :intro-status                          "Kalbėkite su manimi, kad nustatytumėte savo paskyrą ir pakeistumėte savo nustatymus!"
   :intro-message1                        "Sveiki atvykę į Status!\nPalieskite šį pranešimą, kad nustatytumėte slaptažodį ir pradėtumėte!"
   :account-generation-message            "Sekundėlę, vyksta nemenki skaičiavimai! Generuojama jūsų paskyra!"
   :move-to-internal-failure-message      "Mums reikia perkelti keletą svarbių failų iš išorinės į vidinę atmintį. Norėdami tai padaryti, mums reikia jūsų leidimo. Būsimose versijose mes nenaudosime išorinės atminties."
   :debug-enabled                         "Debug serveris buvo paleistas! Dabar galite paleisti *status-dev-cli scan*, kad surastumėte serverį iš savo kompiuterio tame pačiame tinkle."

   ;;phone types
   :phone-e164                            "Tarptautinis 1"
   :phone-international                   "Tarptautinis 2"
   :phone-national                        "Vietinis"
   :phone-significant                     "Reikšmingas"

   ;;chats
   :chats                                 "Pokalbiai"
   :delete-chat                           "Trinti pokalbį"
   :new-group-chat                        "Naujas grupinis pokalbis"
   :new-public-group-chat                 "Jungtis prie grupinio pokalbio"
   :edit-chats                            "Redaguoti grupinius pokalbius"
   :search-chats                          "Ieškoti pokalbių"
   :empty-topic                           "Tuščia tema"
   :topic-format                          "Netinkamas formatas [a-z0-9\\-]+"
   :public-group-topic                    "Tema"

   ;;discover
   :discover                              "Atrasti"
   :none                                  "Jokio"
   :search-tags                           "Įveskite paieškos kriterijus"
   :popular-tags                          "Populiarūs hashtagai"
   :recent                                "Paskiausios būsenos"
   :no-statuses-found                     "Jokių būsenų nerasta"
   :chat                                  "Pokalbis"
   :all                                   "Viskas"
   :public-chats                          "Vieši pokalbiai"
   :soon                                  "Greitai"
   :public-chat-user-count                "{{count}} asmenys"
   :dapps                                 "DApp'ai"
   :dapp-profile                          "ÐApp'ų profilis"
   :no-statuses-discovered                "Jokių būsenų nerasta"
   :no-statuses-discovered-body           "Kai kas nors paskelbia būseną,\nmatysite ją čia."
   :no-hashtags-discovered-title          "Jokių hashtagų nerasta"
   :no-hashtags-discovered-body           "Kai hashtagas tampa populiarus,\nmatysite jį čia."

   ;;settings
   :settings                              "Nustatymai"

   ;;contacts
   :contacts                              "Kontaktai"
   :new-contact                           "Nauji kontaktai"
   :delete-contact                        "Trinti kontaktą"
   :delete-contact-confirmation           "Šis įrašas bus pašalintas iš kontaktų"
   :remove-from-group                     "Pašalinti iš grupės"
   :edit-contacts                         "Redaguoti kontaktus"
   :search-contacts                       "Ieškoti kontaktų"
   :contacts-group-new-chat               "Pradėti naują pokalbį"
   :choose-from-contacts                  "Pasirinkti iš kontaktų"
   :no-contacts                           "Jokių kontaktų nerasta"
   :show-qr                               "Rodyti QR"
   :enter-address                         "Įveskite adresą"
   :more                                  "daugiau"

   ;;group-settings
   :remove                                "Pašalinti"
   :save                                  "išsaugoti"
   :delete                                "Ištrinti"
   :clear-history                         "Naikinti istoriją"
   :mute-notifications                    "Nutildyti pranešimus"
   :leave-chat                            "Palikti pokalbį"
   :chat-settings                         "Pokalbio nustatymai"
   :edit                                  "Redaguoti"
   :add-members                           "Pridėti narių"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

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
   :new-group                             "Nauja grupė"
   :reorder-groups                        "Rūšiuoti grupes"
   :edit-group                            "Redaguoti grupę"
   :delete-group                          "Trinti grupę"
   :delete-group-confirmation             "Ši grupė bus pašalinta iš jūsų grupių sarašo. Jūsų kontaktai nepakis."
   :delete-group-prompt                   "Tai neįtakos jūsų kontaktų"
   :contact-s                             {:one   "kontaktas"
                                           :other "kontaktai"}

   ;;protocol
   :received-invitation                   "gautas pakvietimas į pokalbį"
   :removed-from-chat                     "pašalino tave iš pokalbio"
   :left                                  "išėjo"
   :invited                               "pakviestas"
   :removed                               "perkeltas"
   :You                                   "Tu"

   ;;new-contact
   :add-new-contact                       "Pridėti naują kontaktą"
   :scan-qr                               "Skanuoti QR"
   :name                                  "Vardas"
   :address-explication                   "Gal čia turėtų būti tekstas, kuriame būtų paaiškinta, kas yra adresas ir kur jo ieškoti"
   :enter-valid-public-key                "Įveskite teisingą viešąjį raktą arba skanuokite QR kodą"
   :contact-already-added                 "Šis kontaktas jau pridėtas"
   :can-not-add-yourself                  "Negalima pridėti savęs"
   :unknown-address                       "Nežinomas adresas"

   ;;login
   :connect                               "Jungtis"
   :address                               "Adresas"
   :password                              "Slaptažodis"
   :sign-in-to-status                     "Jungtis į Status"
   :sign-in                               "Prisijungti"
   :wrong-password                        "Neteisingas slaptažodis"
   :enter-password                        "Įveskite slaptažodį"

   ;;recover
   :passphrase                            "Slaptafrazė"
   :recover                               "Atkurti"
   :twelve-words-in-correct-order         "12 žodžių teisinga tvarka"

   ;;accounts
   :recover-access                        "Atkurti prieigą"
   :create-new-account                    "Sukurti naują paskyrą"

   ;;wallet-qr-code
   :done                                  "Baigta"

   ;;validation
   :invalid-phone                         "Neteisingas telefono numeris"
   :amount                                "Paskyra"

   ;;transactions
   :confirm                               "Patvirtinti"
   :transaction                           "Sandėris"
   :unsigned-transaction-expired          "Nepasirašytas sandėris baigėsi"
   :status                                "Status"
   :recipient                             "Gavėjas"
   :to                                    "Į"
   :from                                  "Iš"
   :data                                  "Duomenys"
   :got-it                                "Gavo"
   :block                                 "Blokas"
   :hash                                  "Hash"
   :gas-limit                             "Gas limitas"
   :gas-price                             "Gas kaina"
   :gas-used                              "Gas sunaudota"
   :cost-fee                              "Kaina/Mokestis"
   :nonce                                 "Nonce"
   :confirmations                         "Patvirtinimas"
   :confirmations-helper-text             "Jei norite būti tikri, kad jūsų sandoris nebus kompromituotas, palaukite, kol jis gaus mažiausiai 10 blokų patvirtinimą"
   :copy-transaction-hash                 "Kopijuoti sandėrio hash"
   :open-on-etherscan                     "Atidaryti Etherscan"

   ;;webview
   :web-view-error                        "vajė, klaida"

   ;;testfairy warning
   :testfairy-title                       "Įspėjimas!"
   :testfairy-message                     "Naudojate programą, įdiegtą iš naktinio kūrimo. Testavimo tikslais šiame kūrime įrašomas seansas, jei naudojamas \"Wi-Fi\" ryšys, taigi visa sąveika su programa išsaugoma (kaip vaizdo įrašas ir žurnalas) ir gali būti naudojama kūrėjų komandos, kad ištirtų galimus klausimus. Išsaugotas vaizdo įrašas/žurnalas neįtraukia slaptažodžių. Įrašymas atliekamas tik tada, jei programa yra įdiegta iš naktinio kūrimo. Nieko nėra įrašoma, jei programa įdiegta iš \"PlayStore\" arba \"TestFlight\""

   ;; wallet
   :wallet                                "Piniginė"
   :wallets                               "Piniginės"
   :your-wallets                          "Jūsų piniginės"
   :main-wallet                           "Pagrindinė piniginė"
   :wallet-error                          "Klaida užkraunant duomenis"
   :wallet-send                           "Siųsti"
   :wallet-request                        "Užklausa"
   :wallet-exchange                       "Keitykla"
   :wallet-assets                         "Aktyvai"
   :wallet-add-asset                      "Pridėti aktyvą"
   :wallet-total-value                    "Bendra vertė"
   :wallet-settings                       "Piniginės nustatymai"
   :signing-phrase-description            "Pasirašykite sandorį įvesdami savo slaptažodį. Įsitikinkite, kad aukščiau esantys žodžiai atitinka jūsų slaptą pasirašymo frazę"
   :wallet-insufficient-funds             "Nepakankamos lėšos"
   :request-transaction                   "Reikalauti sandėrio"
   :send-request                          "Siųsti užklausą"
   :share                                 "Dalintis"
   :eth                                   "ETH"
   :currency                              "Valiuta"
   :usd-currency                          "USD"
   :transactions                          "Sandėriai"
   :transaction-details                   "Sandėrių detalės"
   :transaction-failed                    "Sandoris nepavyko"
   :transactions-sign                     "Pasirašyti"
   :transactions-sign-all                 "Pasirašyti viską"
   :transactions-sign-transaction         "Pasirašyti sandorį"
   :transactions-sign-later               "Pasirašyti veliau"
   :transactions-delete                   "Trinti sandorį"
   :transactions-delete-content           "Sandoris bus pašalintas iš nepasirašytų sarašo"
   :transactions-history                  "Istorija"
   :transactions-unsigned                 "Nepasirašyta"
   :transactions-history-empty            "Tu neturi sandorių istorijos"
   :transactions-unsigned-empty           "Jūs neturite nepasirašytų sandorių"
   :transactions-filter-title             "Filtruoti istoriją"
   :transactions-filter-tokens            "Žėtonai"
   :transactions-filter-type              "Tipas"
   :transactions-filter-select-all        "Rinktis viską"
   :view-transaction-details              "Žiūrėti sandėrių detales"
   :transaction-description               "Jei norite būti tikri, kad jūsų sandoris nebus kompromituotas, palaukite, kol jis gaus mažiausiai 10 blokų patvirtinimą"
   :transaction-sent                      "Sandėris išsiųstas"
   :transaction-moved-text                "Sandėris sėkmingai perkeltas į “Nepasirašytos”"
   :transaction-moved-title               "Sandėris perkeltas"
   :sign-later-title                      "Pasirašyti sandėrį vėliau?"
   :sign-later-text                       "Galėsite prisijungti prie operacijų istorijos"
   :not-applicable                        "Netaikoma nepasirašytiems sandėriams"

   ;; Wallet Send
   :wallet-choose-recipient               "Pasirinkti gavėją"
   :wallet-choose-from-contacts           "Pasirinkti iš kontaktų"
   :wallet-address-from-clipboard         "Įklijuoti adresą"
   :wallet-invalid-address                "Neteisingas adresas: \n {{data}}"
   :wallet-invalid-chain-id               "Tinklas nesutampa su: \n {{data}}"
   :wallet-browse-photos                  "Naršyti žinutes"
   :validation-amount-invalid-number      "Kiekio skaičius netinkamas"
   :validation-amount-is-too-precise      "Suma yra per daug tiksli. Mažiausias vienetas, kurį galite siųsti, yra 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Naujas tinklas"
   :add-network                           "Pridėti tinklą"
   :add-new-network                       "Pridėti naują tinklą"
   :existing-networks                     "Egzistuojantys tinklai"
   :add-json-file                         "Pridėti JSON failą"
   :paste-json-as-text                    "Įklijuoti JSON kaip tekstą"
   :paste-json                            "Įklijuoti JSON"
   :specify-rpc-url                       "Nurodykite RPC URL"
   :edit-network-config                   "Redaguoti tinklo konfiguraciją"
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

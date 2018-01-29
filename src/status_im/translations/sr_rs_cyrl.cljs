(ns status-im.translations.sr-rs-cyrl)

(def translations
  {
   ;;common
   :members-title                         "Чланови"
   :not-implemented                       "!није имплементирано"
   :chat-name                             "Назив разговора"
   :notifications-title                   "Нотификације и звукови"
   :offline                               "Ван мреже"
   :search-for                            "Тражи..."
   :cancel                                "Откажи"
   :next                                  "Следећи"
   :open                                  "Отвори"
   :description                           "Опис"
   :url                                   "УРЛ"
   :type-a-message                        "Укуцајте поруку..."
   :type-a-command                        "Укуцајте наредбу..."
   :error                                 "Грешка"
   :unknown-status-go-error               "Непозната status-go грешка"
   :node-unavailable                      "Етереум сервис није покренут"
   :yes                                   "Да"
   :no                                    "Нe"

   :camera-access-error                   "Да би сте дозволили приступ камери, молимо вас да у системским подешавањима селектујете Статус > Камера."
   :photos-access-error                   "Да би сте дозволили приступ фотографијама, молимо вас у системским подешаванјима селектујете Статус > Албум."

   ;;drawer
   :switch-users                          "Промените корисника"
   :current-network                       "Тренутна мрежа"

   ;;chat
   :is-typing                             "куца"
   :and-you                               "и ви"
   :search-chat                           "Претражи разговор"
   :members                               {:one   "1 члан"
                                           :other "{{count}} чланова"
                                           :zero  "нема чланова"}
   :members-active                        {:one   "1 члан"
                                           :other "{{count}} чланови"
                                           :zero  "нема чланова"}
   :public-group-status                   "Јавно"
   :active-online                         "На мрежи"
   :active-unknown                        "Непознат"
   :available                             "Доступан"
   :no-messages                           "Нема порука"
   :suggestions-requests                  "Захтеви"
   :suggestions-commands                  "Наредбе"
   :faucet-success                        "Захтев за славину је примљен"
   :faucet-error                          "Грешка у захтеву за славину"

   ;;sync
   :sync-in-progress                      "Синхронизација..."
   :sync-synced                           "Синхронизовано"

   ;;messages
   :status-sending                        "Слање у току"
   :status-pending                        "Обрађује се"
   :status-sent                           "Послато"
   :status-seen-by-everyone               "Сви су видели"
   :status-seen                           "Прегледано"
   :status-delivered                      "Достављено"
   :status-failed                         "Неуспешно"

   ;;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунди"}
   :datetime-minute                       {:one   "минут"
                                           :other "минута"}
   :datetime-hour                         {:one   "сат"
                                           :other "сати"}
   :datetime-day                          {:one   "дан"
                                           :other "дана"}
   :datetime-ago                          "пре"
   :datetime-yesterday                    "јуче"
   :datetime-today                        "данас"

   ;;profile
   :profile                               "Профил"
   :edit-profile                          "Ажурирајте профил"
   :message                               "Порука"
   :not-specified                         "Није специфицирано"
   :public-key                            "Јавни кључ"
   :phone-number                          "Број телефона"
   :update-status                         "Ажурирајте свој статус..."
   :add-a-status                          "Додајте статус..."
   :status-prompt                         "Поставите свој статус. Коришћење #тагова ће омогућити другима да те упознају и причају о ономе о чему размишљаш"
   :add-to-contacts                       "Додај у контакте"
   :in-contacts                           "У контактима"
   :remove-from-contacts                  "Уклони из контаката"
   :start-conversation                    "Започни разговор"
   :send-transaction                      "Пошаљи трансакцију"
   :testnet-text                          "Налазите се на {{testnet}} тест мрежи. Не шаљите прави ETH или SNT на своју адресу"
   :mainnet-text                          "Налазите се на главној мрежи. Прави ETH ће бити послат"

   ;;make_photo
   :image-source-title                    "Профилна слика"
   :image-source-make-photo               "Сликај"
   :image-source-gallery                  "Изабери из галерије"

   ;;sharing
   :sharing-copy-to-clipboard             "Копирај"
   :sharing-share                         "Подели..."
   :sharing-cancel                        "Одустани"

   :browsing-title                        "Претражи"
   :browsing-open-in-web-browser          "Отвори у интернет претраживачу"
   :browsing-cancel                       "Одустани"

   ;;sign-up
   :contacts-syncronized                  "Ваши контакти су синхронизовани"
   :confirmation-code                     (str "Хвала! Послали смо вам СМС поруку са кодом "
                                               "Молимо вас да унесете код да би сте потврдили свој број телефона")
   :incorrect-code                        (str "Жао нам је код је неисправан, молимо вас покушајте поново.")
   :phew-here-is-your-passphrase          "Уф, то је било тешко. Ово је ваша лозинка, *запишите је и чувајте на сигурном!* Требаће вам за обнову налога."
   :here-is-your-passphrase               "Ово је ваша лозинка, *Запишите је и чувајте на сигурном!* Требаће вам за обнову налога."
   :here-is-your-signing-phrase           "Ово је ваша фраза за потписивање. Користићете је за потврду својих трансакција. *Запишите је и чувајте на сигурном!*"
   :phone-number-required                 "Кликните овде да бисте потврдили свој број телефона и пронаћи ћу ваше пријатеље."
   :shake-your-phone                      "Пронашли сте грешку или имате предлог? Само ~протресите~ свој мобилни телефон!"
   :intro-status                          "Разговарајте са мном да бисте подесили свој налог."
   :intro-message1                        "Добродошли у Статус!\nЗа почетак кликните на ову поруку да поставите своју шифру."
   :account-generation-message            "Само тренутак, одрађујем неку жешћу математику око креирања вашег налога!"
   :move-to-internal-failure-message      "Потребно је да неке важне фајлове пребацимо из екстерног у интерно складиште. Да бисмо то урадили, потребна нам је ваша дозвола. Нећемо користити екстерно складиште у наредним верзијама."
   :debug-enabled                         "Дебаговање сервера је покренуто! Сад можете да извршиш *status-dev-cli scan* да бисте нашли сервер са свог компјутера на истој мрежи."

   ;;phone types
   :phone-e164                            "Међународни 1"
   :phone-international                   "Међународни 2"
   :phone-national                        "Домаћи"
   :phone-significant                     "Значајан"

   ;;chats
   :chats                                 "Разговори"
   :delete-chat                           "Обришите разговор"
   :new-group-chat                        "Нова група за разговор"
   :new-public-group-chat                 "Придружите се јавном разговору"
   :edit-chats                            "Уредите разговор"
   :search-chats                          "Претражите разговор"
   :empty-topic                           "Празна тема"
   :topic-format                          "Погрешан формат [a-z0-9\\-]+"
   :public-group-topic                    "Тема"

   ;;discover
   :discover                              "Открића"
   :none                                  "Ништа"
   :search-tags                           "Укуцајте кључне речи за претрагу"
   :popular-tags                          "Популарни #тагови"
   :recent                                "Нови статуси"
   :no-statuses-found                     "Ниједан статус није пронађен"
   :chat                                  "Разговор"
   :all                                   "Све"
   :public-chats                          "Јавни разговори"
   :soon                                  "Ускоро"
   :public-chat-user-count                "{{count}} људи"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp профил"
   :no-statuses-discovered                "Нема пронађених статуса"
   :no-statuses-discovered-body           "Кад неко објави статус\nвидећете га овде."
   :no-hashtags-discovered-title          "Нема пронађених #тагова"
   :no-hashtags-discovered-body           "Кад #таг постане популаран\nвидећете га овде."

   ;;settings
   :settings                              "Подешавања"

   ;;contacts
   :contacts                              "Контакти"
   :new-contact                           "Нови контакт"
   :delete-contact                        "Обриши контакт"
   :delete-contact-confirmation           "Овај контакт ће бити уклоњен из вашег адресара"
   :remove-from-group                     "Уклони из групе"
   :edit-contacts                         "Уреди контакте"
   :search-contacts                       "Претражи контакте"
   :contacts-group-new-chat               "Започни нов разговор"
   :choose-from-contacts                  "Изабери из контаката"
   :no-contacts                           "Још увек без контаката"
   :show-qr                               "Прикажи QR code"
   :enter-address                         "Унесите адресу"
   :more                                  "још"

   ;;group-settings
   :remove                                "Уклони"
   :save                                  "Сачувај"
   :delete                                "Обриши"
   :clear-history                         "Очисти историју"
   :mute-notifications                    "Утишај обавештења"
   :leave-chat                            "Напусти разговор"
   :chat-settings                         "Подешавања разговора"
   :edit                                  "Уреди"
   :add-members                           "Додај чланове"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "Нова група"
   :reorder-groups                        "Промени распоред група"
   :edit-group                            "Уреди групу"
   :delete-group                          "Обриши групу"
   :delete-group-confirmation             "Ова група ће бити уклоњена са ваше листе група. Ово неће утицати на ваше контакте"
   :delete-group-prompt                   "Ово неће утицати на ваше контакте"
   :contact-s                             {:one   "контакт"
                                           :other "контаката"}

   ;;protocol
   :received-invitation                   "примили сте позивницу за разговор"
   :removed-from-chat                     "напусти групу за разговор"
   :left                                  "остати"
   :invited                               "позвани"
   :removed                               "уклоњени"
   :You                                   "Ви"

   ;;new-contact
   :add-new-contact                       "Додај нови контакт"
   :scan-qr                               "Скенирај QR код"
   :name                                  "Име"
   :address-explication                   "Ваш јавни кључ је ваша адреса на Етереуму и представља низ бројева и слова. Можете га лако пронаћи у свом профилу"
   :enter-valid-public-key                "Молимо унесите исправан јавни кључ или скенирајте QR код"
   :contact-already-added                 "Контакт је већ додат"
   :can-not-add-yourself                  "Не можете да додате самог себе"
   :unknown-address                       "Непозната адреса"

   ;;login
   :connect                               "Повежи"
   :address                               "Адреса"
   :password                              "Лозинка"
   :sign-in-to-status                     "Улогујте се на Статус"
   :sign-in                               "Улогујте се"
   :wrong-password                        "Погрешна лозинка"
   :enter-password                        "Унесите лозинку"

   ;;recover
   :passphrase                            "Тајна фраза"
   :recover                               "Обнова налога"
   :twelve-words-in-correct-order         "12 речи у тачном редоследу"

   ;;accounts
   :recover-access                        "Обнови налог"
   :create-new-account                    "Направи нови налог"

   ;;wallet-qr-code
   :done                                  "Урађено"

   ;;validation
   :invalid-phone                         "Неисправан број телефона"
   :amount                                "Износ"

   ;;transactions
   :confirm                               "Потврди"
   :transaction                           "Трансакција"
   :unsigned-transaction-expired          "Непотписана трансакција је истекла"
   :status                                "Статус"
   :recipient                             "Прималац"
   :to                                    "Коме"
   :from                                  "Од"
   :data                                  "Подаци"
   :got-it                                "ОК"
   :block                                 "Блокирај"
   :hash                                  "Hash"
   :gas-limit                             "Лимит гаса"
   :gas-price                             "Цена гаса"
   :gas-used                              "Искоришћени гас"
   :cost-fee                              "Цена"
   :nonce                                 "Nonce"
   :confirmations                         "Потврде"
   :confirmations-helper-text             "Молимо вас сачекајте најмање 12 потврда како бисте били сигурни да је ваша трансакција безбедно обрађена"
   :copy-transaction-hash                 "Копирај hash трансакције"
   :open-on-etherscan                     "Отвори Etherscan.io"

   ;;webview
   :web-view-error                        "упс, грешка"

   ;;testfairy warning
   :testfairy-title                       "Упозорење!"
   :testfairy-message                     "Користите експерименталну верзију апликације. За потребе тестирања ова верзија снима сесије уколико користите Wi-Fi конекцију. Свака ваша интеракција са овом апликацијом сачувана као видео запис и може бити коришћена од стране нашег развојног тима приликом истраживању могућих проблема. Сачувани видео не садржи ваше лозинке. Снимање се врши искључиво на екперименталним верзијама. Ништа се не снима уколико је апликација инсталирана преко PlayStore-а или TestFlight-а."

   ;; wallet
   :wallet                                "Новчаник"
   :wallets                               "Новчаници"
   :your-wallets                          "Ваш новчаник"
   :main-wallet                           "Главни новчаник"
   :wallet-error                          "Грешка при учитавању података"
   :wallet-send                           "Пошаљи"
   :wallet-request                        "Захтев"
   :wallet-exchange                       "Размена"
   :wallet-assets                         "Средства"
   :wallet-add-asset                      "Додај средства"
   :wallet-total-value                    "Укупна вредност"
   :wallet-settings                       "Подешавања новчаника"
   :signing-phrase-description            "Доделите трансакцију укуцавањем своје лозинке. Побрините се да се речи изнад поклапају са вашом тајном фразом за потписивање"
   :wallet-insufficient-funds             "Недовољно средстава"
   :request-transaction                   "Захтевајте трансакцију"
   :send-request                          "Пошаљите захтев"
   :share                                 "Подели"
   :eth                                   "ETH"
   :currency                              "Валута"
   :usd-currency                          "USD"
   :transactions                          "Трансакције"
   :transaction-details                   "Детаљи трансакције"
   :transaction-failed                    "Неуспела трансакција"
   :transactions-sign                     "Додели"
   :transactions-sign-all                 "Додели све"
   :transactions-sign-transaction         "Додели трансакцију"
   :transactions-sign-later               "Додели касније"
   :transactions-delete                   "Обриши трансакцију"
   :transactions-delete-content           "Трансакција ће бити уклоњена из листе непотписаних"
   :transactions-history                  "Историја"
   :transactions-unsigned                 "Непотписане"
   :transactions-history-empty            "Још увек немате трансакције у историји"
   :transactions-unsigned-empty           "Немате недодељене трансакције"
   :transactions-filter-title             "Филтрирај историју"
   :transactions-filter-tokens            "Токени"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Изабери све"
   :view-transaction-details              "Прикажи детаље трансакција"
   :transaction-description               "Молимо вас сачекајте најмање 12 потврда да бисте потврдили да је ваша трансакција успешно обрађена"
   :transaction-sent                      "Трансакција послата"
   :transaction-moved-text                "Ова трансакција ће остати у листи непотписаних током наредних 5 мин"
   :transaction-moved-title               "Трансакција је померена"
   :sign-later-title                      "Потпишите трансакцију касније?"
   :sign-later-text                       "Проверите историју трансакција да бисте потписали ову трансакцију"
   :not-applicable                        "Није примењиво за непотписане трансакције"

   ;; Wallet Send
   :wallet-choose-recipient               "Изабери примаоца"
   :wallet-choose-from-contacts           "Изабери из контаката"
   :wallet-address-from-clipboard         "Користи адресу из Клипборда"
   :wallet-invalid-address                "Неисправна адреса: \n {{data}}"
   :wallet-browse-photos                  "Претражи слике"
   :validation-amount-invalid-number      "Износ није исправан број"
   :validation-amount-is-too-precise      "Износ има превише децимала. Најмања јединица коју можете послати је 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Нова мрежа"
   :add-network                           "Додај мрежу"
   :add-new-network                       "Додај нову мрежу"
   :existing-networks                     "Постојеће мреже"
   :add-json-file                         "Додај JSON фајл"
   :paste-json-as-text                    "Налепи JSON као текст"
   :paste-json                            "Налепи JSON"
   :specify-rpc-url                       "Дефиниши RPC URL"
   :edit-network-config                   "Уреди конфигурацију мреже"
   :connected                             "Повезано"
   :process-json                          "Обради JSON"
   :error-processing-json                 "Грешка у обради JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Уклони мрежу"
   :network-settings                      "Подешавања мреже"
   :edit-network-warning                  "Будите пажљиви, подешавање података мреже може да онеспособи ову мрежу"
   :connecting-requires-login             "Повезивање на другу мрежу захтева да се поново улогујете"
   :close-app-title                       "Упозорење!"
   :close-app-content                     "Ова апликација ће да буде рестартована. Када је поново отворите, биће коришћена изабрана мрежа"
   :close-app-button                      "Потврди"})

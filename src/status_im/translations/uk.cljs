(ns status-im.translations.uk)

(def translations
  {
   ;;common
   :members-title                         "Учасники"
   :not-implemented                       "!не реалізовано"
   :chat-name                             "Назва групи"
   :notifications-title                   "Сповіщення та звуки"
   :offline                               "Оффлайн"
   :search-for                            "Шукати за..."
   :cancel                                "Відміна"
   :next                                  "Наступне"
   :open                                  "Відкрити"
   :description                           "Опис"
   :url                                   "URL"
   :type-a-message                        "Написати повідомлення..."
   :type-a-command                        "Почніть вводити команду..."
   :error                                 "Помилка"
   :unknown-status-go-error               "Невідома помилка"
   :node-unavailable                      "Не знайдено ethereum вузла"
   :yes                                   "Так"
   :no                                    "Ні"

   :camera-access-error                   "Щоб надати необхідний дозвіл камері, перейдіть до налаштувань вашої системи та переконайтеся, що Status > Camera є вибрана."
   :photos-access-error                   "Щоб надати необхідний дозвіл знімати фото, перейдіть до налаштувань вашої системи та переконайтеся, що Status > Photos є вибране."

   ;;drawer
   :switch-users                          "Зміна користувача"
   :current-network                       "Поточна мережа"

   ;;chat
   :is-typing                             "друкує"
   :and-you                               "і ви"
   :search-chat                           "Пошук групи"
   :members                               {:one   "1 учасник"
                                           :other "{{count}} учасників"
                                           :zero  "немає учасників"}
   :members-active                        {:one   "1 учасник"
                                           :other "{{count}} учасників"
                                           :zero  "немає учасників"}
   :public-group-status                   "Публічно"
   :active-online                         "Онлайн"
   :active-unknown                        "Невідомо"
   :available                             "Доступно"
   :no-messages                           "Немає повідомлень"
   :suggestions-requests                  "Запити"
   :suggestions-commands                  "Команди"
   :faucet-success                        "Запит отримано"
   :faucet-error                          "Помилка запиту"

   ;;sync
   :sync-in-progress                      "Синхронізація..."
   :sync-synced                           "Синхронізовано"

   ;messages
   :status-pending                        "Очікує відправлення"
   :status-sent                           "Надіслано"
   :status-seen-by-everyone               "Надіслано до всіх"
   :status-seen                           "Переглянуто"
   :status-delivered                      "Доставлено"
   :status-failed                         "Помилка"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунд"}
   :datetime-minute                       {:one   "хвилина"
                                           :other "хвилин"}
   :datetime-hour                         {:one   "година"
                                           :other "годин"}
   :datetime-day                          {:one   "день"
                                           :other "днів"}
   :datetime-ago                          "тому"
   :datetime-yesterday                    "вчора"
   :datetime-today                        "сьогодні"

   ;profile
   :profile                               "Профіль"
   :edit-profile                          "Редагувати Профіль"
   :message                               "Повідомлення"
   :not-specified                         "Не вказано"
   :public-key                            "Відкритий ключ"
   :phone-number                          "Номер телефону"
   :update-status                         "Оновіть свій статус..."
   :add-a-status                          "Додати статус..."
   :status-prompt                         "Додайте статус. Використання #hastags допоможе іншим знайти вас і поговорити про те, що у вас на думці"
   :add-to-contacts                       "Додати до контактів"
   :in-contacts                           "В контактах"
   :remove-from-contacts                  "Видалити з контактів"
   :start-conversation                    "Почати розмову"
   :send-transaction                      "Надіслати транзакцію"
   :testnet-text                          "Ви у {{testnet}} Тестовій Мережі. Не надсилайте справжні ETH або SNT на вашу адресу"
   :mainnet-text                          "Ви у основній мережі. Справжні ETH будуть надіслані"

   ;;make_photo
   :image-source-title                    "Фото профілю"
   :image-source-make-photo               "Зробити знімок"
   :image-source-gallery                  "Вибрати з галереї"

   ;;sharing
   :sharing-copy-to-clipboard             "Скопіювати"
   :sharing-share                         "Поділитися..."
   :sharing-cancel                        "Відміна"

   :browsing-title                        "Переглянути"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "Відрити у браузері"
   :browsing-cancel                       "Відміна"

   ;;sign-up
   :contacts-syncronized                  "Ваші контактні дані було синхронізовано"
   :confirmation-code                     (str "Дякуємо! Ми відправили вам текстове повідомлення з кодом "
                                               "підтвердження. Будь ласка, надайте цей код, щоб підтвердити свій номер телефону")
   :incorrect-code                        (str "На жаль, код невірний, будь ласка, введіть ще раз")
   :phew-here-is-your-passphrase          "Фух, це було важко. Ось ваша кодова фраза, *запишіть її та надійно збережіть!* Вона пригодиться для відновлення аккаунту."
   :here-is-your-passphrase               "Ось ваша кодова фраза, *запишіть її та надійно збережіть!* Вона пригодиться для відновлення аккаунту."
   :here-is-your-signing-phrase           "Ось ваша фраза-підпис. Ви будете використовувати це для підтвердження ваших транзакцій. *запишіть її та надійно збережіть!*"
   :phone-number-required                 "Торкніться тут, щоб підтвердити свій номер телефону і я знайду ваших друзів."
   :shake-your-phone                      "Найшли помилку або маєте пропозицію? Просто ~потрусіть~ ваш телефон!"
   :intro-status                          "Напишіть мені, щоб налаштувати свій обліковий запис і змінити свої налаштування."
   :intro-message1                        "Вітаємо у Status!\nТоркніться цього повідомлення, щоб встановити пароль і почати роботу."
   :account-generation-message            "Почекайте секунду, маю виконати страшенно складні розрахунки, щоб створити ваш обліковий запис!"
   :move-to-internal-failure-message      "Нам треба перемістити деякі важливі файли з зовнішньої на внутрішню пам'ять. Для цього нам потрібен ваш дозвіл. Ми не будемо використовувати зовнішню пам'ять у майбутніх версіях."
   :debug-enabled

   ;;phone types
   :phone-e164                            "Міжнародні 1"
   :phone-international                   "Міжнародні 2"
   :phone-national                        "Національні"
   :phone-significant                     "Significant"

   ;;chats
   :chats                                 "Чат"
   :delete-chat                           "Видалити Чат"
   :new-group-chat                        "Створити групу"
   :new-public-group-chat                 "Приєднатися до публічної групи"
   :edit-chats                            "Редагувати чат"
   :search-chats                          "Пошук чатів"
   :empty-topic                           "Порожня тема"
   :topic-format                          "Неправильний формат [a-z0-9\\-]+"
   :public-group-topic                    "Тема"

   ;;discover
   :discover                              "Відкриття"
   :none                                  "Жоден"
   :search-tags                           "Введіть теги для пошуку тут"
   :popular-tags                          "Популярні #теги"
   :recent                                "Нещодавні"
   :no-statuses-discovered                "Статусів не знайдено"
   :chat                                  "Чат"
   :all                                   "Всі"
   :public-chats                          "Публічні чати"
   :soon                                  "Скоро"
   :public-chat-user-count                "{{count}} людей"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp профіль"
   :no-statuses-discovered                "Не знайдено жодного статусу"
   :no-statuses-discovered-body           "Коли хтось публікує \nстатус, ви побачите його тут."
   :no-hashtags-discovered-title          "Не знайдено жодного #тегу"
   :no-hashtags-discovered-body           "Коли #тег стає\nпопулярним, ви знайдете його тут."

   ;settings
   :settings                              "Налаштування"

   ;contacts
   :contacts                              "Контакти"
   :new-contact                           "Новий контакт"
   :delete-contact                        "Видалити контакт"
   :delete-contact-confirmation           "Цей контакт буде видалено з ваших контактів"
   :remove-from-group                     "Видалити з групи"
   :edit-contacts                         "Редагувати контакти"
   :search-contacts                       "Пошук контактів"
   :contacts-group-new-chat               "Почати новий груповий чат"
   :choose-from-contacts                  "Вибрати з контактів"
   :no-contacts                           "Поки немає контактів"
   :show-qr                               "Показати QR"
   :enter-address                         "Введіть адресу"
   :more                                  "більше"

   ;;group-settings
   :remove                                "Видалити"
   :save                                  "Зберегти"
   :delete                                "Видалити"
   :clear-history                         "Очистити історію"
   :mute-notifications                    "Вимкнути сповіщення"
   :leave-chat                            "Покинути чат"
   :chat-settings                         "Налаштування чату"
   :edit                                  "Редагувати"
   :add-members                           "Додати співрозмовників"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ваше поточне місцезнаходження"
   :places-nearby                         "Місця поруч"
   :search-results                        "Результати пошуку"
   :dropped-pin                           "Позначення адреси"
   :location                              "Місцезнаходження"
   :open-map                              "Відкрити Карту"
   :sharing-copy-to-clipboard-address     "Скопіювати Адресу"
   :sharing-copy-to-clipboard-coordinates "Скопіювати координати"

   ;;new-group
   :group-chat-name                       "Назва групи"
   :reorder-groups                        "Переставити групи"
   :edit-group                            "Редагувати групу"
   :delete-group                          "Видалити групу"
   :delete-group-confirmation             "Ця група буде видалена зі списку ваших груп. Це не зачепить список ваших контактів"
   :delete-group-prompt                   "Це не зачепить список ваших контактів"
   :contact-s                             {:one   "контакт"
                                           :other "контакти"}

   ;;protocol
   :received-invitation                   "отримано запрошення до групи"
   :removed-from-chat                     "видалив вас з групи"
   :left                                  "вийшов"
   :invited                               "запрошений"
   :removed                               "видалив"
   :You                                   "Ви"

   ;;new-contact
   :add-new-contact                       "Додати новий контакт"
   :scan-qr                               "Сканувати QR"
   :name                                  "Назва"
   :address-explication                   "Ваш відкритий ключ використовується для створення вашої адреси на Ethereum і являє собою серію цифр і букв. Ви можете легко знайти його у своєму профілі"
   :enter-valid-public-key                "Будь ласка, введіть дійсний відкритий ключ або відскануйте QR-код"
   :contact-already-added                 "Контакт вже додано"
   :can-not-add-yourself                  "Ви не можете додати себе"
   :unknown-address                       "Невідома адреса"


   ;;login
   :connect                               "Підключитися"
   :address                               "Адреса"
   :password                              "Пароль"
   :sign-in-to-status                     "Увійти в Status"
   :sign-in                               "Увійти"
   :wrong-password                        "Невірний пароль"
   :enter-password                        "Введіть пароль"

   ;;recover
   :passphrase                            "Кодова фраза"
   :recover                               "Відновити"
   :twelve-words-in-correct-order         "12 слів у правильному порядку"

   ;;accounts
   :recover-access                        "Відновити доступ"
   :add-account                           "Додати обліковий запис"

   ;;wallet-qr-code
   :done                                  "Готово"

   ;;validation
   :invalid-phone                         "Невірний номер телефону"
   :amount                                "Сума"

   ;;transactions
   :confirm                               "Підтвердити"
   :transaction                           "Транзакція"
   :unsigned-transaction-expired          "Непідписана транзакція закінчилася"
   :status                                "Статус"
   :recipient                             "Одержувач"
   :to                                    "До"
   :from                                  "З"
   :data                                  "Дані"
   :got-it                                "Зрозуміло"
   :block                                 "Блок"
   :hash                                  "Хеш"
   :gas-limit                             "Gas ліміт"
   :gas-price                             "Gas ціна"
   :gas-used                              "Gas використано"
   :cost-fee                              "Вартість/Комісія"
   :nonce                                 "Випадкове число"
   :confirmations                         "Підтверджень"
   :confirmations-helper-text             "Будь ласка, зачекайте щонайменше 12 підтверджень, щоб переконатися, що транзакція оброблена надійно"
   :copy-transaction-hash                 "Скопіювати хеш транзакції"
   :open-on-etherscan                     "Відрити на Etherscan.io"

   ;:webview
   :web-view-error                        "ой, помилка"

   ;;testfairy warning
   :testfairy-title                       "Увага!"
   :testfairy-message                     "Ви використовуєте додаток з 'нічних збірок'. З метою тестування ця збірка включає в себе запис сесії, якщо активне з'єднання WiFi, тому всі ваші взаємодії з цим додатком зберігаються (як відео чи журнали) і можуть використовуватися нашою командою розробників для вивчення можливих проблем. Збережені відео / журнали не включають ваші паролі. Запис виконується лише в тому випадку, якщо додаток встановлено з нічного збірки. Нічого не записується, якщо додаток встановлено з PlayStore або TestFlight."

   ;; wallet
   :wallet                                "Гаманець"
   :wallets                               "Гаманці"
   :your-wallets                          "Ваші гаманці"
   :main-wallet                           "Основний Гаманець"
   :wallet-error                          "Помилка завантаження даних"
   :wallet-send                           "Відправлено"
   :wallet-request                        "Запит"
   :wallet-exchange                       "Обмін"
   :wallet-assets                         "Активи"
   :wallet-add-asset                      "Додати актив"
   :wallet-total-value                    "Загальна вартість"
   :wallet-settings                       "Налаштування гаманця"
   :signing-phrase-description            "Підпишіть транзакцію, вводячи свій пароль. Переконайтеся, що наведені вище слова співпадають з вашою кодовою фразою-підписом"
   :wallet-insufficient-funds             "Недостатньо коштів"
   :request-transaction                   "Запит транзакції"
   :send-request                          "Відправляти запит"
   :share                                 "Поділитися"
   :eth                                   "ETH"
   :currency                              "Валюта"
   :usd-currency                          "USD"
   :transactions                          "Транзакції"
   :transaction-details                   "Деталі транзакції"
   :transaction-failed                    "Транзакція не вдалася"
   :transactions-sign                     "Підписати"
   :transactions-sign-all                 "Підписати всі"
   :transactions-sign-transaction         "Підписати транзакції"
   :transactions-sign-later               "Підписати потім"
   :transactions-delete                   "Видалити транзакцію"
   :transactions-delete-content           "Транзакція буде видалена з списку 'Непідписані'"
   :transactions-history                  "Історія"
   :transactions-unsigned                 "Непідписані"
   :transactions-history-empty            "У вашій історії ще немає транзакцій"
   :transactions-unsigned-empty           "У вас немає непідписних транзакцій"
   :transactions-filter-title             "Фільтрувати історію"
   :transactions-filter-tokens            "Токени"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Вибрати всі"
   :view-transaction-details              "Переглянути деталі транзакції"
   :transaction-description               "Будь ласка, зачекайте щонайменше 12 підтверджень, щоб переконатися, що транзакція оброблена надійно"
   :transaction-sent                      "Транзакція відправлена"
   :transaction-moved-text                "Транзакція залишиться у списку 'Непідписані' протягом наступних 5хв"
   :transaction-moved-title               "Транзакція перенесена"
   :sign-later-title                      "Підписати транзакцію пізніше?"
   :sign-later-text                       "Перейдіть до історії транзакцій, щоб підписати цю транзакцію"
   :not-applicable                        "Не застосовується для непідписаних транзакцій"

   ;; Wallet Send
   :wallet-choose-recipient               "Вибрати Одержувача"
   :wallet-choose-from-contacts           "Вибрати зі Списку Контактів"
   :wallet-address-from-clipboard         "Використовувати скопійовану раніше адресу"
   :wallet-invalid-address                "Невірна адреса: \n {{data}}"
   :wallet-browse-photos                  "Перегляд Фото"
   :validation-amount-invalid-number      "Сума не є дійсним номером"
   :validation-amount-is-too-precise      "Сума занадто точна. Найменша одиниця, яку ви можете надіслати, є 1 Wei (1x10^-18 ETH)"

   ;; network settings
   :new-network                           "Нова мережа"
   :add-network                           "Додати мережу"
   :add-new-network                       "Додати нову мережу"
   :existing-networks                     "Існуючі мережі"
   :add-json-file                         "Додати JSON файл"
   :paste-json-as-text                    "Вставити JSON файл"
   :paste-json                            "Вставити JSON"
   :specify-rpc-url                       "Зазначити RPC URL"
   :edit-network-config                   "Редагувати конфігурацію мережі"
   :connected                             "Підключено"
   :process-json                          "Обробити JSON"
   :error-processing-json                 "Помилка обробки JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Видалити мережу"
   :network-settings                      "Налаштування мережі"
   :edit-network-warning                  "Обережно, редагування мережевих даних може вимкнути цю мережу"
   :connecting-requires-login             "Підключення до іншої мережі вимагає входу в систему"
   :close-app-title                       "Увага!"
   :close-app-content                     "Програма зупиниться та закриється. Коли ви знову відкриєте його, вибрана мережа буде використовуватися"
   :close-app-button                      "Підтвердити"})

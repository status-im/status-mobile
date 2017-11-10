(ns status-im.translations.uk)

(def translations
  {
   ;;common
   :members-title                         "Учасники"
   :not-implemented                       "!не реалізовано"
   :chat-name                             "Назва групи"
   :notifications-title                   "Сповіщення та звуки"
   :offline                               "Оффлайн"
   :search-for                            "Шукати..."
   :cancel                                "Відмінити"
   :next                                  "Наступний"
   :open                                  "Відкрити"
   :description                           "Опис"
   :url                                   "URL"
   :type-a-message                        "Введіть повідомлення..."
   :type-a-command                        "Почніть вводити команду..."
   :error                                 "Помилка"
   :unknown-status-go-error               "Невідома помилка статусу"
   :node-unavailable                      "Жоден вузол ефіру не процює"
   :yes                                   "Так"
   :no                                    "Ні"

   :camera-access-error                   "Щоб надати потрібний дозвіл камери, перейдіть до налаштувань вашої системи та переконайтеся, що статус> Камера активований."
   :photos-access-error                   "Щоб надати дозвіл на отримання необхідних фотографій, перейдіть до налаштувань вашої системи та переконайтеся, що статус> Фото активований."

   ;;drawer
   :switch-users                          "Переключити користувачів"
   :current-network                       "Поточна мережа"

   ;;chat
   :is-typing                             "друкує"
   :and-you                               "і ви"
   :search-chat                           "Найти чат"
   :members                               {:one   "1 учасник"
                                           :other "{{count}} учасника/ів"
                                           :zero  "немає учасників"}
   :members-active                        {:one   "1 учасник"
                                           :other "{{count}} учасника/ів"
                                           :zero  "немає учасників"}
   :public-group-status                   "Паблік"
   :active-online                         "Онлайн"
   :active-unknown                        "Невідомий"
   :available                             "Доступно"
   :no-messages                           "Немає повідомлень"
   :suggestions-requests                  "Запити"
   :suggestions-commands                  "Команди"
   :faucet-success                        "Успішний запит на кран"
   :faucet-error                          "Помилка запиту на кран"

   ;;sync
   :sync-in-progress                      "Синхронізація..."
   :sync-synced                           "Синхронізовано"

   ;;messages
   :status-sending                        "Відправлення..."
   :status-pending                        "Очікує на розгляд"
   :status-sent                           "Надіслано"
   :status-seen-by-everyone               "Показано кожен"
   :status-seen                           "Показано"
   :status-delivered                      "Доставлено"
   :status-failed                         "Невдалий"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунд/и"}
   :datetime-minute                       {:one   "хвилина"
                                           :other "хвилин/и"}
   :datetime-hour                         {:one   "годин"
                                           :other "годин/и"}
   :datetime-day                          {:one   "день"
                                           :other "дні/в"}
   :datetime-ago                          "тому назад"
   :datetime-yesterday                    "вчора"
   :datetime-today                        "сьогодні"

   ;;profile
   :profile                               "Профіль"
   :edit-profile                          "Редагувати профіль"
   :message                               "Повідомлення"
   :not-specified                         "Не визначено"
   :public-key                            "Публічний ключ"
   :phone-number                          "Номер телефону"
   :update-status                         "Оновіть свій статус..."
   :add-a-status                          "Додати статус..."
   :status-prompt                         "Встановіть свій статус. Використання #хештегу допоможе іншим знайти вас і поговорити про те, що у вас на думці"
   :add-to-contacts                       "Додати контакти"
   :in-contacts                           "В контактах"
   :remove-from-contacts                  "Видалити з контактів"
   :start-conversation                    "Почати бесіду"
   :send-transaction                      "Відправити транзакцію"
   :testnet-text                          "Ви знаходитесь на {{testnet}} Testnet. Не надсилайте справжні ETH або SNT на вашу адресу "
   :mainnet-text                          "Ви знаходитесь на Mainnet. Справжні ETH будуть відправлені"

   ;;make_photo
   :image-source-title                    "Зображення профілю"
   :image-source-make-photo               "Зняти"
   :image-source-gallery                  "Вибрати з галереї"

   ;;sharing
   :sharing-copy-to-clipboard             "Копіювати в буфер обміну"
   :sharing-share                         "Поділитись..."
   :sharing-cancel                        "Відмінити"

   :browsing-title                        "Перегляньте"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "Відкрити у браузері"
   :browsing-cancel                       "Відмінити"

   ;;sign-up
   :contacts-syncronized                  "Ваші контакти були синхронізовані"
   :confirmation-code                     (str "Дякуємо! Ми відправили текстове повідомлення з підтвердженням "
                                               "код. Будь ласка, введіть цей код, щоб підтвердити свій номер телефону")
   :incorrect-code                        (str "На жаль, код невірний, будь ласка, введіть його знову")
   :phew-here-is-your-passphrase          "Хух, це було важко. Ось ваша секретна фраза, * напишіть це і збережіть його в безпеці!* Вам знадобиться це, щоб відновити свій обліковий запис."
   :here-is-your-passphrase               "Ось ваша секретна фраза, * напишіть це та збережіть це в безпеці! * Вам це потрібно для відновлення акаунту."
   :here-is-your-signing-phrase           "Ось ваша парольна фраза. Ви будете використовувати її для підтвердження ваших транзакцій. * Запишіть її та тримайте її в безпеці! *"
   :phone-number-required                 "Торкніться тут, щоб підтвердити свій номер телефону, і я знайду ваших друзів."
   :shake-your-phone                      "Знайшли помилку або є пропозиція? Просто ~потрясіть~ ваш телефон!"
   :intro-status                          "Поспілкуйся зі мною, щоб налаштувати свій обліковий запис і змінити свої налаштування."
   :intro-message1                        "Ласкаво просимо до Status!\nTap це повідомлення встановити пароль і почати роботу."
   :account-generation-message            "Почекай секунду, я повинен зробити дивовижні математичні кроки, щоб згенерувати твій акаунт!"
   :move-to-internal-failure-message      "Нам треба перемістити деякі важливі файли з зовнішньої на внутрішню пам'ять. Для цього нам потрібен ваш дозвіл. Ми не будемо використовувати зовнішню пам'ять у майбутніх версіях."
   :debug-enabled                         "Сервер відлагоджень був запущений! Тепер ви можете виконати *status-dev-cli scan* щоб знайти сервер з вашого комп'ютера в тій самій мережі."

   ;;phone types
   :phone-e164                            "Міжнародний 1"
   :phone-international                   "Міжнародний 2"
   :phone-national                        "Державний"
   :phone-significant                     "Показовий"

   ;;chats
   :chats                                 "Чати"
   :delete-chat                           "Видалити чат"
   :new-group-chat                        "Новий груповий чат"
   :new-public-group-chat                 "Долучитися до публічного чату"
   :edit-chats                            "Редагувати чати"
   :search-chats                          "Шукати чати"
   :empty-topic                           "Пуста тема"
   :topic-format                          "Невірний формат [a-z0-9\\-]+"
   :public-group-topic                    "Тема"

   ;;discover
   :discover                              "Знайти"
   :none                                  "Жоден"
   :search-tags                           "Введіть свої пошукові теги тут"
   :popular-tags                          "Популярні #хештеги"
   :recent                                "Останні статуси"
   :no-statuses-found                     "Немає знайдених статусів"
   :chat                                  "Чат"
   :all                                   "Всі"
   :public-chats                          "Публічні чати"
   :soon                                  "Скоро"
   :public-chat-user-count                "{{count}} учасника/ів"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp профіль"
   :no-statuses-discovered                "Немає знайдених статусів"
   :no-statuses-discovered-body           "Коли хтось публікує статус, ви побачите його тут."
   :no-hashtags-discovered-title          "Немає знайдених #хештегів"
   :no-hashtags-discovered-body           "Коли #хештег стане популярним, ви побачите його тут."

   ;;settings
   :settings                              "Налаштування"

   ;;contacts
   :contacts                              "Контакти"
   :new-contact                           "Новий контакт"
   :delete-contact                        "Видалити контакт"
   :delete-contact-confirmation           "Цей контакт буде видалено з ваших контактів"
   :remove-from-group                     "Перемістити з групи"
   :edit-contacts                         "Редагувати контакти"
   :search-contacts                       "Шукати контакти"
   :contacts-group-new-chat               "Почати новий чат"
   :choose-from-contacts                  "Вибрати з контактів"
   :no-contacts                           "Доки немає котнактів"
   :show-qr                               "Показати QR код"
   :enter-address                         "Введіть адресу"
   :more                                  "більше"

   ;;group-settings
   :remove                                "Перемістити"
   :save                                  "Зберегти"
   :delete                                "Видалити"
   :clear-history                         "Очистити історію"
   :mute-notifications                    "Вимкнути сповіщення"
   :leave-chat                            "Покинути чат"
   :chat-settings                         "Настройки чату"
   :edit                                  "Редагувати"
   :add-members                           "Додати учасників"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ваше поточне міце положення"
   :places-nearby                         "Місця поблизу"
   :search-results                        "Шукати результати"
   :dropped-pin                           "Випав пін"
   :location                              "Місце"
   :open-map                              "Відкрити карту"
   :sharing-copy-to-clipboard-address     "Копіювати адресу."
   :sharing-copy-to-clipboard-coordinates "Копіювати координати."

   ;;new-group
   :new-group                             "Нова група"
   :reorder-groups                        "Змінити порядок груп"
   :edit-group                            "Редагувати групу"
   :delete-group                          "Видалити групу"
   :delete-group-confirmation             "Ця група буде вилучена з ваших груп. Це не вплине на ваші контакти"
   :delete-group-prompt                   "Це не вплине на ваші контакти"
   :contact-s                             {:one   "контакт"
                                           :other "контакти/ів"}

   ;;protocol
   :received-invitation                   "отримав запрошення до чату"
   :removed-from-chat                     "вилучив вас з групового чату"
   :left                                  "покинув"
   :invited                               "запросив"
   :removed                               "вилучив"
   :You                                   "Ви"

   ;;new-contact
   :add-new-contact                       "Додати новий контакт"
   :scan-qr                               "Сканувати QR-код"
   :name                                  "Ім'я"
   :address-explication                   "Ваш відкритий ключ використовується для створення вашої адреси на Ethereum і являє собою серію цифр і букв. Ви можете легко знайти його у своєму профілі"
   :enter-valid-public-key                "Будь ласка, введіть дійсний відкритий ключ або скануйте QR-код"
   :contact-already-added                 "Контакт вже додано"
   :can-not-add-yourself                  "Ви не можете додати себе"
   :unknown-address                       "Невідома адреса"

   ;;login
   :connect                               "Підключіться"
   :address                               "Адреса"
   :password                              "Праоль"
   :sign-in-to-status                     "Увійдіть у статус"
   :sign-in                               "Увійдіть"
   :wrong-password                        "Неправильний пароль"
   :enter-password                        "Введіть пароль"

   ;;recover
   :passphrase                            "Секретна фраза"
   :recover                               "Відновити"
   :twelve-words-in-correct-order         "12 слів у правильному порядку"

   ;;accounts
   :recover-access                        "Відновити доступ"
   :create-new-account                    "Створити новий акаунт"

   ;;wallet-qr-code
   :done                                  "Готово"

   ;;validation
   :invalid-phone                         "Недійсний номер телефону"
   :amount                                "Сума"

   ;;transactions
   :confirm                               "Підтвердити"
   :transaction                           "Транзакція"
   :unsigned-transaction-expired          "Непідписана транзакція закінчилась"
   :status                                "Статус"
   :recipient                             "Одержувач"
   :to                                    "До"
   :from                                  "Від"
   :data                                  "Дані"
   :got-it                                "Зрозумів"
   :block                                 "Блок"
   :hash                                  "Хеш"
   :gas-limit                             "Ліміт газу"
   :gas-price                             "Ціна газу"
   :gas-used                              "Газу використано"
   :cost-fee                              "Вартість/Плата"
   :nonce                                 "Не в порядку"
   :confirmations                         "Підтвердження"
   :confirmations-helper-text             "Будь ласка, зачекайте щонайменше 12 підтверджень, щоб переконатися, що транзакція оброблена надійно"
   :copy-transaction-hash                 "Копіювати хеш транзакції"
   :open-on-etherscan                     "Відкрити на Etherscan.io"

   ;;webview
   :web-view-error                        "Ой, помилка"

   ;;testfairy warning
   :testfairy-title                       "Увага!"
   :testfairy-message                     "Ви використовуєте програму, встановлену не з офіційного магазину. Для тестування ця побудова включає в себе запис сесії, якщо використовується Wi-Fi-зв'язок, тому всі ваші взаємодії з цим додатком зберігаються (як відео та журнали) і можуть використовуватися нашою командою розробників для вивчення можливих проблем. Збережені відео / журнали не включають ваші паролі. Запис виконується лише в тому випадку, якщо додаток встановлено з неофіційного магазину. Нічого не записується, якщо додаток встановлено з PlayStore або TestFlight."

   ;; wallet
   :wallet                                "Гаманець"
   :wallets                               "Гаманці"
   :your-wallets                          "Ваші гаманці"
   :main-wallet                           "Основний гаманець"
   :wallet-error                          "Помилка завантаження даних"
   :wallet-send                           "Відправити"
   :wallet-request                        "Запит"
   :wallet-exchange                       "Обмін"
   :wallet-assets                         "Активи"
   :wallet-add-asset                      "Додати актив"
   :wallet-total-value                    "Загальна вартість"
   :wallet-settings                       "Налаштування гаманця"
   :signing-phrase-description            "Підпишіть транзакцію, ввівши свій пароль. Переконайтеся, що наведені вище слова співпадають з вашою таємною фразою"
   :wallet-insufficient-funds             "Недостатньо коштів"
   :request-transaction                   "Запит транзакції"
   :send-request                          "Відправити запит"
   :share                                 "Поділитися"
   :eth                                   "ETH"
   :currency                              "Валюта"
   :usd-currency                          "USD"
   :transactions                          "Транзакції"
   :transaction-details                   "Деталі транзакції"
   :transaction-failed                    "Транзакція не виконана"
   :transactions-sign                     "Підписати"
   :transactions-sign-all                 "Підписати всі"
   :transactions-sign-transaction         "Підписати транзакцію"
   :transactions-sign-later               "Підписати пізніше"
   :transactions-delete                   "Видалити транзакцію"
   :transactions-delete-content           "Транзакції будуть видалені зі списку 'Непідписані' "
   :transactions-history                  "Історія"
   :transactions-unsigned                 "Непідписані"
   :transactions-history-empty            "Поки що немає жодних транзакцій у вашій історії"
   :transactions-unsigned-empty           "Ви не маєте жодних непідписаних транзакцій"
   :transactions-filter-title             "Сортувати історію"
   :transactions-filter-tokens            "Токени"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Вибрати все"
   :view-transaction-details              "Показати деталі транзакції"
   :transaction-description               "Please wait for at least 12 confirmations to make sure your transaction is processed securely"
   :transaction-sent                      "Транзакція відправлена"
   :transaction-moved-text                "Не підписані транзакції будуть видалені протягом наступних 5 хвилин"
   :transaction-moved-title               "Транзакція переміщена"
   :sign-later-title                      "Підписати транзакцію пізніше?"
   :sign-later-text                       "Перевірити історію транзакцій для підписання"
   :not-applicable                        "Не застосовується для непідписаних транзакцій"

   ;; Wallet Send
   :wallet-choose-recipient               "Виберіть отримувача"
   :wallet-choose-from-contacts           "Виберіть з контактів"
   :wallet-address-from-clipboard         "Використати адресу з буферу обміну"
   :wallet-invalid-address                "Невірна адреса: \n {{data}}"
   :wallet-browse-photos                  "Перегляньте фотографії"
   :validation-amount-invalid-number      "Сума не є дійсним числом"
   :validation-amount-is-too-precise      "Сума занадто точна. Найменша одиниця, яку ви можете надіслати - 1 Вей (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Нова мережа"
   :add-network                           "Додати мережу"
   :add-new-network                       "Додати нову мережу"
   :existing-networks                     "Існуючі мережі"
   :add-json-file                         "Додати JSON файл"
   :paste-json-as-text                    "Вставити JSON як текст"
   :paste-json                            "Вставити JSON"
   :specify-rpc-url                       "Вкажіть RPC URL"
   :edit-network-config                   "Редагувати параметри мережы"
   :connected                             "Підключено"
   :process-json                          "Обробити JSON"
   :error-processing-json                 "Помилка обробки JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Видалити мережу"
   :network-settings                      "Настройки мережі"
   :edit-network-warning                  "Будьте обережні, редагуючи дані мережі, вас може відключити від цієї"
   :connecting-requires-login             "Підключення до іншої мережі потребує логіну"
   :close-app-title                       "Увага!"
   :close-app-content                     "Додаток буде зупинений і закртий. Коли ви знову відкриєте його, вибрані мережі будуть застосовані"
   :close-app-button                      "Підтвердити"})

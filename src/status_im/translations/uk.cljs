(ns status-im.translations.uk)

(def translations
  {
   ;;common
   :members-title                         "Учасники"
   :not-implemented                       "!не реалізовано"
   :chat-name                             "Назва чату"
   :notifications-title                   "Сповіщення та звуки"
   :offline                               "Оффлайн"
   :search-for                            "Шукати..."
   :cancel                                "Скасувати"
   :next                                  "Далі"
   :open                                  "Відкрити"
   :description                           "Опис"
   :url                                   "URL-адреса"
   :type-a-message                        "Написати повідомлення..."
   :type-a-command                        "Почніть вводити команду..."
   :error                                 "Помилка"
   :unknown-status-go-error               "Невідома status-go помилка"
   :node-unavailable                      "Немає запущеного ethereum вузла"
   :yes                                   "Так"
   :no                                    "Ні"

   :camera-access-error                   "Щоб надати необхідний дозвіл для камери, будь ласка, перейдіть в налаштування системи і переконайтеся, що вибрано Status > Камера."
   :photos-access-error                   "Щоб надати необхідний дозвіл для фото, будь ласка, перейдіть в налаштування системи і переконайтеся, що вибрано Status > Фотографії."

   ;;drawer
   :switch-users                          "Змінити користувачів"
   :current-network                       "Поточна мережа"

   ;;chat
   :is-typing                             "друкує"
   :and-you                               "і ви"
   :search-chat                           "Шукати чат"
   :members                               {:one   "1 контакт"
                                           :other "{{count}} контакти"
                                           :zero  "немає контактів"}
   :members-active                        {:one   "1 контакт"
                                           :other "{{count}} контакти"
                                           :zero  "немає контактів"}
   :public-group-status                   "Публічний"
   :active-online                         "Онлайн"
   :active-unknown                        "Невідомо"
   :available                             "Доступно"
   :no-messages                           "Немає повідомлень"
   :suggestions-requests                  "Запити"
   :suggestions-commands                  "Команди"
   :faucet-success                        "Запит Faucet був отриманий"
   :faucet-error                          "Помилка запиту Faucet"

   ;;sync
   :sync-in-progress                      "Синхронізація..."
   :sync-synced                           "Синхронізовано"

   ;;messages
   :status-sending                        "Відправляється..."
   :status-pending                        "В очікуванні"
   :status-sent                           "Відправлено"
   :status-seen-by-everyone               "Переглянуто усіма"
   :status-seen                           "Переглянуто"
   :status-delivered                      "Доставлено"
   :status-failed                         "Помилка"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунди"}
   :datetime-minute                       {:one   "хвилина"
                                           :other "хвилин"}
   :datetime-hour                         {:one   "година"
                                           :other "години"}
   :datetime-day                          {:one   "день"
                                           :other "дні"}
   :datetime-ago                          "назад"
   :datetime-yesterday                    "вчора"
   :datetime-today                        "сьогодні"

   ;;profile
   :profile                               "Профіль"
   :edit-profile                          "Редагувати профіль"
   :message                               "Повідомлення"
   :not-specified                         "Не вказано"
   :public-key                            "Публічний ключ"
   :phone-number                          "Номер телефону"
   :update-status                         "Оновіть свій статус..."
   :add-a-status                          "Додати статус..."
   :status-prompt                         "Вкажіть свій статус. Використання #хештегів допоможе іншим дізнатися про вас і розповісти про те, що у вас на думці"
   :add-to-contacts                       "Додати до контактів"
   :in-contacts                           "В контактах"
   :remove-from-contacts                  "Відалити за контактів"
   :start-conversation                    "Почати розмову"
   :send-transaction                      "Відправити транзакцію"
   :testnet-text                          "Ви знаходитесь в {{testnet}} Testnet. Не відправляйте реальний ETH або SNT на свою адресу"
   :mainnet-text                          "Ви в Mainnet. Реальний ETH буде відправлений"

   ;;make_photo
   :image-source-title                    "Зображення профілю"
   :image-source-make-photo               "Сфотографувати"
   :image-source-gallery                  "Вибрати з галереї"

   ;;sharing
   :sharing-copy-to-clipboard             "Скопіювати"
   :sharing-share                         "Поділитися..."
   :sharing-cancel                        "Скасувати"

   :browsing-title                        "Переглядати"
   :browsing-open-in-web-browser          "Відкрити у веб-браузері"
   :browsing-cancel                       "Скасувати"

   ;;sign-up
   :contacts-syncronized                  "Ваші контакти синхронізовано"
   :confirmation-code                     (str "Дякуємо! Ми вам надіслали СМС з кодом підтвердження."
                                               "Введіть цей код для підтвердження свого номера телефону")
   :incorrect-code                        (str "Вибачте, код неправильний, введіть ще раз")
   :phew-here-is-your-passphrase          "*Уф*, це було непросто, ось ваша парольна фраза *запишіть її і зберігайте в надійному місці!* Вона буде потрібна вам для відновлення облікового запису."
   :here-is-your-passphrase               "Ось ваша парольна фраза *запишіть її і зберігайте в надійному місці!* Вона буде потрібна вам для відновлення облікового запису."
   :here-is-your-signing-phrase           "Ось ваша фраза підпису. Ви будете використовувати її для перевірки достовірності своїх транзакцій. *Запишіть її і зберігайте в безпеці!*"
   :phone-number-required                 "Натисніть сюди для введення номера телефону і я знайду ваших друзів."
   :shake-your-phone                      "Знайдена помилка або є пропозиція? Просто ~потрясіть~ свій телефон!"
   :intro-status                          "Поспілкуйтеся зі мною в чаті, щоб налаштувати свій аккаунт і змінити свої налаштування!"
   :intro-message1                        "Ласкаво просимо в Status\nнатисніть на це повідомлення, щоб встановити пароль і почати!"
   :account-generation-message            "Секундочку, мені потрібно виконати шалено складні розрахунки для створення вашого аккаунта!"
   :move-to-internal-failure-message      "Нам потрібно перемістити деякі важливі файли з зовнішнього сховища у внутрішнє. Для цього нам потрібен ваш дозвіл. В наступних версіях ми не будемо використовувати зовнішнє сховища."
   :debug-enabled                         "Запущений сервер налагоджування! Тепер ви можете виконати * status-dev-cli scan *, щоб знайти сервер з вашого комп'ютера в мережі."

   ;;phone types
   :phone-e164                            "Міжнародний 1"
   :phone-international                   "Міжнародний 2"
   :phone-national                        "Державний"
   :phone-significant                     "Значний"

   ;;chats
   :chats                                 "Чати"
   :delete-chat                           "Видалити чат"
   :new-group-chat                        "Новий груповий чат"
   :new-public-group-chat                 "Приєднатися до публічного чату"
   :edit-chats                            "Редагувати чати"
   :search-chats                          "Пошук чатів"
   :empty-topic                           "Порожня тема"
   :topic-format                          "Неправильний формат [a-z0-9\\-]+"
   :public-group-topic                    "Тема"

   ;;discover
   :discover                              "Пошук"
   :none                                  "Жоден"
   :search-tags                           "Введіть теги для пошуку сюди"
   :popular-tags                          "Популярні #хештеги"
   :recent                                "Останні статуси"
   :no-statuses-found                     "Статуси не виявлені"
   :chat                                  "Чат"
   :all                                   "Всі"
   :public-chats                          "Публічні чати"
   :soon                                  "Незабаром"
   :public-chat-user-count                "{{count}} людей"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp профіль"
   :no-statuses-discovered                "Статуси не виявлені"
   :no-statuses-discovered-body           "Коли хтось створить\nстатус, ви побачите його тут."
   :no-hashtags-discovered-title          "#хештеги не виявлені"
   :no-hashtags-discovered-body           "Коли #хештег стане\nпопулярним, ви побачите його тут."

   ;;settings
   :settings                              "Налаштування"

   ;;contacts
   :contacts                              "Контакти"
   :new-contact                           "Новий контакт"
   :delete-contact                        "Видалити контакт"
   :delete-contact-confirmation           "Цей контакт буде видалено з ваших контактів"
   :remove-from-group                     "Видалити з групи"
   :edit-contacts                         "Редагування контактів"
   :search-contacts                       "Пошук контактів"
   :contacts-group-new-chat               "Новий груповий чат"
   :choose-from-contacts                  "Вибрати з контактів"
   :no-contacts                           "Поки що немає контактів"
   :show-qr                               "Показати QR код"
   :enter-address                         "Ввести адресу"
   :more                                  "більше"

   ;;group-settings
   :remove                                "Видалити"
   :save                                  "Зберегти"
   :delete                                "Видалити"
   :clear-history                         "Видалити історію"
   :mute-notifications                    "Вимкнути звук в сповіщеннях"
   :leave-chat                            "Вийти з чату"
   :chat-settings                         "Налаштування чату"
   :edit                                  "Редагувати"
   :add-members                           "Додати учасників"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ваше поточне місцезнаходження"
   :places-nearby                         "Місця поблизу"
   :search-results                        "Результати пошуку"
   :dropped-pin                           "Вибране місцезнаходження"
   :location                              "Місцезнаходження"
   :open-map                              "Відкрити Карту"
   :sharing-copy-to-clipboard-address     "Скопіювати адресу"
   :sharing-copy-to-clipboard-coordinates "Скопіювати координати"

   ;;new-group
   :new-group                             "Нова група"
   :reorder-groups                        "Впорядкувати групи"
   :edit-group                            "Редагувати групу"
   :delete-group                          "Видалити групу"
   :delete-group-confirmation             "Ця група буде видалена з ваших груп. Це не вплине на ваші контакти"
   :delete-group-prompt                   "Це не вплине на ваші контакти"
   :contact-s                             {:one   "контакт"
                                           :other "контакти"}

   ;;protocol
   :received-invitation                   "отримав запрошення у чат"
   :removed-from-chat                     "видалив вас з групового чату"
   :left                                  "вийшов"
   :invited                               "запрошений"
   :removed                               "видалений"
   :You                                   "Ви"

   ;;new-contact
   :add-new-contact                       "Додати новий контакт"
   :scan-qr                               "Сканувати QR код"
   :name                                  "Ім'я"
   :address-explication                   "Ваш публічний ключ використовується для створення вашої адреси в Ethereum і являє собою ряд цифр і букв. Ви можете легко знайти його в своєму профілі"
   :enter-valid-public-key                "Введіть дійсний публічний ключ або скануйте QR код"
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
   :passphrase                            "Парольна фраза"
   :recover                               "Відновити"
   :twelve-words-in-correct-order         "12 слів в правильному порядку"

   ;;accounts
   :recover-access                        "Відновлення доступу"
   :create-new-account                    "Створити новий обліковий запис"

   ;;wallet-qr-code
   :done                                  "Готово"

   ;;validation
   :invalid-phone                         "Неправильний номер телефону"
   :amount                                "Сума"

   ;;transactions
   :confirm                               "Підтвердити"
   :transaction                           "Транзакція"
   :unsigned-transaction-expired          "Термін дії непідписаної транзакції завершився"
   :status                                "Status"
   :recipient                             "Одержувач"
   :to                                    "Кому"
   :from                                  "Від"
   :data                                  "Дані"
   :got-it                                "Зрозуміло"
   :block                                 "Блок"
   :hash                                  "Хеш"
   :gas-limit                             "Ліміт Газу"
   :gas-price                             "Ціна Газу"
   :gas-used                              "Використаний Газ"
   :cost-fee                              "Вартість/Комісія"
   :nonce                                 "Nonce"
   :confirmations                         "Підтвердження"
   :confirmations-helper-text             "Будь ласка, зачекайте як мінімум 12-ти підтверджень, щоб переконатися, що транзакція оброблена безпечно"
   :copy-transaction-hash                 "Копіювати хеш транзакції"
   :open-on-etherscan                     "Відкрити посилання на Etherscan.io"
   :incoming                              "Вхідні"
   :outgoing                              "Вихідні"
   :pending                               "В очікуванні"
   :postponed                             "Відкладені"

   ;;webview
   :web-view-error                        "ой, помилка"

   ;;testfairy warning
   :testfairy-title                       "Попередження!"
   :testfairy-message                     "Ви використовуєте додаток, встановлений з нічної збірки. Для цілей тестування ця збірка включає в себе запис сеансу, якщо використовується з'єднання Wi-Fi, так що всі ваші взаємодії з цим додатком зберігаються (як відео і логи) і можуть використовуватися нашою командою розробників для вивчення можливих проблем. Збережені відео / логи не включають в себе ваші паролі. Запис виконується тільки в тому випадку, якщо додаток встановлено з нічного збирання. Нічого не записується, якщо додаток встановлено з PlayStore або TestFlight."

   ;; wallet
   :wallet                                "Гаманець"
   :wallets                               "Гаманці"
   :your-wallets                          "Ваші гаманці"
   :main-wallet                           "Основний Гаманець"
   :wallet-error                          "Помилка при завантаженні даних"
   :wallet-send                           "Відправити"
   :wallet-send-token                     "Відправити {{symbol}}"
   :wallet-request                        "Запросити"
   :wallet-exchange                       "Обміняти"
   :wallet-assets                         "Активи"
   :wallet-add-asset                      "Додати актив"
   :wallet-total-value                    "Загальна вартість"
   :wallet-settings                       "Налаштування гаманця"
   :wallet-manage-assets                  "Управління Активами"
   :signing-phrase-description            "Підпишіть транзакцію, ввівши свій пароль. Переконайтеся, що слова вище відповідають вашій секретній фразі підпису"
   :wallet-insufficient-funds             "Недостатньо коштів"
   :request-transaction                   "Запит транзакції"
   :send-request                          "Надіслати запит"
   :share                                 "Поділитися"
   :eth                                   "ETH"
   :currency                              "Валюта"
   :usd-currency                          "USD"
   :transactions                          "Транзакції"
   :transaction-details                   "Деталі транзакції"
   :transaction-failed                    "Не вдалося виконати транзакцію"
   :transactions-sign                     "Підписати"
   :transactions-sign-all                 "Підписати все"
   :transactions-sign-transaction         "Підписати транзакцію"
   :transactions-sign-later               "Підписати пізніше"
   :transactions-delete                   "Видалити транзакцію"
   :transactions-delete-content           "Транзакція буде видалена зі списку 'Непідписані'"
   :transactions-history                  "Історія"
   :transactions-unsigned                 "Непідписані"
   :transactions-history-empty            "У вашій історії ще немає транзакцій"
   :transactions-unsigned-empty           "У вас немає жодних непідписаних транзакцій"
   :transactions-filter-title             "Фільтри історії"
   :transactions-filter-tokens            "Токени"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Вибрати все"
   :view-transaction-details              "Подивитися деталі транзакції"
   :transaction-description               "Будь ласка, почекайте як мінімум 12 підтверджень, щоб переконатися, що транзакція оброблена безпечно"
   :transaction-sent                      "Транзакція відправлена"
   :transaction-moved-text                "Транзакція буде знаходитися в списку 'Непідписані' протягом 5 хвилин"
   :transaction-moved-title               "Транзакція переміщена"
   :sign-later-title                      "Підписати транзакцію пізніше?"
   :sign-later-text                       "Відкрийте історію транзакцій, щоб підписати цю транзакцію"
   :not-applicable                        "Не можна застосувати для непідписаних транзакцій"

   ;; Wallet Send
   :wallet-choose-recipient               "Виберіть Одержувача"
   :wallet-choose-from-contacts           "Вибрати з Контактів"
   :wallet-address-from-clipboard         "Використати Адресу Із Буфера Обміну"
   :wallet-invalid-address                "Недійсна адреса: \n {{data}}"
   :wallet-invalid-chain-id               "Мережа не відповідає: \n {{data}}"
   :wallet-browse-photos                  "Переглянути Фотографії"
   :validation-amount-invalid-number      "Сума недійсна"
   :validation-amount-is-too-precise      "Занадто багато цифр після десяткової коми. Найменша сума, яку ви можете надіслати це 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Нова мережа"
   :add-network                           "Додати мережу"
   :add-new-network                       "Додати нову мережу"
   :add-wnode                             "Додати поштовий сервер"
   :existing-networks                     "Існуючі мережі"
   ;; TODO(dmitryn): come up with better description/naming. Suggested namings: Mailbox and Master Node
   :existing-wnodes                       "Існуючі поштові сервери"
   :add-json-file                         "Додати JSON файл"
   :paste-json-as-text                    "Вставити JSON як текст"
   :paste-json                            "Вставити JSON"
   :specify-rpc-url                       "Вкажіть RPC URL-адресу"
   :edit-network-config                   "Редагувати конфігурацію мережі"
   :connected                             "Підключений"
   :process-json                          "Обробити JSON"
   :error-processing-json                 "Помилка обробки JSON"
   :rpc-url                               "RPC URL-адреса"
   :remove-network                        "Видалити мережу"
   :network-settings                      "Налаштування мережі"
   :offline-messaging-settings            "Параметри оффлайн повідомленнь"
   :edit-network-warning                  "Будьте обережні, зміна налаштувань мережі може зробити її недоступною для вас"
   :connecting-requires-login             "Підключення до мережі вимагає входу в систему"
   :close-app-title                       "Попередження!"
   :close-app-content                     "Додаток зупиниться і закриється. При повторному відкритті, буде використовуватися обрана мережа"
   :close-app-button                      "Підтвердити"})

(ns status-im.translations.uk)

(def translations
  {
   ;;common
   :members-title                         "Учасники"
   :not-implemented                       "!не реалізовано"
   :chat-name                             "Назва групи"
   :notifications-title                   "Сповіщення та звуки"
   :offline                               "Офлайн"
   :search-for                            "Шукати..."
   :cancel                                "Відміна"
   :next                                  "Далі"
   :open                                  "Відкрити"
   :description                           "Опис"
   :url                                   "URL"
   :type-a-message                        "Написати повідомлення..."
   :type-a-command                        "Почніть вводити команду..."
   :error                                 "Помилка"
   :unknown-status-go-error               "Невідома status-go помилка"
   :node-unavailable                      "Не знайдено ethereum вузол"
   :yes                                   "Так"
   :no                                    "Ні"
    
   :camera-access-error                   "Щоб надати доступ до камери, перейдіть до налаштувань вашої системи та переконайтеся, що вибранo Status > Камера"
   :photos-access-error                   "Щоб надати доступ до фото, перейдіть до налаштувань вашої системи та переконайтеся, що вибранo Status > Фото"

   ;;drawer
   :switch-users                          "Змінити користувачів"
   :current-network                       "Поточна мережа"

   ;;chat
   :is-typing                             "друкує"
   :and-you                               "і ви"
   :search-chat                           "Пошук по чату"
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
   :faucet-success                        "Faucet запит отримано"
   :faucet-error                          "Помилка запиту Faucet"

   ;;sync
   :sync-in-progress                      "Синхронізація..."
   :sync-synced                           "Синхронізовано"
    
   ;messages
   :status-sending                        "Відправлення"
   :status-pending                        "Очікує відправлення"
   :status-sent                           "Надіслано"
   :status-seen-by-everyone               "Переглянуто всіма"
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
   :browsing-open-in-web-browser          "Відкрити у веб-браузері"
   :browsing-cancel                       "Відміна"

   ;;sign-up
   :contacts-syncronized                  "Ваші контактні дані було синхронізовано"
   :confirmation-code                     (str "Дякуємо! Ми відправили вам текстове повідомлення з кодом "
                                               "підтвердження. Будь ласка, надайте цей код, щоб підтвердити свій номер телефону")
   :incorrect-code                        (str "На жаль, код невірний, будь ласка, введіть ще раз")
   :phew-here-is-your-passphrase          "Фух, це було важко. Ось ваша кодова фраза, *запишіть її та надійно збережіть!* Вона пригодиться для відновлення облікового запису."
   :here-is-your-passphrase               "Ось ваша кодова фраза, *запишіть її та надійно збережіть!* Вона пригодиться для відновлення облікового запису."
   :here-is-your-signing-phrase           "Ось ваша фраза-підпис. Ви будете використовувати її для перевірки достовірності своїх транзакцій. *запишіть її та надійно збережіть!*"
   :phone-number-required                 "Торкніться тут, щоб підтвердити свій номер телефону і я знайду ваших друзів."
   :shake-your-phone                      "Знайшли помилку або маєте пропозицію? Просто ~потрусіть~ ваш телефон!"
   :intro-status                          "Напишіть мені, щоб налаштувати свій обліковий запис і змінити свої налаштування."
   :intro-message1                        "Вітаємо у Status!\nТоркніться цього повідомлення, щоб встановити пароль і почати роботу."
   :account-generation-message            "Почекайте секунду, маю виконати страшенно складні розрахунки, щоб створити ваш обліковий запис!"
   :move-to-internal-failure-message      "Нам треба перемістити деякі важливі файли з зовнішньої на внутрішню пам'ять. Для цього нам потрібен ваш дозвіл. Ми не будемо використовувати зовнішню пам'ять у майбутніх версіях."
   :debug-enabled                         "Запущений сервер налагоджування! Тепер ви можете виконати * status-dev-cli scan *, щоб знайти сервер з вашого комп'ютера в мережі."

   ;;phone types
   :phone-e164                            "Міжнародний 1"
   :phone-international                   "Міжнародний 2"
   :phone-national                        "Національний"
   :phone-significant                     "Простий"

   ;;chats
   :chats                                 "Чат"
   :delete-chat                           "Видалити Чат"
   :new-group-chat                        "Новий груповий чат"
   :new-public-group-chat                 "Приєднатися до публічного чату"
   :edit-chats                            "Редагувати чат"
   :search-chats                          "Шукати чати"
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
   :dropped-pin                           "Вибране місцезнаходження"
   :location                              "Місцезнаходження"
   :open-map                              "Відкрити Карту"
   :sharing-copy-to-clipboard-address     "Скопіювати Адресу"
   :sharing-copy-to-clipboard-coordinates "Скопіювати координати"

   ;;new-group
   :group-chat-name                       "Нова група"
   :reorder-groups                        "Змінити порядок груп"
   :edit-group                            "Редагувати групу"
   :delete-group                          "Видалити групу"
   :delete-group-confirmation             "Ця група буде видалена зі списку ваших груп. Це не вплине на список ваших контактів"
   :delete-group-prompt                   "Це не вплине на список ваших контактів"
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
   :address-explication                   "Ваш відкритий ключ використовується для створення вашої адреси в Ethereum і являє собою серію цифр і букв. Ви можете легко знайти його у своєму профілі"
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
   :to                                    "Кому"
   :from                                  "Від"
   :data                                  "Дані"
   :got-it                                "Зрозуміло"
   :block                                 "Блок"
   :hash                                  "Хеш"
   :gas-limit                             "Ліміт газу"
   :gas-price                             "Ціна газу"
   :gas-used                              "Використано газу"
   :cost-fee                              "Вартість/Комісія"
   :nonce                                 "Випадкове число"
   :confirmations                         "Підтверджень"
   :confirmations-helper-text             "Будь ласка, зачекайте щонайменше 12 підтверджень, щоб переконатися, що транзакція оброблена надійно"
   :copy-transaction-hash                 "Скопіювати хеш транзакції"
   :open-on-etherscan                     "Відрити на Etherscan.io"
   :incoming                              "Отримані"
   :outgoing                              "Відправлені"
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
   :wallet-my-token                       "Мій {{symbol}}"
   :wallet-market-value                   "Ринкова вартість"
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
   :sign-later-text                       "Перейдіть до історії транзакцій, щоб підписати цю транзакцію"
   :not-applicable                        "Не можна застосувати до непідписаних транзакцій"

   ;; Wallet Send
   :wallet-choose-recipient               "Вибрати Одержувача"
   :wallet-choose-from-contacts           "Вибрати зі Списку Контактів"
   :wallet-address-from-clipboard         "Використати Адресу з буферу обміну"
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
   :specify-rpc-url                       "Зазначити RPC URL"
   :edit-network-config                   "Редагувати конфігурацію мережі"
   :connected                             "Підключено"
   :process-json                          "Обробити JSON"
   :error-processing-json                 "Помилка обробки JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Видалити мережу"
   :network-settings                      "Налаштування мережі"
   :offline-messaging-settings            "Офлайн налаштування повідомлень"
   :edit-network-warning                  "Обережно, редагування мережевих даних може вимкнути цю мережу"
   :connecting-requires-login             "Підключення до іншої мережі вимагає входу в систему"
   :close-app-title                       "Увага!"
   :close-app-content                     "Додаток буде зупинено і закрито. Обрана мережа буде використана під час наступного запуску."
   :close-app-button                      "Підтвердити"})

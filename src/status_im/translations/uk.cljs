(ns status-im.translations.uk)

(def translations
  {
   ;common
   :members-title                         "Учасники"
   :not-implemented                       "!не реалізовано"
   :chat-name                             "Назва групи"
   :notifications-title                   "Сповіщення та звуки"
   :offline                               "Оффлайн"

   ;drawer
   :invite-friends                        "Запросити друзів"
   :faq                                   "Часті питання"
   :switch-users                          "Зміна користувача"

   ;chat
   :is-typing                             "друкує"
   :and-you                               "і ви"
   :search-chat                           "Пошук групи"
   :members                               {:one   "1 учасник"
                                           :other "{{count}} учасників"
                                           :zero  "немає учасників"}
   :members-active                        {:one   "1 учасник, 1 активний"
                                           :other "{{count}} учасників, {{count}} активних"
                                           :zero  "немає учасників"}
   :active-online                         "Онлайн"
   :active-unknown                        "Невідомо"
   :available                             "Доступно"
   :no-messages                           "Немає повідомлень"
   :suggestions-requests                  "Запити"
   :suggestions-commands                  "Команди"

   ;sync
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
   :datetime-second                       {:one   "секунда"
                                           :other "секунд"}
   :datetime-minute                       {:one   "хвилина"
                                           :other "хвилин"}
   :datetime-hour                         {:one   "година"
                                           :other "годин"}
   :datetime-day                          {:one   "день"
                                           :other "днів"}
   :datetime-multiple                     "с"
   :datetime-ago                          "тому"
   :datetime-yesterday                    "вчора"
   :datetime-today                        "сьогодні"

   ;profile
   :profile                               "Профіль"
   :report-user                           "ДОПОВІСТИ ПРО КОРИСТУВАЧА"
   :message                               "Повідомлення"
   :username                              "Ім'я користувача"
   :not-specified                         "Не вказано"
   :public-key                            "Відкритий ключ"
   :phone-number                          "Номер телефону"
   :email                                 "Ел. пошта"
   :profile-no-status                     "Немає статусу"
   :add-to-contacts                       "Додати до контактів"
   :error-incorrect-name                  "Будь ласка, виберіть інше ім'я"
   :error-incorrect-email                 "Невірна ел. пошта"

   ;;make_photo
   :image-source-title                    "Фото профілю"
   :image-source-make-photo               "Зробити знімок"
   :image-source-gallery                  "Вибрати з галереї"
   :image-source-cancel                   "Відміна"

   ;;sharing
   :sharing-copy-to-clipboard             "Скопіювати"
   :sharing-share                         "Поділитися..."
   :sharing-cancel                        "Відміна"

   ;sign-up
   :contacts-syncronized                  "Ваші контактні дані було синхронізовано"
   :confirmation-code                     (str "Дякуємо! Ми відправили вам текстове повідомлення з кодом "
                                               "підтвердження. Будь ласка, надайте цей код, щоб підтвердити свій номер телефону")
   :incorrect-code                        (str "На жаль, код невірний, будь ласка, введіть ще раз")
   :generate-passphrase                   (str "Я створю ключову фразу для вас, щоб ви могли відновити ваш "
                                               "доступ або увійти з іншого пристрою")
   :phew-here-is-your-passphrase          "*Оце так*, було складно, ось ваша ключова фраза, *запишіть її та надійно зберігайте!* Вона вам знадобиться для відновлення облікового запису."
   :here-is-your-passphrase               "Ось ваша ключова фраза, *запишіть її та надійно зберігайте!* Вона вам знадобиться для відновлення облікового запису."
   :written-down                          "Переконайтеся, що ви надійно її записали"
   :phone-number-required                 "Торкніться тут, щоб ввести ваш номер телефону і я знайду ваших друзів"
   :intro-status                          "Спілкуйтеся зі мною, щоб налаштувати свій обліковий запис і змінити налаштування!"
   :intro-message1                        "Вітаємо в Статус\nТоркніться цього повідомлення, щоб встановити пароль і почати!"
   :account-generation-message            "Почекайте секунду, маю виконати страшенно складні розрахунки, щоб створити ваш обліковий запис!"

   ;chats
   :chats                                 "Групи"
   :new-chat                              "Новий чат"
   :new-group-chat                        "Новий груповий чат"

   ;discover
   :discover                              "Відкриття"
   :none                                  "Жоден"
   :search-tags                           "Введіть теги для пошуку тут"
   :popular-tags                          "Популярні теги"
   :recent                                "Нещодавні"
   :no-statuses-discovered                "Статусів не знайдено"

   ;settings
   :settings                              "Налаштування"

   ;contacts
   :contacts                              "Контакти"
   :new-contact                           "Новий контакт"
   :show-all                              "ПОКАЗАТИ ВСІ"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Люди"
   :contacts-group-new-chat               "Почати нову розмову"
   :no-contacts                           "Поки що контактів немає"
   :show-qr                               "Показати QR"

   ;group-settings
   :remove                                "Видалити"
   :save                                  "Зберегти"
   :change-color                          "Змінити колір"
   :clear-history                         "Очистити історію"
   :delete-and-leave                      "Видалити та залишити"
   :chat-settings                         "Налаштування розмови"
   :edit                                  "Редагувати"
   :add-members                           "Додати учасників"
   :blue                                  "Голубий"
   :purple                                "Фіолетовий"
   :green                                 "Зелений"
   :red                                   "Червоний"

   ;commands
   :money-command-description             "Надіслати гроші"
   :location-command-description          "Надіслати місцезнаходження"
   :phone-command-description             "Надіслати номер телефону"
   :phone-request-text                    "Запит номеру телефону"
   :confirmation-code-command-description "Надіслати код підтвердження"
   :confirmation-code-request-text        "Запит коду підтвердження"
   :send-command-description              "Надіслати місцезнаходження"
   :request-command-description           "Надіслати запит"
   :keypair-password-command-description  ""
   :help-command-description              "Допомога"
   :request                               "Запит"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH для {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH від {{chat-name}}"

   ;new-group
   :group-chat-name                       "Назва розмови"
   :empty-group-chat-name                 "Будь ласка, введіть назву"
   :illegal-group-chat-name               "Будь ласка, виберіть іншу назву"

   ;participants
   :add-participants                      "Додайте учасників"
   :remove-participants                   "Видаліть учасників"

   ;protocol
   :received-invitation                   "отримано запрошення до розмови"
   :removed-from-chat                     "видалив вас з розмови"
   :left                                  "вийшов"
   :invited                               "запрошений"
   :removed                               "видалив"
   :You                                   "Ви"

   ;new-contact
   :add-new-contact                       "Додати новий контакт"
   :import-qr                             "Імпорт"
   :scan-qr                               "Сканувати QR"
   :name                                  "Назва"
   :whisper-identity                      "Прошепотіти справжність"
   :address-explication                   "Можливо тут повинен бути текст, який пояснює, що таке адреса і де її шукати"
   :enter-valid-address                   "Будь ласка, введіть дійсну адресу або відскануйте QR-код"
   :enter-valid-public-key                "Будь ласка, введіть дійсний відкритий ключ або відскануйте QR-код"
   :contact-already-added                 "Контакт вже додано"
   :can-not-add-yourself                  "Ви не можете додати себе"
   :unknown-address                       "Невідома адреса"


   ;login
   :connect                               "Підключитися"
   :address                               "Адреса"
   :password                              "Пароль"
   :login                                 "Логін"
   :wrong-password                        "Невірний пароль"

   ;recover
   :recover-from-passphrase               "Відновити з ключовою фразою"
   :recover-explain                       "Будь ласка, введіть ключову фразу для вашого пароля, щоб відновити доступ"
   :passphrase                            "Ключова фраза"
   :recover                               "Відновити"
   :enter-valid-passphrase                "Будь ласка, введіть ключову фразу"
   :enter-valid-password                  "Будь ласка, введіть пароль"

   ;accounts
   :recover-access                        "Відновити доступ"
   :add-account                           "Додати обліковий запис"

   ;wallet-qr-code
   :done                                  "Готово"
   :main-wallet                           "Основний гаманець"

   ;validation
   :invalid-phone                         "Невірний номер телефону"
   :amount                                "Сума"
   :not-enough-eth                        (str "Недостатньо ETH на балансі "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Підтвердити транзакцію"
                                           :other "Підтвердити {{count}} транзакції"
                                           :zero  "Немає транзакцій"}
   :status                                "Статус"
   :pending-confirmation                  "Очікує підтвердження"
   :recipient                             "Отримувач"
   :one-more-item                         "Ще один пункт"
   :fee                                   "Комісія"
   :value                                 "Значення"

   ;:webview
   :web-view-error                        "ой, помилка"

   :confirm                               "Підтвердити"
   :phone-national                        "Національне"
   :transactions-confirmed                {:one   "Транзакцію підтверджено"
                                           :other "Підтверджено {{count}} транзакцій"
                                           :zero  "Підтверджені транзакції відсутні"}
   :public-group-topic                    "Тема"
   :debug-enabled                         "Debug server запущено! Тепер ви можете додати свій DApp запустивши *status-dev-cli scan* зі свого комп’ютера"
   :new-public-group-chat                 "Приєднатися до публічного чату"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :share-qr                              "Поділитись QR"
   :feedback                              "Маєте відгук?\nСтряхніть свій телефон!"
   :twelve-words-in-correct-order         "12 слів в правильному порядку"
   :remove-from-contacts                  "Видалити з контактів"
   :delete-chat                           "Видалити чат"
   :edit-chats                            "Редагувати чати"
   :sign-in                               "Вхід"
   :create-new-account                    "Створити новий пароль"
   :sign-in-to-status                     "Увійти в Статус"
   :got-it                                "Зрозуміло"
   :move-to-internal-failure-message      "Нам треба перемістити деякі важливі файли із карти пам’яті у внутрішню пам’ять вашого пристрою. Для цього нам треба мати дозвіл. У майбутніх версіях ми не будемо використовувати карти пам’яті."
   :edit-group                            "Редагувати групу"
   :delete-group                          "Видалити групу"
   :browsing-title                        "Перегляд"
   :reorder-groups                        "Перепланувати групи"
   :browsing-cancel                       "Відмінити"
   :faucet-success                        "Розширений запит отримано"
   :choose-from-contacts                  "Виберіть з контактів"
   :new-group                             "Нова група"
   :phone-e164                            "Міжнародне 1"
   :remove-from-group                     "Видалити з групи"
   :search-contacts                       "Пошук контактів"
   :transaction                           "Операція"
   :public-group-status                   "Публічне"
   :leave-chat                            "Живий чат"
   :start-conversation                    "Почати розмову"
   :topic-format                          "Невірний формат [a-z0-9\\-]+"
   :faucet-error                          "Збій запиту"
   :phone-significant                     "Важливий"
   :search-for                            "Шукати за..."
   :phone-international                   "Міжнародне 2"
   :enter-address                         "Ввести адресу"
   :send-transaction                      "Відправити транзакцію"
   :delete-contact                        "Видалити контакт"
   :mute-notifications                    "Вимкнути сповіщення"

   :contact-s
                                          {:one   "контакт"
                                           :other "контакти"}
   :group-name                            "Назва групи"
   :next                                  "Наступне"
   :from                                  "З"
   :search-chats                          "Шукати чати"
   :in-contacts                           "В контактах"

   :type-a-message                        "Написати повідомлення..."
   :type-a-command                        "Почніть вводити команду..."
   :shake-your-phone                      "Знайшли помилку або маєте пропозиції? Просто ~стряхніть~ свій телефон!"
   :status-prompt                         "Створіть статус, щоб допомогти людям дізнатись про речі, які ви пропонуєте. Можете також використовувати #хештеги."
   :add-a-status                          "Додати статус..."
   :error                                 "Збій"
   :edit-contacts                         "Редагувати контакти"
   :more                                  "ще"
   :cancel                                "Відмінити"
   :no-statuses-found                     "Статуси не знайдено"
   :swow-qr                               "Показати QR"
   :browsing-open-in-web-browser          "Відкрити у браузері"
   :delete-group-prompt                   "Це не вплине на контакти"
   :edit-profile                          "Редагувати профіль"

   :enter-password-transactions
                                          {:one   "Підтвердіть операцію, ввівши пароль"
                                           :other "Підтвердіть операції, ввівши пароль"}
   :unsigned-transactions                 "Непідписані транзакції"
   :empty-topic                           "Тема пуста"
   :to                                    "До"
   :group-members                         "Члени групи"
   :estimated-fee                         "Прибл. комісія"
   :data                                  "Дані"})
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
   :discover                             "Відкриття"
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
   :web-view-error                        "ой, помилка"})

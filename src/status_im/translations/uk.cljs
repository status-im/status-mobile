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
   :datetime-ago                          "тому"
   :datetime-yesterday                    "вчора"
   :datetime-today                        "сьогодні"

   ;profile
   :profile                               "Профіль"
   :message                               "Повідомлення"
   :not-specified                         "Не вказано"
   :public-key                            "Відкритий ключ"
   :phone-number                          "Номер телефону"
   :add-to-contacts                       "Додати до контактів"

   ;;make_photo
   :image-source-title                    "Фото профілю"
   :image-source-make-photo               "Зробити знімок"
   :image-source-gallery                  "Вибрати з галереї"

   ;;sharing
   :sharing-copy-to-clipboard             "Скопіювати"
   :sharing-share                         "Поділитися..."
   :sharing-cancel                        "Відміна"

   ;sign-up
   :contacts-syncronized                  "Ваші контактні дані було синхронізовано"
   :confirmation-code                     (str "Дякуємо! Ми відправили вам текстове повідомлення з кодом "
                                               "підтвердження. Будь ласка, надайте цей код, щоб підтвердити свій номер телефону")
   :incorrect-code                        (str "На жаль, код невірний, будь ласка, введіть ще раз")
   :phew-here-is-your-passphrase          "*Оце так*, було складно, ось ваша ключова фраза, *запишіть її та надійно зберігайте!* Вона вам знадобиться для відновлення облікового запису."
   :here-is-your-passphrase               "Ось ваша ключова фраза, *запишіть її та надійно зберігайте!* Вона вам знадобиться для відновлення облікового запису."
   :phone-number-required                 "Торкніться тут, щоб ввести ваш номер телефону і я знайду ваших друзів"
   :intro-status                          "Спілкуйтеся зі мною, щоб налаштувати свій обліковий запис і змінити налаштування!"
   :intro-message1                        "Вітаємо в Статус\nТоркніться цього повідомлення, щоб встановити пароль і почати!"
   :account-generation-message            "Почекайте секунду, маю виконати страшенно складні розрахунки, щоб створити ваш обліковий запис!"

   ;chats
   :chats                                 "Групи"
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
   :contacts-group-new-chat               "Почати нову розмову"
   :no-contacts                           "Поки що контактів немає"
   :show-qr                               "Показати QR"

   ;group-settings
   :remove                                "Видалити"
   :save                                  "Зберегти"
   :clear-history                         "Очистити історію"
   :chat-settings                         "Налаштування розмови"
   :edit                                  "Редагувати"
   :add-members                           "Додати учасників"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "отримано запрошення до розмови"
   :removed-from-chat                     "видалив вас з розмови"
   :left                                  "вийшов"
   :invited                               "запрошений"
   :removed                               "видалив"
   :You                                   "Ви"

   ;new-contact
   :add-new-contact                       "Додати новий контакт"
   :scan-qr                               "Сканувати QR"
   :name                                  "Назва"
   :address-explication                   "Можливо тут повинен бути текст, який пояснює, що таке адреса і де її шукати"
   :enter-valid-public-key                "Будь ласка, введіть дійсний відкритий ключ або відскануйте QR-код"
   :contact-already-added                 "Контакт вже додано"
   :can-not-add-yourself                  "Ви не можете додати себе"
   :unknown-address                       "Невідома адреса"


   ;login
   :connect                               "Підключитися"
   :address                               "Адреса"
   :password                              "Пароль"
   :wrong-password                        "Невірний пароль"

   ;recover
   :passphrase                            "Ключова фраза"
   :recover                               "Відновити"

   ;accounts
   :recover-access                        "Відновити доступ"

   ;wallet-qr-code
   :done                                  "Готово"
   :main-wallet                           "Основний гаманець"

   ;validation
   :invalid-phone                         "Невірний номер телефону"
   :amount                                "Сума"
   ;transactions
   :status                                "Статус"
   :recipient                             "Отримувач"

   ;:webview
   :web-view-error                        "ой, помилка"

   :confirm                               "Підтвердити"
   :phone-national                        "Національне"
   :public-group-topic                    "Тема"
   :debug-enabled                         "Debug server запущено! Тепер ви можете додати свій DApp запустивши *status-dev-cli scan* зі свого комп’ютера"
   :new-public-group-chat                 "Приєднатися до публічного чату"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
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
   :browsing-open-in-web-browser          "Відкрити у браузері"
   :delete-group-prompt                   "Це не вплине на контакти"
   :edit-profile                          "Редагувати профіль"

   :empty-topic                           "Тема пуста"
   :to                                    "До"
   :data                                  "Дані"})
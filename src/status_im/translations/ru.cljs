(ns status-im.translations.ru)

(def translations
  {
   ;;common
   :members-title                         "Участники"
   :not-implemented                       "!не реализовано"
   :chat-name                             "Имя чата"
   :notifications-title                   "Уведомления и звуки"
   :offline                               "Оффлайн"
   :search-for                            "Искать..."
   :cancel                                "Отмена"
   :next                                  "Продолжить"
   :open                                  "Открыть"
   :description                           "Описание"
   :url                                   "URL-адрес"
   :type-a-message                        "Напишите сообщение..."
   :type-a-command                        "Начинайте вводить команду..."
   :error                                 "Ошибка"
   :unknown-status-go-error               "Неизвестная status-go ошибка"
   :node-unavailable                      "Нет запущенного ethereum узла"
   :yes                                   "Да"
   :no                                    "Нет"

   :camera-access-error                   "Чтобы предоставить необходимое разрешение для камеры, пожалуйста, перейдите в настройки системы и убедитесь, что выбрано Status > Камера."
   :photos-access-error                   "Чтобы предоставить необходимое разрешение для фото, пожалуйста, перейдите в настройки системы и убедитесь, что выбрано Status > Фотографии."

   ;;drawer
   :switch-users                          "Переключить пользователей"
   :current-network                       "Текущая сеть"

   ;;chat
   :is-typing                             "печатает"
   :and-you                               "и вы"
   :search-chat                           "Поиск по чату"
   :members                               {:one   "1 член"
                                           :other "{{count}} члена(ов)"
                                           :zero  "нет членов"}
   :members-active                        {:one   "1 член"
                                           :other "{{count}} члена(ов)"
                                           :zero  "нет членов"}
   :public-group-status                   "Публичный"
   :active-online                         "В сети"
   :active-unknown                        "Неизвестно"
   :available                             "Доступно"
   :no-messages                           "Нет сообщений"
   :suggestions-requests                  "Запросы"
   :suggestions-commands                  "Команды"
   :faucet-success                        "Запрос Faucet был получен"
   :faucet-error                          "Ошибка запроса Faucet"

   ;;sync
   :sync-in-progress                      "Синхронизация..."
   :sync-synced                           "Синхронизируется"

   ;;messages
   :status-sending                        "Отправляется..."
   :status-pending                        "В ожидании"
   :status-sent                           "Отправлено"
   :status-seen-by-everyone               "Просмотрено всеми"
   :status-seen                           "Просмотрено"
   :status-delivered                      "Доставлено"
   :status-failed                         "Ошибка"
    
   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунды"}
   :datetime-minute                       {:one   "минута"
                                           :other "минуты"}
   :datetime-hour                         {:one   "час"
                                           :other "часы"}
   :datetime-day                          {:one   "день"
                                           :other "дни"}
   :datetime-ago                          "назад"
   :datetime-yesterday                    "вчера"
   :datetime-today                        "сегодня"

   ;;profile
   :profile                               "Профиль"
   :edit-profile                          "Редактировать профиль"
   :message                               "Сообщение"
   :not-specified                         "Не указано"
   :public-key                            "Публичный ключ"
   :phone-number                          "Номер телефона"
   :update-status                         "Обновите свой статус..."
   :add-a-status                          "Добавить статус..."
   :status-prompt                         "Укажите свой статус. Использование #хэштегов поможет другим узнать о вас и рассказать о том, что у вас на уме"
   :add-to-contacts                       "Добавить в контакты"
   :in-contacts                           "В контактах"
   :remove-from-contacts                  "Удалить из контактов"
   :start-conversation                    "Начать разговор"
   :send-transaction                      "Отправить транзакцию"
   :testnet-text                          "Вы находитесь в {{testnet}} Testnet. Не отправляйте реальный ETH или SNT на свой адрес"
   :mainnet-text                          "Вы в Mainnet. Реальный ETH будет отправлен"

   ;;make_photo
   :image-source-title                    "Изображение профиля"
   :image-source-make-photo               "Сфотографировать"
   :image-source-gallery                  "Выбрать из галереи"

   ;;sharing
   :sharing-copy-to-clipboard             "Скопировать"
   :sharing-share                         "Поделиться..."
   :sharing-cancel                        "Отмена"

   :browsing-title                        "Просматривать"
   :browsing-open-in-web-browser          "Открыть в веб-браузере"
   :browsing-cancel                       "Отмена"

   ;;sign-up
   :contacts-syncronized                  "Ваши контакты синхронизированы"
   :confirmation-code                     (str "Спасибо! Мы отправили вам СМС с кодом подтверждения."
                                               "Введите этот код для подтверждения своего номера телефона")
   :incorrect-code                        (str "Извините, код неправильный, введите еще раз")
   :phew-here-is-your-passphrase          "*Уф*, это было непросто, вот ваша парольная фраза, *запишите ее и храните в надежном месте!* Она будет нужна вам для восстановления аккаунта."
   :here-is-your-passphrase               "Вот ваша парольная фраза, *запишите ее и храните в надежном месте!* Она будет нужна вам для восстановления аккаунта."
   :here-is-your-signing-phrase           "Вот ваша фраза подписи. Вы будете использовать ее для проверки достоверности своих транзакций. *Запишите ее и храните в безопасности!*"
   :phone-number-required                 "Нажмите сюда для ввода своего номера телефона и я найду ваших друзей."
   :shake-your-phone                      "Найдена ошибка или есть предложение? Просто ~потрясите~ свой телефон!"
   :intro-status                          "Пообщайтесь со мной в чате, чтобы настроить свой аккаунт и изменить свои настройки!"
   :intro-message1                        "Добро пожаловать в Status\nНажмите на это сообщение, чтобы установить пароль и начать!"
   :account-generation-message            "Секундочку, мне нужно выполнить безумно сложные расчеты для создания вашего аккаунта!"
   :move-to-internal-failure-message      "Нам нужно переместить некоторые важные файлы из внешнего хранилища во внутреннее. Для этого нам нужно ваше разрешение. В следующих версиях мы не будем использовать внешнее хранилища."
   :debug-enabled                         "Запущен сервер отладки! Теперь вы можете выполнить * status-dev-cli scan *, чтобы найти сервер с вашего компьютера в той же сети."

   ;;phone types
   :phone-e164                            "Международный 1"
   :phone-international                   "Международный 2"
   :phone-national                        "Государственный"
   :phone-significant                     "Простой"

   ;;chats
   :chats                                 "Чаты"
   :delete-chat                           "Удалить чат"
   :new-group-chat                        "Новый групповой чат"
   :new-public-group-chat                 "Присоединиться к публичному чату"
   :edit-chats                            "Редактировать чаты"
   :search-chats                          "Поиск чатов"
   :empty-topic                           "Пустая тема"
   :topic-format                          "Неверный формат [a-z0-9\\-]+"
   :public-group-topic                    "Тема"

   ;;discover
   :discover                              "Поиск"
   :none                                  "Нет"
   :search-tags                           "Введите теги для поиска сюда"
   :popular-tags                          "Популярные теги"
   :recent                                "Последние статусы"
   :no-statuses-found                     "Статусы не обнаружены"
   :chat                                  "Чат"
   :all                                   "Все"
   :public-chats                          "Публичные чаты"
   :soon                                  "Скоро"
   :public-chat-user-count                "{{count}} людей"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp профиль"
   :no-statuses-discovered                "Статусы не обнаружены"
   :no-statuses-discovered-body           "Когда кто-то создаст\nстатус, вы увидите его здесь."
   :no-hashtags-discovered-title          "#хэштеги не обнаружены"
   :no-hashtags-discovered-body           "Когда #хэштег станет\nпопулярным, вы увидите его здесь."

   ;;settings
   :settings                              "Настройки"

   ;;contacts
   :contacts                              "Контакты"
   :new-contact                           "Новый контакт"
   :delete-contact                        "Удалить контакт"
   :delete-contact-confirmation           "Этот контакт будет удален из ваших контактов"
   :remove-from-group                     "Удалить из группы"
   :edit-contacts                         "Редактирование контактов"
   :search-contacts                       "Поиск контактов"
   :contacts-group-new-chat               "Новый групповой чат"
   :choose-from-contacts                  "Выбрать из контактов"
   :no-contacts                           "Пока нет контактов"
   :show-qr                               "Показать QR"
   :enter-address                         "Ввести адрес"
   :more                                  "Больше"

   ;;group-settings
   :remove                                "Удалить"
   :save                                  "Сохранить"
   :delete                                "Удалить"
   :clear-history                         "Очистить историю"
   :mute-notifications                    "Отключить звук в оповещениях"
   :leave-chat                            "Выйти из чата"
   :chat-settings                         "Настройки чата"
   :edit                                  "Изменить"
   :add-members                           "Добавить членов"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "Новая группа"
   :reorder-groups                        "Упорядочить группы"
   :edit-group                            "Изменить группу"
   :delete-group                          "Удалить группу"
   :delete-group-confirmation             "Эта группа будет удалена из ваших групп. Это не повлияет на ваши контакты"
   :delete-group-prompt                   "Это не повлияет на ваши контакты"
   :contact-s                             {:one   "контакт"
                                           :other "контакты"}

   ;;protocol
   :received-invitation                   "получил(а) приглашение в чат"
   :removed-from-chat                     "удалил(а) вас из группового чата"
   :left                                  "осталось"
   :invited                               "приглашен(а)"
   :removed                               "удален(а)"
   :You                                   "Вы"

   ;;new-contact
   :add-new-contact                       "Добавить новый контакт"
   :scan-qr                               "Сканировать QR"
   :name                                  "Имя"
   :address-explication                   "Ваш публичный ключ используется для создания вашего адреса в Ethereum и представляет собой ряд цифр и букв. Вы можете легко найти его в своем профиле"
   :enter-valid-public-key                "Введите действительный публичный ключ или сканируйте QR-код"
   :contact-already-added                 "Контакт уже добавлен"
   :can-not-add-yourself                  "Вы не можете добавить себя"
   :unknown-address                       "Неизвестный адрес"

   ;;login
   :connect                               "Подключиться"
   :address                               "Адрес"
   :password                              "Пароль"
   :sign-in-to-status                     "Войти в Status"
   :sign-in                               "Войти"
   :wrong-password                        "Неверный пароль"
   :enter-password                        "Введите пароль"

   ;;recover
   :passphrase                            "Парольная фраза"
   :recover                               "Восстановить"
   :twelve-words-in-correct-order         "12 слов в правильном порядке"

   ;;accounts
   :recover-access                        "Восстановление доступа"
   :create-new-account                    "Создать новый аккаунт"

   ;;wallet-qr-code
   :done                                  "Готово"

   ;;validation
   :invalid-phone                         "Неправильный номер телефона"
   :amount                                "Сумма"

   ;;transactions
   :confirm                               "Подтвердить"
   :transaction                           "Транзакция"
   :unsigned-transaction-expired          "Неподписанная транзакция истекла"
   :status                                "Status"
   :recipient                             "Получатель"
   :to                                    "Кому"
   :from                                  "От"
   :data                                  "Данные"
   :got-it                                "Понятно"
   :block                                 "Блок"
   :hash                                  "Хеш"
   :gas-limit                             "Лимит Гaзa"
   :gas-price                             "Цена на Газ"
   :gas-used                              "Использованный Газ"
   :cost-fee                              "Стоимость/Комиссия"
   :nonce                                 "Nonce"
   :confirmations                         "Подтверждения"
   :confirmations-helper-text             "Пожалуйста, дождитесь как минимум 12-ти подтверждений, чтобы убедиться, что транзакция обработана безопасно"
   :copy-transaction-hash                 "Копировать транзакционный хеш"
   :open-on-etherscan                     "Открыть ссылку на Etherscan.io"

   ;;webview
   :web-view-error                        "ой, ошибка"

   ;;testfairy warning
   :testfairy-title                       "Предупреждение!"
   :testfairy-message                     "Вы используете приложение, установленное из ночной сборки. Для целей тестирования эта сборка включает в себя запись сеанса, если используется соединение Wi-Fi, так что все ваши взаимодействия с этим приложением сохраняются (как видео и логи) и могут использоваться нашей командой разработчиков для изучения возможных проблем. Сохраненные видео / логи не включают в себя ваши пароли. Запись выполняется только в том случае, если приложение установлено из ночной сборки. Ничего не записывается, если приложение установлено из PlayStore или TestFlight."
   ;; wallet
   :wallet                                "Кошелек"
   :wallets                               "Кошельки"
   :your-wallets                          "Ваши кошельки"
   :main-wallet                           "Основной Кошелек"
   :wallet-error                          "Ошибка при загрузке данных"
   :wallet-send                           "Отправить"
   :wallet-request                        "Запрос"
   :wallet-exchange                       "Обмен"
   :wallet-assets                         "Активы"
   :wallet-add-asset                      "Добавить актив"
   :wallet-total-value                    "Общая стоимость"
   :wallet-settings                       "Настройки кошелька"
   :signing-phrase-description            "Подпишите транзакцию, введя свой пароль. Убедитесь, что слова выше соответствуют вашей секретной фразе подписи"
   :wallet-insufficient-funds             "Недостаточно средств"
   :request-transaction                   "Запрос транзакции"
   :send-request                          "Послать запрос"
   :share                                 "Поделиться"
   :eth                                   "ETH"
   :currency                              "Валюта"
   :usd-currency                          "USD"
   :transactions                          "Транзакции"
   :transaction-details                   "Детали транзакции"
   :transaction-failed                    "Не удалось выполнить транзакцию"
   :transactions-sign                     "Подписать"
   :transactions-sign-all                 "Подписать все"
   :transactions-sign-transaction         "Подписать транзакцию"
   :transactions-sign-later               "Подписать позже"
   :transactions-delete                   "Удалить транзакцию"
   :transactions-delete-content           "Транзакция будет удалена из списка 'Неподписанные'"
   :transactions-history                  "История"
   :transactions-unsigned                 "Неподписанные"
   :transactions-history-empty            "В вашей истории еще нет транзакций"
   :transactions-unsigned-empty           "У вас нет никаких неподписанных транзакций"
   :transactions-filter-title             "Фильтры Истории"
   :transactions-filter-tokens            "Токены"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Выбрать все"
   :view-transaction-details              "Посмотреть детали транзакции"
   :transaction-description               "Пожалуйста, подождите как минимум 12 подтверждений, чтобы убедиться, что транзакция обработана безопасно"
   :transaction-sent                      "Транзакция отправлена"
   :transaction-moved-text                "Транзакция будет находиться в списке 'Неподписанные' в течение 5 минут"
   :transaction-moved-title               "Транзакция перемещена"
   :sign-later-title                      "Подписать транзакцию позже?"
   :sign-later-text                       "Откройте историю транзакций, чтобы подписать эту транзакцию"
   :not-applicable                        "Неприменимо для неподписанных транзакций"

   ;; Wallet Send
   :wallet-choose-recipient               "Выберите Получателя"
   :wallet-choose-from-contacts           "Выберите Из Контактов"
   :wallet-address-from-clipboard         "Использовать Адрес Из Буфера Обмена"
   :wallet-invalid-address                "Недействительный адрес: \n {{data}}"
   :wallet-browse-photos                  "Просмотреть Фотографии"
   :validation-amount-invalid-number      "Сумма недействительна"
   :validation-amount-is-too-precise      "Слишком много чисел после запятой. Наименьшая сумма, которую вы можете отправить это 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Новая сеть"
   :add-network                           "Добавить сеть"
   :add-new-network                       "Добавить новую сеть"
   :existing-networks                     "Существующие сети"
   :add-json-file                         "Добавить JSON файл"
   :paste-json-as-text                    "Вставить JSON как текст"
   :paste-json                            "Вставить JSON"
   :specify-rpc-url                       "Укажите RPC URL-адрес"
   :edit-network-config                   "Редактировать конфигурацию сети"
   :connected                             "Подключен"
   :process-json                          "Обработать JSON"
   :error-processing-json                 "Ошибка обработки JSON"
   :rpc-url                               "RPC URL-адрес"
   :remove-network                        "Удалить сеть"
   :network-settings                      "Настройки сети"
   :edit-network-warning                  "Будьте осторожны, изменение настроек сети может сделать её недоступной для вас"
   :connecting-requires-login             "Подключение к другой сети требует входа в систему"
   :close-app-title                       "Предупреждение!"
   :close-app-content                     "Приложение остановится и закроется. При повторном открытии, будет использоваться выбранная сеть"
   :close-app-button                      "Подтвердить"})

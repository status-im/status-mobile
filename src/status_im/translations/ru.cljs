(ns status-im.translations.ru)

(def translations
  {
   ;;common
   :members-title                         "Участники"
   :not-implemented                       "!не реализовано"
   :chat-name                             "Имя чата"
   :notifications-title                   "Уведомления и звуки"
   :offline                               "Оффлайн"
   :search-for                            "Поиск..."
   :cancel                                "Отмена"
   :next                                  "Продолжить"
   :open                                  "Открыть"
   :description                           "Описание"
   :url                                   "Сслылка"
   :type-a-message                        "Напечатать сообщение..."
   :type-a-command                        "Начать ввод комманды..."
   :error                                 "Ошибка"
   :unknown-status-go-error               "Неизвестный статус-произошла ошибка"
   :node-unavailable                      "Нет запущенного ethereum узла"
   :yes                                   "Да"
   :no                                    "Нет"

   :camera-access-error                   "Чтобы сделать камеру доступной, пожалуйста зайдите в настройки системы и убедитесь что разрешение Status > Камера выбрано."
   :photos-access-error                   "Чтобы сделать фото доступными, пожалуйста зайдите в настройки системы и убедитесь что разрешение Status > Фото выбрано."

   ;;drawer
   :switch-users                          "Сменить пользователей"
   :current-network                       "текущая сеть"

   ;;chat
   :is-typing                             "печатает"
   :and-you                               "и вы"
   :search-chat                           "Поиск по чату"
   :members                               {:one   "1 участник"
                                           :other "{{count}} участника(ов)"
                                           :zero  "нет участника(ов)"}
   :members-active                        {:one   "1  участник"
                                           :other "{{count}} участника(ов)"
                                           :zero  "нет участника(ов)"}
   :public-group-status                   "Публичная"
   :active-online                         "В сети"
   :active-unknown                        "Неизвестно"
   :available                             "Доступно"
   :no-messages                           "Нет сообщений"
   :suggestions-requests                  "Запросы"
   :suggestions-commands                  "Команды"
   :faucet-success                        "Был получен кран запрос"
   :faucet-error                          "Ошибка кран запроса"

   ;;sync
   :sync-in-progress                      "Синхронизация..."
   :sync-synced                           "Синхронизируется"

   ;;messages
   :status-pending                        "Ожидание"
   :status-sent                           "Отправлено"
   :status-seen-by-everyone               "Виден для всех"
   :status-seen                           "Виден"
   :status-delivered                      "Доставлено"
   :status-failed                         "Неудачно"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "секунда"
                                           :other "секунд(ы)"}
   :datetime-minute                       {:one   "минута"
                                           :other "минут(ы)"}
   :datetime-hour                         {:one   "час"
                                           :other "часа(ов)"}
   :datetime-day                          {:one   "день"
                                           :other "дня(ей)"}
   :datetime-ago                          "назад"
   :datetime-yesterday                    "вчера"
   :datetime-today                        "сегодня"

   ;;profile
   :profile                               "Профиль"
   :edit-profile                          "Изменить профиль"
   :message                               "Сообщение"
   :not-specified                         "Не указано"
   :public-key                            "Публичный ключ"
   :phone-number                          "Номер телефона"
   :update-status                         "Обновить ваш статус..."
   :add-a-status                          "Добавить статус..."
   :status-prompt                         "Поставьте ваш статус. Использование #Хэштэгов поможет остальным узнать о вас и обсудить то, что вы предлагаете"
   :add-to-contacts                       "Добавить в контакты"
   :in-contacts                           "В контактах"
   :remove-from-contacts                  "Убрать из контактов"
   :start-conversation                    "Начать общение"
   :send-transaction                      "Отправить транзакцию"
   :testnet-text                          "Вы подключены к тестовой сети {{testnet}}. Не отправляйте ETH или SNT на ваш адрес"
   :mainnet-text                          "Вы в основной сети. Будет производиться отправка настоящего ETH"

   ;;make_photo
   :image-source-title                    "Изображение профиля"
   :image-source-make-photo               "Снять"
   :image-source-gallery                  "Выбрать из галереи"

   ;;sharing
   :sharing-copy-to-clipboard             "Поделиться..."
   :sharing-cancel                        "Отмена"

   :browsing-title                        "Обзор"
   :browsing-browse                       "@обзор"
   :browsing-open-in-web-browser          "Открыть в вэб браузере"
   :browsing-cancel                       "Отмена"

   ;;sign-up
   :contacts-syncronized                  "Ваши контакты синхронизированы"
   :confirmation-code                     (str "Спасибо! Мы отправили вам СМС с кодом подтверждения."
                                               "код. Введите этот код для подтверждения вашего номера телефона")
   :incorrect-code                        (str "Извините неверный код, пожалуйста попробуйте ещё раз")
   :phew-here-is-your-passphrase          "Фух, это было непросто. Это ваша кодовая фраза, *запишите ее и храните в надежном месте!* Она вам потребуется для восстановления аккаунта."
   :here-is-your-passphrase               "Это ваша кодовая фраза,*запишите ее и храните в надежном месте!* Она вам потребуется для восстановления аккаунта."
   :here-is-your-signing-phrase           "Это ваша подпись. Вы будете её использовать для совершения транзакций. *запишите ее и храните в надежном месте!*"
   :phone-number-required                 "Нажмите сюда для подтверждения вашего номера телефона и мы начнем поиск ваших друзей"
   :shake-your-phone                      "Нашли ошибку или есть предложение? Просто ~потрясите~ телефон!"
   :intro-status                          "Пообщайтесь со мной в чате, чтобы настроить ваш аккаунт и изменить свои параметры!"
   :intro-message1                        "Добро пожаловать в Статус\nНажмите на это сообщение, чтобы установить пароль и начать!"
   :account-generation-message            "Секундочку, мне необходимо произвести безумно сложные вычисления для создания вашего аккаунта!"
   :move-to-internal-failure-message      "Нам необходимо перенести некоторые важные файлы с внешнего накопителя на внутренний. Для этого нам необходимо ваше разрешение. В следующих версиях мы не будем использовать внешний накопитель."
   :debug-enabled                         "Произведен запуск Debug сервера! Выполнив *status-dev-cli scan* на вашем компьютере, вы найдёте сервер в той-же сети."

   ;;phone types
   :phone-e164                            "Международный 1"
   :phone-international                   "Международный 2"
   :phone-national                        "Государственный"
   :phone-significant                     "Особый"

   ;;chats
   :chats                                 "Чаты"
   :delete-chat                           "Удалить чат"
   :new-group-chat                        "Новый групповой чат"
   :new-public-group-chat                 "Присоединиться к публичному чату"
   :edit-chats                            "Изменить чаты"
   :search-chats                          "Поиск по чату"
   :empty-topic                           "Пустой заголовок"
   :topic-format                          "Неверный формат [a-z0-9\\-]+"
   :public-group-topic                    "Заголовок"

   ;;discover
   :discover                              "Исследовать"
   :none                                  "Нет"
   :search-tags                           "Введите теги для поиска сюда"
   :popular-tags                          "Популярные #Хэштеги"
   :recent                                "Последние статусы"
   :no-statuses-found                     "Статусы не найдены"
   :chat                                  "Чат"
   :all                                   "Все"
   :public-chats                          "Публичные чаты"
   :soon                                  "Скоро"
   :public-chat-user-count                "{{count}} участника(ов)"
   :dapps                                 "ÐAppЫ"
   :dapp-profile                          "ÐApp профиль"
   :no-statuses-discovered                "Статусы не обнаружены"
   :no-statuses-discovered-body           "Когда ктонибудь опубликует\nстатус вы увидите его здесь."
   :no-hashtags-discovered-title          "#Хэштегов не обнаружено"
   :no-hashtags-discovered-body           "Когда #Хэштег становится\nпопулярным вы увидите его здесь."

   ;;settings
   :settings                              "Параметры"

   ;;contacts
   :contacts                              "Контакты"
   :new-contact                           "Новый контакт"
   :delete-contact                        "Удалить контакт"
   :delete-contact-confirmation           "Этот контакт будет удален из вашего списка контактов"
   :remove-from-group                     "Убрать из группы"
   :edit-contacts                         "Изменить контакты"
   :search-contacts                       "Поиск контактов"
   :contacts-group-new-chat               "Начать новый чат"
   :choose-from-contacts                  "Выбирите из контактов"
   :no-contacts                           "Контактов пока нет"
   :show-qr                               "Покажите QR-код"
   :enter-address                         "Введите адрес"
   :more                                  "ещё"

   ;;group-settings
   :remove                                "Убрать"
   :save                                  "Сохранить"
   :delete                                "Удалить"
   :clear-history                         "Очистить историю"
   :mute-notifications                    "Отключить оповещения"
   :leave-chat                            "Покинуть чат"
   :chat-settings                         "Настройки чата"
   :edit                                  "Изменить"
   :add-members                           "Добавить пользователя"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ваше текущее местоположение"
   :places-nearby                         "Места рядом"
   :search-results                        "Результаты поиска"
   :dropped-pin                           "Булавка"
   :location                              "Место"
   :open-map                              "Открыть карту"
   :sharing-copy-to-clipboard-address     "Копировать адрес"
   :sharing-copy-to-clipboard-coordinates "Скопировать координаты"

   ;;new-group
   :new-group                             "Новая группа"
   :reorder-groups                        "Упорядочить группы"
   :edit-group                            "Изменить группу"
   :delete-group                          "Удалить группу"
   :delete-group-confirmation             "Эта группа будет удалена из списка ваших групп. Это не повлияет на выши контакты"
   :delete-group-prompt                   "Это не повлияет на выши контакты"
   :contact-s                             {:one   "контакт"
                                           :other "контакты"}

   ;;protocol
   :received-invitation                   "получено приглашение в чат"
   :removed-from-chat                     "убрал(а) вас из группового чата"
   :left                                  "покинул(а)"
   :invited                               "приглашен(а)"
   :removed                               "убран(а)"
   :You                                   "Вы"

   ;;new-contact
   :add-new-contact                       "Добавить новый контакт"
   :scan-qr                               "Сканировать QR-код"
   :name                                  "Имя"
   :address-explication                   "Ваш публичный ключ используется для создания вашего адреса в сети Ethereum и является набором букв и цифр. Вы можете легко найти его у себя в профиле"
   :enter-valid-public-key                "Пожалуйста введите публичный ключ или отсканируйте QR-код"
   :contact-already-added                 "Этот контакт уже добавлен"
   :can-not-add-yourself                  "Вы не можете добавить самого себя"
   :unknown-address                       "Неизвестный адрес"

   ;;login
   :connect                               "Подключиться"
   :address                               "Адрес"
   :password                              "Пароль"
   :sign-in-to-status                     "Войти в Статус"
   :sign-in                               "Войти"
   :wrong-password                        "Неверный пароль"
   :enter-password                        "Введите пароль"

   ;;recover
   :passphrase                            "Ключевая фраза"
   :recover                               "восстановить"
   :twelve-words-in-correct-order         "12 слов в верном порядке"

   ;;accounts
   :recover-access                        "Восстановить доступ"
   :create-new-account                    "Создать новый аккаунт"

   ;;wallet-qr-code
   :done                                  "Готово"

   ;;validation
   :invalid-phone                         "Неверный номер телефона"
   :amount                                "Количестро"

   ;;transactions
   :confirm                               "Подтвердить"
   :transaction                           "Транзакция"
   :unsigned-transaction-expired          "Истек срок неподписанной транзакции"
   :status                                "Статус"
   :recipient                             "Получатель"
   :to                                    "Кому"
   :from                                  "От кого"
   :data                                  "Data"
   :got-it                                "Понятно"
   :block                                 "Блок"
   :hash                                  "Хэш"
   :gas-limit                             "Gas лимит"
   :gas-price                             "Gas цена"
   :gas-used                              "Gas использованно"
   :cost-fee                              "Стоимость/Комиссия"
   :nonce                                 "Данное время"
   :confirmations                         "Подтверждений"
   :confirmations-helper-text             "Пожалуйста дождитесь хотябы 12 подтверждений, чтобы быть уверенным в том, что ваша транзакция прошла успешно "
   :copy-transaction-hash                 "Скопировать хэш транзакции"
   :open-on-etherscan                     "Открыть в Etherscan.io"

   ;;webview
   :web-view-error                        "упс, произола ошибка"

   ;;testfairy warning
   :testfairy-title                       "Осторожно!"
   :testfairy-message                     "Вы используете nightly build версию. В целях тестирования данная версия приложения производит запись сеанса если используется wifi подключение это значит, что любое ваше взаимодействие с данным приложением (Видео и логи) могут быть использованы командой разработчиков для выявления возможных проблем. Сохраненные видео/логи не содержат паролей. Запись производится только если установлена nightly build версия приложения. Запись не производится если приложение усиановлено из PlayStore или TestFlight."

   ;; wallet
   :wallet                                "Кошелёк"
   :wallets                               "Кошельки"
   :your-wallets                          "Ваши кошельки"
   :main-wallet                           "Главный Кошелёк"
   :wallet-error                          "Ошибка загрузки данных"
   :wallet-send                           "Отправить"
   :wallet-request                        "Запросить"
   :wallet-exchange                       "Обменять"
   :wallet-assets                         "Актив"
   :wallet-add-asset                      "Доьавить актив"
   :wallet-total-value                    "Общая стоимость"
   :wallet-settings                       "Настройки кошелька"
   :signing-phrase-description            "Подписать транзакцию, посредством ввода вашего пароля. Убедитесь, что введенные выше слова, совпадают с вашей секретной фразой-подписью"
   :wallet-insufficient-funds             "Недастаточно средств"
   :request-transaction                   "Запрос транзакции"
   :send-request                          "Отправить запрос"
   :share                                 "Поделиться"
   :eth                                   "ETH"
   :currency                              "Валюта"
   :usd-currency                          "USD"
   :transactions                          "Транзакции"
   :transaction-details                   "детали транзакции"
   :transaction-failed                    "Неудачная транзакция"
   :transactions-sign                     "Подписать"
   :transactions-sign-all                 "Подписать все"
   :transactions-sign-transaction         "Подписать транзакции"
   :transactions-sign-later               "Подписать позже"
   :transactions-delete                   "Удалить транзакцию"
   :transactions-delete-content           "Транзакция будет убрана из списка 'Неподписанные' "
   :transactions-history                  "История"
   :transactions-unsigned                 "Неподписанные"
   :transactions-history-empty            "В вашей истории пока нет транзакций"
   :transactions-unsigned-empty           "У вас нет ниодной неподписанной транзакции"
   :transactions-filter-title             "Отфильтровать историю"
   :transactions-filter-tokens            "Токены"
   :transactions-filter-type              "Тип"
   :transactions-filter-select-all        "Выбрать все"
   :view-transaction-details              "Показать детали транзакции"
   :transaction-description               "Пожалуйста дождитесь хотябы 12 подтверждений, чтобы быть уверенным в том, что ваша транзакция прошла"
   :transaction-sent                      "Тпанзакция отправлена"
   :transaction-moved-text                "Транзакция останется в списке 'Неподписанные' в течении 5 минут"
   :transaction-moved-title               "Транзакция передвинута"
   :sign-later-title                      "Подписать транзакцию позже?"
   :sign-later-text                       "Проверьте историю транзакций для подписи данной транзакции"
   :not-applicable                        "Не применимо к неподписанным транзакциям"

   ;; Wallet Send
   :wallet-choose-recipient               "Выбрать получателя"
   :wallet-choose-from-contacts           "Выбрать из контактов"
   :wallet-address-from-clipboard         "Использовать адрес из буфера"
   :wallet-invalid-address                "Неверный адрес: \n {{data}}"
   :wallet-browse-photos                  "Показать фото"
   :validation-amount-invalid-number      "Указано неверное номерное значение"
   :validation-amount-is-too-precise      "Слишком маленькое значение. Меньшее значение которое вы можете отправить это 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Новая сеть"
   :add-network                           "Добавить сеть"
   :add-new-network                       "Добавить новую сеть"
   :existing-networks                     "существующие сети"
   :add-json-file                         "Добавить JSON файл"
   :paste-json-as-text                    "Вставить JSON как текст"
   :paste-json                            "Вставить JSON"
   :specify-rpc-url                       "Задать RPC ссылку"
   :edit-network-config                   "Изменить настройки сети"
   :connected                             "Подключено"
   :process-json                          "Обработка JSON"
   :error-processing-json                 "Огибка обработки JSON"
   :rpc-url                               "RPC ссылка"
   :remove-network                        "Убрать сеть"
   :network-settings                      "Настройки сети"
   :edit-network-warning                  "Бкдьте осторожны, изменение данных сети может отключить вас от нее"
   :connecting-requires-login             "Для подключение к другой сети необходимо авторизироваться"
   :close-app-title                       "Острожно!"
   :close-app-content                     "Приложение будет остановленно и закрыто. Когда вы его снова откроете, булкт использована выбранная сеть"
   :close-app-button                      "Подтвердить"})

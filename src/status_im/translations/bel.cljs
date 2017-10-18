(ns status-im.translations.bel)

(def translations
  {
   ;common
   :members-title                         "Удзельнікі"
   :not-implemented                       "!не рэалізавана"
   :chat-name                             "Імя чата"
   :notifications-title                   "Апавяшчэнні і гукі"
   :offline                               "Оффлайн"
   :cancel                                "Адмена"
   :next                                  "Працягнуць"

   ;drawer
   :invite-friends                        "Запрасіць сяброў"
   :faq                                   "ЧАВО"
   :switch-users                          "Пераключыць карыстальнікау"

   ;chat
   :is-typing                             "друкуе"
   :and-you                               "і вы"
   :search-chat                           "Пошук па чату"
   :members                               {:one   "1 член"
                                           :other "{{count}} члена(ау)"
                                           :zero  "няма членов"}
   :members-active                        {:one   "1 член, 1 актыўны"
                                           :other "{{count}} члена(ау), {{count}} актыўных"
                                           :zero  "няма членау"}
   :active-online                         "У сетцы"
   :active-unknown                        "Невядома"
   :available                             "Даступна"
   :no-messages                           "Няма паведамленняў"
   :suggestions-requests                  "Запыты"
   :suggestions-commands                  "Каманды"

   ;sync
   :sync-in-progress                      "Сінхранізацыя..."
   :sync-synced                           "Сінхранізуецца"

   ;messages
   :status-sending                        "Адпраўляецца"
   :status-pending                        "у чаканні"
   :status-sent                           "Адправіць"
   :status-seen-by-everyone               "Прагледжана усімі"
   :status-seen                           "Прагледжана"
   :status-delivered                      "Дастаўлена"
   :status-failed                         "Памылка"

   ;datetime
   :datetime-second                       {:one   "секунда"
                                           :other "секунды"}
   :datetime-minute                       {:one   "хвіліна"
                                           :other "хвіліны"}
   :datetime-hour                         {:one   "гадзіна"
                                           :other "гадзін"}
   :datetime-day                          {:one   "дзень"
                                           :other "дні"}
   :datetime-multiple                     "c" ; TODO probably wrong
   :datetime-ago                          "назад"
   :datetime-yesterday                    "учора"
   :datetime-today                        "сёння"

   ;profile
   :profile                               "Профіль"
   :report-user                           "ПАСКАРДЗIЦЦА НА КАРЫСТАЛЬНIКА"
   :message                               "Паведамленне"
   :username                              "Імя карыстальніка"
   :not-specified                         "Не пазначана"
   :public-key                            "Публічны ключ"
   :phone-number                          "Нумар тэлефона"
   :email                                 "Электронная пошта"
   :profile-no-status                     "Няма статусу"
   :add-to-contacts                       "Дадаць у кантакты"
   :error-incorrect-name                  "Выбраць іншае імя"
   :error-incorrect-email                 "Няправільная электронная пошта"

   ;;make_photo
   :image-source-title                    "Mалюнак профілю"
   :image-source-make-photo               "Сфатаграфаваць"
   :image-source-gallery                  "Выбраць з галерэі"
   :image-source-cancel                   "Адмена"

   ;;sharing
   :sharing-copy-to-clipboard             "Скапіяваць"
   :sharing-share                         "Падзяліцца..."
   :sharing-cancel                        "Адмена"

   ;sign-up
   :contacts-syncronized                  "Вашы кантакты сінхранізаваныя"
   :confirmation-code                     (str "Дзякуй! Мы адправілі вам СМС з кодам пацверджання."
                                               "Калі ласка, увядзіце гэты код для пацвярджэння свайго нумара тэлефона")
   :incorrect-code                        (str "Выбачайце, код няправільны, увядзіце яшчэ раз")
   :generate-passphrase                   (str "Я ствару для вас парольную фразу, каб вы змаглі аднавіць ваш"
                                               "доступ або ўвайсці з іншага прылады")
   :phew-here-is-your-passphrase          (str "*Уф*, эгэта было няпроста, вось ваша парольная фраза, *запішыце яе і захавайце ў надзейным месцы!* "
                                               "Яна будзе патрэбна вам для аднаўлення акаўнта.")
   :here-is-your-passphrase               (str "Вось ваша парольная фраза, *запішыце яе і захавайце ў надзейным месцы!* "
                                               "Яна будзе патрэбна вам для аднаўлення акаўнта.")
   :written-down                          "Пераканайцеся, што вы запісалі яе ў надзейным месцы"
   :phone-number-required                 "Націсніце сюды для ўводу свайго нумара тэлефона і пошуку сваіх сяброў"
   :intro-status                          "Пагутарыце са мной у чаце, каб наладзіць свой рахунак і змяніць свае налады!"
   :intro-message1                        "Сардэчна запрашаем у Статус\nНацісніце на гэтае паведамленне, каб усталяваць пароль і пачаць!"
   :account-generation-message            "Секундачку, мне трэба выканаць вар'яцка складаныя разлікі для стварэння вашага акаўнта!"

   ;chats
   :chats                                 "Чаты"
   :new-chat                              "Новы чат"
   :new-group-chat                        "Новы групавы чат"

   ;discover
   :discover                             "Пошук"
   :none                                  "Няма"
   :search-tags                           "Увядзіце тэгі для пошуку сюды"
   :popular-tags                          "Папулярныя тэгі"
   :recent                                "Апошнія"
   :no-statuses-discovered                "Статусы не выяўлены"

   ;settings
   :settings                              "Налады"

   ;contacts
   :contacts                              "Кантакты"
   :new-contact                           "Новы кантакт"
   :edit-contacts                         "Рэдагаванне кантактаў"
   :show-all                              "ПАКАЗАЦЬ УСЕ"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Людзі"
   :contacts-group-new-chat               "Пачаць новы чат"
   :no-contacts                           "Пакуль няма кантактаў"
   :show-qr                               "Паказаць QR"
   :enter-address                         "Ўвесці адрас"

   ;group-settings
   :remove                                "Выдаліць"
   :save                                  "Захаваць"
   :change-color                          "Змяніць колер"
   :clear-history                         "Ачысціць гісторыю"
   :delete-and-leave                      "Выдаліць і пакінуць"
   :chat-settings                         "Налады чата"
   :edit                                  "Змяніць"
   :add-members                           "Дадаць членаў"
   :blue                                  "Сіні"
   :purple                                "Фіялетавы"
   :green                                 "Зялёны"
   :red                                   "Чырвоны"

   ;commands
   :money-command-description             "Адправіць грошы"
   :location-command-description          "Адправіць месцазнаходжанне"
   :phone-command-description             "Адправіць нумар тэлефона"
   :phone-request-text                    "Запыт нумара тэлефона"
   :confirmation-code-command-description "Адправіць код пацверджання"
   :confirmation-code-request-text        "Запыт кода пацверджання"
   :send-command-description              "адправіць месцазнаходжанне"
   :request-command-description           "Адправіць запыт"
   :keypair-password-command-description  ""
   :help-command-description              "Дапамога"
   :request                               "Запыт"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH на адрас {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH ад {{chat-name}}"

   ;new-group
   :group-chat-name                       "Імя чата"
   :empty-group-chat-name                 "Ўвядзіце імя"
   :illegal-group-chat-name               "Трэба выбраць іншае імя"
   :new-group                             "Новая група"
   :group-name                            "Назва групы"
   :reorder-groups                        "Упарадкаваць групы"

   ;participants
   :add-participants                      "Дадаць удзельнікаў"
   :remove-participants                   "Выдаліць удзельнікаў"

   ;protocol
   :received-invitation                   "атрымаў(ла) запрашэнне ў чат"
   :removed-from-chat                     "выдаліў(ла) вас з групавога чата"
   :left                                  "засталося"
   :invited                               "запрошаны(на)"
   :removed                               "выдалены(на)"
   :You                                   "Вы"

   ;new-contact
   :add-new-contact                       "Дадаць новы кантакт"
   :import-qr                             "Iмпарт"
   :scan-qr                               "Сканаваць QR"
   :name                                  "Імя"
   :whisper-identity                      "Прыхаваная асоба"
   :address-explication                   "Можа быць, тут павінен быць нейкі тэкст, які тлумачыць адрас і тое, дзе яго шукаць"
   :enter-valid-address                   "Калі ласка, увядзіце сапраўдны адрас або скануйце QR-код"
   :enter-valid-public-key                "Калі ласка, увядзіце сапраўдны публічны ключ ці скануйце QR-код"
   :contact-already-added                 "Кантакт ўжо дададзены"
   :can-not-add-yourself                  "Вы не можаце дадаць сябе"
   :unknown-address                       "Невядомы адрас"


   ;login
   :connect                               "Падлучыцца"
   :address                               "Адрас"
   :password                              "Пароль"
   :login                                 "Уваход"
   :wrong-password                        "Няправільны пароль"

   ;recover
   :recover-from-passphrase               "Аднаўленне з дапамогай парольной фразы"
   :recover-explain                       "Увядзіце парольную фразу замест вашага пароля для аднаўлення доступу"
   :passphrase                            "Парольная фраза"
   :recover                               "Аднавіць"
   :enter-valid-passphrase                "Увядзіце парольную фразу"
   :enter-valid-password                  "Увядзіце пароль"

   ;accounts
   :recover-access                        "Аднавіць доступ"
   :add-account                           "Дадаць аккаунт"

   ;wallet-qr-code
   :done                                  "Гатова"
   :main-wallet                           "Асноўны кашалёк"

   ;validation
   :invalid-phone                         "Няправільны нумар тэлефона"
   :amount                                "Сума"
   :not-enough-eth                        (str "Не хапае ETH на балансе "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Пацвердзіць транзакцыю"
                                           :other "Пацвердзіце {{count}} транзакции(ый)"
                                           :zero  "Няма транзакций"}
   :status                                "Статус"
   :pending-confirmation                  "У чаканні пацверджання"
   :recipient                             "Атрымальнік"
   :one-more-item                         "Яшчэ адна пазіцыя"
   :fee                                   "Камісія"
   :value                                 "Сума"

   ;:webview
   :web-view-error                        "ой, памылка"})

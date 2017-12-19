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
   :datetime-ago                          "назад"
   :datetime-yesterday                    "учора"
   :datetime-today                        "сёння"

   ;profile
   :profile                               "Профіль"
   :message                               "Паведамленне"
   :not-specified                         "Не пазначана"
   :public-key                            "Публічны ключ"
   :phone-number                          "Нумар тэлефона"
   :add-to-contacts                       "Дадаць у кантакты"

   ;;make_photo
   :image-source-title                    "Mалюнак профілю"
   :image-source-make-photo               "Сфатаграфаваць"
   :image-source-gallery                  "Выбраць з галерэі"

   ;;sharing
   :sharing-copy-to-clipboard             "Скапіяваць"
   :sharing-share                         "Падзяліцца..."
   :sharing-cancel                        "Адмена"

   ;sign-up
   :contacts-syncronized                  "Вашы кантакты сінхранізаваныя"
   :confirmation-code                     (str "Дзякуй! Мы адправілі вам СМС з кодам пацверджання."
                                               "Калі ласка, увядзіце гэты код для пацвярджэння свайго нумара тэлефона")
   :incorrect-code                        (str "Выбачайце, код няправільны, увядзіце яшчэ раз")
   :phew-here-is-your-passphrase          (str "*Уф*, эгэта было няпроста, вось ваша парольная фраза, *запішыце яе і захавайце ў надзейным месцы!* "
                                               "Яна будзе патрэбна вам для аднаўлення акаўнта.")
   :here-is-your-passphrase               (str "Вось ваша парольная фраза, *запішыце яе і захавайце ў надзейным месцы!* "
                                               "Яна будзе патрэбна вам для аднаўлення акаўнта.")
   :phone-number-required                 "Націсніце сюды для ўводу свайго нумара тэлефона і пошуку сваіх сяброў"
   :intro-status                          "Пагутарыце са мной у чаце, каб наладзіць свой рахунак і змяніць свае налады!"
   :intro-message1                        "Сардэчна запрашаем у Статус\nНацісніце на гэтае паведамленне, каб усталяваць пароль і пачаць!"
   :account-generation-message            "Секундачку, мне трэба выканаць вар'яцка складаныя разлікі для стварэння вашага акаўнта!"

   ;chats
   :chats                                 "Чаты"
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
   :contacts-group-new-chat               "Пачаць новы чат"
   :no-contacts                           "Пакуль няма кантактаў"
   :show-qr                               "Паказаць QR"
   :enter-address                         "Ўвесці адрас"

   ;group-settings
   :remove                                "Выдаліць"
   :save                                  "Захаваць"
   :clear-history                         "Ачысціць гісторыю"
   :chat-settings                         "Налады чата"
   :edit                                  "Змяніць"
   :add-members                           "Дадаць членаў"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Новая група"
   :reorder-groups                        "Упарадкаваць групы"

   ;participants

   ;protocol
   :received-invitation                   "атрымаў(ла) запрашэнне ў чат"
   :removed-from-chat                     "выдаліў(ла) вас з групавога чата"
   :left                                  "засталося"
   :invited                               "запрошаны(на)"
   :removed                               "выдалены(на)"
   :You                                   "Вы"

   ;new-contact
   :add-new-contact                       "Дадаць новы кантакт"
   :scan-qr                               "Сканаваць QR"
   :name                                  "Імя"
   :address-explication                   "Можа быць, тут павінен быць нейкі тэкст, які тлумачыць адрас і тое, дзе яго шукаць"
   :enter-valid-public-key                "Калі ласка, увядзіце сапраўдны публічны ключ ці скануйце QR-код"
   :contact-already-added                 "Кантакт ўжо дададзены"
   :can-not-add-yourself                  "Вы не можаце дадаць сябе"
   :unknown-address                       "Невядомы адрас"


   ;login
   :connect                               "Падлучыцца"
   :address                               "Адрас"
   :password                              "Пароль"
   :wrong-password                        "Няправільны пароль"

   ;recover
   :passphrase                            "Парольная фраза"
   :recover                               "Аднавіць"

   ;accounts
   :recover-access                        "Аднавіць доступ"

   ;wallet-qr-code
   :done                                  "Гатова"
   :main-wallet                           "Асноўны кашалёк"

   ;validation
   :invalid-phone                         "Няправільны нумар тэлефона"
   :amount                                "Сума"
   ;transactions
   :status                                "Статус"
   :recipient                             "Атрымальнік"

   ;:webview
   :web-view-error                        "ой, памылка"})

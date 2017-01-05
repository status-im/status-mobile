(ns status-im.translations.ur)

(def translations
  {
   ;common
   :members-title                         "ممبران"
   :not-implemented                       "نافذ نہیں ہوا!"
   :chat-name                             "چیٹ کا نام"
   :notifications-title                   "نوٹیفیکیشن اور آوازیں"
   :offline                               "آف لائن"

   ;drawer
   :invite-friends                        "دوستوں کو مدعو کریں"
   :faq                                   "FAQ"
   :switch-users                          "صارف تبدیل کریں"

   ;chat
   :is-typing                             "اندراج کر رہا ہے"
   :and-you                               "اور آپ"
   :search-chat                           "چیٹ تلاش کریں"
   :members                               {:one   "1 ممبر"
                                           :other "{{count}} ممبران"
                                           :zero  "کوئی ممبر نہیں"}
   :members-active                        {:one   "1 ممبر, 1 فعال"
                                           :other "{{count}} ممبران, {count}} فعال"
                                           :zero  "کوئی ممبر نہیں"}
   :active-online                         "آن لائن"
   :active-unknown                        "نامعلوم"
   :available                             "دستیاب"
   :no-messages                           "کوئی پیغام نہیں"
   :suggestions-requests                  "درخواستیں"
   :suggestions-commands                  "کمانڈ"

   ;sync
   :sync-in-progress                      "سنکرونائز ہو رہا ہے"
   :sync-synced                           "دوران سنکرونائز"

   ;messages
   :status-sending                        "بھیجا جا رہا ہے"
   :status-pending                        "زیر غور"
   :status-sent                           "بھیج دیا گیا"
   :status-seen-by-everyone               "ہر ایک نے دیکھ لیا ہے"
   :status-seen                           "دیکھ لیا گیا"
   :status-delivered                      "پہنچا دیا گیا"
   :status-failed                         "ناکام ہو گیا"

   ;datetime
   :datetime-second                       {:one   "سیکنڈ"
                                           :other "سیکنڈ"}
   :datetime-minute                       {:one   "منٹ"
                                           :other "منٹ"}
   :datetime-hour                         {:one   "گھنٹہ"
                                           :other "گھنٹے"}
   :datetime-day                          {:one   "دن"
                                           :other "دن"}
   :datetime-multiple                     "s"
   :datetime-ago                          "قبل"
   :datetime-yesterday                    "کل"
   :datetime-today                        "آج"

   ;profile
   :profile                               "پروفائل"
   :report-user                           "صارف کی رپورٹ کریں"
   :message                               "پیغام"
   :username                              "صارف کا نام"
   :not-specified                         "واضح نہیں کیا گیا"
   :public-key                            "Public Key"
   :phone-number                          "فون نمبر"
   :email                                 "ای میل"
   :profile-no-status                     "کوئی سٹیٹس نہیں"
   :add-to-contacts                       "اپنے رابطوں میں درج کریں"
   :error-incorrect-name                  "برائے مہربانی کوئی اور نام چنیں"
   :error-incorrect-email                 "غلط ای میل"

   ;;make_photo
   :image-source-title                    "پروفائل تصویر"
   :image-source-make-photo               "کھینچیں"
   :image-source-gallery                  "گیلری سے چنیں"
   :image-source-cancel                   "کینسل"

   ;sign-up
   :contacts-syncronized                  "آپ کے کانٹیکٹس سنکرونائز ہو گئے ہیں"
   :confirmation-code                     (str "شکریہ! ہم نے آپ کو تصدیق کا پیغام بھیج دیا ہے "
                                               "کوڈ کے ساتھ۔ برائے مہربانی اپنا نمبر کنفرم کرنے کے لیے وہ کوڈ فراہم کریں۔")
   :incorrect-code                        (str "معذرت، کوڈ غلط تھا، برائے مہربانی دوبارہ درج کریں")
   :generate-passphrase                   (str "میں آپ کے لیے passphrase تشکیل دوں گا تا کہ آپ "
                                               "کسی دوسری ڈیوائس سے لاگ ان یا رسائی حاصل کر سکیں")
   :phew-here-is-your-passphrase          "*اف* یہ مشکل تھا, یہ آپ کی passphrase ہے۔, *اسے لکھ لیں اور اپنے پاس محفوظ کر لیں۔* آپ کو اپنا اکاؤنٹ بحال کرنے کے لیے اس کی ضرورت ہو گی۔"
   :here-is-your-passphrase               "یہ آپ کی passphrase ہے۔, *ا اکاؤنٹ بحال کرنے کے لیے اس کی ضرورت * ہے۔, *اسے لکھ لیں اور اپنے پاس محفوظ کر لیں۔* آپ کو ا"
   :written-down                          "یہ یقینی بنا لیں کہ آپ نے اسے بحفاظت لکھ لیا ہے۔"
   :phone-number-required                 "اپنا فون نمبر درج کرنے کے لیے یہاں ٹیپ کریں اور میں آپ کے دوستوں کو تلاش کروں گا"
   :intro-status                          "اپنا اکاؤنٹ سیٹ اپ کرنے اور سیٹنگز تبدیل کرنے کے لیے مجھ سے چیٹ کریں!"
   :intro-message1                        "سٹیٹس میں خوش آمدید اور اپنا پاسورڈ سیٹ کرنے کے لیے اس کو ٹیپ کریں اور شروعات کریں۔"
   :account-generation-message            "مجھے ایک سیکنڈ دیں، اپ کا اکاؤنٹ تشکیل دینے کے لیے مجھے کچھ ریاضی سے کام کرنا ہے۔!"

   ;chats
   :chats                                 "چیٹ"
   :new-chat                              "نئی چیٹ"
   :new-group-chat                        "نئی گروپ چیٹ"

   ;discover
   :discover                             "دریافت"
   :none                                  "کوئی نہیں"
   :search-tags                           "اپنا تلاش کا ٹیگ یہاں درج کریں"
   :popular-tags                          "مشہور ٹیگ"
   :recent                                "حالیہ"
   :no-statuses-discovered                "کوئی سٹیٹس نہیں پایا گیا"

   ;settings
   :settings                              "سیٹنگز"

   ;contacts
   :contacts                              "رابطے"
   :new-contact                           "نئے رابطے"
   :show-all                              "سب دکھائیں"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "لوگ"
   :contacts-group-new-chat               "نئی چیٹ شروع کریں"
   :no-contacts                           "ابھی یہاں کوئی کنٹیکٹ نہیں"
   :show-qr                               "دکھائیں QR"

   ;group-settings
   :remove                                "نکالیں"
   :save                                  "محفوظ کریں"
   :change-color                          "رنگ تبدیل کریں"
   :clear-history                         "تاریخ مٹا دیں"
   :delete-and-leave                      "حذف کریں اور چھوڑ دیں"
   :chat-settings                         "چیٹ کی سیٹنگز"
   :edit                                  "تصحیح"
   :add-members                           "ممبران شامل کریں"
   :blue                                  "نیلا"
   :purple                                "جامنی"
   :green                                 "سبز"
   :red                                   "سرخ"

   ;commands
   :money-command-description             "رقم بھیجیں"
   :location-command-description          "مقام بھیجیں"
   :phone-command-description             "فون نمبر بھیجیں"
   :phone-request-text                    "فون نمبر کی درخواست کریں"
   :confirmation-code-command-description "برائے مہربانی یقین دہانی کا کوڈ بھیجیں"
   :confirmation-code-request-text        "تصدیقی کوڈ کی درخواست"
   :send-command-description              "مقام بھیجیں"
   :request-command-description           "درخواست بھیجیں"
   :keypair-password-command-description  ""
   :help-command-description              "مدد"
   :request                               "درخواست"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH کرنے {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH سے {{chat-name}}"
   :command-text-location                 "مقام: {{address}}"
   :command-text-browse                   "ویب پیج براؤز کریں: {{webpage}}"
   :command-text-send                     "بیوپار: {{amount}} ETH"
   :command-text-help                     "مدد"

   ;new-group
   :group-chat-name                       "چیٹ کا نام"
   :empty-group-chat-name                 "برائے مہربانی کو ایک نام درج کریں"
   :illegal-group-chat-name               "برائے مہربانی کوئی اور نام درج کریں"

   ;participants
   :add-participants                      "ساتھیوں کو شامل کریں"
   :remove-participants                   "ساتھیوں کو نکالیں"

   ;protocol
   :received-invitation                   "چیٹ کا دعوت نامہ وصول ہوا ہے"
   :removed-from-chat                     "گروپ سے نکال دیا ہے"
   :left                                  "چھوڑ دیا"
   :invited                               "مدعو"
   :removed                               "نکال دیا گیا"
   :You                                   "آپ"

   ;new-contact
   :add-new-contact                       "نیا کانٹیکٹ شامل کریں"
   :import-qr                             "امپورٹ"
   :scan-qr                               "سکین کریں QR"
   :name                                  "نام"
   :whisper-identity                      "شناخت بتائیں"
   :address-explication                   "شائد یہاں آپ کو کچھ لکھنا چاہیے دیکھنے کے لیے کہ پتہ کیا ہے اور اسے کہاں ہونا چاہیے"
   :enter-valid-address                   "برائے مہربانی  درست  پتہ یا QR سکین کریں"
   :contact-already-added                 "کانٹیکٹ پہلے سے شامل ہے"
   :can-not-add-yourself                  "آپ خود کو شامل نہیں کر سکتے"
   :unknown-address                       "نامعلوم پتہ"


   ;login
   :connect                               "ملیں"
   :address                               "پتہ"
   :password                              "پاسورڈ"
   :login                                 "لاگ ان"
   :wrong-password                        "غلط پاسورڈ"

   ;recover
   :recover-from-passphrase               "passphrase کے ذریعے بحال کریں"
   :recover-explain                       "رسائی حاصل کرنے کے لیے اپنے پاسورڈ کا passphrase درج کریں"
   :passphrase                            "Passphrase"
   :recover                               "بحال کریں"
   :enter-valid-passphrase                "برائے مہربانی passphrase درج کریں"
   :enter-valid-password                  "برائے مہربانی پاسورڈ لکھیں"

   ;accounts
   :recover-access                        "رسائی بحال کریں"
   :add-account                           "اکاؤنٹ شامل کریں"

   ;wallet-qr-code
   :done                                  "ہو گیا"
   :main-wallet                           "مرکزی والٹ"

   ;validation
   :invalid-phone                         "غلط فون نمبر"
   :amount                                "رقم"
   :not-enough-eth                        (str "بیلنس میں کافی ETH موجود نہیں۔ "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "بیوپار کی تصدیق کریں"
                                           :other "تصدیق {{count}} بیوپار"
                                           :zero  "کوئی بیوپار نہیں ہوا"}
   :status                                "سٹیٹس"
   :pending-confirmation                  "تصدیق کی ضرورت ہے"
   :recipient                             "وصول کنندہ"
   :one-more-item                         "ایک اور چیز"
   :fee                                   "فیس"
   :value                                 "رقم"

   ;:webview
   :web-view-error                        "اوہ، غلطی ہو گئی"})

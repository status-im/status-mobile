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
   :datetime-ago                          "قبل"
   :datetime-yesterday                    "کل"
   :datetime-today                        "آج"

   ;profile
   :profile                               "پروفائل"
   :message                               "پیغام"
   :not-specified                         "واضح نہیں کیا گیا"
   :public-key                            "Public Key"
   :phone-number                          "فون نمبر"
   :add-to-contacts                       "اپنے رابطوں میں درج کریں"

   ;;make_photo
   :image-source-title                    "پروفائل تصویر"
   :image-source-make-photo               "کھینچیں"
   :image-source-gallery                  "گیلری سے چنیں"

   ;sign-up
   :contacts-syncronized                  "آپ کے کانٹیکٹس سنکرونائز ہو گئے ہیں"
   :confirmation-code                     (str "شکریہ! ہم نے آپ کو تصدیق کا پیغام بھیج دیا ہے "
                                               "کوڈ کے ساتھ۔ برائے مہربانی اپنا نمبر کنفرم کرنے کے لیے وہ کوڈ فراہم کریں۔")
   :incorrect-code                        (str "معذرت، کوڈ غلط تھا، برائے مہربانی دوبارہ درج کریں")
   :phew-here-is-your-passphrase          "*اف* یہ مشکل تھا, یہ آپ کی passphrase ہے۔, *اسے لکھ لیں اور اپنے پاس محفوظ کر لیں۔* آپ کو اپنا اکاؤنٹ بحال کرنے کے لیے اس کی ضرورت ہو گی۔"
   :here-is-your-passphrase               "یہ آپ کی passphrase ہے۔, *ا اکاؤنٹ بحال کرنے کے لیے اس کی ضرورت * ہے۔, *اسے لکھ لیں اور اپنے پاس محفوظ کر لیں۔* آپ کو ا"
   :phone-number-required                 "اپنا فون نمبر درج کرنے کے لیے یہاں ٹیپ کریں اور میں آپ کے دوستوں کو تلاش کروں گا"
   :intro-status                          "اپنا اکاؤنٹ سیٹ اپ کرنے اور سیٹنگز تبدیل کرنے کے لیے مجھ سے چیٹ کریں!"
   :intro-message1                        "سٹیٹس میں خوش آمدید اور اپنا پاسورڈ سیٹ کرنے کے لیے اس کو ٹیپ کریں اور شروعات کریں۔"
   :account-generation-message            "مجھے ایک سیکنڈ دیں، اپ کا اکاؤنٹ تشکیل دینے کے لیے مجھے کچھ ریاضی سے کام کرنا ہے۔!"

   ;chats
   :chats                                 "چیٹ"
   :new-group-chat                        "نئی گروپ چیٹ"

   ;discover
   :discover                              "دریافت"
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
   :contacts-group-new-chat               "نئی چیٹ شروع کریں"
   :no-contacts                           "ابھی یہاں کوئی کنٹیکٹ نہیں"
   :show-qr                               "دکھائیں QR"

   ;group-settings
   :remove                                "نکالیں"
   :save                                  "محفوظ کریں"
   :clear-history                         "تاریخ مٹا دیں"
   :chat-settings                         "چیٹ کی سیٹنگز"
   :edit                                  "تصحیح"
   :add-members                           "ممبران شامل کریں"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "چیٹ کا دعوت نامہ وصول ہوا ہے"
   :removed-from-chat                     "گروپ سے نکال دیا ہے"
   :left                                  "چھوڑ دیا"
   :invited                               "مدعو"
   :removed                               "نکال دیا گیا"
   :You                                   "آپ"

   ;new-contact
   :add-new-contact                       "نیا کانٹیکٹ شامل کریں"
   :scan-qr                               "سکین کریں QR"
   :name                                  "نام"
   :address-explication                   "شائد یہاں آپ کو کچھ لکھنا چاہیے دیکھنے کے لیے کہ پتہ کیا ہے اور اسے کہاں ہونا چاہیے"
   :contact-already-added                 "کانٹیکٹ پہلے سے شامل ہے"
   :can-not-add-yourself                  "آپ خود کو شامل نہیں کر سکتے"
   :unknown-address                       "نامعلوم پتہ"


   ;login
   :connect                               "ملیں"
   :address                               "پتہ"
   :password                              "پاسورڈ"
   :wrong-password                        "غلط پاسورڈ"

   ;recover
   :passphrase                            "Passphrase"
   :recover                               "بحال کریں"

   ;accounts
   :recover-access                        "رسائی بحال کریں"

   ;wallet-qr-code
   :done                                  "ہو گیا"
   :main-wallet                           "مرکزی والٹ"

   ;validation
   :invalid-phone                         "غلط فون نمبر"
   :amount                                "رقم"
   ;transactions
   :status                                "سٹیٹس"
   :recipient                             "وصول کنندہ"

   ;:webview
   :web-view-error                        "اوہ، غلطی ہو گئی"

   :confirm                               "توثیق کریں"
   :phone-national                        "قومی"
   :public-group-topic                    "موضوع"
   :debug-enabled                         "ڈی بگ سرور کو لانچ کیا گیا ہے! آپ کا آئی پتہ ہے۔ اب آپ اپنے کمپیوٹر سے *status-dev-cli scan* کو چلاتے ہوئے اپنا DApp شامل کر سکتے ہیں"
   :new-public-group-chat                 "عوامی چیٹ میں شامل ہوں"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "منسوخ کریں"
   :twelve-words-in-correct-order         "درست ترتیب میں 12 الفاظ"
   :remove-from-contacts                  "روابط سے خارج کریں"
   :delete-chat                           "چیٹ کو حذف کریں"
   :edit-chats                            "چیٹس میں ترمیم کریں"
   :sign-in                               "سائن اِن کریں"
   :create-new-account                    "نیا اکاؤنٹ تخلیق کریں"
   :sign-in-to-status                     "اسٹیٹس میں سائن اِن کریں"
   :got-it                                "سمجھ گیا"
   :move-to-internal-failure-message      "ہمیں خارجی اسٹوریج سے داخلی اسٹوریج کی طرف کچھ فائلیں منتقل کرنی ہوگی۔ ایسا کرنے کے لیے، ہمیں آپ کی اجازت درکار ہے۔ ہم مستقبل کے ورژن میں بیرونی اسٹوریج کا استعمال نہیں کریں گے۔"
   :edit-group                            "گروپ میں ترمیم کریں"
   :delete-group                          "گروپ کو حذف کریں"
   :browsing-title                        "براؤز"
   :reorder-groups                        "ریکارڈر کے گروپ"
   :browsing-cancel                       "منسوخ کریں"
   :faucet-success                        "فوسیٹ درخواست موصول ہوئی ہے"
   :choose-from-contacts                  "روابط سے منتخب کریں"
   :new-group                             "نیا گروپ"
   :phone-e164                            "بین الاقوامی 1"
   :remove-from-group                     "گروپ سے خارج کریں"
   :search-contacts                       "روابط کی تلاش کریں"
   :transaction                           "لین دین"
   :public-group-status                   "عوامی"
   :leave-chat                            "چیٹ سے باہر نکلیں"
   :start-conversation                    "بات چیت شروع کریں"
   :topic-format                          "غلط فارمیٹ [a-z0-9\\-]+"
   :enter-valid-public-key                "برائے مہربانی ایک درست عوامی کلید داخل کریں یا کوئی کیو آر کوڈ اسکین کریں"
   :faucet-error                          "فاسیٹ درخواست کی خرابی"
   :phone-significant                     "اہم"
   :search-for                            ".... کو تلاش کریں"
   :sharing-copy-to-clipboard             "کلپ بورڈ پر نقل کیا گیا"
   :phone-international                   "بین الاقوامی 2"
   :enter-address                         "پتہ داخل کریں"
   :send-transaction                      "لین دین بھیجیں"
   :delete-contact                        "رابطہ حذف کریں"
   :mute-notifications                    "اطلاع نامے خاموش کریں"

   :contact-s
                                          {:one   "رابطہ"
                                           :other "روابط"}
   :next                                  "اگلا"
   :from                                  "سے"
   :search-chats                          "چیٹس تلاش کریں"
   :in-contacts                           "روابط میں"

   :sharing-share                         "اشتراک کریں....."
   :type-a-message                        "کوئی پیغام ٹائپ کریں...."
   :type-a-command                        "کمانڈ ٹائپ کرنا شروع کریں..."
   :shake-your-phone                      "کسی بگ کا پتہ لگا یا ذہن میں کوئی مشورہ ہے؟ بس اپنا فون ~ہلائیں~!"
   :status-prompt                         "آپ کے ذریعہ پیشکش کی جانے والی چیزوں کے متعلق لوگوں کو جاننے میں مدد دینے کے لیے کوئی اسٹیٹس تخلیق کریں۔ آپ #ہیشٹیگ بھی استعمال کرسکتے ہیں۔"
   :add-a-status                          "کوئی اسٹیٹس شامل کریں..."
   :error                                 "خرابی"
   :edit-contacts                         "روابط کو حذف کریں"
   :more                                  "مزید"
   :cancel                                "منسوخ کریں"
   :no-statuses-found                     "کسی اسٹیٹس کا پتہ نہیں چلا"
   :browsing-open-in-web-browser          "ویب براؤزر میں کھولیں"
   :delete-group-prompt                   "اس سے روابط متاثر نہیں ہونگے"
   :edit-profile                          "پروفائل میں ترمیم کریں"

   :empty-topic                           "خالی موضوع"
   :to                                    "بجانب"
   :data                                  "ڈیٹا"})

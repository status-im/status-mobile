(ns status-im.translations.ar)

(def translations
  {
   ;common
   :members-title                         "الأعضاء"
   :not-implemented                       "!غير مُطَبّق"
   :chat-name                             "اسم الدردشة"
   :notifications-title                   "الإخطارات والأصوات"
   :offline                               "غير متصل"

   ;drawer
   :invite-friends                        "دعوة الأصدقاء"
   :faq                                   "الأسئلة الشائعة"
   :switch-users                          "التبديل بين المستخدمين"

   ;chat
   :is-typing                             "يكتب"
   :and-you                               "وأنت أيضاً"
   :search-chat                           "البحث في الدردشة"
   :members                               {:one   "1 عضو"
                                           :other "{{count}} أعضاء"
                                           :zero  "لا يوجد أعضاء"}
   :members-active                        {:one   "1 عضو، 1 نشط"
                                           :other "{{count}} أعضاء، {{count}} نشط"
                                           :zero  "لا يوجد أعضاء"}
   :active-online                         "متصل بالانترنت"
   :active-unknown                        "غير معروف"
   :available                             "متاح"
   :no-messages                           "لا توجد رسائل"
   :suggestions-requests                  "الطلبات"
   :suggestions-commands                  "الأوامر"

   ;sync
   :sync-in-progress                      "قيد المزامنة..."
   :sync-synced                           "بالتزامن"

   ;messages
   :status-sending                        "إرسال"
   :status-pending                        "ريثما"
   :status-sent                           "تم الإرسال"
   :status-seen-by-everyone               "شوهد من قبل الجميع"
   :status-seen                           "شوهد"
   :status-delivered                      "تم الاستلام"
   :status-failed                         "فشل"

   ;datetime
   :datetime-second                       {:one   "ثانية"
                                           :other "ثوان"}
   :datetime-minute                       {:one   "دقيقة"
                                           :other "دقائق"}
   :datetime-hour                         {:one   "ساعة"
                                           :other "ساعات"}
   :datetime-day                          {:one   "يوم"
                                           :other "أيام"}
   :datetime-multiple                     "ث" ; TODO
   :datetime-ago                          "منذ"
   :datetime-yesterday                    "الأمس"
   :datetime-today                        "اليوم"

   ;profile
   :profile                               "الملف الشخصي"
   :report-user                           "الإبلاغ عن مستخدم"
   :message                               "الرسالة"
   :username                              "اسم المستخدم"
   :not-specified                         "غير محدد"
   :public-key                            "المفتاح العمومي"
   :phone-number                          "رقم الهاتف"
   :email                                 "البريد الإلكتروني"
   :profile-no-status                     "لا توجد حالة"
   :add-to-contacts                       "أضف إلى جهات الاتصال"
   :error-incorrect-name                  "الرجاء اختيار اسم آخر"
   :error-incorrect-email                 "بريد إلكتروني غير صحيح"

   ;;make_photo
   :image-source-title                    "الصورة الشخصية"
   :image-source-make-photo               "التقاط صورة"
   :image-source-gallery                  "الاختيار من معرض الصور"
   :image-source-cancel                   "إلغاء"

   ;sign-up
   :contacts-syncronized                  "تمت مزامنة جهات الاتصال الخاصة بك"
   :confirmation-code                     (str "شكراً! لقد أرسلنا لك رسالة نصية تتضمن رمز تأكيد"
                                               "يرجى إدخال هذا الرمز لتأكيد رقم هاتفك")
   :incorrect-code                        (str "عذراً الرمز غير صحيح، يرجى إدخاله مرة أخرى")
   :generate-passphrase                   (str "سوف أقوم بإنشاء عبارة مرور لك حتى تتمكن من "
                                               "الوصول أو الدخول ثانية من جهاز آخر")
   :phew-here-is-your-passphrase          "*أف* لقد كان أمراً شاقاً، إليك عبارة المرور الخاصة بك، * قم بتسجيلها واحتفظ بها في مكان آمن!* سوف تحتاج إليها لاسترداد حسابك."
   :here-is-your-passphrase               "إليك عبارة المرور الخاصة بك، * قم بتسجيلها واحتفظ بها في مكان آمن! *سوف تحتاج إليها لاسترداد حسابك"
   :written-down                          "تأكد بأنك قد دونتها بشكل آمن"
   :phone-number-required                 "اضغط هنا لإدخال رقم الهاتف الخاص بك وسوف تعثر على أصدقائك"
   :intro-status                          "دردش معي لتثبيت حسابك وقم بتغيير الإعدادات الخاصة بك!"
   :intro-message1                        "مرحبا بك في Status \n اضغط على هذه الرسالة لتعيين كلمة المرور الخاصة بك وابدأ!"
   :account-generation-message            "امنحني ثانية واحدة، سوف أحتاج إلى إجراء بعض الحسابات الرياضية المجنونة لإنتاج الحساب الخاص بك!"

   ;chats
   :chats                                 "الدردشات"
   :new-chat                              "دردشة جديدة"
   :new-group-chat                        "مجموعة دردشة جديدة"

   ;discover
   :discover                             "اكتشاف"
   :none                                  "لا شيء"
   :search-tags                           "اكتب بيانات بحثك هنا"
   :popular-tags                          "العلامات المشهورة"
   :recent                                "حديثة"
   :no-statuses-discovered                "لم يتم الكشف عن حالات"

   ;settings
   :settings                              "الإعدادات"

   ;contacts
   :contacts                              "جهات الاتصال"
   :new-contact                           "جهة اتصال جديدة"
   :show-all                              "عرض الكل"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "الناس"
   :contacts-group-new-chat               "ابدأ دردشة جديدة"
   :no-contacts                           "لا توجد جهات اتصال بعد"
   :show-qr                               "عرض شفرة التعريف"

   ;group-settings
   :remove                                "إزالة"
   :save                                  "حفظ"
   :change-color                          "تغيير اللون"
   :clear-history                         "حذف التاريخ"
   :delete-and-leave                      "الحذف والمغادرة"
   :chat-settings                         "إعدادات الدردشة"
   :edit                                  "تحرير"
   :add-members                           "إضافة أعضاء"
   :blue                                  "أزرق"
   :purple                                "أرجواني"
   :green                                 "أخضر"
   :red                                   "أحمر"

   ;commands
   :money-command-description             "إرسال الأموال"
   :location-command-description          "إرسال الموقع"
   :phone-command-description             "إرسال رقم الهاتف"
   :phone-request-text                    "طلب رقم الهاتف"
   :confirmation-code-command-description "إرسال رمز التأكيد"
   :confirmation-code-request-text        "طلب رمز التأكيد"
   :send-command-description              "إرسال الموقع"
   :request-command-description           "إرسال طلب"
   :keypair-password-command-description  ""
   :help-command-description              "المساعدة"
   :request                               "طلب"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH إلى {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH من {{chat-name}}"
   :command-text-location                 "الموقع {{address}}"
   :command-text-browse                   "تصفح صفحة الويب {{webpage}}"
   :command-text-send                     "المعاملة: {{amount}} ETH"
   :command-text-help                     "المساعدة"

   ;new-group
   :group-chat-name                       "اسم الدردشة"
   :empty-group-chat-name                 "الرجاء إدخال اسم"
   :illegal-group-chat-name               "الرجاء اختيار اسم آخر"

   ;participants
   :add-participants                      "إضافة مشاركين"
   :remove-participants                   "إزالة مشاركين"

   ;protocol
   :received-invitation                   "تسلم دعوة الدردشة"
   :removed-from-chat                     "قام بإزالتك من مجموعة الدردشة"
   :left                                  "غادر"
   :invited                               "مَدْعُوّ"
   :removed                               "مُسْتَبْعَد"
   :You                                   "أنت"

   ;new-contact
   :add-new-contact                       "إضافة جهة اتصال جديدة"
   :import-qr                             "جلب"
   :scan-qr                               "مسح شفرة التعريف"
   :name                                  "اسم"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "ربما يتعين أن تتوافر هنا بعض النصوص التي تشرح ما هو العنوان وأين تبحث عنه"
   :enter-valid-address                   "الرجاء إدخال عنوان صحيح أو قم بمسح شفرة التعريف"
   :contact-already-added                 "تمت إضافة جهة الاتصال"
   :can-not-add-yourself                  "لا يمكنك إضافة نفسك"
   :unknown-address                       "عنوان غير معروف"


   ;login
   :connect                               "اتصال"
   :address                               "عنوان"
   :password                              "كلمة مرور"
   :login                                 "تسجيل الدخول"
   :wrong-password                        "كلمة مرور خاطئة"

   ;recover
   :recover-from-passphrase               "استرداد بواسطة عبارة المرور"
   :recover-explain                       "الرجاء إدخال عبارة المرور الخاصة بكلمة المرور الخاصة بك لاستعادة إمكانية الوصول"
   :passphrase                            "عبارة المرور"
   :recover                               "استعادة"
   :enter-valid-passphrase                "الرجاء إدخال عبارة المرور"
   :enter-valid-password                  "الرجاء إدخال كلمة المرور"

   ;accounts
   :recover-access                        "استعادة إمكانية الوصول"
   :add-account                           "إضافة حساب"

   ;wallet-qr-code
   :done                                  "تم التنفيذ"
   :main-wallet                           "المحفظة الرئيسية"

   ;validation
   :invalid-phone                         "رقم هاتف غير صحيح"
   :amount                                "الكمية"
   :not-enough-eth                        (str "لا يوجدETH كافي بالحساب "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "تأكيد المعاملة"
                                           :other "تأكيد {{count}}  معاملات"
                                           :zero  "لا توجد معاملات"}
   :status                                "الحالة"
   :pending-confirmation                  "في انتظار التأكيد"
   :recipient                             "المستلم"
   :one-more-item                         "بند واحد آخر"
   :fee                                   "الرسوم"
   :value                                 "القيمة"

   ;:webview
   :web-view-error                        "عفواً، حدث خطأ ما"})

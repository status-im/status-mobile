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
   :datetime-ago                          "منذ"
   :datetime-yesterday                    "الأمس"
   :datetime-today                        "اليوم"

   ;profile
   :profile                               "الملف الشخصي"
   :message                               "الرسالة"
   :not-specified                         "غير محدد"
   :public-key                            "المفتاح العمومي"
   :phone-number                          "رقم الهاتف"
   :add-to-contacts                       "أضف إلى جهات الاتصال"

   ;;make_photo
   :image-source-title                    "الصورة الشخصية"
   :image-source-make-photo               "التقاط صورة"
   :image-source-gallery                  "الاختيار من معرض الصور"

   ;sign-up
   :contacts-syncronized                  "تمت مزامنة جهات الاتصال الخاصة بك"
   :confirmation-code                     (str "شكراً! لقد أرسلنا لك رسالة نصية تتضمن رمز تأكيد"
                                               "يرجى إدخال هذا الرمز لتأكيد رقم هاتفك")
   :incorrect-code                        (str "عذراً الرمز غير صحيح، يرجى إدخاله مرة أخرى")
   :phew-here-is-your-passphrase          "*أف* لقد كان أمراً شاقاً، إليك عبارة المرور الخاصة بك، * قم بتسجيلها واحتفظ بها في مكان آمن!* سوف تحتاج إليها لاسترداد حسابك."
   :here-is-your-passphrase               "إليك عبارة المرور الخاصة بك، * قم بتسجيلها واحتفظ بها في مكان آمن! *سوف تحتاج إليها لاسترداد حسابك"
   :phone-number-required                 "اضغط هنا لإدخال رقم الهاتف الخاص بك وسوف تعثر على أصدقائك"
   :intro-status                          "دردش معي لتثبيت حسابك وقم بتغيير الإعدادات الخاصة بك!"
   :intro-message1                        "مرحبا بك في Status \n اضغط على هذه الرسالة لتعيين كلمة المرور الخاصة بك وابدأ!"
   :account-generation-message            "امنحني ثانية واحدة، سوف أحتاج إلى إجراء بعض الحسابات الرياضية المجنونة لإنتاج الحساب الخاص بك!"

   ;chats
   :chats                                 "الدردشات"
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
   :contacts-group-new-chat               "ابدأ دردشة جديدة"
   :no-contacts                           "لا توجد جهات اتصال بعد"
   :show-qr                               "عرض شفرة التعريف"

   ;group-settings
   :remove                                "إزالة"
   :save                                  "حفظ"
   :clear-history                         "حذف التاريخ"
   :chat-settings                         "إعدادات الدردشة"
   :edit                                  "تحرير"
   :add-members                           "إضافة أعضاء"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "تسلم دعوة الدردشة"
   :removed-from-chat                     "قام بإزالتك من مجموعة الدردشة"
   :left                                  "غادر"
   :invited                               "مَدْعُوّ"
   :removed                               "مُسْتَبْعَد"
   :You                                   "أنت"

   ;new-contact
   :add-new-contact                       "إضافة جهة اتصال جديدة"
   :scan-qr                               "مسح شفرة التعريف"
   :name                                  "اسم"
   :address-explication                   "ربما يتعين أن تتوافر هنا بعض النصوص التي تشرح ما هو العنوان وأين تبحث عنه"
   :contact-already-added                 "تمت إضافة جهة الاتصال"
   :can-not-add-yourself                  "لا يمكنك إضافة نفسك"
   :unknown-address                       "عنوان غير معروف"


   ;login
   :connect                               "اتصال"
   :address                               "عنوان"
   :password                              "كلمة مرور"
   :wrong-password                        "كلمة مرور خاطئة"

   ;recover
   :passphrase                            "عبارة المرور"
   :recover                               "استعادة"

   ;accounts
   :recover-access                        "استعادة إمكانية الوصول"

   ;wallet-qr-code
   :done                                  "تم التنفيذ"
   :main-wallet                           "المحفظة الرئيسية"

   ;validation
   :invalid-phone                         "رقم هاتف غير صحيح"
   :amount                                "الكمية"
   ;transactions
   :status                                "الحالة"
   :recipient                             "المستلم"

   ;:webview
   :web-view-error                        "عفواً، حدث خطأ ما"})

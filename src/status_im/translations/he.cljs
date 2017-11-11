(ns status-im.translations.he)

(def translations
  {
   ;common
   :members-title                         "חבר"
   :not-implemented                       "!עדיין לא יושם"
   :chat-name                             "שם הצ'אט"
   :notifications-title                   "התראות וצלילים"
   :offline                               "לא מחובר"
   :search-for                            "...חפש"
   :cancel                                "בטל"
   :next                                  "הבא"
   :type-a-message                        "...הקלד הודעה"
   :type-a-command                        "...הקלד פעולה"
   :error                                 "שגיאה"

   :camera-access-error                   ".כדי להעניק את הרשאת המצלמה הנדרשת, בבקשה, תלכו להגדרות המערכת ותוודאו שהאופציה שנבחרה היא סטטוס > מצלמה"
   :photos-access-error                   ".כדי להעניק את הרשאת התמונות הנדרשת, בבקשה, תלכו להגדרות המערכת ותוודאו שהאופציה שנבחרה היא סטטוס > תמונות"

   ;drawer
   :invite-friends                        "הזמן חברים"
   :faq                                   "שאלות נפוצות"
   :switch-users                          "שנה משתמש"
   :feedback                              "Got feedback?\nShake your phone!"
   :view-all                              "View all"
   :current-network                       "Current network"

   ;chat
   :is-typing                             "מקליד"
   :and-you                               "גם אתה"
   :search-chat                           "חפש בצ'אט"
   :members                               {:one   "חבר אחד"
                                           :other "{{count}} חברים"
                                           :zero  "אין חברים"}
   :members-active                        {:one   "חבר אחד"
                                           :other "{{count}} חברים"
                                           :zero  "אין חברים"}
   :public-group-status                   "פומבי"
   :active-online                         "מחובר"
   :active-unknown                        "לא ידוע"
   :available                             "זמין"
   :no-messages                           "אין הודעות"
   :suggestions-requests                  "בקשות"
   :suggestions-commands                  "פקודות"
   :faucet-success                        "בקשת הברז התקבלה"
   :faucet-error                          "שגיאה בבקשת הברז"

   ;sync
   :sync-in-progress                      "...מסנכרן"
   :sync-synced                           "מסונכרן"

   ;messages
   :status-sending                        "שולח"
   :status-pending                        "ממתין"
   :status-sent                           "נשלחה"
   :status-seen-by-everyone               "פומבי"
   :status-seen                           "נראה"
   :status-delivered                      "התקבל"
   :status-failed                         "נכשל"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "שניה"
                                           :other "שניות"}
   :datetime-minute                       {:one   "דקה"
                                           :other "דקות"}
   :datetime-hour                         {:one   "שעה"
                                           :other "שעות"}
   :datetime-day                          {:one   "יום"
                                           :other "ימים"}
   :datetime-multiple                     "s"
   :datetime-ago                          "לפני"
   :datetime-yesterday                    "אתמול"
   :datetime-today                        "היום"

    ;profile
   :profile                               "פרופיל"
   :edit-profile                          "ערוך פרופיל"
   :report-user                           "דווח על המשתמש"
   :message                               "הודעה"
   :username                              "שם משתמש"
   :not-specified                         "לא מוגדר"
   :public-key                            "מפתח פומבי"
   :phone-number                          "מספר טלפון"
   :email                                 "אי-מייל"
   :update-status                         "...עדכן סטטוס"
   :add-a-status                          "...הוסף סטטוס"
   :status-prompt                         ".#hashtags צור סטטוס כדי ליידע אחרים בקשר לדברים אותם אתה מציע. אתה יכול גם להשתמש ב"

   :add-to-contacts                       "הוסף לאנשי קשר"
   :in-contacts                           "באנשי קשר"
   :remove-from-contacts                  "הסר מאנשי קשר"
   :start-conversation                    "התחל שיחה"
   :send-transaction                      "שלח עיסקה"
   :share-qr                              "שתף ברקוד"
   :error-incorrect-name                  "אנא בחר שם אחר"
   :error-incorrect-email                 "כתובת מייל שגויה"

   ;;make_photo
   :image-source-title                    "תמונת פרופיל"
   :image-source-make-photo               "צלם"
   :image-source-gallery                  "בחר מהגלריה"
   :image-source-cancel                   "בטל"

   ;;sharing
   :sharing-copy-to-clipboard             "העתק לשולחן העבודה"
   :sharing-share                         "...שתף"
   :sharing-cancel                        "בטל"

   :browsing-title                        "דפדף"
   :browsing-open-in-web-browser          "פתח בדפדפן"
   :browsing-cancel                       "בטל"

   ;sign-up
   :contacts-syncronized                  "אנשי הקשר שלך סונכרנו"
   :confirmation-code                     (str "תודה! שלחנו לך הודעה עם קוד"
                                               "אישור. אנא ספק את הקוד כדי לאשר את מספר הטלפון שלך")
   :incorrect-code                        (str "סליחה הקוד שגוי, אנא נסה שנית")
   :generate-passphrase                   (str "אנא צור משפט קוד כדי שתוכל לשחזר את הגישה שלך או להתחבר ממכשיר אחר")                                               
   :phew-here-is-your-passphrase          "פוו זה היה קשה, הנה משפט הקוד שלך, *תרשום אותו ותשמור עליו!* אתה תצטרך אותו כדי לשחזר את המשתמש שלך"
   :here-is-your-passphrase               "הנה משפט הקוד שלך, *תרשום אותו ותשמור עליו!* אתה תצטרך אותו כדי לשחזר את המשתמש שלך."
   :written-down                          "אנא וודא ששמרת אותו בצורה מאובטחת"
   :phone-number-required                 "הקש כאן כדי להזין את מספר הטלפון שלך ואני אמצא את החברים שלך"
   :shake-your-phone                      "!מצאת באג או שיש לך הצעה? פשוט טלטל את הפלאפון"
   :intro-status                          "דבר איתי כדי להסדיר את המשתמש שלך ולשנות את ההגדרות"
   :intro-message1                        "!ברוך הבא לסטטוס הקש על הודעה זו כדי לייצר סיסמא ולהתחיל"
   :account-generation-message            "!שניה, אני הולך להשתמש בקצת קסם מתמטי ולייצר את חשבונך"
   :move-to-internal-failure-message      "אנו זקוקים לאישורך כדי להזיז קבצים חשובים מהזיכרון החיצוני לפנימי. אנחנו לא נשתמש בזיכרון חיצוני בגרסאות עתידיות"
   :debug-enabled                         ".שרת הניפוי הותחל! אתה יכול כעת לבצע *status-dev-cli scan* כדי למצוא את השרת מהמחשב שלך על אותה הרשת"

   ;phone types
   :phone-e164                            "בין לאומי 1"
   :phone-international                   "בין לאומי 2"
   :phone-national                        "לאומי"
   :phone-significant                     "משמעותי"

   ;chats
   :chats                                 "צ'אטים"
   :new-chat                              "צ'אט חדש"
   :delete-chat                           "מחק צ'אט"
   :new-group-chat                        "צ'אט קבוצתי חדש"
   :new-public-group-chat                 "הצטרף לצ'אט פומבי"
   :edit-chats                            "ערוך צ'אטים"
   :search-chats                          "חפש בצ'אטים"
   :empty-topic                           "נושא ריק"
   :topic-format                          "פורמט שגוי [a-z0-9\\-]+"
   :public-group-topic                    "נושא"

   ;discover
   :discover                              "גילוי"
   :none                                  "אף אחד"
   :search-tags                           "תקליד את תג החיפוש שלך כאן"
   :popular-tags                          "תגים פופולאריים"
   :recent                                "חדש"
   :no-statuses-discovered                "לא התגלה שום סטטוס"
   :no-statuses-found                     "לא נמצא שום סטטוס"

   ;settings
   :settings                              "הגדרות"

   ;contacts
   :contacts                              "אנשי קשר"
   :new-contact                           "איש קשר חדש"
   :delete-contact                        "מחק איש קשר"
   :delete-contact-confirmation           "המשתמש הזה ימחק מרשימת אנשי הקשר שלך"
   :remove-from-group                     "הסר מהקבוצה"
   :edit-contacts                         "ערוך אנשי קשר"
   :search-contacts                       "חפש באנשי הקשר"
   :show-all                              "הראה את כולם"
   :contacts-group-dapps                  "אפליקציות מבוזרות"
   :contacts-group-people                 "אנשים"
   :contacts-group-new-chat               "התחל צ'אט חדש"
   :choose-from-contacts                  "בחר מאנשי הקשר"
   :no-contacts                           "אין שום אנשי קשר"
   :show-qr                               "הראה ברקוד"
   :enter-address                         "הזן כתובת"
   :more                                  "עוד"

   ;group-settings
   :remove                                "הסר"
   :save                                  "שמור"
   :delete                                "מחק"
   :change-color                          "שנה צבע"
   :clear-history                         "נקה היסטוריה"
   :mute-notifications                    "השתק התראות"
   :leave-chat                            "צא מהצ'אט"
   :delete-and-leave                      "מחק וצא"
   :chat-settings                         "הגדרות צ'אט"
   :edit                                  "ערוך"
   :add-members                           "הוסף מספרים"
   :blue                                  "כחול"
   :purple                                "סגול"
   :green                                 "ירוק"
   :red                                   "אדום"

   ;commands
   :money-command-description             "שלח כסף"
   :location-command-description          "שלח מיקום"
   :phone-command-description             "שלח מספר טלפון"
   :phone-request-text                    "בקשת מספר טלפון"
   :confirmation-code-command-description "שלח קוד אישור"
   :confirmation-code-request-text        "בקשת קוד אישור"
   :send-command-description              "שלח מיקום"
   :request-command-description           "שלח בקשה"
   :keypair-password-command-description  ""
   :help-command-description              "עזרה"
   :request                               "בקשה"
   :chat-send-eth                         "{{amount}} אתר"
   :chat-send-eth-to                      "{{amount}} אתר ל {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} מאתר {{chat-name}}"

   ;new-group
   :group-chat-name                       "שם הצ'אט"
   :empty-group-chat-name                 "אנא הזן שם"
   :illegal-group-chat-name               "אנא בחר שם שונה"
   :new-group                             "קבוצה חדשה"
   :reorder-groups                        "סדר מחדש את הקבוצה"
   :group-name                            "שם הקבוצה"
   :edit-group                            "ערוך את הקבוצה"
   :delete-group                          "מחר את הקבוצה"
   :delete-group-confirmation             "הקבוצה הזאת תמחק מרשימת הקבוצות. פעולה זאת לא תשפיע על אנשי הקשר"
   :delete-group-prompt                   "זה לא ישפיע על אנשי הקשר"
   :group-members                         "חברי הקבוצה"
   :contact-s                             {:one   "איש קשר"
                                           :other "אנשי קשר"}
   ;participants
   :add-participants                      "הוסף משתתפים"
   :remove-participants                   "הסר משתתפים"

   ;protocol
   :received-invitation                   "התקבלה הזמנה לצ'אט"
   :removed-from-chat                     "הוסרת מהצ'אט הקבוצתי"
   :left                                  "עזב"
   :invited                               "הוזמן"
   :removed                               "הוסר"
   :You                                   "אתה"

   ;new-contact
   :add-new-contact                       "הוסף איש קשר חדש"
   :import-qr                             "לייבא"
   :scan-qr                               "סרוק ברקוד"
   :swow-qr                               "הראה ברקוד"
   :name                                  "שם"
   :whisper-identity                      "לחש זהות"
   :address-explication                   "Maybe here should be some text explaining what an address is and where to look for it"
   :enter-valid-address                   "אנא הזן כתובת תקפה או סרוק ברקוד"
   :enter-valid-public-key                "אנא הזן קוד פומבי תקף או סרוק ברקוד"
   :contact-already-added                 "איש הקשר כבר קיים"
   :can-not-add-yourself                  "אתה לא יכול להוסיף את עצמך"
   :unknown-address                       "כתובת לא ידועה"


   ;login
   :connect                               "התחבר"
   :address                               "כתובת"
   :password                              "סיסמא"
   :login                                 "כניסה תלמערכת"
   :sign-in-to-status                     "התחבר לסטטוס"
   :sign-in                               "התחבר"
   :wrong-password                        "סיסמא שגויה"

   ;recover
   :recover-from-passphrase               "שחזר סיסמא ממשפט קוד"
   :recover-explain                       "אנא הזן את משפט הקוד של הסיסמא שלך כדי לשחזר גישה"
   :passphrase                            "משפט קוד"
   :recover                               "שחזר"
   :enter-valid-passphrase                "אנא הקש משפט קוד"
   :enter-valid-password                  "אנא הקש סיסמא"
   :twelve-words-in-correct-order         "12 מילים בסדר הנכון"

   ;accounts
   :recover-access                        "שחזר גישה"
   :add-account                           "הוסף איש קשר"
   :create-new-account                    "יצר איש קשר חדש"

   ;wallet-qr-code
   :done                                  "הושלם"
   :main-wallet                           "ארנק ראשי"

   ;validation
   :invalid-phone                         "מספר טלפון שגוי"
   :amount                                "כמות"
   :not-enough-eth                        (str "לא מספיק אתר בחשבון "
                                               "({{balance}} אתר)")
   ;transactions
   :confirm                               "אשר"
   :confirm-transactions                  {:one   "אשר העברה"
                                           :other "אשר {{count}} העברות"
                                           :zero  "אין העברות"}
   :transactions-confirmed                {:one   "העברה אושרה"
                                           :other "{{count}} העברות אושרו"
                                           :zero  "אין העברות שהושלמו"}
   :transaction                           "העברות"
   :unsigned-transactions                 "העברות ללא חתימה"
   :no-unsigned-transactions              "אין העברות ללא חתימה"
   :enter-password-transactions           {:one   "אשר העברה אל ידי הקשת סיסמתך"
                                           :other "אשר העברות על ידי הקשת סיסמתך"}
   :status                                "סטטוס"
   :pending-confirmation                  "ממתיך לאישור העברה"
   :recipient                             "מקבל"
   :one-more-item                         "עוד פריט אחד"
   :fee                                   "עמלה"
   :estimated-fee                         "עמלה מעורכת"
   :value                                 "שווי"
   :to                                    "ל"
   :from                                  "מ"
   :data                                  "נתונים"
   :got-it                                "קיבלתי"
   :contract-creation                     "יצירת חוזה"

   ;:webview
   :web-view-error                        "אופס,טעות"})

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

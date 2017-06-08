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
   :is-typing                             "is typing"
   :and-you                               "and you"
   :search-chat                           "Search chat"
   :members                               {:one   "1 member"
                                           :other "{{count}} members"
                                           :zero  "no members"}
   :members-active                        {:one   "1 member"
                                           :other "{{count}} members"
                                           :zero  "no members"}
   :public-group-status                   "Public"
   :active-online                         "Online"
   :active-unknown                        "Unknown"
   :available                             "Available"
   :no-messages                           "No messages"
   :suggestions-requests                  "Requests"
   :suggestions-commands                  "Commands"
   :faucet-success                        "Faucet request has been received"
   :faucet-error                          "Faucet request error"

   ;sync
   :sync-in-progress                      "Syncing..."
   :sync-synced                           "In sync"

   ;messages
   :status-sending                        "Sending"
   :status-pending                        "Pending"
   :status-sent                           "Sent"
   :status-seen-by-everyone               "Seen by everyone"
   :status-seen                           "Seen"
   :status-delivered                      "Delivered"
   :status-failed                         "Failed"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "second"
                                           :other "seconds"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "hour"
                                           :other "hours"}
   :datetime-day                          {:one   "day"
                                           :other "days"}
   :datetime-multiple                     "s"
   :datetime-ago                          "ago"
   :datetime-yesterday                    "yesterday"
   :datetime-today                        "today"

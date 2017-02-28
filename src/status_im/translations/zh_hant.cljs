(ns status-im.translations.zh-hant)

(def translations
  {
   ;common
   :members-title                         "成員"
   :not-implemented                       "!尚未完成"
   :chat-name                             "聊天名稱"
   :notifications-title                   "通知與聲音"
   :offline                               "離線"

   ;drawer
   :invite-friends                        "邀請好友"
   :faq                                   "常見問題"
   :switch-users                          "切換使用者"

   ;chat
   :is-typing                             "正在輸入"
   :and-you                               "和您"
   :search-chat                           "搜尋聊天"
   :members                               {:one   "1個成員"
                                           :other "{{count}} 個成員"
                                           :zero  "沒有成員"}
   :members-active                        {:one   "1 個成員, 1 個活動中"
                                           :other "{{count}} 個成員, {{count}} 個活動中"
                                           :zero  "沒有成員"}
   :active-online                         "線上"
   :active-unknown                        "未知"
   :available                             "有空"
   :no-messages                           "沒有訊息"
   :suggestions-requests                  "請求"
   :suggestions-commands                  "指令"

   ;sync
   :sync-in-progress                      "同步中..."
   :sync-synced                           "同步完成"

   ;messages
   :status-sending                        "正在傳送"
   :status-pending                        "等待中"
   :status-sent                           "已傳送"
   :status-seen-by-everyone               "所有人都已看過"
   :status-seen                           "已看過"
   :status-delivered                      "已送達"
   :status-failed                         "傳送失敗"

   ;datetime
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分"
                                           :other "分"}
   :datetime-hour                         {:one   "時"
                                           :other "時"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-multiple                     "秒"
   :datetime-ago                          "以前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "個人資料"
   :report-user                           "檢舉使用者"
   :message                               "訊息"
   :username                              "使用者名稱"
   :not-specified                         "未指定"
   :public-key                            "公開金鑰"
   :phone-number                          "電話號碼"
   :email                                 "電子郵件"
   :profile-no-status                     "沒有狀態"
   :add-to-contacts                       "新增至聯絡資訊"
   :error-incorrect-name                  "錯誤的名稱"
   :error-incorrect-email                 "錯誤的電子郵件地址"

   ;;make_photo
   :image-source-title                    "個人圖片"
   :image-source-make-photo               "拍攝"
   :image-source-gallery                  "從相簿中選擇"
   :image-source-cancel                   "取消"

   ;sign-up
   :contacts-syncronized                  "您的聯絡資訊已同步完成"
   :confirmation-code                     (str "謝謝！我們已向您發送了帶有 "
                                               "確認代碼的訊息。請輸入此代碼以驗證您的手機號碼")
   :incorrect-code                        (str "很抱歉，代碼錯誤，請重新輸入")
   :generate-passphrase                   (str "我會為您產生一個密碼短語，您可以使用它 "
                                               "在其他裝置上回復帳號或登入")
   :phew-here-is-your-passphrase          "*啊哈*，這是您的密碼短語，*請寫下來，並好好保管！* 您需要它來回復您的帳號。"
   :here-is-your-passphrase               "這是您的密碼短語，*請寫下來，並好好保管！* 您需要它來回復您的帳號。"
   :written-down                          "請確認您已將密碼短語寫下，並存放在安全的地點"
   :phone-number-required                 "點選這裡輸入您的手機號碼，我會找到您的好友"
   :intro-status                          "與我聊天以設定您的帳號並變更設定！"
   :intro-message1                        "歡迎來到Status\n點選此訊息以設定您的密碼並開始使用！"
   :account-generation-message            "給我幾秒鐘，我正在努力產生您的帳號！"

   ;chats
   :chats                                 "聊天室"
   :new-chat                              "新對話"
   :new-group-chat                        "新群組"

   ;discover
   :discover                             "發現"
   :none                                  "無"
   :search-tags                           "在這裡輸入搜尋標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近"
   :no-statuses-discovered                "未發現狀態"

   ;settings
   :settings                              "設定"

   ;contacts
   :contacts                              "聯絡資訊"
   :new-contact                           "新聯絡人"
   :show-all                              "顯示全部"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "成員"
   :contacts-group-new-chat               "開始新對話"
   :no-contacts                           "尚無聯絡資訊"
   :show-qr                               "顯示QR碼"

   ;group-settings
   :remove                                "移除"
   :save                                  "保存"
   :change-color                          "更改顏色"
   :clear-history                         "清理歷史記錄"
   :delete-and-leave                      "刪除並離開"
   :chat-settings                         "聊天設定"
   :edit                                  "編輯"
   :add-members                           "新增成員"
   :blue                                  "藍色"
   :purple                                "紫色"
   :green                                 "綠色"
   :red                                   "紅色"

   ;commands
   :money-command-description             "匯款"
   :location-command-description          "傳送位置"
   :phone-command-description             "傳送手機號碼"
   :phone-request-text                    "請求手機號碼"
   :confirmation-code-command-description "傳送確認碼"
   :confirmation-code-request-text        "請求確認碼"
   :send-command-description              "傳送位置"
   :request-command-description           "傳送請求"
   :keypair-password-command-description  ""
   :help-command-description              "幫助"
   :request                               "請求"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH 給 {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH 來自 {{chat-name}}"

   ;new-group
   :group-chat-name                       "群組名稱"
   :empty-group-chat-name                 "請輸入名稱"
   :illegal-group-chat-name               "請選擇其他名稱"

   ;participants
   :add-participants                      "新增參與者"
   :remove-participants                   "移除參與者"

   ;protocol
   :received-invitation                   "收到的聊天邀請"
   :removed-from-chat                     "將您移出了群組"
   :left                                  "剩餘"
   :invited                               "已邀請"
   :removed                               "已移出"
   :You                                   "您"

   ;new-contact
   :add-new-contact                       "新增新的聯絡人"
   :import-qr                             "匯入"
   :scan-qr                               "掃描QR碼"
   :name                                  "名稱"
   :whisper-identity                      "Whisper身份"
   :address-explication                   "也許這裡要有些文字介紹位址是什麼，又能在哪裡找到它"
   :enter-valid-address                   "請輸入有效位址或掃描QR碼"
   :contact-already-added                 "已新增該聯絡人"
   :can-not-add-yourself                  "您不能新增自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "連線"
   :address                               "地址"
   :password                              "密碼"
   :login                                 "登入"
   :wrong-password                        "密碼錯誤"

   ;recover
   :recover-from-passphrase               "使用密碼短語回復"
   :recover-explain                       "請輸入此密碼的密碼短語來回復登入權限"
   :passphrase                            "密碼短語"
   :recover                               "回復"
   :enter-valid-passphrase                "請輸入密碼短語"
   :enter-valid-password                  "請輸入密碼"

   ;accounts
   :recover-access                        "回復登入權限"
   :add-account                           "新增帳號"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主錢包"

   ;validation
   :invalid-phone                         "無效的手機號碼"
   :amount                                "金額"
   :not-enough-eth                        (str "ETH餘額不足 "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "確認交易"
                                           :other "確認 {{count}} 筆交易"
                                           :zero  "無交易"}
   :status                                "狀態"
   :pending-confirmation                  "等待確認中"
   :recipient                             "接收方"
   :one-more-item                         "再一項"
   :fee                                   "費用"
   :value                                 "價值"

   ;:webview
   :web-view-error                        "哎呀，出錯了"})

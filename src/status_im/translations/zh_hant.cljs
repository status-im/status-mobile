(ns status-im.translations.zh-hant)

(def translations
  {
   ;common
   :members-title                         "成員"
   :not-implemented                       "還未實施"
   :chat-name                             "聊天名稱"
   :notifications-title                   "通知與聲音"
   :offline                               "離線"

   ;drawer
   :invite-friends                        "邀請好友"
   :faq                                   "常見問題"
   :switch-users                          "切換使用者"

   ;chat
   :is-typing                             "正在鍵入中"
   :and-you                               "和您"
   :search-chat                           "搜尋聊天"
   :members                               {:one   "1個成員"
                                           :other "{{count}} 個成員"
                                           :zero  "沒有成員"}
   :members-active                        {:one   "1 個成員, 1 個活動"
                                           :other "{{count}} 個成員, {{count}} 個活動"
                                           :zero  "沒有成員"}
   :active-online                         "離線"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "沒有訊息"
   :suggestions-requests                  "請求"
   :suggestions-commands                  "命令"

   ;sync
   :sync-in-progress                      "同步中..."
   :sync-synced                           "同步"

   ;messages
   :status-sending                        "正在發送"
   :status-pending                        "待定"
   :status-sent                           "已發送"
   :status-seen-by-everyone               "全部可見"
   :status-seen                           "可見"
   :status-delivered                      "已送達"
   :status-failed                         "送達失敗"

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
   :profile                               "簡況"
   :report-user                           "報告使用者"
   :message                               "訊息"
   :username                              "使用者名稱"
   :not-specified                         "未指定"
   :public-key                            "公開金鑰"
   :phone-number                          "手機號碼"
   :email                                 "電子郵件"
   :profile-no-status                     "無狀態"
   :add-to-contacts                       "添加到聯絡資訊"
   :error-incorrect-name                  "請選擇另一個姓名"
   :error-incorrect-email                 "錯誤的電子郵寄地址"

   ;;make_photo
   :image-source-title                    "個人照片"
   :image-source-make-photo               "捕捉"
   :image-source-gallery                  "從相簿中選擇"
   :image-source-cancel                   "取消"

   ;sign-up
   :contacts-syncronized                  "您的聯絡資訊已同步"
   :confirmation-code                     (str "謝謝！我們已向您發送了包含 "
                                               "確認碼的訊息。請鍵入此代碼以驗證您的手機號碼")
   :incorrect-code                        (str "很抱歉，代碼不正確，請重新鍵入")
   :generate-passphrase                   (str "我會給您生成一條口令句，您可以使用它 "
                                               "在其他裝置上以訪問或登入")
   :phew-here-is-your-passphrase          "啊哈，這是您的口令句，*請記下來並 好好保管！ *您需要使用它以回復您帳號。"
   :here-is-your-passphrase               "這是您的口令句，*請記下來並 好好保管！ *您需要使用它以回復您帳號。"
   :written-down                          "請一定要把它記下來"
   :phone-number-required                 "點選這裡以鍵入您的手機號碼，我會發現您的好友"
   :intro-status                          "與我聊天以配置您的帳號，變更您的設定！"
   :intro-message1                        "歡迎來到Status\n點選此訊息以設定您的密碼並開始使用！"
   :account-generation-message            "給我幾秒鐘，我正在努力生成您的帳號！"

   ;chats
   :chats                                 "聊天"
   :new-chat                              "新聊天"
   :new-group-chat                        "新的群聊"

   ;discover
   :discover                             "發現"
   :none                                  "無"
   :search-tags                           "在這裡鍵入您的搜尋標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近"
   :no-statuses-discovered                "未發現狀態"

   ;settings
   :settings                              "設定"

   ;contacts
   :contacts                              "聯絡資訊"
   :new-contact                           "新聯絡資訊"
   :show-all                              "顯示全部"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "人"
   :contacts-group-new-chat               "開始新聊天"
   :no-contacts                           "還沒有聯絡資訊"
   :show-qr                               "顯示QR碼"

   ;group-settings
   :remove                                "移除"
   :save                                  "保存"
   :change-color                          "變更顏色"
   :clear-history                         "清理歷史記錄"
   :delete-and-leave                      "刪除並離開"
   :chat-settings                         "聊天設定"
   :edit                                  "編輯"
   :add-members                           "添加成員"
   :blue                                  "藍"
   :purple                                "紫"
   :green                                 "綠"
   :red                                   "紅"

   ;commands
   :money-command-description             "轉帳"
   :location-command-description          "發送位置"
   :phone-command-description             "發送手機號碼"
   :phone-request-text                    "請求手機號碼"
   :confirmation-code-command-description "發送確認碼"
   :confirmation-code-request-text        "請求確認碼"
   :send-command-description              "發送位置"
   :request-command-description           "發送請求"
   :keypair-password-command-description  ""
   :help-command-description              "幫助"
   :request                               "請求"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH 給 {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH 來自 {{chat-name}}"
   :command-text-location                 "位置: {{address}}"
   :command-text-browse                   "流覽網頁: {{webpage}}"
   :command-text-send                     "交易: {{amount}} ETH"
   :command-text-help                     "幫助"

   ;new-group
   :group-chat-name                       "聊天名稱"
   :empty-group-chat-name                 "請鍵入一個名稱"
   :illegal-group-chat-name               "請選擇其他名稱"

   ;participants
   :add-participants                      "添加參與人"
   :remove-participants                   "移除參與人"

   ;protocol
   :received-invitation                   "接受的聊天邀請"
   :removed-from-chat                     "把您移出了群聊"
   :left                                  "留下的"
   :invited                               "邀請的"
   :removed                               "移出的"
   :You                                   "您"

   ;new-contact
   :add-new-contact                       "添加新的聯絡資訊"
   :import-qr                             "匯入"
   :scan-qr                               "掃描QR碼"
   :name                                  "名稱"
   :whisper-identity                      "私聊身份"
   :address-explication                   "也許這裡會有文本介紹是什麼樣的位址，在哪裡能找到它"
   :enter-valid-address                   "請鍵入有效的位址或掃描QR碼"
   :contact-already-added                 "聯絡資訊已添加"
   :can-not-add-yourself                  "您無法添加自己"
   :unknown-address                       "未知得的地址"


   ;login
   :connect                               "連線"
   :address                               "地址"
   :password                              "密碼"
   :login                                 "登入"
   :wrong-password                        "密碼錯誤"

   ;recover
   :recover-from-passphrase               "從口令句恢復"
   :recover-explain                       "請鍵入密碼的口令句以恢復訪問"
   :passphrase                            "口令句"
   :recover                               "恢復"
   :enter-valid-passphrase                "請鍵入口令句"
   :enter-valid-password                  "請鍵入密碼"

   ;accounts
   :recover-access                        "恢復訪問"
   :add-account                           "添加帳號"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "錢包"

   ;validation
   :invalid-phone                         "無效的手機號碼"
   :amount                                "金額"
   :not-enough-eth                        (str "餘額中ETH不足 "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "確認交易"
                                           :other "確認 {{count}} 交易"
                                           :zero  "無交易"}
   :status                                "狀態"
   :pending-confirmation                  "等待確認"
   :recipient                             "接收人"
   :one-more-item                         "再來一個"
   :fee                                   "費用"
   :value                                 "價值"

   ;:webview
   :web-view-error                        "哎呀，出錯了"})

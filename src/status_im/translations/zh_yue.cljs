(ns status-im.translations.zh-yue)

(def translations
  {
   ;common
   :members-title                         "會員"
   :not-implemented                       "!未能實現"
   :chat-name                             "用戶名稱"
   :notifications-title                   "通知及聲音設定"
   :offline                               "離線"

   ;drawer
   :invite-friends                        "邀請好友"
   :faq                                   "常見問題"
   :switch-users                          "切換用戶"

   ;chat
   :is-typing                             "正在輸入"
   :and-you                               "與你"
   :search-chat                           "搜索聊天記錄"
   :members                               {:one   "1位成員"
                                           :other "{{count}} 位成員"
                                           :zero  "暫無成員"}
   :members-active                        {:one   "1位成員, 1位活躍"
                                           :other "{{count}}位成員, {{count}}位活躍"
                                           :zero  "暫無成員"}
   :active-online                         "在線"
   :active-unknown                        "未知"
   :available                             "有空"
   :no-messages                           "沒有新訊息"
   :suggestions-requests                  "徵求"
   :suggestions-commands                  "指令"

   ;sync
   :sync-in-progress                      "正在同步..."
   :sync-synced                           "已同步"

   ;messages
   :status-sending                        "發送中"
   :status-pending                        "待定"
   :status-sent                           "發送成功"
   :status-seen-by-everyone               "所有人已讀"
   :status-seen                           "已讀"
   :status-delivered                      "已發送"
   :status-failed                         "失敗"

   ;datetime
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分鐘"
                                           :other "分鐘"}
   :datetime-hour                         {:one   "小時"
                                           :other "小時"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-multiple                     "s"
   :datetime-ago                          "之前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "用戶簡介"
   :report-user                           "投訴用戶"
   :message                               "短訊"
   :username                              "用戶名稱"
   :not-specified                         "未標明"
   :public-key                            "公共鑰匙"
   :phone-number                          "電話號碼"
   :email                                 "電郵"
   :profile-no-status                     "無狀態"
   :add-to-contacts                       "添加到通訊錄"
   :error-incorrect-name                  "請選擇其他名稱"
   :error-incorrect-email                 "電郵錯誤"

   ;;make_photo
   :image-source-title                    "封面照片"
   :image-source-make-photo               "捕獲照片"
   :image-source-gallery                  "從圖庫中選取"
   :image-source-cancel                   "取消"

   ;sign-up
   :contacts-syncronized                  "你的聯繫人已同步"
   :confirmation-code                     (str "謝謝！我們已以短訊形式將確認信息發送給你"
                                               "代碼。請提供該代碼，以確認你的電話號碼")
   :incorrect-code                        (str "對不起，代碼不正確，請重新輸入")
   :generate-passphrase                   (str "正在為你產生臨時登入碼，以便恢復你的"
                                               "從另一設備訪問或登錄")
   :phew-here-is-your-passphrase          "*啊* 那可真不簡單。 這是你的臨時登入碼。 *請記錄並保存於安全地方!* 你將會需要它，以恢復你的帳戶。"
   :here-is-your-passphrase               "這是你的臨時登入碼。 *請記錄並保存於安全地方!* 你將會需要它，以恢復你的帳戶。"
   :written-down                          "請確保你已經將其安全寫下來"
   :phone-number-required                 "請點擊這裡輸入您的電話號碼，我們將為你尋覓你的好友"
   :intro-status                          "如需設置新帳戶或更改現有設置，請與我聊天！"
   :intro-message1                        "歡迎來到Status\n請點擊此短訊，以設置你的密碼及開始！"
   :account-generation-message            "請給我一秒鐘。我正在瘋狂地運算，以啟動你的帳戶！"

   ;chats
   :chats                                 "聊天史"
   :new-chat                              "新增聊天"
   :new-group-chat                        "新增群聊"

   ;discover
   :discover                             "新發現"
   :none                                  "不存在"
   :search-tags                           "請輸入你的搜索標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近發現"
   :no-statuses-discovered                "沒有發現任何狀態"

   ;settings
   :settings                              "設置"

   ;contacts
   :contacts                              "聯絡人"
   :new-contact                           "新增聯絡"
   :show-all                              "顯示所有"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "用戶"
   :contacts-group-new-chat               "開啟新聊天"
   :no-contacts                           "No contacts yet"
   :show-qr                               "暫無聯絡人"

   ;group-settings
   :remove                                "刪除"
   :save                                  "儲存"
   :change-color                          "更改顏色"
   :clear-history                         "清除歷史記錄"
   :delete-and-leave                      "刪除並離開"
   :chat-settings                         "聊天設置"
   :edit                                  "編輯"
   :add-members                           "增添成員"
   :blue                                  "藍"
   :purple                                "紫"
   :green                                 "綠"
   :red                                   "紅"

   ;commands
   :money-command-description             "發送金錢"
   :location-command-description          "發送位置"
   :phone-command-description             "發送電話號碼"
   :phone-request-text                    "請求電話號碼"
   :confirmation-code-command-description "發送確認碼"
   :confirmation-code-request-text        "徵求確認碼"
   :send-command-description              "發送位置"
   :request-command-description           "發送請求"
   :keypair-password-command-description  ""
   :help-command-description              "幫助"
   :request                               "徵求"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH 予 {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH 來自 {{chat-name}}"
   :command-text-location                 "地點: {{address}}"
   :command-text-browse                   "瀏覽網頁: {{webpage}}"
   :command-text-send                     "交易: {{amount}} ETH"
   :command-text-help                     "幫助"

   ;new-group
   :group-chat-name                       "聊天用戶名稱"
   :empty-group-chat-name                 "請輸入名稱"
   :illegal-group-chat-name               "請選擇其他名稱"

   ;participants
   :add-participants                      "增添參與者"
   :remove-participants                   "刪除參與者"

   ;protocol
   :received-invitation                   "收到聊天邀請"
   :removed-from-chat                     "已將你從群組聊天中刪除"
   :left                                  "離開"
   :invited                               "邀請"
   :removed                               "刪除"
   :You                                   "你"

   ;new-contact
   :add-new-contact                       "添加新聯繫人"
   :import-qr                             "輸入"
   :scan-qr                               "掃描 QR"
   :name                                  "名稱"
   :whisper-identity                      "秘密身份"
   :address-explication                   "請注意： 此文本解釋地址是什麼，以及在何處可找到它。"
   :enter-valid-address                   "請輸入有效地址或掃描QR碼"
   :contact-already-added                 "此聯繫人已添加"
   :can-not-add-yourself                  "你不能添加自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "連接"
   :address                               "地址"
   :password                              "密碼"
   :login                                 "登入"
   :wrong-password                        "密碼錯誤"

   ;recover
   :recover-from-passphrase               "以臨時登入碼恢復"
   :recover-explain                       "請輸入密碼的臨時登入碼，以恢復訪問"
   :passphrase                            "臨時登入碼"
   :recover                               "還原"
   :enter-valid-passphrase                "請輸入臨時登入碼"
   :enter-valid-password                  "請輸入密碼"

   ;accounts
   :recover-access                        "恢復訪問"
   :add-account                           "新增帳戶"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主錢包"

   ;validation
   :invalid-phone                         "電話號碼無效"
   :amount                                "金額"
   :not-enough-eth                        (str "沒有足夠ETH餘額"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "確認交易"
                                           :other "確認{{{count}}個交易"
                                           :zero  "無交易"}
   :status                                "狀態"
   :pending-confirmation                  "待確認"
   :recipient                             "收件人"
   :one-more-item                         "多一件"
   :fee                                   "費用"
   :value                                 "價值"

   ;:webview
   :web-view-error                        "抱歉，錯誤"})

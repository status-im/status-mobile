(ns status-im.translations.zh-yue)

(def translations
  {
   ;common
   :members-title                         "會員"
   :not-implemented                       "!未能實現"
   :chat-name                             "用戶名稱"
   :notifications-title                   "通知及聲音設定"
   :offline                               "離線"
   :search-for                            "搜索..."
   :cancel                                "取消"
   :next                                  "下一個"
   :type-a-message                        "輸入訊息..."
   :type-a-command                        "開始輸入指令..."
   :error                                 "錯誤"

   :camera-access-error                   "要授予所需的攝像機許可，請轉到系統設置，並確定選擇了“狀態”>“攝像機”。"
   :photos-access-error                   "要授予所需的照片許可，請轉到系統設置，並確保選擇“狀態“>“照片“。"

   ;drawer
   :switch-users                          "切換用戶"
   :current-network                       "當前網絡"

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
   :public-group-status                   "公共"
   :active-online                         "在線"
   :active-unknown                        "未知"
   :available                             "有空"
   :no-messages                           "沒有新訊息"
   :suggestions-requests                  "徵求"
   :suggestions-commands                  "指令"
   :faucet-success                        "水龍頭請求已經收到"
   :faucet-error                          "水龍頭請求錯誤"

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
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分鐘"
                                           :other "分鐘"}
   :datetime-hour                         {:one   "小時"
                                           :other "小時"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-ago                          "之前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "用戶簡介"
   :edit-profile                          "編輯個人資料"
   :message                               "短訊"
   :not-specified                         "未標明"
   :public-key                            "公共鑰匙"
   :phone-number                          "電話號碼"
   :update-status                         "更新你的狀態..."
   :add-a-status                          "添加狀態..."
   :status-prompt                         "建立一個狀態以幫助人們了解您提供的內容。 你也可以使用#hashtags。"
   :add-to-contacts                       "添加到通訊錄"
   :in-contacts                           "在聯絡資訊"
   :remove-from-contacts                  "從聯絡資訊中刪除"
   :start-conversation                    "開始對話"
   :send-transaction                      "發送交易"

   ;;make_photo
   :image-source-title                    "封面照片"
   :image-source-make-photo               "捕獲照片"
   :image-source-gallery                  "從圖庫中選取"

   ;;sharing
   :sharing-copy-to-clipboard             "複製到剪貼板"
   :sharing-share                         "分享..."
   :sharing-cancel                        "取消"

   :browsing-title                        "瀏覽"
   :browsing-open-in-web-browser          "在網絡瀏覽器中打開"
   :browsing-cancel                       "取消"

   ;sign-up
   :contacts-syncronized                  "你的聯繫人已同步"
   :confirmation-code                     (str "謝謝！我們已以短訊形式將確認信息發送給你"
                                               "代碼。請提供該代碼，以確認你的電話號碼")
   :incorrect-code                        (str "對不起，代碼不正確，請重新輸入")
   :phew-here-is-your-passphrase          "*啊* 那可真不簡單。 這是你的臨時登入碼。 *請記錄並保存於安全地方!* 你將會需要它，以恢復你的帳戶。"
   :here-is-your-passphrase               "這是你的臨時登入碼。 *請記錄並保存於安全地方!* 你將會需要它，以恢復你的帳戶。"
   :phone-number-required                 "請點擊這裡輸入您的電話號碼，我們將為你尋覓你的好友"
   :shake-your-phone                      "發現錯誤或有建議？ 只要〜搖一搖〜你的手機！"
   :intro-status                          "如需設置新帳戶或更改現有設置，請與我聊天！"
   :intro-message1                        "歡迎來到Status\n請點擊此短訊，以設置你的密碼及開始！"
   :account-generation-message            "請給我一秒鐘。我正在瘋狂地運算，以啟動你的帳戶！"
   :move-to-internal-failure-message      "我們需要將一些重要的文件從外部存儲移動到內部存儲。 為此，我們需要你的許可。 我們在將來的版本不會使用外部存儲。"
   :debug-enabled                         "調試服務器已經推出！ 現在你可以執行* status-dev-cli scan *在同一網絡上的電腦上查找服務器。"

   ;phone types
   :phone-e164                            "國際1"
   :phone-international                   "國際2"
   :phone-national                        "國內"
   :phone-significant                     "顯著"

   ;chats
   :chats                                 "聊天史"
   :delete-chat                           "刪除對話"
   :new-group-chat                        "新增群聊"
   :new-public-group-chat                 "加入公開聊天"
   :edit-chats                            "編輯對話"
   :search-chats                          "搜索對話"
   :empty-topic                           "主題空白"
   :topic-format                          "格式錯誤 [a-z0-9\\-]+"
   :public-group-topic                    "主題"

   ;discover
   :discover                             "新發現"
   :none                                  "不存在"
   :search-tags                           "請輸入你的搜索標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近發現"
   :no-statuses-discovered                "沒有發現任何狀態"
   :no-statuses-found                     "找不到狀態"

   ;settings
   :settings                              "設置"

   ;contacts
   :contacts                              "聯絡人"
   :new-contact                           "新增聯絡"
   :delete-contact                        "刪除聯絡人"
   :delete-contact-confirmation           "此聯絡人將從你的聯絡人中刪除"
   :remove-from-group                     "從群組中移除"
   :edit-contacts                         "編輯聯絡人"
   :search-contacts                       "搜尋聯絡人"
   :contacts-group-new-chat               "開啟新聊天"
   :choose-from-contacts                  "從聯絡人中選擇"
   :no-contacts                           "No contacts yet"
   :show-qr                               "暫無聯絡人"
   :enter-address                         "輸入地址"
   :more                                  "更多"

   ;group-settings
   :remove                                "刪除"
   :save                                  "儲存"
   :delete                                "刪除"
   :clear-history                         "清除歷史記錄"
   :mute-notifications                    "靜音通知"
   :leave-chat                            "離開對話"
   :chat-settings                         "聊天設置"
   :edit                                  "編輯"
   :add-members                           "增添成員"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "新增群組"
   :reorder-groups                        "重新排序群組"
   :edit-group                            "編輯群組"
   :delete-group                          "刪除群組"
   :delete-group-confirmation             "此群組將從你的群組中移除。 這不會影響聯絡人"
   :delete-group-prompt                   "這不會影響聯絡人"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}
   ;participants

   ;protocol
   :received-invitation                   "收到聊天邀請"
   :removed-from-chat                     "已將你從群組聊天中刪除"
   :left                                  "離開"
   :invited                               "邀請"
   :removed                               "刪除"
   :You                                   "你"

   ;new-contact
   :add-new-contact                       "添加新聯繫人"
   :scan-qr                               "掃描 QR"
   :name                                  "名稱"
   :address-explication                   "請注意： 此文本解釋地址是什麼，以及在何處可找到它。"
   :enter-valid-public-key                "請輸入有效公鑰或掃描QR碼"
   :contact-already-added                 "此聯繫人已添加"
   :can-not-add-yourself                  "你不能添加自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "連接"
   :address                               "地址"
   :password                              "密碼"
   :sign-in-to-status                     "登入Status"
   :sign-in                               "登入"
   :wrong-password                        "密碼錯誤"

   ;recover
   :passphrase                            "臨時登入碼"
   :recover                               "還原"
   :twelve-words-in-correct-order         "正確排序12個字"

   ;accounts
   :recover-access                        "恢復訪問"
   :create-new-account                    "建立新帳號"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主錢包"

   ;validation
   :invalid-phone                         "電話號碼無效"
   :amount                                "金額"
   ;transactions
   :confirm                               "確認"
   :transaction                           "交易"
   :status                                "狀態"
   :recipient                             "收件人"
   :to                                    "至"
   :from                                  "從"
   :data                                  "數據"
   :got-it                                "得到了"

   ;:webview
   :web-view-error                        "抱歉，錯誤"})

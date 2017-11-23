(ns status-im.translations.zh-hant)

(def translations
  {
   ;common
   :members-title                         "成員"
   :not-implemented                       "!尚未完成"
   :chat-name                             "聊天名稱"
   :notifications-title                   "通知與聲音"
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
   :switch-users                          "切換使用者"
   :current-network                       "當前網絡"

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
   :public-group-status                   "公共"
   :active-online                         "線上"
   :active-unknown                        "未知"
   :available                             "有空"
   :no-messages                           "沒有訊息"
   :suggestions-requests                  "請求"
   :suggestions-commands                  "指令"
   :faucet-success                        "水龍頭請求已經收到"
   :faucet-error                          "水龍頭請求錯誤"

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
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分"
                                           :other "分"}
   :datetime-hour                         {:one   "時"
                                           :other "時"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-ago                          "以前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "個人資料"
   :edit-profile                          "編輯個人資料"
   :message                               "訊息"
   :not-specified                         "未指定"
   :public-key                            "公開金鑰"
   :phone-number                          "電話號碼"
   :update-status                         "更新你的狀態..."
   :add-a-status                          "添加狀態..."
   :status-prompt                         "建立一個狀態以幫助人們了解您提供的內容。 你也可以使用#hashtags。"
   :add-to-contacts                       "新增至聯絡資訊"
   :in-contacts                           "在聯絡資訊"
   :remove-from-contacts                  "從聯絡資訊中刪除"
   :start-conversation                    "開始對話"
   :send-transaction                      "發送交易"

   ;;make_photo
   :image-source-title                    "個人圖片"
   :image-source-make-photo               "拍攝"
   :image-source-gallery                  "從相簿中選擇"

   ;;sharing
   :sharing-copy-to-clipboard             "複製到剪貼板"
   :sharing-share                         "分享..."
   :sharing-cancel                        "取消"

   :browsing-title                        "瀏覽"
   :browsing-open-in-web-browser          "在網絡瀏覽器中打開"
   :browsing-cancel                       "取消"

   ;sign-up
   :contacts-syncronized                  "您的聯絡資訊已同步完成"
   :confirmation-code                     (str "謝謝！我們已向您發送了帶有 "
                                               "確認代碼的訊息。請輸入此代碼以驗證您的手機號碼")
   :incorrect-code                        (str "很抱歉，代碼錯誤，請重新輸入")
   :phew-here-is-your-passphrase          "*啊哈*，這是您的密碼短語，*請寫下來，並好好保管！* 您需要它來回復您的帳號。"
   :here-is-your-passphrase               "這是您的密碼短語，*請寫下來，並好好保管！* 您需要它來回復您的帳號。"
   :phone-number-required                 "點選這裡輸入您的手機號碼，我會找到您的好友"
   :shake-your-phone                      "發現錯誤或有建議？ 只要〜搖一搖〜你的手機！"
   :intro-status                          "與我聊天以設定您的帳號並變更設定！"
   :intro-message1                        "歡迎來到Status\n點選此訊息以設定您的密碼並開始使用！"
   :account-generation-message            "給我幾秒鐘，我正在努力產生您的帳號！"
   :move-to-internal-failure-message      "我們需要將一些重要的文件從外部存儲移動到內部存儲。 為此，我們需要你的許可。 我們在將來的版本不會使用外部存儲。"
   :debug-enabled                         "調試服務器已經推出！ 現在你可以執行* status-dev-cli scan *在同一網絡上的電腦上查找服務器。"

   ;phone types
   :phone-e164                            "國際1"
   :phone-international                   "國際2"
   :phone-national                        "國內"
   :phone-significant                     "顯著"

   ;chats
   :chats                                 "聊天室"
   :delete-chat                           "刪除對話"
   :new-group-chat                        "新群組"
   :new-public-group-chat                 "加入公開聊天"
   :edit-chats                            "編輯對話"
   :search-chats                          "搜索對話"
   :empty-topic                           "主題空白"
   :topic-format                          "格式錯誤 [a-z0-9\\-]+"
   :public-group-topic                    "主題"

   ;discover
   :discover                              "發現"
   :none                                  "無"
   :search-tags                           "在這裡輸入搜尋標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近"
   :no-statuses-discovered                "未發現狀態"
   :no-statuses-found                     "找不到狀態"

   ;settings
   :settings                              "設定"

   ;contacts
   :contacts                              "聯絡資訊"
   :new-contact                           "新聯絡人"
   :delete-contact                        "刪除聯絡人"
   :delete-contact-confirmation           "此聯絡人將從你的聯絡人中刪除"
   :remove-from-group                     "從群組中移除"
   :edit-contacts                         "編輯聯絡人"
   :search-contacts                       "搜尋聯絡人"
   :contacts-group-new-chat               "開始新對話"
   :choose-from-contacts                  "從聯絡人中選擇"
   :no-contacts                           "尚無聯絡資訊"
   :show-qr                               "顯示QR碼"
   :enter-address                         "輸入地址"
   :more                                  "更多"

   ;group-settings
   :remove                                "移除"
   :save                                  "保存"
   :delete                                "刪除"
   :clear-history                         "清理歷史記錄"
   :mute-notifications                    "靜音通知"
   :leave-chat                            "離開對話"
   :chat-settings                         "聊天設定"
   :edit                                  "編輯"
   :add-members                           "新增成員"

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
   :received-invitation                   "收到的聊天邀請"
   :removed-from-chat                     "將您移出了群組"
   :left                                  "剩餘"
   :invited                               "已邀請"
   :removed                               "已移出"
   :You                                   "您"

   ;new-contact
   :add-new-contact                       "新增新的聯絡人"
   :scan-qr                               "掃描QR碼"
   :name                                  "名稱"
   :address-explication                   "也許這裡要有些文字介紹位址是什麼，又能在哪裡找到它"
   :enter-valid-public-key                "請輸入有效公鑰或掃描QR碼"
   :contact-already-added                 "已新增該聯絡人"
   :can-not-add-yourself                  "您不能新增自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "連線"
   :address                               "地址"
   :password                              "密碼"
   :sign-in-to-status                     "登入Status"
   :sign-in                               "登入"
   :wrong-password                        "密碼錯誤"

   ;recover
   :passphrase                            "密碼短語"
   :recover                               "回復"
   :twelve-words-in-correct-order         "正確排序12個字"

   ;accounts
   :recover-access                        "回復登入權限"
   :create-new-account                    "建立新帳號"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主錢包"

   ;validation
   :invalid-phone                         "無效的手機號碼"
   :amount                                "金額"
   ;transactions
   :confirm                               "確認"
   :transaction                           "交易"
   :status                                "狀態"
   :recipient                             "接收方"
   :to                                    "至"
   :from                                  "從"
   :data                                  "數據"
   :got-it                                "得到了"

   ;:webview
   :web-view-error                        "哎呀，出錯了"})
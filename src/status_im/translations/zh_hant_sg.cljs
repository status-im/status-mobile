(ns status-im.translations.zh-hant-sg)

(def translations
  {
   ;;common
   :members-title                         "成員"
   :not-implemented                       "!未實現"
   :chat-name                             "聊天名稱"
   :notifications-title                   "通知和聲音"
   :offline                               "離線"
   :search-for                            "搜尋..."
   :cancel                                "取消"
   :next                                  "下一個"
   :open                                  "開啟"
   :description                           "簡介"
   :url                                   "連結"
   :type-a-message                        "輸入訊息..."
   :type-a-command                        "開始輸入指令..."
   :error                                 "錯誤"
   :unknown-status-go-error               "未知的 status-go 錯誤"
   :node-unavailable                      "沒有可用的以太坊節點"
   :yes                                   "是"
   :no                                    "否"

   :camera-access-error                   "要授予所需的攝像機許可，請轉到系統設定，並確定選中了 “Status” > “攝像機”。"
   :photos-access-error                   "要授予所需的照片許可，請轉到系統設定，並確保選中了 “Status” > “照片”。"

   ;;drawer
   :switch-users                          "切換使用者"
   :current-network                       "當前網路"

   ;;chat
   :is-typing                             "正在輸入"
   :and-you                               "你"
   :search-chat                           "搜尋聊天"
   :members                               {:one   "1 個成員"
                                           :other "{{count}} 個成員"
                                           :zero  "無成員"}
   :members-active                        {:one   "1 個成員"
                                           :other "{{count}} 個成員"
                                           :zero  "無成員"}
   :public-group-status                   "公共"
   :active-online                         "線上"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "無訊息"
   :suggestions-requests                  "請求"
   :suggestions-commands                  "命令"
   :faucet-success                        "水龍頭請求已經收到"
   :faucet-error                          "水龍頭請求錯誤"

   ;;sync
   :sync-in-progress                      "正在同步..."
   :sync-synced                           "已同步"

   ;;messages
   :status-sending                        "正在傳送..."
   :status-pending                        "等待中"
   :status-sent                           "已傳送"
   :status-seen-by-everyone               "所有人可見"
   :status-seen                           "可見"
   :status-delivered                      "已傳送"
   :status-failed                         "失敗"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分鐘"
                                           :other "分鐘"}
   :datetime-hour                         {:one   "小時"
                                           :other "小時"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;;profile
   :profile                               "個人資料"
   :edit-profile                          "編輯個人資料"
   :message                               "資訊"
   :not-specified                         "未指定"
   :public-key                            "公鑰"
   :phone-number                          "電話號碼"
   :update-status                         "更新你的狀態..."
   :add-a-status                          "新增狀態..."
   :status-prompt                         "設定你的 Status。使用 #hastags 可以幫助其他人瞭解你，並且談論你正在想些什麼。"
   :add-to-contacts                       "新增到聯絡人"
   :in-contacts                           "已新增聯絡人"
   :remove-from-contacts                  "從聯絡人中刪除"
   :start-conversation                    "開始對話"
   :send-transaction                      "傳送交易"
   :testnet-text                          "你正在使用 {{testnet}} 測試網路，請不要向你的地址傳送真實的 ETH 或者 SNT。"
   :mainnet-text                          "你正在使用主幹網路，可以傳送真正的 ETH。"

   ;;make_photo
   :image-source-title                    "個人資料圖片"
   :image-source-make-photo               "拍攝"
   :image-source-gallery                  "從相簿中選擇"

   ;;sharing
   :sharing-copy-to-clipboard             "複製到剪貼簿"
   :sharing-share                         "分享..."
   :sharing-cancel                        "取消"

   :browsing-title                        "瀏覽"
   :browsing-open-in-web-browser          "在網路瀏覽器中開啟"
   :browsing-cancel                       "取消"

   ;;sign-up
   :contacts-syncronized                  "已同步你的聯絡人"
   :confirmation-code                     (str "謝謝！我們已經向你傳送了一條包含確認程式碼的簡訊，"
                                               "請提供該程式碼以確認你的電話號碼。")
   :incorrect-code                        (str "抱歉，程式碼不正確，請重新輸入")
   :phew-here-is-your-passphrase          "嗨，真不容易。這是你的密碼，*寫下來並好好保管它!* 你需要用它來恢復你的帳戶。"
   :here-is-your-passphrase               "這是你的密碼，*寫下來並好好保管它!* 你需要用它來恢復你的帳戶。"
   :here-is-your-signing-phrase           "這是你的簽名密碼，你需要用它來驗證你的交易，*寫下來並好好保管它!*"
   :phone-number-required                 "點選此處輸入你的電話號碼，我會找到你的朋友"
   :shake-your-phone                      "發現錯誤或有建議？僅需 ~搖一搖~ 你的手機！"
   :intro-status                          "跟我聊天，以設定你的帳戶並更改你的設定！"
   :intro-message1                        "歡迎來到 Status\n點選該訊息來設定你的密碼並開始！"
   :account-generation-message            "給我一點時間，我得瘋狂地計算一下，才能生成你的帳戶！"
   :move-to-internal-failure-message      "我們需要將一些重要的檔案從外部儲存移動到內部儲存。為此，我們需要你的許可。在之後的版本中，我們將不會使用外部儲存。"
   :debug-enabled                         "除錯伺服器已經推出！現在你可以在自己的計算機上執行 *status-dev-cli scan* 來查詢在同一網路上的伺服器。"

   ;;phone types
   :phone-e164                            "國際1"
   :phone-international                   "國際2"
   :phone-national                        "國內"
   :phone-significant                     "重要"

   ;;chats
   :chats                                 "聊天"
   :delete-chat                           "刪除聊天"
   :new-group-chat                        "新的群聊"
   :new-public-group-chat                 "加入公共聊天"
   :edit-chats                            "編輯對話"
   :search-chats                          "搜尋對話"
   :empty-topic                           "清空話題"
   :topic-format                          "格式錯誤 [a-z0-9\\-]+"
   :public-group-topic                    "話題"

   ;;discover
   :discover                              "發現"
   :none                                  "無"
   :search-tags                           "在此處輸入搜尋標籤"
   :popular-tags                          "熱門標籤"
   :recent                                "最近"
   :no-statuses-found                     "未發現狀態"
   :chat                                  "聊天"
   :all                                   "全部"
   :public-chats                          "公共聊天"
   :soon                                  "不久"
   :public-chat-user-count                "{{count}} 人"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp 簡介"
   :no-statuses-discovered                "未發現狀態"
   :no-statuses-discovered-body           "當某個人釋出\n一條狀態時，你可以在這裡看到它。"
   :no-hashtags-discovered-title          "未發現 #hashtags"
   :no-hashtags-discovered-body           "當一個 #hashtag 變為流行標籤，你可以在這裡看到它。"

   ;;settings
   :settings                              "設定"

   ;;contacts
   :contacts                              "聯絡人"
   :new-contact                           "新的聯絡人"
   :delete-contact                        "刪除聯絡人"
   :delete-contact-confirmation           "此聯絡人將從你的聯絡人中刪除"
   :remove-from-group                     "從群組中移除"
   :edit-contacts                         "編輯聯絡人"
   :search-contacts                       "搜尋聯絡人"
   :contacts-group-new-chat               "開始新的聊天"
   :choose-from-contacts                  "從聯絡人中選擇"
   :no-contacts                           "還沒有聯絡人"
   :show-qr                               "顯示二維碼"
   :enter-address                         "輸入地址"
   :more                                  "更多"

   ;;group-settings
   :remove                                "刪除"
   :save                                  "儲存"
   :delete                                "刪除"
   :clear-history                         "清空歷史記錄"
   :mute-notifications                    "靜音通知"
   :leave-chat                            "離開對話"
   :chat-settings                         "對話設定"
   :edit                                  "編輯"
   :add-members                           "新增成員"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "新增群組"
   :reorder-groups                        "重新排序群組"
   :edit-group                            "編輯群組"
   :delete-group                          "刪除群組"
   :delete-group-confirmation             "此群組將從你的群組中移除，這不會影響你的聯絡人。"
   :delete-group-prompt                   "這不會影響你的聯絡人"
   :contact-s                             {:one   "聯絡人"
                                           :other "聯絡人"}

   ;;protocol
   :received-invitation                   "收到的聊天邀請"
   :removed-from-chat                     "已將你從群聊中刪除"
   :left                                  "剩餘"
   :invited                               "已邀請"
   :removed                               "已刪除"
   :You                                   "你"

   ;;new-contact
   :add-new-contact                       "新增新的聯絡人"
   :scan-qr                               "掃描二維碼"
   :name                                  "名稱"
   :address-explication                   "你的公鑰可以用來生成以太坊地址，由一系列數字和字母組成，可以在個人簡介中輕鬆地找到。"
   :enter-valid-public-key                "請輸入一個有效的公鑰或掃描二維碼"
   :contact-already-added                 "已新增該聯絡人"
   :can-not-add-yourself                  "不能新增自己"
   :unknown-address                       "未知地址"

   ;;login
   :connect                               "連線"
   :address                               "地址"
   :password                              "密碼"
   :sign-in-to-status                     "登入 Status"
   :sign-in                               "登入"
   :wrong-password                        "密碼錯誤"
   :enter-password                        "輸入密碼"

   ;;recover
   :passphrase                            "密碼"
   :recover                               "恢復"
   :twelve-words-in-correct-order         "正確排序的12個單詞"

   ;;accounts
   :recover-access                        "恢復訪問"
   :create-new-account                    "建立新賬戶"

   ;;wallet-qr-code
   :done                                  "完成"

   ;;validation
   :invalid-phone                         "無效的電話號碼"
   :amount                                "金額"

   ;;transactions
   :confirm                               "確認"
   :transaction                           "交易"
   :unsigned-transaction-expired          "未簽名的交易過期"
   :status                                "狀態"
   :recipient                             "接收方"
   :to                                    "到"
   :from                                  "從"
   :data                                  "資料"
   :got-it                                "知道了"
   :block                                 "塊"
   :hash                                  "雜湊"
   :gas-limit                             "Gas 限制"
   :gas-price                             "Gas 價格"
   :gas-used                              "使用的 Gas"
   :cost-fee                              "成本/費用"
   :nonce                                 "隨機數"
   :confirmations                         "確認"
   :confirmations-helper-text             "請等待達到至少12個確認，來確保交易已被安全地處理。"
   :copy-transaction-hash                 "複製交易雜湊"
   :open-on-etherscan                     "開啟 Etherscan.io"

   ;;webview
   :web-view-error                        "糟糕，出錯了"

   ;;testfairy warning
   :testfairy-title                       "警告！"
   :testfairy-message                     "你正在使用一個從 nightly build 上安裝的應用。為了測試目的，在使用 WIFI 連線情況下，本構建包含了會話記錄，即和本應用互動的所有記錄（如視訊和日誌）將被儲存，並且可能被我們的開發團隊用於調查可能存在的問題。儲存的視訊、日誌不包含你的密碼。只有從 nightly build 下載安裝本應用時，才會進行記錄。如果本應用是從 PlayStore 或者 TestFlight 上下載安裝，則不會記錄。"

   ;; wallet
   :wallet                                "錢包"
   :wallets                               "錢包"
   :your-wallets                          "你的錢包"
   :main-wallet                           "主錢包"
   :wallet-error                          "載入資料錯誤"
   :wallet-send                           "傳送"
   :wallet-request                        "請求"
   :wallet-exchange                       "交易"
   :wallet-assets                         "資產"
   :wallet-add-asset                      "新增資產"
   :wallet-total-value                    "總值"
   :wallet-settings                       "錢包設定"
   :signing-phrase-description            "通過輸入密碼對交易進行簽名，請確保輸入的詞語和你的簽名密碼相匹配"
   :wallet-insufficient-funds             "資金不足"
   :request-transaction                   "請求交易"
   :send-request                          "傳送請求"
   :share                                 "分享"
   :eth                                   "ETH"
   :currency                              "貨幣"
   :usd-currency                          "USD"
   :transactions                          "交易"
   :transaction-details                   "交易明細"
   :transaction-failed                    "交易失敗"
   :transactions-sign                     "簽名"
   :transactions-sign-all                 "簽名所有"
   :transactions-sign-transaction         "簽名交易"
   :transactions-sign-later               "稍後簽名"
   :transactions-delete                   "刪除交易"
   :transactions-delete-content           "交易將從“未簽名”列表中移除"
   :transactions-history                  "歷史記錄"
   :transactions-unsigned                 "未簽名"
   :transactions-history-empty            "暫無交易歷史記錄"
   :transactions-unsigned-empty           "你沒有任何未簽名的交易"
   :transactions-filter-title             "過濾歷史記錄"
   :transactions-filter-tokens            "代幣"
   :transactions-filter-type              "型別"
   :transactions-filter-select-all        "全部選中"
   :view-transaction-details              "檢視交易明細"
   :transaction-description               "請等待達到至少12個確認，來確保交易已被安全地處理。"
   :transaction-sent                      "傳送的交易"
   :transaction-moved-text                "本交易將在接下來的5分鐘保持在“未簽名”列表中"
   :transaction-moved-title               "移動的交易"
   :sign-later-title                      "稍後對交易進行簽名？"
   :sign-later-text                       "檢查交易歷史記錄，可以對本次交易進行簽名"
   :not-applicable                        "對未簽名交易不可用"

   ;; Wallet Send
   :wallet-choose-recipient               "選擇接收方"
   :wallet-choose-from-contacts           "從聯絡人中選擇"
   :wallet-address-from-clipboard         "使用剪貼簿中的地址"
   :wallet-invalid-address                "無效的地址: \n {{data}}"
   :wallet-browse-photos                  "瀏覽相簿"
   :validation-amount-invalid-number      "金額不是一個有效的數字"
   :validation-amount-is-too-precise      "金額過於精確，所能傳送的最小單位是1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新建網路"
   :add-network                           "新增網路"
   :add-new-network                       "新增新網路"
   :existing-networks                     "已有的網路"
   :add-json-file                         "新增一個 JSON 檔案"
   :paste-json-as-text                    "貼上 JSON 為文字"
   :paste-json                            "貼上 JSON"
   :specify-rpc-url                       "指定一個 RPC 連結"
   :edit-network-config                   "編輯網路配置"
   :connected                             "已連線"
   :process-json                          "處理 JSON"
   :error-processing-json                 "處理 JSON 錯誤"
   :rpc-url                               "RPC 連結"
   :remove-network                        "移除網路"
   :network-settings                      "網路設定"
   :edit-network-warning                  "請小心，編輯網路資料可能導致當前網路不可用"
   :connecting-requires-login             "連線其它網路需要登入"
   :close-app-title                       "警告！"
   :close-app-content                     "應用將會停止並關閉。當你重新開啟應用，選中的網路將會被使用"
   :close-app-button                      "確認"})

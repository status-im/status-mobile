(ns status-im.translations.zh-hans)

(def translations
  {
   ;common
   :members-title                         "成员"
   :not-implemented                       "!未实现"
   :chat-name                             "聊天名称"
   :notifications-title                   "通知和声音"
   :offline                               "离线"
   :search-for                            "搜索..."
   :cancel                                "取消"
   :next                                  "下一个"
   :open                                  "开启"
   :description                           "Description"
   :url                                   "连结"
   :type-a-message                        "输入讯息..."
   :type-a-command                        "开始输入指令..."
   :error                                 "错误"
   :unknown-status-go-error               "未知 status-go 错误"
   :node-unavailable                      "无可用的以太坊节点"
   :yes                                   "是"
   :no                                    "否"

   :camera-access-error                   "要授予所需的摄像机许可，请转到系统设置，并确定选择了“状态”>“摄像机”。"
   :photos-access-error                   "要授予所需的照片许可，请转到系统设置，并确保选择“状态“>“照片“。"

   ;drawer
   :switch-users                          "切换用户"
   :current-network                       "当前网络"

   ;chat
   :is-typing                             "正在打字"
   :and-you                               "你"
   :search-chat                           "搜索聊天"
   :members                               {:one   "1个成员"
                                           :other "{{count}}个成员"
                                           :zero  "没有成员"}
   :members-active                        {:one   "1个成员，1个活跃"
                                           :other "{{count}}个成员，{{count}}个活跃"
                                           :zero  "没有成员"}
   :public-group-status                   "公共"
   :active-online                         "在线"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "没有消息"
   :suggestions-requests                  "请求"
   :suggestions-commands                  "命令"
   :faucet-success                        "水龙头请求已经收到"
   :faucet-error                          "水龙头请求错误"

   ;sync
   :sync-in-progress                      "正在同步…"
   :sync-synced                           "已同步"

   ;messages
   :status-pending                        "等待中"
   :status-sent                           "已发送"
   :status-seen-by-everyone               "所有人可见"
   :status-seen                           "可见"
   :status-delivered                      "已交付"
   :status-failed                         "失败"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分钟"
                                           :other "分钟"}
   :datetime-hour                         {:one   "小时"
                                           :other "小时"}
   :datetime-day                          {:one   "天"
                                           :other "天"}
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "个人资料"
   :edit-profile                          "编辑个人资料"
   :message                               "信息"
   :not-specified                         "未指定"
   :public-key                            "公钥"
   :phone-number                          "电话号码"
   :update-status                         "更新你的状态..."
   :add-a-status                          "添加状态..."
   :status-prompt                         "建立一个状态以帮助人们了解您提供的内容。 你也可以使用#hashtags。"
   :add-to-contacts                       "添加到联系人"
   :in-contacts                           "在联系人"
   :remove-from-contacts                  "从联系人中删除"
   :start-conversation                    "开始对话"
   :send-transaction                      "发送交易"
   :testnet-text                          "你正在 {{testnet}} 测试网. 请不要发送真实 ETH 或 SNT 到您的位址"
   :mainnet-text                          "你正在主要网. 真实的 ETH 会被发送"

   ;;make_photo
   :image-source-title                    "个人资料图片"
   :image-source-make-photo               "拍摄"
   :image-source-gallery                  "从图库中选择"

   ;;sharing
   :sharing-copy-to-clipboard             "复制到剪贴板"
   :sharing-share                         "分享..."
   :sharing-cancel                        "取消"

   :browsing-title                        "浏览"
   :browsing-open-in-web-browser          "在网络浏览器中打开"
   :browsing-cancel                       "取消"

   ;sign-up
   :contacts-syncronized                  "已同步你的联系人"
   :confirmation-code                     (str "谢谢！我们向你发送了一条包含确认信息的短信 "
                                               "代码。请提供该代码以确认你的电话号码")
   :incorrect-code                        (str "抱歉，代码不正确，请重新输入")
   :phew-here-is-your-passphrase          "*唷* 真不容易，这是你的密码短语，*写下来，好好保管它！*你需要用它来恢复你的帐户。"
   :here-is-your-passphrase               "这是你的密码短语，*写下来，好好保管它！*你需要用它来恢复你的帐户。"
   :here-is-your-signing-phrase           "这是你的签证短语 你会用它来确认交易. *写下来，好好保管它！ **"
   :phone-number-required                 "点击此处输入你的电话号码，我会找到你的朋友"
   :shake-your-phone                      "发现错误或有建议？ 只要〜摇一摇〜你的手机！"
   :intro-status                          "跟我聊天，以设置你的帐户并更改你的设置！"
   :intro-message1                        "欢迎来到Status\n点击该消息，以设置你的密码并开始！"
   :account-generation-message            "给我一点时间，我得疯狂地计算一下，以生成你的帐户！"
   :move-to-internal-failure-message      "我们需要将一些重要的文件从外部存储移动到内部存储。 为此，我们需要你的许可。 我们在将来的版本不会使用外部存储。"
   :debug-enabled                         "调试服务器已经推出！ 现在你可以执行* status-dev-cli scan *在同一网络上的电脑上查找服务器。"

   ;phone types
   :phone-e164                            "国际1"
   :phone-international                   "国际2"
   :phone-national                        "国内"
   :phone-significant                     "显着"

   ;chats
   :chats                                 "聊天"
   :delete-chat                           "刪除聊天"
   :new-group-chat                        "新的群聊"
   :new-public-group-chat                 "加入公开聊天"
   :edit-chats                            "编辑对话"
   :search-chats                          "搜索对话"
   :empty-topic                           "主题空白"
   :topic-format                          "格式错误 [a-z0-9\\-]+"
   :public-group-topic                    "主题"

   ;discover
   :discover                              "发现"
   :none                                  "无"
   :search-tags                           "在此处输入搜索标签"
   :popular-tags                          "热门标签"
   :recent                                "最近"
   :no-statuses-found                     "找不到状态"
   :chat                                  "聊天"
   :all                                   "全部"
   :public-chats                          "公开聊天"
   :soon                                  "将要"
   :public-chat-user-count                "{{count}} 人"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp 页面"
   :no-statuses-discovered                "未发现状态"
   :no-statuses-discovered-body           "当有人发文\n你会在这看到状态"
   :no-hashtags-discovered-title          "无发现 #标签"
   :no-hashtags-discovered-body           "当一个 #标签 变\n热门时你会在这看到"

   ;settings
   :settings                              "设置"

   ;contacts
   :contacts                              "联系人"
   :new-contact                           "新的联系人"
   :delete-contact                        "删除联络人"
   :delete-contact-confirmation           "此联络人将从你的联络人中删除"
   :remove-from-group                     "从群组中移除"
   :edit-contacts                         "编辑联络人"
   :search-contacts                       "搜寻联络人"
   :contacts-group-new-chat               "开始新的聊天"
   :choose-from-contacts                  "从联络人中选择"
   :no-contacts                           "还没有联系人"
   :show-qr                               "显示二维码"
   :enter-address                         "輸入地址"
   :more                                  "更多"

   ;group-settings
   :remove                                "删除"
   :save                                  "保存"
   :delete                                "刪除"
   :clear-history                         "清除历史记录"
   :mute-notifications                    "静音通知"
   :leave-chat                            "离开对话"
   :chat-settings                         "聊天设置"
   :edit                                  "编辑"
   :add-members                           "添加成员"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "现在所在地"
   :places-nearby                         "附近地点"
   :search-results                        "搜索结果"
   :dropped-pin                           "放置标记"
   :location                              "位置"
   :open-map                              "开启地图"
   :sharing-copy-to-clipboard-address     "复制地址"
   :sharing-copy-to-clipboard-coordinates "复制经纬度"

   ;new-group
   :new-group                             "新增群組"
   :reorder-groups                        "重新排序群組"
   :edit-group                            "編輯群組"
   :delete-group                          "刪除群組"
   :delete-group-confirmation             "此群組將從你的群組中移除。 這不會影響聯絡人"
   :delete-group-prompt                   "這不會影響聯絡人"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}

   ;protocol
   :received-invitation                   "收到了聊天邀请"
   :removed-from-chat                     "已将你从群聊中删除"
   :left                                  "剩余"
   :invited                               "已邀请"
   :removed                               "已删除"
   :You                                   "你"

   ;new-contact
   :add-new-contact                       "添加新的联系人"
   :scan-qr                               "扫描二维码"
   :name                                  "名称"
   :address-explication                   "也许这里应该有一些文本来解释什么是地址，以及在哪里查找它"
   :enter-valid-public-key                "请输入有效公钥或扫描QR码"
   :contact-already-added                 "已添加该联系人"
   :can-not-add-yourself                  "不能添加自己"
   :unknown-address                       "未知地址"

   ;login
   :connect                               "连接"
   :address                               "地址"
   :password                              "密码"
   :sign-in-to-status                     "登录Status"
   :sign-in                               "登录"
   :wrong-password                        "密码错误"
   :enter-password                        "输入密码"

   ;recover
   :passphrase                            "密码短语"
   :recover                               "恢复"
   :twelve-words-in-correct-order         "正确排序12个字"

   ;accounts
   :recover-access                        "恢复访问"
   :create-new-account                    "建立新帐户"

   ;wallet-qr-code
   :done                                  "完成"

   ;validation
   :invalid-phone                         "电话号码无效"
   :amount                                "金额"

   ;transactions
   :confirm                               "确认"
   :transaction                           "交易"
   :unsigned-transactions-expired         "未确认交易过期"
   :status                                "状态"
   :recipient                             "接受方"
   :to                                    "到"
   :from                                  "从"
   :data                                  "数据"
   :got-it                                "收到"
   :block                                 "区块"
   :hash                                  "哈希"
   :gas-limit                             "Gas 限制"
   :gas-price                             "Gas 价格"
   :gas-used                              "已使用 Gas"
   :cost-fee                              "花费"
   :nonce                                 "随机数"
   :confirmations                         "确认数"
   :confirmations-helper-text             "请等待至少12个确认以确保交易安全地完成"
   :copy-transaction-hash                 "复制交易哈希"
   :open-on-etherscan                     "在 Etherscan.io 上开启"

   ;:webview
   :web-view-error                        "糟糕，出错了"

   ;;testfairy warning
   :testfairy-title                       "警告！"
   :testfairy-message                     "你正在使用试运行版. 这个版本将会在有Wifi的情况下纪录使用数据，所用软件使用数据（包括录像和记录) 会被开发团队来检查可能问题，影像和记录不会包括你的密码，纪录只会在试运行版里进行，从PlayStore 或TestFlight 安装的软件不会纪录任何资讯"

   ;; wallet
   :wallet                                "钱包"
   :wallets                               "钱包"
   :your-wallets                          "你的钱包"
   :main-wallet                           "主要钱包"
   :wallet-error                          "钱包读取错误"
   :wallet-send                           "发送"
   :wallet-request                        "请求"
   :wallet-exchange                       "交易"
   :wallet-assets                         "资产"
   :wallet-add-asset                      "增加资产"
   :wallet-total-value                    "所有资产"
   :wallet-settings                       "钱包设置"
   :signing-phrase-description            "输入密码已签正交易，确认上面的字符合你的秘密短语"
   :wallet-insufficient-funds             "余额不足"
   :request-transaction                   "请求交易"
   :send-request                          "发出请求"
   :share                                 "分享"
   :eth                                   "ETH"
   :currency                              "货币"
   :usd-currency                          "USD"
   :transactions                          "交易"
   :transaction-details                   "交易细节"
   :transaction-failed                    "交易失败"
   :transactions-sign                     "签证"
   :transactions-sign-all                 "签证所有"
   :transactions-sign-transaction         "签证交易"
   :transactions-sign-later               "稍后签证"
   :transactions-delete                   "删除交易"
   :transactions-delete-content           "交易将从“未签证“中移除"
   :transactions-history                  "交易纪录"
   :transactions-unsigned                 "未签证"
   :transactions-history-empty            "无交易纪录"
   :transactions-unsigned-empty           "没有未签证交易"
   :transactions-filter-title             "过滤历史"
   :transactions-filter-tokens            "代币"
   :transactions-filter-type              "类型"
   :transactions-filter-select-all        "选取全部"
   :view-transaction-details              "查看交易细节"
   :transaction-description               "请等待至少12个确认以确保交易安全地完成"
   :transaction-sent                      "交易发送"
   :transaction-moved-text                "交易会在”未签证“列表中５分钟"
   :transaction-moved-title               "交易移动"
   :sign-later-title                      "稍后签证？"
   :sign-later-text                       "查看交易纪录来签证交易"
   :not-applicable                        "不适用未签证交易"

   ;; Wallet Send
   :wallet-choose-recipient               "选取接收者"
   :wallet-choose-from-contacts           "从联络人中选择"
   :wallet-address-from-clipboard         "从剪贴簿中复制地址"
   :wallet-invalid-address                "无效位址: \n {{data}}"
   :wallet-browse-photos                  "浏览照片"
   :validation-amount-invalid-number      "数量不是有效数字"
   :validation-amount-is-too-precise      "数量太精确，最小发送单位是 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新网路"
   :add-network                           "添加网路"
   :add-new-network                       "添加新网路"
   :existing-networks                     "现有网路"
   :add-json-file                         "添加 JSON 档案"
   :paste-json-as-text                    "贴上 JSON 成文字"
   :paste-json                            "贴上 JSON"
   :specify-rpc-url                       "注明 RPC URL"
   :edit-network-config                   "改变网路设置"
   :connected                             "已连结"
   :process-json                          "处理 JSON"
   :error-processing-json                 "JSON 处理错误"
   :rpc-url                               "RPC URL"
   :remove-network                        "移除网路"
   :network-settings                      "网路设置"
   :edit-network-warning                  "小心，改变网路资讯可能导致网路无法使用"
   :connecting-requires-login             "连接到不同网路需要登入"
   :close-app-title                       "注意！"
   :close-app-content                     "这个软件将会关闭，重启时选择的网路将可以使用"
   :close-app-button                      "确认"})})

(ns status-im.translations.zh-hans)

(def translations
  {
   ;;common
   :members-title                         "成员"
   :not-implemented                       "!未实现"
   :chat-name                             "聊天名称"
   :notifications-title                   "通知和声音"
   :offline                               "离线"
   :search-for                            "搜索..."
   :cancel                                "取消"
   :next                                  "下一个"
   :open                                  "打开"
   :description                           "简介"
   :url                                   "链接"
   :type-a-message                        "输入讯息..."
   :type-a-command                        "开始输入指令..."
   :error                                 "错误"
   :unknown-status-go-error               "未知的 status-go 错误"
   :node-unavailable                      "没有运行的以太坊节点"
   :yes                                   "是"
   :no                                    "否"

   :camera-access-error                   "要授予所需的摄像机许可，请转到系统设置，并确定选中了 “Status” > “摄像机”。"
   :photos-access-error                   "要授予所需的照片许可，请转到系统设置，并确保选中了 “Status” > “照片”。"

   ;;drawer
   :switch-users                          "切换用户"
   :current-network                       "当前网络"

   ;;chat
   :is-typing                             "正在输入"
   :and-you                               "你"
   :search-chat                           "搜索聊天"
   :members                               {:one   "1 个成员"
                                           :other "{{count}} 个成员"
                                           :zero  "无成员"}
   :members-active                        {:one   "1 个成员"
                                           :other "{{count}} 个成员"
                                           :zero  "无成员"}
   :public-group-status                   "公共"
   :active-online                         "在线"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "无消息"
   :suggestions-requests                  "请求"
   :suggestions-commands                  "命令"
   :faucet-success                        "水龙头请求已经收到"
   :faucet-error                          "水龙头请求错误"

   ;;sync
   :sync-in-progress                      "正在同步..."
   :sync-synced                           "已同步"

   ;;messages
   :status-sending                        "正在发送..."
   :status-pending                        "等待中"
   :status-sent                           "已发送"
   :status-seen-by-everyone               "所有人可见"
   :status-seen                           "可见"
   :status-delivered                      "已发送"
   :status-failed                         "失败"

   ;;datetime
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

   ;;profile
   :profile                               "个人资料"
   :edit-profile                          "编辑个人资料"
   :message                               "信息"
   :not-specified                         "未指定"
   :public-key                            "公钥"
   :phone-number                          "电话号码"
   :update-status                         "更新你的状态..."
   :add-a-status                          "添加状态..."
   :status-prompt                         "设置你的 Status。使用 #hashtags 可以帮助其他人了解你，并且谈论你正在想些什么。"
   :add-to-contacts                       "添加到联系人"
   :in-contacts                           "已添加联系人"
   :remove-from-contacts                  "从联系人中删除"
   :start-conversation                    "开始对话"
   :send-transaction                      "发送交易"
   :testnet-text                          "你正在使用{{testnet}}测试网络，请不要向你的地址发送真实的 ETH 或者 SNT。"
   :mainnet-text                          "你正在使用主干网络，可以发送真正的 ETH。"

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

   ;;sign-up
   :contacts-syncronized                  "已同步你的联系人"
   :confirmation-code                     (str "谢谢！我们已经向你发送了一条包含确认代码的短信，"
                                               "请提供该代码以确认你的电话号码。")
   :incorrect-code                        (str "抱歉，代码不正确，请重新输入")
   :phew-here-is-your-passphrase          "嗨，真不容易。这是你的密码，*写下来并好好保管它!* 你需要用它来恢复你的帐户。"
   :here-is-your-passphrase               "这是你的密码，*写下来并好好保管它!* 你需要用它来恢复你的帐户。"
   :here-is-your-signing-phrase           "这是你的签名密码，你需要用它来验证你的交易，*写下来并好好保管它!*"
   :phone-number-required                 "点击此处输入你的电话号码，我会找到你的朋友"
   :shake-your-phone                      "发现错误或有建议？只要 ~摇一摇~ 你的手机！"
   :intro-status                          "跟我聊天，以设置你的帐户并更改你的设置！"
   :intro-message1                        "欢迎来到Status\n点击该消息来设置你的密码并开始！"
   :account-generation-message            "给我一点时间，我得疯狂地计算一下，才能生成你的帐户！"
   :move-to-internal-failure-message      "我们需要将一些重要的文件从外部存储移动到内部存储。为此，我们需要你的许可。在之后的版本中，我们将不会使用外部存储。"
   :debug-enabled                         "调试服务器已经推出！现在你可以在自己的计算机上执行 *status-dev-cli scan* 来查找在同一网络上的服务器。"

   ;;phone types
   :phone-e164                            "国际1"
   :phone-international                   "国际2"
   :phone-national                        "国内"
   :phone-significant                     "重要"

   ;;chats
   :chats                                 "聊天"
   :delete-chat                           "刪除聊天"
   :new-group-chat                        "新的群聊"
   :new-public-group-chat                 "加入公共聊天"
   :edit-chats                            "编辑对话"
   :search-chats                          "搜索对话"
   :empty-topic                           "清空话题"
   :topic-format                          "格式错误 [a-z0-9\\-]+"
   :public-group-topic                    "话题"

   ;;discover
   :discover                              "发现"
   :none                                  "无"
   :search-tags                           "在此处输入搜索标签"
   :popular-tags                          "热门标签"
   :recent                                "最近"
   :no-statuses-found                     "未发现状态"
   :chat                                  "聊天"
   :all                                   "全部"
   :public-chats                          "公共聊天"
   :soon                                  "不久"
   :public-chat-user-count                "{{count}}人"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp简介"
   :no-statuses-discovered                "未发现状态"
   :no-statuses-discovered-body           "当某个人发布\n一条状态时，你可以在这里看到它。"
   :no-hashtags-discovered-title          "未发现 #hashtags"
   :no-hashtags-discovered-body           "当一个 #hashtag 变为流行标签，你可以在这里看到它。"

   ;;settings
   :settings                              "设置"

   ;;contacts
   :contacts                              "联系人"
   :new-contact                           "新的联系人"
   :delete-contact                        "删除联系人"
   :delete-contact-confirmation           "此联系人将从你的联系人中删除"
   :remove-from-group                     "从群组中移除"
   :edit-contacts                         "编辑联系人"
   :search-contacts                       "搜索联系人"
   :contacts-group-new-chat               "开始新的聊天"
   :choose-from-contacts                  "从联系人中选择"
   :no-contacts                           "还没有联系人"
   :show-qr                               "显示二维码"
   :enter-address                         "输入地址"
   :more                                  "更多"

   ;;group-settings
   :remove                                "删除"
   :save                                  "保存"
   :delete                                "刪除"
   :clear-history                         "清空历史记录"
   :mute-notifications                    "静音通知"
   :leave-chat                            "离开对话"
   :chat-settings                         "对话设置"
   :edit                                  "编辑"
   :add-members                           "添加成员"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;new-group
   :new-group                             "新增群组"
   :reorder-groups                        "重新排序群组"
   :edit-group                            "编辑群组"
   :delete-group                          "删除群组"
   :delete-group-confirmation             "此群组将从你的群组中移除，这不会影响你的联系人。"
   :delete-group-prompt                   "这不会影响你的联系人"
   :contact-s                             {:one   "联系人"
                                           :other "联系人"}

   ;;protocol
   :received-invitation                   "收到的聊天邀请"
   :removed-from-chat                     "已将你从群聊中删除"
   :left                                  "剩余"
   :invited                               "已邀请"
   :removed                               "已删除"
   :You                                   "你"

   ;;new-contact
   :add-new-contact                       "添加新的联系人"
   :scan-qr                               "扫描二维码"
   :name                                  "名称"
   :address-explication                   "你的公钥可以用来生成以太坊地址，由一系列数字和字母组成，可以在个人简介中轻松地找到。"
   :enter-valid-public-key                "请输入一个有效的公钥或扫描二维码"
   :contact-already-added                 "已添加该联系人"
   :can-not-add-yourself                  "不能添加自己"
   :unknown-address                       "未知地址"

   ;;login
   :connect                               "连接"
   :address                               "地址"
   :password                              "密码"
   :sign-in-to-status                     "登录 Status"
   :sign-in                               "登录"
   :wrong-password                        "密码错误"
   :enter-password                        "输入密码"

   ;;recover
   :passphrase                            "密码"
   :recover                               "恢复"
   :twelve-words-in-correct-order         "正确排序的12个单词"

   ;;accounts
   :recover-access                        "恢复访问"
   :create-new-account                    "创建新账户"

   ;;wallet-qr-code
   :done                                  "完成"

   ;;validation
   :invalid-phone                         "无效的电话号码"
   :amount                                "金额"

   ;;transactions
   :confirm                               "确认"
   :transaction                           "交易"
   :unsigned-transaction-expired          "未签名的交易过期"
   :status                                "状态"
   :recipient                             "接收方"
   :to                                    "到"
   :from                                  "从"
   :data                                  "数据"
   :got-it                                "知道了"
   :block                                 "块"
   :hash                                  "哈希"
   :gas-limit                             "Gas 限制"
   :gas-price                             "Gas 价格"
   :gas-used                              "使用的 Gas"
   :cost-fee                              "成本/费用"
   :nonce                                 "随机数"
   :confirmations                         "确认"
   :confirmations-helper-text             "请等待达到至少12个确认，来确保交易已被安全地处理。"
   :copy-transaction-hash                 "复制交易哈希"
   :open-on-etherscan                     "打开 Etherscan.io"

   ;;webview
   :web-view-error                        "糟糕，出错了"

   ;;testfairy warning
   :testfairy-title                       "警告！"
   :testfairy-message                     "你正在使用一个从 nightly build 上安装的应用。为了测试目的，在使用 WIFI 连接情况下，本构建包含了会话记录，即和本应用交互的所有记录（如视频和日志）将被保存，并且可能被我们的开发团队用于调查可能存在的问题。保存的视频、日志不包含你的密码。只有从 nightly build 下载安装本应用时，才会进行记录。如果本应用是从 PlayStore 或者 TestFlight 上下载安装，则不会记录。"

   ;; wallet
   :wallet                                "钱包"
   :wallets                               "钱包"
   :your-wallets                          "你的钱包"
   :main-wallet                           "主钱包"
   :wallet-error                          "加载数据错误"
   :wallet-send                           "发送"
   :wallet-request                        "请求"
   :wallet-exchange                       "交易"
   :wallet-assets                         "资产"
   :wallet-add-asset                      "添加资产"
   :wallet-total-value                    "总值"
   :wallet-settings                       "钱包设置"
   :signing-phrase-description            "通过输入密码对交易进行签名，请确保输入的词语和你的签名密码相匹配"
   :wallet-insufficient-funds             "资金不足"
   :request-transaction                   "请求交易"
   :send-request                          "发送请求"
   :share                                 "分享"
   :eth                                   "ETH"
   :currency                              "货币"
   :usd-currency                          "USD"
   :transactions                          "交易"
   :transaction-details                   "交易明细"
   :transaction-failed                    "交易失败"
   :transactions-sign                     "签名"
   :transactions-sign-all                 "签名所有"
   :transactions-sign-transaction         "签名交易"
   :transactions-sign-later               "稍后签名"
   :transactions-delete                   "删除交易"
   :transactions-delete-content           "交易将从“未签名”列表中移除"
   :transactions-history                  "历史记录"
   :transactions-unsigned                 "未签名"
   :transactions-history-empty            "暂无交易历史记录"
   :transactions-unsigned-empty           "你没有任何未签名的交易"
   :transactions-filter-title             "过滤历史记录"
   :transactions-filter-tokens            "代币"
   :transactions-filter-type              "类型"
   :transactions-filter-select-all        "全部选中"
   :view-transaction-details              "查看交易明细"
   :transaction-description               "请等待达到至少12个确认，来确保交易已被安全地处理。"
   :transaction-sent                      "发送的交易"
   :transaction-moved-text                "本交易将在接下来的5分钟保持在“未签名”列表中"
   :transaction-moved-title               "移动的交易"
   :sign-later-title                      "稍后对交易进行签名？"
   :sign-later-text                       "检查交易历史记录，可以对本次交易进行签名"
   :not-applicable                        "对未签名交易不可用"

   ;; Wallet Send
   :wallet-choose-recipient               "选择接收方"
   :wallet-choose-from-contacts           "从联系人中选择"
   :wallet-address-from-clipboard         "使用剪贴板中的地址"
   :wallet-invalid-address                "无效的地址: \n {{data}}"
   :wallet-browse-photos                  "浏览相册"
   :validation-amount-invalid-number      "金额不是一个有效的数字"
   :validation-amount-is-too-precise      "金额过于精确，所能发送的最小单位是1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新建网络"
   :add-network                           "添加网络"
   :add-new-network                       "添加新网络"
   :existing-networks                     "已有的网络"
   :add-json-file                         "添加一个 JSON 文件"
   :paste-json-as-text                    "粘贴 JSON 为文本"
   :paste-json                            "粘贴 JSON"
   :specify-rpc-url                       "指定一个 RPC 链接"
   :edit-network-config                   "编辑网络配置"
   :connected                             "已连接"
   :process-json                          "处理 JSON"
   :error-processing-json                 "处理 JSON 错误"
   :rpc-url                               "RPC 链接"
   :remove-network                        "移除网络"
   :network-settings                      "网络设置"
   :edit-network-warning                  "请小心，编辑网络数据可能导致当前网络不可用"
   :connecting-requires-login             "连接其它网络需要登录"
   :close-app-title                       "警告！"
   :close-app-content                     "应用将会停止并关闭。当你重新打开应用，选中的网络将会被使用"
   :close-app-button                      "确认"})

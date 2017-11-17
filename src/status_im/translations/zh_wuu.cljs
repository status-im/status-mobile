(ns status-im.translations.zh-wuu)

(def translations
  {
   ;;common
   :members-title                         "会员"
   :not-implemented                       "未实行"
   :chat-name                             "聊天名称"
   :notifications-title                   "通知与声音"
   :offline                               "离线"
   :search-for                            "搜寻..."
   :cancel                                "取消"
   :next                                  "下一个"
   :open                                  "开启"
   :description                           "描述"
   :url                                   "网址"
   :type-a-message                        "开始输入一个信息..."
   :type-a-command                        "开始输入一个指令..."
   :error                                 "错误"
   :unknown-status-go-error               "未知的status-go错误"
   :node-unavailable                      "没有以太坊的节点在运行"
   :yes                                   "是"
   :no                                    "否"

   :camera-access-error                   "要授予摄像机许可权，请到系统设置，并确定摄像机的许可状态是被选取的"
   :photos-access-error                   "要授予相片许可权，请到系统设置，并确定相片的许可状态是被选取的"

   ;;drawer
   :switch-users                          "切换用户"
   :current-network                       "当前网络"

   ;;chat
   :is-typing                             "正在输入"
   :and-you                               "和您"
   :search-chat                           "搜索聊天"
   :members                               {:one   "1个会员"
                                           :other "{{count}} 会员"
                                           :zero  "无会员"}
   :members-active                        {:one   "1个会员"
                                           :other "{{count}} 会员"
                                           :zero  "0个会员"}
   :public-group-status                   "公开"
   :active-online                         "在线"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "无信息"
   :suggestions-requests                  "请求"
   :suggestions-commands                  "命令"
   :faucet-success                        "源头请求已经收到"
   :faucet-error                          "源头请求错误"

   ;;sync
   :sync-in-progress                      "同步中..."
   :sync-synced                           "同步"

   ;;messages
   :status-sending                        "发送中..."
   :status-pending                        "延迟中"
   :status-sent                           "已发送"
   :status-seen-by-everyone               "已被每个人阅"
   :status-seen                           "已阅"
   :status-delivered                      "已发送"
   :status-failed                         "发送失败"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分钟"
                                           :other "分钟"}
   :datetime-hour                         {:one   "小时"
                                           :other "小时"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-ago                          "之前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;;profile
   :profile                               "个人资料"
   :edit-profile                          "编辑个人资料"
   :message                               "消息"
   :not-specified                         "未指定"
   :public-key                            "公共密钥"
   :phone-number                          "电话号码"
   :update-status                         "更新你的状态..."
   :add-a-status                          "添加状态..."
   :status-prompt                         "建立一个状态以帮助人们了解您提供的内容。 你也可以使用#hashtags。"
   :add-to-contacts                       "添加到联系人"
   :in-contacts                           "联系人"
   :remove-from-contacts                  "从联系人中删除"
   :start-conversation                    "开始对话"
   :send-transaction                      "发送交易"
   :testnet-text                          "你目前在测试链中，勿传送正式链上的ETH或是SNT到您的地址"
   :mainnet-text                          "你目前在正式链中，真实的ETH将被发送"

   ;;make_photo
   :image-source-title                    "个人资料照片"
   :image-source-make-photo               "截图"
   :image-source-gallery                  "从相册中选择"

   ;;sharing
   :sharing-copy-to-clipboard             "复制到剪贴板"
   :sharing-share                         "分享..."
   :sharing-cancel                        "取消"

   :browsing-title                        "浏览"
   :browsing-open-in-web-browser          "在网络浏览器中打开"
   :browsing-cancel                       "取消"

   ;;sign-up
   :contacts-syncronized                  "您的联系人已同步"
   :confirmation-code                     (str "谢谢！我们已经给您发了一个确认短信"
                                               "代码。请提供该代码以确认您的电话号码")
   :incorrect-code                        (str "对不起，代码不正确，请再输入一次")
   :phew-here-is-your-passphrase          "*唷* 是很难的，这是您的口令短语，*写下它并且安全的保存它！* 您将需要它来恢复您的帐户。"
   :here-is-your-passphrase               "这是您的口令短语，*写下它并且安全的保存它！* 您将需要它来恢复您的帐户。"
   :here-is-your-signing-phrase           "这是您的签章短语，您将会使用它来确认您的交易。 *写下它并且安全的保存它。*"
   :phone-number-required                 "点击这里确定您的电话号码，我会找到您的朋友"
   :shake-your-phone                      "发现错误或有建议？ 只要〜摇一摇〜你的手机！"
   :intro-status                          "与我聊天设置您的帐户，并更改您的设置！"
   :intro-message1                        "欢迎来到\n点击这一消息设置您的密码并开始!"
   :account-generation-message            "给我一秒，我要做一些疯狂的计算生成您的帐号!"
   :move-to-internal-failure-message      "我们需要将一些重要的文件从外部存储移动到内部存储。 为此，我们需要你的许可。 我们在将来的版本不会使用外部存储。"
   :debug-enabled                         "调试服务器已经推出！ 现在你可以执行* status-dev-cli scan *在同一网络上的电脑上查找服务器。"

   ;;phone types
   :phone-e164                            "国际1"
   :phone-international                   "国际2"
   :phone-national                        "国内"
   :phone-significant                     "重要的"

   ;;chats
   :chats                                 "对话"
   :delete-chat                           "刪除聊天"
   :new-group-chat                        "新的群聊"
   :new-public-group-chat                 "加入公开聊天"
   :edit-chats                            "编辑对话"
   :search-chats                          "搜索对话"
   :empty-topic                           "主题空白"
   :topic-format                          "格式错误 [a-z0-9\\-]+"
   :public-group-topic                    "主题"

   ;;discover
   :discover                              "发现"
   :none                                  "无"
   :search-tags                           "在这里键入您的搜索标签"
   :popular-tags                          "热门 #hashtags"
   :recent                                "最近状态"
   :no-statuses-found                     "找不到状态"
   :chat                                  "聊天"
   :all                                   "全部"
   :public-chats                          "公开聊天"
   :soon                                  "即将"
   :public-chat-user-count                "{{count}} 人"
   :dapps                                 "去中心化应用程式"
   :dapp-profile                          "去中心化应用程式资料"
   :no-statuses-discovered                "未发现状态"
   :no-statuses-discovered-body           "当有人展示\n一個狀態，您可以在這裡看到它."
   :no-hashtags-discovered-title          "无 #hashtags 发现"
   :no-hashtags-discovered-body           "当一个 #hashtag 变得\n受欢迎的，你将在这裡看到它."

   ;;settings
   :settings                              "设置"

   ;;contacts
   :contacts                              "联系人"
   :new-contact                           "新的联系人"
   :delete-contact                        "删除联络人"
   :delete-contact-confirmation           "此联络人将从你的联络人中删除"
   :remove-from-group                     "从群组中移除"
   :edit-contacts                         "编辑联络人"
   :search-contacts                       "搜寻联络人"
   :contacts-group-new-chat               "开始新的对话"
   :choose-from-contacts                  "从联繫人中选取"
   :no-contacts                           "还未有联繫人"
   :show-qr                               "展示 QR code"
   :enter-address                         "输入地址"
   :more                                  "更多"

   ;;group-settings
   :remove                                "移动"
   :save                                  "保存"
   :delete                                "刪除"
   :clear-history                         "清除历史"
   :mute-notifications                    "静音通知"
   :leave-chat                            "离开对话"
   :chat-settings                         "对话设置"
   :edit                                  "编辑"
   :add-members                           "添加会员"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "你现有位置"
   :places-nearby                         "位置附近"
   :search-results                        "搜寻结果"
   :dropped-pin                           "摆设的标记"
   :location                              "位置"
   :open-map                              "打开地图"
   :sharing-copy-to-clipboard-address     "拷贝地址"
   :sharing-copy-to-clipboard-coordinates "拷贝座标"

   ;;new-group
   :new-group                             "新群组"
   :reorder-groups                        "重新排序群组"
   :edit-group                            "编辑群组"
   :delete-group                          "删除群组"
   :delete-group-confirmation             "此群组将从你的群组中移除。 这不会影响联络人"
   :delete-group-prompt                   "这不会影响联络人"
   :contact-s                             {:one   "联繫人"
                                           :other "联繫人们"}

   ;;protocol
   :received-invitation                   "收到对话邀请"
   :removed-from-chat                     "将你从聊天群组内移除"
   :left                                  "离开"
   :invited                               "被邀请"
   :removed                               "被移除"
   :You                                   "您"

   ;;new-contact
   :add-new-contact                       "增加新的联繫人"
   :scan-qr                               "扫描 QR code"
   :name                                  "名称"
   :address-explication                   "您的公有钥匙被使用在产生你的以太坊地址，而这组公钥是由一串数字与文字组成，您可以在您的资料中轻易的找寻到。"
   :enter-valid-public-key                "请输入一个固定的公有钥匙或是扫描 QR code"
   :contact-already-added                 "这个联繫人已经被加入了"
   :can-not-add-yourself                  "您不能加入您自己"
   :unknown-address                       "不明的地址"

   ;;login
   :connect                               "连结"
   :address                               "地址"
   :password                              "密码"
   :sign-in-to-status                     "登入状态"
   :sign-in                               "登入"
   :wrong-password                        "错误的密码"
   :enter-password                        "输入密码"

   ;;recover
   :passphrase                            "口令短语"
   :recover                               "恢复"
   :twelve-words-in-correct-order         "正确排序的12个字"

   ;;accounts
   :recover-access                        "恢复访问"
   :create-new-account                    "建立新帐户"

   ;;wallet-qr-code
   :done                                  "完成"

   ;;validation
   :invalid-phone                         "无效的电话号码"
   :amount                                "数量"

   ;;transactions
   :confirm                               "确认"
   :transaction                           "交易"
   :unsigned-transaction-expired          "未确认交易过期"
   :status                                "状态"
   :recipient                             "收件人"
   :to                                    "至"
   :from                                  "从"
   :data                                  "数据"
   :got-it                                "得到了"
   :block                                 "区块"
   :hash                                  "哈希值"
   :gas-limit                             "燃料限制"
   :gas-price                             "燃料价格"
   :gas-used                              "燃料使用量"
   :cost-fee                              "费用"
   :nonce                                 "交易编号"
   :confirmations                         "确认"
   :confirmations-helper-text             "请等待至少十二个确认数已确定您的交易程序是安全的"
   :copy-transaction-hash                 "複製交易哈西值"
   :open-on-etherscan                     "在Etherscan.io开启"

   ;;webview
   :web-view-error                        "错误"

   ;;testfairy warning
   :testfairy-title                       "警告!"
   :testfairy-message                     "您正在使用每日构建的安装的应用程序。出于测试的目的，如果使用WiFi连接，此版本将包含会话记录，因此您与该应用的所有交互都将被保存（视频和日誌），并可能被我们的开发团队用于调查可能的问题。保存的视频/日誌不包含您的密码。只有在每日构建的安装应用程序情况下才能进行录製。如果应用程序是从PlayStore或TestFlight安装的，则不会记录任何内容。"

   ;; wallet
   :wallet                                "钱包"
   :wallets                               "钱包"
   :your-wallets                          "您的钱包"
   :main-wallet                           "主要的钱包"
   :wallet-error                          "读取资料错误"
   :wallet-send                           "寄送"
   :wallet-request                        "请求"
   :wallet-exchange                       "交换"
   :wallet-assets                         "资产"
   :wallet-add-asset                      "增加资产"
   :wallet-total-value                    "总值"
   :wallet-settings                       "钱包设定"
   :signing-phrase-description            "输入密码签核此比交易。确认你所输入的文字符合您的签核口令"
   :wallet-insufficient-funds             "资金不足"
   :request-transaction                   "请求交易"
   :send-request                          "送出需求"
   :share                                 "分享"
   :eth                                   "ETH"
   :currency                              "货币"
   :usd-currency                          "美金"
   :transactions                          "交易"
   :transaction-details                   "交易明细"
   :transaction-failed                    "交易失败"
   :transactions-sign                     "签章"
   :transactions-sign-all                 "签章全部"
   :transactions-sign-transaction         "签章交易"
   :transactions-sign-later               "稍后签核"
   :transactions-delete                   "删除交易"
   :transactions-delete-content           "交易将从未签章列表中删除"
   :transactions-history                  "历史纪录"
   :transactions-unsigned                 "未签章"
   :transactions-history-empty            "您的交易历史中还未有任何交易"
   :transactions-unsigned-empty           "你没有任何未签章交易"
   :transactions-filter-title             "过滤历史纪录"
   :transactions-filter-tokens            "代币"
   :transactions-filter-type              "型态"
   :transactions-filter-select-all        "选择全部"
   :view-transaction-details              "观看所有交易纪录"
   :transaction-description               "请等待至少十二组确认信息已确保您的交易是符合正常之程序"
   :transaction-sent                      "交易送出"
   :transaction-moved-text                "交易将在未来5分钟内保留在“未签章”列表中"
   :transaction-moved-title               "已移动交易"
   :sign-later-title                      "稍后签章吗?"
   :sign-later-text                       "检查交易记录以签章此交易"
   :not-applicable                        "不适用于未签章的交易"

   ;; Wallet Send
   :wallet-choose-recipient               "选择接收人"
   :wallet-choose-from-contacts           "从联繫人中选择"
   :wallet-address-from-clipboard         "从剪贴板中选择地址"
   :wallet-invalid-address                "无效的地址: \n {{data}}"
   :wallet-invalid-chain-id               "网络未符合: \n {{data}}"
   :wallet-browse-photos                  "浏览相片"
   :validation-amount-invalid-number      "数量非有效之数字"
   :validation-amount-is-too-precise      "數量非常的精確。最小你可以用來寄送的單位為1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新网络"
   :add-network                           "增加网络"
   :add-new-network                       "增加新网络"
   :existing-networks                     "已经存在的网络"
   :add-json-file                         "增加JSON档"
   :paste-json-as-text                    "贴上JSON作为文件"
   :paste-json                            "贴上JSON"
   :specify-rpc-url                       "指定RPC网址"
   :edit-network-config                   "编辑网络配置"
   :connected                             "連接中"
   :process-json                          "处理 JSON"
   :error-processing-json                 "處理JSON錯誤"
   :rpc-url                               "RPC网址"
   :remove-network                        "移除网络"
   :network-settings                      "网络设定"
   :edit-network-warning                  "请注意，编辑网络配置有可能会造成您的网络无法使用"
   :connecting-requires-login             "需要透过登入才能连结到其他网络"
   :close-app-title                       "警告!"
   :close-app-content                     "这个应用程式将会停止与关闭。当您在开启它时，被选择的网络将被使用"
   :close-app-button                      "确认"})

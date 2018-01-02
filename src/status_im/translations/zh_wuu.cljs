(ns status-im.translations.zh-wuu)

(def translations
  {
   ;;common
   :members-title                         "会员"
   :not-implemented                       "!勿可实现"
   :chat-name                             "聊天名字"
   :notifications-title                   "通知跟声音"
   :offline                               "离线"
   :search-for                            "搜索..."
   :cancel                                "勿要了"
   :next                                  "下一只"
   :open                                  "打开"
   :description                           "描述"
   :url                                   "URL"
   :type-a-message                        "输入讯息..."
   :type-a-command                        "开始输入指令..."
   :error                                 "错特了"
   :unknown-status-go-error               "未知的status-go出错"
   :node-unavailable                      "么ethereum节点辣运行"
   :yes                                   "是额"
   :no                                    "勿是"

   :camera-access-error                   "要授予所需额摄像机许可，请打开侬额系统设置，并保证“状态”>“摄像机”是选中额。"
   :photos-access-error                   "要授予所需额照片许可，请打开侬额系统设置，并保证“状态“>“照片“是选中额。"

   ;;drawer
   :switch-users                          "切换用户"
   :current-network                       "当前网络"

   ;;chat
   :is-typing                             "辣辣输入"
   :and-you                               "跟侬"
   :search-chat                           "搜索聊天"
   :members                               {:one   "1只"
                                           :other "{{count}}会员"
                                           :zero  "么会员"}
   :members-active                        {:one   "1只会员，1只活跃成员"
                                           :other "{{count}} 会员, {{count}} 活跃成员"
                                           :zero  "么会员"}
   :public-group-status                   "公共"
   :active-online                         "在线"
   :active-unknown                        "啊伐晓得"
   :available                             "好用额"
   :no-messages                           "么信息"
   :suggestions-requests                  "请求"
   :suggestions-commands                  "命令"
   :faucet-success                        "水龙头请求已经收到了"
   :faucet-error                          "水龙头请求错特额"

   ;;sync
   :sync-in-progress                      "同步中..."
   :sync-synced                           "同步"

   ;;messages
   :status-sending                        "发送中"
   :status-pending                        "待定"
   :status-sent                           "已发送"
   :status-seen-by-everyone               "大家赛可见"
   :status-seen                           "可见"
   :status-delivered                      "已发送"
   :status-failed                         "发送么成功"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分钟"
                                           :other "分钟"}
   :datetime-hour                         {:one   "钟头"
                                           :other "钟头"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-ago                          "之前"
   :datetime-yesterday                    "昨日"
   :datetime-today                        "今朝"

   ;;profile
   :profile                               "个人资料"
   :edit-profile                          "编辑个人资料"
   :message                               "消息"
   :not-specified                         "还没指定"
   :public-key                            "公共密码"
   :phone-number                          "电话号头"
   :update-status                         "更新侬额状态..."
   :add-a-status                          "添加状态..."
   :status-prompt                         "侬可以写滴状态。还可以用#hashtags，酿宁嘎寻倒侬，了解侬，跟侬一道驾栅糊。"
   :add-to-contacts                       "添加到联系人"
   :in-contacts                           "在联系人"
   :remove-from-contacts                  "从联系人里厢删特"
   :start-conversation                    "开始讲言话了"
   :send-transaction                      "发送交易"
   :testnet-text                          "侬当前在 {{testnet}} Testnet. 勿要发送真实额ETH或者SNT到侬额地址！"
   :mainnet-text                          "侬当前在 Mainnet。真实的ETH会得发送拨侬～～"

   ;;make_photo
   :image-source-title                    "个人资料照片"
   :image-source-make-photo               "截图"
   :image-source-gallery                  "从相册里厢选择"

   ;;sharing
   :sharing-copy-to-clipboard             "复制到剪贴板"
   :sharing-share                         "分享..."
   :sharing-cancel                        "勿要了"

   :browsing-title                        "浏览"
   :browsing-open-in-web-browser          "轧网络浏览器里厢打开"
   :browsing-cancel                       "勿要了"

   ;;sign-up
   :contacts-syncronized                  "侬额联系人同步好啦"
   :confirmation-code                     (str "谢谢！阿拉已经帮侬发了一只确认短信"
                                               "代码。请填上侬收到额口令code 来确认侬额电话号头")
   :incorrect-code                        (str "对伐起，口令code 勿对，请再输入一次")
   :phew-here-is-your-passphrase          "*唷* 是老难额，个是侬额口令短语 passphrase，*写下来并保证伊额安全！* 侬将来会用到伊来恢复侬额帐户呃。"
   :here-is-your-passphrase               "个是侬额口令短语passphrase，*写下来并保证伊额安全！* 侬将会用到伊来恢复侬额帐户。*"
   :here-is-your-signing-phrase           "个是侬额登录短语signing phrase. *写下来并保证伊额安全！* 侬将会用到伊来恢复侬额帐户。*"
   :phone-number-required                 "点击格德激活侬额电话号头，侬会寻到侬交关多额朋友"
   :shake-your-phone                      "发现错特额地方或者有啥建议想港把阿拉听？ 只要〜摇一摇〜侬额手机！"
   :intro-status                          "侬可以跟阿拉聊两句，好开始设置侬额帐户，并更改设置！"
   :intro-message1                        "欢迎来到\n点击格只消息设置侬额密码，然后侬就好开始啦!"
   :account-generation-message            "等吾一秒钟，吾要做滴疯狂计算好生成侬额帐号!"
   :move-to-internal-failure-message      "阿拉需要将一些重要额文件从外部存储移到内部存储。 所以，阿拉需要侬额许可。阿拉勿会轧将来额版本上头使用外部存储呃。"
   :debug-enabled                         "调试服务器已经运行！ 现在侬可以执行* status-dev-cli scan *在同一网络上头额电脑上查找服务器。"

   ;;phone types
   :phone-e164                            "国际1"
   :phone-international                   "国际2"
   :phone-national                        "国内"
   :phone-significant                     "要紧"

   ;;chats
   :chats                                 "聊天"
   :delete-chat                           "刪特聊天"
   :new-group-chat                        "新额群聊"
   :new-public-group-chat                 "加入公开聊天"
   :edit-chats                            "编辑对话"
   :search-chats                          "搜索对话"
   :empty-topic                           "清空话题"
   :topic-format                          "格式错特了 [a-z0-9\\-]+"
   :public-group-topic                    "话题"

   ;;discover
   :discover                              "发现"
   :none                                  "么"
   :search-tags                           "在格德键入侬额搜索标签"
   :popular-tags                          "热门标签"
   :recent                                "最近"
   :no-statuses-found                     "寻伐着状态"
   :chat                                  "聊天"
   :all                                   "全部"
   :public-chats                          "开放聊天"
   :soon                                  "快了"
   :public-chat-user-count                "{{count}}只宁"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp 资料"
   :no-statuses-discovered                "么发现状态"
   :no-statuses-discovered-body           "当有宁发消息\n 侬会得辣格得看到。"
   :no-hashtags-discovered-title          "么#hashtags被发现"
   :no-hashtags-discovered-body           "当#hashtag变了\n 老受欢迎额，侬会得辣格得看到。"

   ;;settings
   :settings                              "设置"

   ;;contacts
   :contacts                              "联系人"
   :new-contact                           "新加联系人"
   :delete-contact                        "删特联络人"
   :delete-contact-confirmation           "此联络人将从侬额联络人里厢做特"
   :remove-from-group                     "从群组里厢移除"
   :edit-contacts                         "编辑联络人"
   :search-contacts                       "搜寻联络人"
   :contacts-group-new-chat               "开始新额聊天"
   :choose-from-contacts                  "从联络人里厢选择"
   :no-contacts                           "暂么联系人"
   :show-qr                               "显示二维码"
   :enter-address                         "輸入地址"
   :more                                  "更多"

   ;;group-settings
   :remove                                "移动"
   :save                                  "保存"
   :clear-history                         "清除历史"
   :delete                                "刪除特"
   :mute-notifications                    "静音通知"
   :leave-chat                            "离开对话"
   :chat-settings                         "聊天设置"
   :edit                                  "编辑"
   :add-members                           "添加会员"

   ;;commands
   :chat-send-eth                         "{{amount}}只ETH"

   ;;location command
   :your-current-location                 "侬当前额位置"
   :places-nearby                         "附近额地方"
   :search-results                        "搜索结果"
   :dropped-pin                           "插只大头钉"
   :location                              "位置"
   :open-map                              "打开地图"
   :sharing-copy-to-clipboard-address     "拷贝地址"
   :sharing-copy-to-clipboard-coordinates "拷贝坐标"

   ;;new-group
   :new-group                             "新增群组"
   :reorder-groups                        "重新排序群组"
   :edit-group                            "编辑群组"
   :delete-group                          "删特群组"
   :delete-group-confirmation             "将从侬额群组里厢移特格只群聊。该操作勿会影响侬额联络宁"
   :delete-group-prompt                   "格勿会影响侬联络宁"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}

   ;;protocol
   :received-invitation                   "接受聊天邀请"
   :removed-from-chat                     "将侬从群聊里厢移除特"
   :left                                  "离开"
   :invited                               "已邀请"
   :removed                               "移出特了"
   :You                                   "侬"

   ;;new-contact
   :add-new-contact                       "增加新额联系人"
   :scan-qr                               "扫描二维码"
   :name                                  "名称"
   :address-explication                   "侬额公钥将用于生成你在Ethereum上的地址——由一串数字和字母构成。侬可以很容易在你的个人资料里寻到伊"
   :enter-valid-public-key                "请输入有效额公钥或者扫描QR码"
   :contact-already-added                 "联系人已被添加好叻"
   :can-not-add-yourself                  "勿能添加侬自家额"
   :unknown-address                       "伐晓得额地址"

   ;;login
   :connect                               "连接"
   :address                               "地址"
   :password                              "密码"
   :sign-in-to-status                     "登录Status"
   :sign-in                               "登录"
   :wrong-password                        "密码伐对额"
   :enter-password                        "输入密码"

   ;;recover
   :passphrase                            "口令短语passphrase"
   :recover                               "恢复"
   :twelve-words-in-correct-order         "正确排序12只字"

   ;;accounts
   :recover-access                        "恢复访问"
   :create-new-account                    "建立新帐户"

   ;;wallet-qr-code
   :done                                  "完成"

   ;;validation
   :invalid-phone                         "无效额电话号头"
   :amount                                "数量"

   ;;transactions
   :confirm                               "确认"
   :transaction                           "交易"
   :unsigned-transaction-expired          "么签名额交易过期叻"
   :status                                "状态"
   :recipient                             "收件宁"
   :to                                    "到"
   :from                                  "从"
   :data                                  "数据"
   :got-it                                "拿到了"
   :block                                 "块"
   :hash                                  "哈希"
   :gas-limit                             "Gas限制"
   :gas-price                             "Gas价地"
   :gas-used                              "用特额Gas"
   :cost-fee                              "价地/费用"
   :nonce                                 "Nonce"
   :confirmations                         "确认"
   :confirmations-helper-text             "请等待至少12只确认，以确保侬额交易安全完成"
   :copy-transaction-hash                 "拷贝交易哈希"
   :open-on-etherscan                     "打开 Etherscan.io"
   :incoming                              "收入"
   :outgoing                              "花特额"
   :pending                               "等待中额"
   :postponed                             "延迟交易"

   ;;webview
   :web-view-error                        "要系，错特了"

   ;;testfairy warning
   :testfairy-title                       "出事体了!"
   :testfairy-message                     "侬正在使用一只每日构建版本。格只版本出于测试目的会在使用wifi连接额辰光记录session。所以侬所有跟格只App额互动都会以视频或日志形式被保存，被阿拉开发团队用于调查可能的故障。勿用担心，保存的视频或日志中勿会包含侬额密码。只有每日构建版本会记录session。从PlayStore或者TestFlight安装的版本则勿会记录。"

   ;; wallet
   :wallet                                "皮夹子"
   :wallets                               "皮夹子"
   :your-wallets                          "侬额皮夹子"
   :main-wallet                           "主要皮夹子"
   :wallet-error                          "错额载入数据"
   :wallet-send                           "发送"
   :wallet-send-token                     "发送 {{symbol}}"
   :wallet-request                        "请求"
   :wallet-exchange                       "交换"
   :wallet-assets                         "资产"
   :wallet-add-asset                      "增加资产"
   :wallet-total-value                    "总值"
   :wallet-settings                       "皮夹子配置"
   :wallet-manage-assets                  "管理资产"
   :signing-phrase-description            "输入侬额密码来签发格趟交易。请确保下面的文本匹配你的秘密登录短语signing phrase"
   :wallet-insufficient-funds             "侬剩额铜钿不足"
   :wallet-my-token                       "吾额{{symbol}}"
   :wallet-market-value                   "市值"
   :request-transaction                   "请求交易"
   :send-request                          "发送请求"
   :share                                 "股份"
   :eth                                   "ETH"
   :currency                              "币种"
   :usd-currency                          "美元铜钿"
   :transactions                          "交易"
   :transaction-details                   "交易细节"
   :transaction-failed                    "交易失败"
   :transactions-sign                     "签名"
   :transactions-sign-all                 "签名全部"
   :transactions-sign-transaction         "签名交易"
   :transactions-sign-later               "等特歇再签名"
   :transactions-delete                   "删特交易"
   :transactions-delete-content           "交易会从 '未签名' 列表里被删特"
   :transactions-history                  "历史"
   :transactions-unsigned                 "么签名"
   :transactions-history-empty            "侬额历史里还么交易"
   :transactions-unsigned-empty           "侬还么任何么签名额交易"
   :transactions-filter-title             "过滤历史"
   :transactions-filter-tokens            "代币Tokens"
   :transactions-filter-type              "类型"
   :transactions-filter-select-all        "全选"
   :view-transaction-details              "查看所有交易细节"
   :transaction-description               "请等待至少12只确认，以确保侬额交易安全完成"
   :transaction-sent                      "交易发送"
   :transaction-moved-text                "格只交易会在'么签名'列表里再显示5分钟"
   :transaction-moved-title               "交易已移走"
   :sign-later-title                      "等歇再签名交易?"
   :sign-later-text                       "看看历史后再签名交易"
   :not-applicable                        "不能用于么签名额交易"

   ;; Wallet Send
   :wallet-choose-recipient               "选择收件人"
   :wallet-choose-from-contacts           "从联系人中选择"
   :wallet-address-from-clipboard         "使用剪贴板里的地址"
   :wallet-invalid-address                "无效地址: \n {{data}}"
   :wallet-invalid-chain-id               "网络不适配: \n {{data}}"
   :wallet-browse-photos                  "浏览照片"
   :validation-amount-invalid-number      "金额不是有效的数字"
   :validation-amount-is-too-precise      "金额特小辣。你最小能发送的单位是1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新网络"
   :add-network                           "添加网络"
   :add-new-network                       "添加新网络"
   :existing-networks                     "已有网络"
   :add-json-file                         "添加一只JSON文件"
   :paste-json-as-text                    "把JSON粘贴为文本"
   :paste-json                            "粘贴JSON"
   :specify-rpc-url                       "明确一只RPC URL"
   :edit-network-config                   "编辑网络配置"
   :connected                             "已连接"
   :process-json                          "处理JSON"
   :error-processing-json                 "JSON处理错特辣"
   :rpc-url                               "RPC URL"
   :remove-network                        "删特网络"
   :network-settings                      "网络设置"
   :edit-network-warning                  "当心！编辑格只网络可能让伊不可用"
   :connecting-requires-login             "连接到的网络需要登录"
   :close-app-title                       "当心!"
   :close-app-content                     "格只应用会停止并关闭.当侬重新打开伊额辰光，格只选中额网络将会被应用"
   :close-app-button                      "确认"})

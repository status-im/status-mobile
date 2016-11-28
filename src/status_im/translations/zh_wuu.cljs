(ns status-im.translations.zh-wuu)

(def translations
  {
   ;common
   :members-title                         "会员"
   :not-implemented                       "!未实现"
   :chat-name                             "聊天名称"
   :notifications-title                   "通知和声音"
   :offline                               "离线"

   ;drawer
   :invite-friends                        "邀请朋友"
   :faq                                   "常见问题"
   :switch-users                          "切换用户"

   ;chat
   :is-typing                             "正在输入"
   :and-you                               "和您"
   :search-chat                           "搜索聊天"
   :members                               {:one   "1人"
                                           :other "{{count}}会员"
                                           :zero  "无会员"}
   :members-active                        {:one   "1个会员，1个活跃成员"
                                           :other "{{count}} 会员, {{count}} 活跃成员"
                                           :zero  "无会员"}
   :active-online                         "在线"
   :active-unknown                        "未知"
   :available                             "可用"
   :no-messages                           "无信息"
   :suggestions-requests                  "请求"
   :suggestions-commands                  "命令"

   ;sync
   :sync-in-progress                      "同步中..."
   :sync-synced                           "同步"

   ;messages
   :status-sending                        "发送中"
   :status-pending                        "待定"
   :status-sent                           "已发送"
   :status-seen-by-everyone               "每个人已阅"
   :status-seen                           "已阅"
   :status-delivered                      "已发送"
   :status-failed                         "发送失败"

   ;datetime
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分钟"
                                           :other "分钟"}
   :datetime-hour                         {:one   "小时"
                                           :other "小时"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-multiple                     "s"
   :datetime-ago                          "之前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "个人资料"
   :report-user                           "报告用户"
   :message                               "消息"
   :username                              "用户名"
   :not-specified                         "未指定"
   :public-key                            "公共密钥"
   :phone-number                          "电话号码"
   :email                                 "电子邮箱"
   :profile-no-status                     "无状态"
   :add-to-contacts                       "添加至通讯录"
   :error-incorrect-name                  "请选择另一个名字"
   :error-incorrect-email                 "不正确的电子邮箱"

   ;;make_photo
   :image-source-title                    "个人资料照片"
   :image-source-make-photo               "截图"
   :image-source-gallery                  "从相册中选择"
   :image-source-cancel                   "取消"

   ;sign-up
   :contacts-syncronized                  "您的联系人已同步"
   :confirmation-code                     (str "谢谢！我们已经给您发了一个确认短信"
                                               "代码。请提供该代码以确认您的电话号码")
   :incorrect-code                        (str "对不起，代码不正确，请再输入一次")
   :generate-passphrase                   (str "我会为您生成一个口令短语，您可以恢复您的 "
                                               "从另一个设备访问或登录")
   :phew-here-is-your-passphrase          "*唷* 是很难的，这是您的口令短语，*写下来并保证安全！* 您将需要它来恢复您的帐户。"
   :here-is-your-passphrase               "这是您的口令短语，*写下来并保证安全！* 您将需要它来恢复您的帐户。"
   :written-down                          "确保您已经安全地写下来"
   :phone-number-required                 "点击这里进入您的电话号码，我会找到您的朋友"
   :intro-status                          "与我聊天设置您的帐户，并更改您的设置！"
   :intro-message1                        "欢迎来到\n点击这一消息设置您的密码并开始!"
   :account-generation-message            "给我一秒，我要做一些疯狂的计算生成您的帐号!"

   ;chats
   :chats                                 "聊天"
   :new-chat                              "新的聊天"
   :new-group-chat                        "新的群聊"

   ;discover
   :discover                             "发现"
   :none                                  "无"
   :search-tags                           "在这里键入您的搜索标签"
   :popular-tags                          "热门标签"
   :recent                                "最近"
   :no-statuses-discovered                "未发现状态"

   ;settings
   :settings                              "设置"

   ;contacts
   :contacts                              "联系人"
   :new-contact                           "新的联系人"
   :show-all                              "显示全部"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "人"
   :contacts-group-new-chat               "开始新的聊天"
   :no-contacts                           "暂无联系人"
   :show-qr                               "显示QR"

   ;group-settings
   :remove                                "移动"
   :save                                  "保存"
   :change-color                          "改变颜色"
   :clear-history                         "清除历史"
   :delete-and-leave                      "删除并离开"
   :chat-settings                         "聊天设置"
   :edit                                  "编辑"
   :add-members                           "添加会员"
   :blue                                  "蓝色"
   :purple                                "紫色"
   :green                                 "绿色"
   :red                                   "红色"

   ;commands
   :money-command-description             "发送钱"
   :location-command-description          "发送地址"
   :phone-command-description             "发送电话号码"
   :phone-request-text                    "请求电话号码"
   :confirmation-code-command-description "发送确认码"
   :confirmation-code-request-text        "请求确认码"
   :send-command-description              "发送地址"
   :request-command-description           "发送请求"
   :keypair-password-command-description  ""
   :help-command-description              "帮助"
   :request                               "请求"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH至 {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH来自 {{chat-name}}"
   :command-text-location                 "地址: {{address}}"
   :command-text-browse                   "浏览网页: {{webpage}}"
   :command-text-send                     "交易: {{amount}} ETH"
   :command-text-help                     "帮助"

   ;new-group
   :group-chat-name                       "聊天名称"
   :empty-group-chat-name                 "请输入一个名称"
   :illegal-group-chat-name               "选择另一个名称"

   ;participants
   :add-participants                      "添加参与者"
   :remove-participants                   "删除参与者"

   ;protocol
   :received-invitation                   "接受聊天邀请"
   :removed-from-chat                     "将您从群聊中移除"
   :left                                  "离开"
   :invited                               "已邀请"
   :removed                               "已移除"
   :You                                   "您"

   ;new-contact
   :add-new-contact                       "增加新的联系人"
   :import-qr                             "导入"
   :scan-qr                               "扫描QR"
   :name                                  "名称"
   :whisper-identity                      "耳语身份"
   :address-explication                   "也许这应该有一个文本，解释地址是什么以及在哪里寻找它"
   :enter-valid-address                   "请输入一个有效的地址或扫描QR码"
   :contact-already-added                 "联系人已被添加"
   :can-not-add-yourself                  "您不能添加您自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "连接"
   :address                               "地址"
   :password                              "密码"
   :login                                 "登录"
   :wrong-password                        "错误的密码"

   ;recover
   :recover-from-passphrase               "恢复口令短语"
   :recover-explain                       "请输入密码的口令短语来恢复访问"
   :passphrase                            "口令短语"
   :recover                               "恢复"
   :enter-valid-passphrase                "请输入口令短语"
   :enter-valid-password                  "请输入密码"

   ;accounts
   :recover-access                        "恢复访问"
   :add-account                           "添加账户"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主要钱包"

   ;validation
   :invalid-phone                         "无效的电话号码"
   :amount                                "金额"
   :not-enough-eth                        (str "ETH余额不足"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "确认交易"
                                           :other "确认{{count}} 交易"
                                           :zero  "无交易"}
   :status                                "状态"
   :pending-confirmation                  "待确认"
   :recipient                             "收件人"
   :one-more-item                         "一个以上项目"
   :fee                                   "金额"
   :value                                 "价值"

   ;:webview
   :web-view-error                        "啊哦，错误"})

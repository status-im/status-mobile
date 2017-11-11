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
   :type-a-message                        "输入讯息..."
   :type-a-command                        "开始输入指令..."
   :error                                 "错误"

   :camera-access-error                   "要授予所需的摄像机许可，请转到系统设置，并确定选择了“状态”>“摄像机”。"
   :photos-access-error                   "要授予所需的照片许可，请转到系统设置，并确保选择“状态“>“照片“。"

   ;drawer
   :invite-friends                        "邀请朋友"
   :faq                                   "常问问题解答"
   :switch-users                          "切换用户"
   :feedback                              "有反馈？\n摇动你的手机！"
   :view-all                              "查看所有"
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
   :status-sending                        "正在发送"
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
   :datetime-multiple                     "s"
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨天"
   :datetime-today                        "今天"

   ;profile
   :profile                               "个人资料"
   :edit-profile                          "编辑个人资料"
   :report-user                           "举报用户"
   :message                               "信息"
   :username                              "用户名"
   :not-specified                         "未指定"
   :public-key                            "公钥"
   :phone-number                          "电话号码"
   :email                                 "电子邮件"
   :update-status                         "更新你的状态..."
   :add-a-status                          "添加状态..."
   :status-prompt                         "建立一个状态以帮助人们了解您提供的内容。 你也可以使用#hashtags。"
   :add-to-contacts                       "添加到联系人"
   :in-contacts                           "在联系人"
   :remove-from-contacts                  "从联系人中删除"
   :start-conversation                    "开始对话"
   :send-transaction                      "发送交易"
   :share-qr                              "分享QR码"
   :error-incorrect-name                  "请选择其它名称"
   :error-incorrect-email                 "电子邮件不正确"

   ;;make_photo
   :image-source-title                    "个人资料图片"
   :image-source-make-photo               "拍摄"
   :image-source-gallery                  "从图库中选择"
   :image-source-cancel                   "取消"

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
   :generate-passphrase                   (str "我会为你生成一个密码短语，以便你恢复自己的密码 "
                                               "从另一台设备访问或登录")
   :phew-here-is-your-passphrase          "*唷* 真不容易，这是你的密码短语，*写下来，好好保管它！*你需要用它来恢复你的帐户。"
   :here-is-your-passphrase               "这是你的密码短语，*写下来，好好保管它！*你需要用它来恢复你的帐户。"
   :written-down                          "确保你把它安全地写下来"
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
   :new-chat                              "新的聊天"
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
   :no-statuses-discovered                "未发现状态"
   :no-statuses-found                     "找不到状态"

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
   :show-all                              "显示所有"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "联系人"
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
   :change-color                          "更改颜色"
   :clear-history                         "清除历史记录"
   :delete-and-leave                      "删除并退出"
   :mute-notifications                    "静音通知"
   :leave-chat                            "离开对话"
   :chat-settings                         "聊天设置"
   :edit                                  "编辑"
   :add-members                           "添加成员"
   :blue                                  "蓝色"
   :purple                                "紫色"
   :green                                 "绿色"
   :red                                   "红色"

   ;commands
   :money-command-description             "汇款"
   :location-command-description          "发送位置"
   :phone-command-description             "发送电话号码"
   :phone-request-text                    "电话号码请求"
   :confirmation-code-command-description "发送确认码"
   :confirmation-code-request-text        "确认码请求"
   :send-command-description              "发送位置"
   :request-command-description           "发送请求"
   :keypair-password-command-description  ""
   :help-command-description              "帮助"
   :request                               "请求"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "给{{chat-name}}的{{amount}} ETH"
   :chat-send-eth-from                    "来自{{chat-name}}的{{amount}} ETH"

   ;new-group
   :group-chat-name                       "聊天名称"
   :empty-group-chat-name                 "请输入名称"
   :illegal-group-chat-name               "请选择其它名称"
   :new-group                             "新增群組"
   :reorder-groups                        "重新排序群組"
   :group-name                            "群組名稱"
   :edit-group                            "編輯群組"
   :delete-group                          "刪除群組"
   :delete-group-confirmation             "此群組將從你的群組中移除。 這不會影響聯絡人"
   :delete-group-prompt                   "這不會影響聯絡人"
   :group-members                         "群組成員"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}
   ;participants
   :add-participants                      "添加参与者"
   :remove-participants                   "删除参与者"

   ;protocol
   :received-invitation                   "收到了聊天邀请"
   :removed-from-chat                     "已将你从群聊中删除"
   :left                                  "剩余"
   :invited                               "已邀请"
   :removed                               "已删除"
   :You                                   "你"

   ;new-contact
   :add-new-contact                       "添加新的联系人"
   :import-qr                             "导入"
   :scan-qr                               "扫描二维码"
   :swow-qr                               "顯示QR碼"
   :name                                  "名称"
   :whisper-identity                      "Whisper身份"
   :address-explication                   "也许这里应该有一些文本来解释什么是地址，以及在哪里查找它"
   :enter-valid-address                   "请输入有效地址或扫描二维码"
   :enter-valid-public-key                "请输入有效公钥或扫描QR码"
   :contact-already-added                 "已添加该联系人"
   :can-not-add-yourself                  "不能添加自己"
   :unknown-address                       "未知地址"


   ;login
   :connect                               "连接"
   :address                               "地址"
   :password                              "密码"
   :login                                 "登录"
   :sign-in-to-status                     "登录Status"
   :sign-in                               "登录"
   :wrong-password                        "密码错误"

   ;recover
   :recover-from-passphrase               "从密码短语恢复"
   :recover-explain                       "请输入密码短语，以便使密码恢复访问"
   :passphrase                            "密码短语"
   :recover                               "恢复"
   :enter-valid-passphrase                "请输入密码短语"
   :enter-valid-password                  "请输入密码"
   :twelve-words-in-correct-order         "正确排序12个字"

   ;accounts
   :recover-access                        "恢复访问"
   :add-account                           "添加帐户"
   :create-new-account                    "建立新帐户"

   ;wallet-qr-code
   :done                                  "完成"
   :main-wallet                           "主钱包"

   ;validation
   :invalid-phone                         "电话号码无效"
   :amount                                "金额"
   :not-enough-eth                        (str "ETH余额不足"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "确认"
   :confirm-transactions                  {:one   "确认交易"
                                           :other "确认{{count}}笔交易"
                                           :zero  "无交易"}
   :transactions-confirmed                {:one   "已确认交易"
                                           :other "已确认 {{count}} 笔交易"
                                           :zero  "无确认交易"}
   :transaction                           "交易"
   :unsigned-transactions                 "未确认交易"
   :no-unsigned-transactions              "无未确认交易"
   :enter-password-transactions           {:one   "输入密码以确认交易"
                                           :other "输入密码以确认交易"}
   :status                                "状态"
   :pending-confirmation                  "待确认"
   :recipient                             "接受方"
   :one-more-item                         "另一项"
   :fee                                   "费用"
   :estimated-fee                         "预算费用"
   :value                                 "值"
   :to                                    "至"
   :from                                  "从"
   :data                                  "数据"
   :got-it                                "得到了"
   :contract-creation                     "创建合同"

   ;:webview
   :web-view-error                        "糟糕，出错了"})
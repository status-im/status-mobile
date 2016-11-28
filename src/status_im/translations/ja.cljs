(ns status-im.translations.ja)

(def translations
  {
   ;common
   :members-title                         "メンバー"
   :not-implemented                       "！実装されていません"
   :chat-name                             "チャット名"
   :notifications-title                   "通知と音声"
   :offline                               "オフライン"

   ;drawer
   :invite-friends                        "友だちを招待"
   :faq                                   "よくある質問"
   :switch-users                          "ユーザーを切り替え"

   ;chat
   :is-typing                             "タイプ中です"
   :and-you                               "そしてあなた"
   :search-chat                           "チャットを検索"
   :members                               {:one   "1人のメンバー"
                                           :other "{{count}}人のメンバー"
                                           :zero  "メンバーがいません"}
   :members-active                        {:one   "1人のメンバー、1人がアクティブ"
                                           :other "{{count}}人のメンバー、{{count}}人がアクティブ"
                                           :zero  "メンバーがいません"}
   :active-online                         "オンライン"
   :active-unknown                        "不明"
   :available                             "利用可能"
   :no-messages                           "メッセージがありません"
   :suggestions-requests                  "リクエスト"
   :suggestions-commands                  "コマンド"

   ;sync
   :sync-in-progress                      "同期中..."
   :sync-synced                           "同期しています"

   ;messages
   :status-sending                        "送信中"
   :status-pending                        "保留中"
   :status-sent                           "送信済み"
   :status-seen-by-everyone               "全員が閲覧"
   :status-seen                           "閲覧"
   :status-delivered                      "配達済み"
   :status-failed                         "失敗しました"

   ;datetime
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分"
                                           :other "分"}
   :datetime-hour                         {:one   "時間"
                                           :other "時間"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-multiple                     "秒"
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨日"
   :datetime-today                        "今日"

   ;profile
   :profile                               "プロフィール"
   :report-user                           "ユーザーを通報"
   :message                               "メッセージ"
   :username                              "ユーザー名"
   :not-specified                         "特定されていません"
   :public-key                            "公開鍵"
   :phone-number                          "電話番号"
   :email                                 "メールアドレス"
   :profile-no-status                     "ステータスがありません"
   :add-to-contacts                       "連絡先に追加"
   :error-incorrect-name                  "別の名前を選択してください"
   :error-incorrect-email                 "間違ったメールアドレス"

   ;;make_photo
   :image-source-title                    "プロフィール画像"
   :image-source-make-photo               "キャプチャ"
   :image-source-gallery                  "ギャラリーから選択"
   :image-source-cancel                   "キャンセル"

   ;sign-up
   :contacts-syncronized                  "連絡先が同期されました"
   :confirmation-code                     (str "ありがとうございます！ 確認コードが記載されたメッセージが"
                                               "送信されました。電話番号を確認するにはそのコードを入力してください")
   :incorrect-code                        (str "申し訳ありませんがコードが間違っています。もう一度入力してください")
   :generate-passphrase                   (str "パスフレーズを生成してアクセスを復元したり、"
                                               "別のデバイスからログインしたりすることができます")
   :phew-here-is-your-passphrase          "*お疲れ様でした。*これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります。"
   :here-is-your-passphrase               "これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります。"
   :written-down                          "書き留めて安全な場所に保管してください。"
   :phone-number-required                 "ここをタップして電話番号を入力するとお友達を検索します。"
   :intro-status                          "チャットしてアカウントを設定し、設定を変更してください！"
   :intro-message1                        "ステータスにようこそ\nこのメッセージをタップしてパスワードを設定して開始しましょう！"
   :account-generation-message            "少々お待ちください。アカウントを生成するには複雑な計算が必要です！"

   ;chats
   :chats                                 "チャット"
   :new-chat                              "新規チャット"
   :new-group-chat                        "新規グループチャット"

   ;discover
   :discover                             "発見"
   :none                                  "なし"
   :search-tags                           "ここに検索タグを入力してください"
   :popular-tags                          "人気のタグ"
   :recent                                "最近"
   :no-statuses-discovered                "ステータスが見つかりませんでした"

   ;settings
   :settings                              "設定"

   ;contacts
   :contacts                              "連絡先"
   :new-contact                           "新規連絡先"
   :show-all                              "全て表示"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "人々"
   :contacts-group-new-chat               "新規チャットを開始"
   :no-contacts                           "まだ連絡先がありません"
   :show-qr                               "QRを表示"

   ;group-settings
   :remove                                "削除"
   :save                                  "保存"
   :change-color                          "色を変更"
   :clear-history                         "履歴を消去"
   :delete-and-leave                      "削除して閉じる"
   :chat-settings                         "チャット設定"
   :edit                                  "編集"
   :add-members                           "メンバーを追加"
   :blue                                  "ブルー"
   :purple                                "パープル"
   :green                                 "グリーン"
   :red                                   "レッド"

   ;commands
   :money-command-description             "送金"
   :location-command-description          "位置を送信"
   :phone-command-description             "電話番号を送信"
   :phone-request-text                    "電話番号をリクエスト"
   :confirmation-code-command-description "確認コードを送信"
   :confirmation-code-request-text        "確認コードリクエスト"
   :send-command-description              "位置を送信"
   :request-command-description           "リクエストを送信"
   :keypair-password-command-description  ""
   :help-command-description              "ヘルプ"
   :request                               "リクエスト"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETHを{{chat-name}}に"
   :chat-send-eth-from                    "{{amount}} ETHを{{chat-name}}から"
   :command-text-location                 "位置: {{address}}"
   :command-text-browse                   "ウェブページ閲覧: {{webpage}}"
   :command-text-send                     "トランザクション: {{amount}} ETH"
   :command-text-help                     "ヘルプ"

   ;new-group
   :group-chat-name                       "チャット名"
   :empty-group-chat-name                 "名前を入力してください"
   :illegal-group-chat-name               "別の名前を選択してください"

   ;participants
   :add-participants                      "参加者を追加"
   :remove-participants                   "参加者を削除"

   ;protocol
   :received-invitation                   "チャット招待状を受信しました"
   :removed-from-chat                     "グループチャットから削除されました"
   :left                                  "残り"
   :invited                               "招待済み"
   :removed                               "削除済み"
   :You                                   "あなた"

   ;new-contact
   :add-new-contact                       "新規連絡先を追加"
   :import-qr                             "インポート"
   :scan-qr                               "QRをスキャン"
   :name                                  "名前"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "ここにアドレスについての説明やどこで見つけられるのかを入力してください"
   :enter-valid-address                   "有効なアドレスを入力するかQRコードをスキャンしてください"
   :contact-already-added                 "連絡先はすでに追加されています"
   :can-not-add-yourself                  "自分自身を追加することはできません"
   :unknown-address                       "不明のアドレス"


   ;login
   :connect                               "接続"
   :address                               "アドレス"
   :password                              "パスワード"
   :login                                 "ログイン"
   :wrong-password                        "パスワードが間違っています"

   ;recover
   :recover-from-passphrase               "パスフレーズから復元"
   :recover-explain                       "パスワードのためにパスフレーズを入力してアクセスを復元してください"
   :passphrase                            "パスフレーズ"
   :recover                               "復元"
   :enter-valid-passphrase                "パスフレーズを入力してください"
   :enter-valid-password                  "パスワードを入力してください"

   ;accounts
   :recover-access                        "アクセスを復元"
   :add-account                           "アカウントを追加"

   ;wallet-qr-code
   :done                                  "完了"
   :main-wallet                           "主要のウォレット"

   ;validation
   :invalid-phone                         "無効な電話番号"
   :amount                                "金額"
   :not-enough-eth                        (str "十分なETH残高がありません"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "トランザクションを確認"
                                           :other "{{count}}トランザクションを確認"
                                           :zero  "トランザクションがありません"}
   :status                                "ステータス"
   :pending-confirmation                  "確認が保留中です"
   :recipient                             "受信者"
   :one-more-item                         "さらに一つの項目"
   :fee                                   "手数料"
   :value                                 "値"

   ;:webview
   :web-view-error                        "おっと、エラーが発生しました。"})

(ns status-im.translations.ja)

(def translations
  {
   ;common
   :members-title                         "メンバー"
   :not-implemented                       "実装されていません"
   :chat-name                             "チャット名"
   :notifications-title                   "通知と音声"
   :offline                               "オフライン"
   :search-for                            "検索..."
   :cancel                                "キャンセル"
   :next                                  "次へ"
   :type-a-message                        "メッセージを入力する..."
   :type-a-command                        "コマンドを入力する..."
   :error                                 "エラー"

   :camera-access-error                   "カメラを利用できません。設定よりカメラの使用を許可してください。"
   :photos-access-error                   "写真を取得できません。設定より写真へのアクセスを許可してください。"

   ;drawer
   :switch-users                          "ユーザーの切り替え"
   :current-network                       "現在のネットワーク"

   ;chat
   :is-typing                             "が入力中"
   :and-you                               "とあなた"
   :search-chat                           "チャットを検索"
   :members                               {:one   "1人のメンバー"
                                           :other "{{count}}人のメンバー"
                                           :zero  "メンバーがいません"}
   :members-active                        {:one   "1人がアクティブ"
                                           :other "{{count}}人がアクティブ"
                                           :zero  "アクティブなメンバーはいません"}
   :public-group-status                   "公開"
   :active-online                         "オンライン"
   :active-unknown                        "不明"
   :available                             "利用可能"
   :no-messages                           "メッセージがありません"
   :suggestions-requests                  "リクエスト"
   :suggestions-commands                  "コマンド"
   :faucet-success                        "フォーセットリクエストを受け取りました"
   :faucet-error                          "フォーセットリクエストエラー"

   ;sync
   :sync-in-progress                      "同期中..."
   :sync-synced                           "同期しました"

   ;messages
   :status-sending                        "送信中"
   :status-pending                        "保留中"
   :status-sent                           "送信済み"
   :status-seen-by-everyone               "全員が閲覧"
   :status-seen                           "閲覧"
   :status-delivered                      "配達済み"
   :status-failed                         "失敗しました"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分"
                                           :other "分"}
   :datetime-hour                         {:one   "時間"
                                           :other "時間"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨日"
   :datetime-today                        "今日"

   ;profile
   :profile                               "プロフィール"
   :edit-profile                          "プロフィールを編集"
   :message                               "メッセージ"
   :not-specified                         "特定されていません"
   :public-key                            "公開鍵"
   :phone-number                          "電話番号"
   :update-status                         "ステータスを更新..."
   :add-a-status                          "ステータスを追加..."
   :status-prompt                         "あなたのオファーを知らせるためにステータスを作成してください。#hashtagsも使用できます。"
   :add-to-contacts                       "連絡先に追加"
   :in-contacts                           "連絡先"
   :remove-from-contacts                  "連絡先から削除"
   :start-conversation                    "会話を始める"
   :send-transaction                      "トランザクションを送信"

   ;;make_photo
   :image-source-title                    "プロフィール画像"
   :image-source-make-photo               "カメラを起動"
   :image-source-gallery                  "ギャラリーから選択"

   ;;sharing
   :sharing-copy-to-clipboard             "クリップボードにコピー"
   :sharing-share                         "共有..."
   :sharing-cancel                        "キャンセル"

   :browsing-title                        "閲覧"
   :browsing-open-in-web-browser          "ブラウザーで閲覧"
   :browsing-cancel                       "キャンセル"

   ;sign-up
   :contacts-syncronized                  "連絡先が同期されました"
   :confirmation-code                     (str "ありがとうございます！ 確認コードが記載されたメッセージが"
                                               "送信されました。電話番号を確認するにはそのコードを入力してください")
   :incorrect-code                        (str "申し訳ありません。コードが間違っています。もう一度入力してください")
   :phew-here-is-your-passphrase          "*お疲れ様でした。*これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります。"
   :here-is-your-passphrase               "これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります。"
   :phone-number-required                 "ここをタップして電話番号を入力するとお友達を検索します。"
   :shake-your-phone                      "問題点や改善点を報告する場合は、スマホを振って下さい！"
   :intro-status                          "チャットしてアカウントを設定し、設定を変更してください！"
   :intro-message1                        "ステータスにようこそ\nこのメッセージをタップして、パスワードを設定し開始しましょう！"
   :account-generation-message            "少々お待ちください。アカウントの生成するにはお時間がかかります。"
   :move-to-internal-failure-message      "いくつかの重要なファイルを外部メディアに保存します。続行するには、許可が必要です。（将来的には外部メディアを使用しないようにします。）"
   :debug-enabled                         "デバッグサーバーを起動しました！ *status-dev-cli scan*を実行すると、同じネットワーク上のコンピュータからサーバーを見つけることができます。"

   ;phone types
   :phone-e164                            "国際 1"
   :phone-international                   "国際 2"
   :phone-national                        "国内"
   :phone-significant                     "Significant"

   ;chats
   :chats                                 "チャット"
   :delete-chat                           "チャットを削除"
   :new-group-chat                        "新規グループチャット"
   :new-public-group-chat                 "公開チャットに参加"
   :edit-chats                            "チャットを偏執"
   :search-chats                          "チャットを検索"
   :empty-topic                           "空のトピック"
   :topic-format                          "間違った形式 [a-z0-9\\-]+"
   :public-group-topic                    "トピック"

   ;discover
   :discover                              "発見"
   :none                                  "なし"
   :search-tags                           "ここに検索タグを入力してください"
   :popular-tags                          "人気のタグ"
   :recent                                "最近"
   :no-statuses-discovered                "ステータスを発見できませんでした"
   :no-statuses-found                     "ステータスが見つかりませんでした"

   ;settings
   :settings                              "設定"

   ;contacts
   :contacts                              "連絡先"
   :new-contact                           "新規連絡先"
   :delete-contact                        "連絡先を削除"
   :delete-contact-confirmation           "連絡帳よりこの連絡先を削除します"
   :remove-from-group                     "グループから削除"
   :edit-contacts                         "連絡先を偏執"
   :search-contacts                       "連絡先を検索"
   :contacts-group-new-chat               "新規チャットを開始"
   :choose-from-contacts                  "連絡先から選択"
   :no-contacts                           "まだ連絡先がありません"
   :show-qr                               "QRを表示"
   :enter-address                         "アドレスを入力してください"
   :more                                  "もっと"

   ;group-settings
   :remove                                "削除"
   :save                                  "保存"
   :delete                                "削除"
   :clear-history                         "履歴の消去"
   :mute-notifications                    "お知らせをミュート"
   :leave-chat                            "チャットから退出"
   :chat-settings                         "チャット設定"
   :edit                                  "偏執"
   :add-members                           "メンバーを追加"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "新しいグループ"
   :reorder-groups                        "グループを並び替え"
   :edit-group                            "グループを偏執"
   :delete-group                          "グループを削除"
   :delete-group-confirmation             "このグループを削除します。連絡先には影響を与えません。"
   :delete-group-prompt                   "連絡先には影響を与えません。"
   :contact-s                             {:one   "連絡先"
                                           :other "連絡先"}
   ;participants

   ;protocol
   :received-invitation                   "チャット招待状を受信しました"
   :removed-from-chat                     "グループチャットから削除されました"
   :left                                  "残り"
   :invited                               "招待済み"
   :removed                               "削除済み"
   :You                                   "あなた"

   ;new-contact
   :add-new-contact                       "新規連絡先を追加"
   :scan-qr                               "QRをスキャン"
   :name                                  "名前"
   :address-explication                   "ここにアドレスについての説明や、どこで見つけられるのかを入力してください"
   :enter-valid-public-key                "有効な公開鍵を入力するかQRコードをスキャンしてください"
   :contact-already-added                 "連絡先はすでに追加されています"
   :can-not-add-yourself                  "自分自身を追加することはできません"
   :unknown-address                       "不明なアドレス"


   ;login
   :connect                               "接続"
   :address                               "アドレス"
   :password                              "パスワード"
   :sign-in-to-status                     "ステータスにサインイン"
   :sign-in                               "サインイン"
   :wrong-password                        "パスワードが間違っています"

   ;recover
   :passphrase                            "パスフレーズ"
   :recover                               "復元"
   :twelve-words-in-correct-order         "正しい順序で12ワード"

   ;accounts
   :recover-access                        "アクセスを復元"
   :create-new-account                    "新規アカウントを作成"

   ;wallet-qr-code
   :done                                  "完了"
   :main-wallet                           "メインウォレット"

   ;validation
   :invalid-phone                         "無効な電話番号"
   :amount                                "金額"
   ;transactions
   :confirm                               "確認"
   :transaction                           "トランザクション"
   :status                                "ステータス"
   :recipient                             "受信者"
   :to                                    "宛先"
   :from                                  "送信元"
   :data                                  "データ"
   :got-it                                "理解しました"

   ;:webview
   :web-view-error                        "おっと、エラーが発生しました。"})

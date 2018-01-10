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
   :open                                  "開く"
   :description                           "説明"
   :url                                   "URL"
   :type-a-message                        "メッセージを入力する..."
   :type-a-command                        "コマンドを入力する..."
   :error                                 "エラー"
   :unknown-status-go-error               "不明なstatus-goがエラー"
   :node-unavailable                      "実行中のエーテル　ノードがありません"
   :yes                                   "はい"
   :no                                    "いいえ"

   :camera-access-error                   "必要なカメラの許可を与えるには、システム設定に行き、ステータス>カメラが選択されていることを確認してください。"
   :photos-access-error                   "必要な写真の許可を与えるには、あなたのシステム設定に行き、ステータス> 写真が選択されていることを確認してください。"

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
   :status-prompt                         "ステータスを設定します。#hastagsを使用すると、他の人があなたを見つけて、あなたのことをより知るのに役立ちます"
   :add-to-contacts                       "連絡先に追加"
   :in-contacts                           "連絡先"
   :remove-from-contacts                  "連絡先から削除"
   :start-conversation                    "会話を始める"
   :send-transaction                      "トランザクションを送信"
   :testnet-text                          "あなたはテストネットにいる. 実際のETHまたはSNTをあなたの住所に送信しないでください"
   :mainnet-text                          "あなたはメインネットにいる. 実際のETHが送信されます"

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
   :here-is-your-signing-phrase           "あなたの署名フレーズがあります。 あなたはそれを使って取引を確認します。 *書き留めて安全に保管してください！*"
   :phone-number-required                 "ここをタップして電話番号を入力するとお友達を検索します。"
   :shake-your-phone                      "問題点や改善点を発見? スマホを振って下さい！"
   :intro-status                          "チャットしてアカウントを設定し、設定を変更してください！"
   :intro-message1                        "ステータスにようこそ\nこのメッセージをタップして、パスワードを設定し開始しましょう！"
   :account-generation-message            "少々お待ちください。アカウントの生成するにはお時間がかかります。"
   :move-to-internal-failure-message      "いくつかの重要なファイルを外部メディアに保存します。続行するには、許可が必要です。（将来的には外部メディアを使用しないようにします。）"
   :debug-enabled                         "デバッグサーバーを起動しました！ *status-dev-cli scan*を実行すると、同じネットワーク上のコンピュータからサーバーを見つけることができます。"

   ;;phone types
   :phone-e164                            "国際 1"
   :phone-international                   "国際 2"
   :phone-national                        "国内"
   :phone-significant                     "重要"

   ;;chats
   :chats                                 "チャット"
   :delete-chat                           "チャットを削除"
   :new-group-chat                        "新規グループチャット"
   :new-public-group-chat                 "公開チャットに参加"
   :edit-chats                            "チャットを編集"
   :search-chats                          "チャットを検索"
   :empty-topic                           "空のトピック"
   :topic-format                          "間違った形式 [a-z0-9\\-]+"
   :public-group-topic                    "トピック"

   ;;discover
   :discover                              "発見"
   :none                                  "なし"
   :search-tags                           "ここに検索タグを入力してください"
   :popular-tags                          "人気のタグ"
   :recent                                "最近のステータス"
   :no-statuses-found                     "ステータスが見つかりませんでした"
   :chat                                  "チャット"
   :all                                   "全部"
   :public-chats                          "公開チャット"
   :soon                                  "すぐに"
   :public-chat-user-count                "{{count}} 人"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp プロフィール"
   :no-statuses-discovered                "ステータスは見つかりませんでした"
   :no-statuses-discovered-body           "誰かがステータスを投稿すると、ここに表示されます."
   :no-hashtags-discovered-title          "#hashtags が見つかりませんでした"
   :no-hashtags-discovered-body           "#hashtag ハッシュタグが普及すると、ここに表示されます。"
   ;;settings
   :settings                              "設定"

   ;;contacts
   :contacts                              "連絡先"
   :new-contact                           "新規連絡先"
   :delete-contact                        "連絡先を削除"
   :delete-contact-confirmation           "連絡帳よりこの連絡先を削除します"
   :remove-from-group                     "グループから削除"
   :edit-contacts                         "連絡先を編集"
   :search-contacts                       "連絡先を検索"
   :contacts-group-new-chat               "新規チャットを開始"
   :choose-from-contacts                  "連絡先から選択"
   :no-contacts                           "まだ連絡先がありません"
   :show-qr                               "QRを表示"
   :enter-address                         "アドレスを入力してください"
   :more                                  "もっと"

   ;;group-settings
   :remove                                "削除"
   :save                                  "保存"
   :delete                                "削除"
   :clear-history                         "履歴の消去"
   :mute-notifications                    "お知らせをミュート"
   :leave-chat                            "チャットから退出"
   :chat-settings                         "チャット設定"
   :edit                                  "編集"
   :add-members                           "メンバーを追加"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "あなたの現在の場所"
   :places-nearby                         "近くの場所"
   :search-results                        "検索結果"
   :dropped-pin                           "ドロップされたピン"
   :location                              "場所"
   :open-map                              "地図を開く"
   :sharing-copy-to-clipboard-address     "住所をコピーする"
   :sharing-copy-to-clipboard-coordinates "座標をコピーする"

   ;;new-group
   :new-group                             "新しいグループ"
   :reorder-groups                        "グループを並び替え"
   :edit-group                            "グループを編集"
   :delete-group                          "グループを削除"
   :delete-group-confirmation             "このグループを削除します。連絡先には影響を与えません。"
   :delete-group-prompt                   "連絡先には影響を与えません。"
   :contact-s                             {:one   "連絡先"
                                           :other "連絡先"}

   ;;protocol
   :received-invitation                   "チャット招待状を受信しました"
   :removed-from-chat                     "グループチャットから削除されました"
   :left                                  "離れた"
   :invited                               "招待済み"
   :removed                               "削除済み"
   :You                                   "あなた"

   ;;new-contact
   :add-new-contact                       "新規連絡先を追加"
   :scan-qr                               "QRコードをスキャン"
   :name                                  "名前"
   :address-explication                   "あなたの公開鍵は、Ethereum上にあなたのアドレスを生成するために使用され、一連の数字と文字です。 あなたのプロフィールで簡単に見つけることができます"
   :enter-valid-public-key                "有効な公開鍵を入力するかQRコードをスキャンしてください"
   :contact-already-added                 "連絡先は既に追加されています"
   :can-not-add-yourself                  "自分を追加することはできません"
   :unknown-address                       "不明な住所"

   ;;login
   :connect                               "接続"
   :address                               "アドレス"
   :password                              "パスワード"
   :sign-in-to-status                     "ステータスにサインイン"
   :sign-in                               "サインイン"
   :wrong-password                        "パスワードが間違っています"
   :enter-password                        "パスワードを入力する"

   ;;recover
   :passphrase                            "パスフレーズ"
   :recover                               "復元"
   :twelve-words-in-correct-order         "正しい順序で12単語を並ぶ"

   ;;accounts
   :recover-access                        "アクセスを復元"
   :create-new-account                    "新規アカウントを作成"

   ;;wallet-qr-code
   :done                                  "完了"

   ;;validation
   :invalid-phone                         "無効な電話番号"
   :amount                                "金額"

   ;;transactions
   :confirm                               "確認"
   :transaction                           "取引"
   :unsigned-transaction-expired          "署名されていない取引は期限切れです"
   :status                                "ステータス"
   :recipient                             "受信者"
   :to                                    "宛先"
   :from                                  "送信元"
   :data                                  "データ"
   :got-it                                "理解しました"
   :block                                 "ブロック"
   :hash                                  "ハッシュ"
   :gas-limit                             "gas制限"
   :gas-price                             "gas価格"
   :gas-used                              "使用Gas"
   :cost-fee                              "費用/料金"
   :nonce                                 "ノンス"
   :confirmations                         "確認"
   :confirmations-helper-text             "あなたの取引が安全に処理されることを確認するために少なくとも12回の確認を待ってください。"
   :copy-transaction-hash                 "トランザクションハッシュをコピーする"
   :open-on-etherscan                     "Etherscan.ioを開く"
   :incoming                              "着信"
   :outgoing                              "送信"
   :pending                               "未定"
   :postponed                             "延期された"

   ;;webview
   :web-view-error                         "エラーが発生しました"

   ;;testfairy warning
   :testfairy-title                       "警告!"
   :testfairy-message                     "あなたはインストールされたアプリは夜間のバージョンです。 テスト目的のために、このバージョンにWi-Fi接続が使用されている場合は会話記録が含まれています。このアプリとのすべてのやり取りは（動画やログとして）保存され、開発チームが問題の調査に使用する可能性があります。 保存されたビデオ/ログにはパスワードは含まれません。 会話記録は、アプリが夜間のバージョンからインストールされている場合にのみ実行されます。 アプリがPlayStoreまたはTestFlightからインストールされている場合は記録されません。"

   ;; wallet
   :wallet                                "ウォレット"
   :wallets                               "全てのウォレット"
   :your-wallets                          "あなたの全てのウォレット"
   :main-wallet                           "メインウォレット"
   :wallet-error                          "データの読み込みエラー"
   :wallet-send                           "送信"
   :wallet-send-token                     "送信 {{symbol}}"
   :wallet-request                        "要求"
   :wallet-exchange                       "交換"
   :wallet-assets                         "資産"
   :wallet-add-asset                      "資産を追加"
   :wallet-total-value                    "総価値"
   :wallet-settings                       "ウォレット設定"
   :wallet-manage-assets                  "資産の管理"
   :signing-phrase-description            "パスワードを入力して取引に署名します。 上記の単語があなたの秘密の署名句に一致することを確認してください"
   :wallet-insufficient-funds             "残高不足"
   :request-transaction                   "取引をリクエストする"
   :send-request                          "リクエストを送信する"
   :share                                 "シェア"
   :eth                                   "ETH"
   :currency                              "通貨"
   :usd-currency                          "USD"
   :transactions                          "取引"
   :transaction-details                   "取引の詳細"
   :transaction-failed                    "取引が失敗しました"
   :transactions-sign                     "署名"
   :transactions-sign-all                 "全てに署名する"
   :transactions-sign-transaction         "取引に署名する"
   :transactions-sign-later               "後で署名する"
   :transactions-delete                   "取引を削除する"
   :transactions-delete-content           "取引は「未署名」リストから削除されます。"
   :transactions-history                  "取引履歴"
   :transactions-unsigned                 "署名なし"
   :transactions-history-empty            "あなたの履歴にまだ取引はありません"
   :transactions-unsigned-empty           "署名のない取引はありません"
   :transactions-filter-title             "フィルタ履歴"
   :transactions-filter-tokens            "トークン"
   :transactions-filter-type              "タイプ"
   :transactions-filter-select-all        "全て選択"
   :view-transaction-details              "取引の詳細を表示する"
   :transaction-description               "あなたの取引が安全に処理されることを確認するために少なくとも12回の確認を待ってください。"
   :transaction-sent                      "送信された取引"
   :transaction-moved-text                "取引は'未署名'リストに５分間を残ります"
   :transaction-moved-title               "取引の移動タイトル"
   :sign-later-title                      "後で取引に署名する?"
   :sign-later-text                       "この取引に署名するために取引履歴を確認する"
   :not-applicable                        "署名のない取引には適用されません"

   ;; Wallet Send
   :wallet-choose-recipient               "受信者を選択"
   :wallet-choose-from-contacts           "連絡先から選択"
   :wallet-address-from-clipboard         "アドレスをクリップボードから使用する"
   :wallet-invalid-address                "無効なアドレス: \n {{data}}"
   :wallet-invalid-chain-id               "ネットワークが一致しません: \n {{data}} でも、現在のチェーン は {{chain}}"
   :wallet-browse-photos                  "写真を閲覧"
   :wallet-advanced                       "高度な"
   :wallet-transaction-fee                "取引手数料"
   :wallet-transaction-fee-details        "Gas リミットは、取引で送付するgasの量です。 この数を増やしても、取引はより速く処理されません"
   :wallet-transaction-total-fee          "合計料金"
   :validation-amount-invalid-number      "金額は有効な数字ではありません"
   :validation-amount-is-too-precise      "金額が高すぎます。送れる最小単位は1 Wei（1x10 ^ -18 ETH）です。"


   ;; network settings
   :new-network                           "新しいネットワーク"
   :add-network                           "ネットワークを追加する"
   :add-new-network                       "新しいネットワークを追加"
   :add-wnode                             "メールサーバーを追加する"
   :existing-networks                     "既存のネットワーク"
   ;; TODO(dmitryn): come up with better description/naming. Suggested namings: Mailbox and Master Node
   :existing-wnodes                       "既存のメールサーバー"
   :add-json-file                         "JSONファイルを追加する"
   :paste-json-as-text                    "JSONをテキストとして貼り付ける"
   :paste-json                            "JSONを貼り付け"
   :specify-rpc-url                       "RPC URLを指定する"
   :edit-network-config                   "ネットワーク設定を編集する"
   :connected                             "接続済み"
   :process-json                          "JSONを処理"
   :error-processing-json                 "JSONの処理エラー"
   :rpc-url                               "RPC URL"
   :remove-network                        "ネットワークを削除"
   :network-settings                      "ネットワーク設定"
   :offline-messaging-settings            "オフラインメッセージの設定"
   :edit-network-warning                  "注意してください。ネットワークデータを編集すると、このネットワークが無効になることがあります"
   :connecting-requires-login             "別のネットワークに接続するにはログインが必要です"
   :close-app-title                       "警告!"
   :close-app-content                     "アプリは停止して終了します。 これを再び開くと、選択したネットワークが使用されます"
   :close-app-button                      "確認"})

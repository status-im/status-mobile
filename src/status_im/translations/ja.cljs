(ns status-im.translations.ja)

(def translations
  {
   ;;common
   :members-title                       　　　　"メンバー"
   :not-implemented                     　　 "実装されていません"
   :chat-name                           　　 "チャット名"
   :notifications-title                 　　 "通知と音声"
   :offline                               "オフライン"
   :search-for                            "検索..."
   :cancel                                "キャンセル"
   :next                                  "次へ"
   :open                                  "オープン"
   :description                           "記述"
   :url                                   "URL"
   :type-a-message                        "メッセージを入力..."
   :type-a-command                        ""コマンドを入力..."
   :error                                 "エラー"
   :unknown-status-go-error               "不明なステータス-エラー"
   :node-unavailable                      "イサレアムのノードが稼働してません"
   :yes                                   "はい"
   :no                                    "いいえ"

   :camera-access-error                   "カメラを利用できません。システム設定にいきカメラの使用を許可してください"
   :photos-access-error                   "写真にアクセスできません。システム設定より写真へのアクセスを許可してください"

   ;;drawer
   :switch-users                          "ユーザーの切り替え"
   :current-network                       "現在のネットワーク"

   ;;chat
   :is-typing                             "が入力中"
   :and-you                               "とあなた"
   :search-chat                           "チャットを検索"
   :members                               {:one   "1人のメンバー"
                                           :other "{{count}} メンバー"
                                           :zero  "メンバーがいません"}
   :members-active                        {:one   "1人がアクティブ"
                                           :other "{{count}}人がアクティブ"
                                           :zero  "アクティブなメンバーはいません"}
   :public-group-status                   "公開"
   :active-online                         "オンライン"
   :active-unknown                        "不明"
   :available                             "利用可能"
   :no-messages                           "メッセージなし"
   :suggestions-requests                  "リクエスト"
   :suggestions-commands                  "コマンド"
   :faucet-success                        "フォーセットリクエストを受け取りました"
   :faucet-error                          "フォーセットリクエストエラー"

   ;;sync
   :sync-in-progress                      
   :sync-synced                           "同期中"

   ;;messages
   :status-pending                        "同期中..."
   :status-sent                           "送信済み"
   :status-seen-by-everyone               "全員が閲覧済み"
   :status-seen                           "閲覧済み"
   :status-delivered                      "配達済み"
   :status-failed                        　　"失敗"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "秒"
                                           :other "秒"}
   :datetime-minute                       {:one   "分"
                                           :other "分"}
   :datetime-hour                         {:one   "時"
                                           :other "時"}
   :datetime-day                          {:one   "日"
                                           :other "日"}
   :datetime-ago                          "前"
   :datetime-yesterday                    "昨日"
   :datetime-today                        "昨日"
   ;;profile
   :profile                               "プロフィール"
   :edit-profile                          "プロフィールを編集"
   :message                               "メッセージ"
   :not-specified                         "特定されていません"
   :public-key                            "公開鍵"
   :phone-number                          "電話番号"
   :update-status                         "ステータスを更新..."
   :add-a-status                          "ステータスを追加..."
   :status-prompt                         "ステータスを作成してください。#hashtagsを使用すると他人があなたを発見し、あなたの考えを話し合うことできます
   :in-contacts                           "連絡先"
   :remove-from-contacts                  "連絡先から削除"
   :start-conversation                    "会話を始める"
   :send-transaction                      "トランザクションを送信"
   :testnet-text                          "あなたは {{testnet}} のテストネット上にいます. ETHやSNTをあなたのアドレスに送付しないで下さい"
   :mainnet-text                          "あなたはメインのネット上にいます。ETHを送付することができます"
   ;;make_photo
   :image-source-title                    "プロフィール画像"
   :image-source-make-photo               "キャプチャー"
   :image-source-gallery                  "ギャラリーから選択"

   ;;sharing
   :sharing-copy-to-clipboard             "クリップボードにコピー"
   :sharing-share                         "共有..."
   :sharing-cancel                        "キャンセル"

   :browsing-title                        "閲覧"
   :browsing-browse                       "@閲覧"
   :browsing-open-in-web-browser          "ブラウザで閲覧"
   :browsing-cancel                       "キャンセル"

   ;;sign-up
   :contacts-syncronized                  "連絡先が同期されました"
   :confirmation-code                     (str ""ありがとうございます！ 確認コードが記載されたメッセージが送信されました"
                                               "電話番号を確認するためにそのコードを入力してください")
   :incorrect-code                        (str "申し訳ありません。コードが間違っています。もう一度入力してください")
   :phew-here-is-your-passphrase          "お疲れ様でした。*これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります。"
   :here-is-your-passphrase               "*これがパスフレーズです。*書き留めて安全な場所に保管してください！*アカウントの復元に必要になります"
   :here-is-your-signing-phrase           "これがサインインのフレーズです。あなたのトランザクションを確認するのに使用します。*書き留めて安全な場所に保管してください！*"
   :phone-number-required                 "ここをタップして電話番号を入力して下さい。お友達を検索します"
   :shake-your-phone                      "問題点や改善点を報告する場合は、スマホを振って下さい！"
   :intro-status                          "チャットしてアカウントを設定し、設定を変更してください！"
   :intro-message1                        "ステータスにようこそ\nこのメッセージをタップして、パスワードを設定し、スタートしましょう！"
   :account-generation-message            ""少々お待ちください。アカウントの生成するに数学を駆使するので少しお時間がかかります"
   :move-to-internal-failure-message      "いくつかの重要なファイルを外部メディアから内部のメディアに移動します。続行するには、許可が必要です。将来的には外部メディアを使用しないようにします"  
   :debug-enabled                         "デバッグサーバーを起動しました！ *status-dev-cli scan*を実行すると、同じネットワーク上のコンピュータからサーバーを見つけることができます"

   ;;phone types
   :phone-e164                            "国際電話 1"
   :phone-international                   "国際電話 2"
   :phone-national                        "国内電話"
   :phone-significant                     "重要な電話"

   ;;chats
   :chats                                 "チャット"
   :delete-chat                           "新規チャット"
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
   :popular-tags                          "人気の#ハッシュタグ"
   :recent                                "最新のステータス"
   :no-statuses-found                     "ステータスが見つかりませんでした"
   :chat                                  "チャット"
   :all                                   "全員"
   :public-chats                          "公開チャット"
   :soon                                  "間も無く"
   :public-chat-user-count                "{{count}} 人"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp プロフィール"
   :no-statuses-discovered                "ステータスを発見できませんでした"
   :no-statuses-discovered-body           "誰かが\ステータスが見つからないとポストした際、ここでわかります"
   :no-hashtags-discovered-title          "#ハッシュタグが見つかりませんでした"
   :no-hashtags-discovered-body           "#ハッシュタグが\人気のない時、ここでわかります"

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
   :show-qr                               "QRコードを表示"
   :enter-address                         "アドレスを入力してください"
   :more                                  "もっと"

   ;;group-settings
   :remove                                "削除"
   :save                                  "保存"
   :delete                                "削除"
   :clear-history                         "履歴を消去"
   :mute-notifications                    "通知を止める"
   :leave-chat                            "チャットから退出"
   :chat-settings                         "チャットの設定"
   :edit                                  "編集"
   :add-members                           "メンバーを追加"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "あなたの現在位置"
   :places-nearby                         "近くの場所"
   :search-results                        "検索結果"
   :dropped-pin                           "ピンを指定する"
   :location                              "位置"
   :open-map                              "地図を開く"
   :sharing-copy-to-clipboard-address     "アドレスをコピー"
   :sharing-copy-to-clipboard-coordinates "座標のコピー"

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
   :left                                  "残り"
   :invited                               "招待されました"
   :removed                               "削除されました"
   :You                                   "あなた"

   ;;new-contact
   :add-new-contact                       "新規連絡先を追加"
   :scan-qr                               "QRをスキャン"
   :name                                  "名前"
   :address-explication                   "あなたの公開鍵はイサレアム上でのあなたのアドレスを生成するのに使用されます。 それは連続した数字と文字で、プロフィールの中で簡単に見つけることができます"
   :enter-valid-public-key                "有効な公開鍵を入力するかQRコードをスキャンしてください"
   :contact-already-added                 "連絡先はすでに追加されています"
   :can-not-add-yourself                  "自分自身を追加することはできません"
   :unknown-address                       "不明なアドレス"

   ;;login
   :connect                               "接続"
   :address                               "アドレス"
   :password                              "パスワード"
   :sign-in-to-status                     "ステータスにサインイン"
   :sign-in                               "サインイン"
   :wrong-password                        "間違ったパスワードです"
   :enter-password                        "パスワードを入力して下さい"

   ;;recover
   :passphrase                            "パスフレーズ"
   :recover                               "復元"
   :twelve-words-in-correct-order         "正しい順序で12ワード"

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
   :transaction                           "トランザクション"
   :unsigned-transaction-expired          "署名されていないトランザクションは期限切れとなりました"
   :status                                "ステータス"
   :recipient                             "受信者"
   :to                                    "宛先"
   :from                                  "送信元"
   :data                                  "データ"
   :got-it                                "理解しました"
   :block                                 "ブロック"
   :hash                                  "ハッシュ"
   :gas-limit                             "ガスの制限値"
   :gas-price                             "ガスの価格"
   :gas-used                              "使用されたガス"
   :cost-fee                              "コスト/フィー"
   :nonce                                 "ノンス"
   :confirmations                         "確認"
   :confirmations-helper-text             "トランザクションが安全にプロセスするため最低12の確認作業をお待ち下さい"
   :copy-transaction-hash                 "トランザクションのハッシュをコピー"
   :open-on-etherscan                     "Etherscan.io上でオープン"

   ;;webview
   :web-view-error                        "おっと, エラーです"

   ;;testfairy warning
   :testfairy-title                       "警告!"
   :testfairy-message                     "現在のアップはナイトリービルドです。 Wifi環境下ではテストのためにセッションを記録することがあります。 従い全てのアップとのやり取りはビデオやログとして保存され潜在的な問題のために開発チームに利用される可能性があります。 但しパスワードは保存しません。 この記録はアップをナイトリービルドから利用した際のみに行われます"
   ;; wallet
   :wallet                                "ウォレット"
   :wallets                               "ウォレット"
   :your-wallets                          "あなたのウォレット"
   :main-wallet                           "メインのウォレット"
   :wallet-error                          "データローディング中のエラー"
   :wallet-send                           "送信"
   :wallet-request                        "リクエスト"
   :wallet-exchange                       "交換"
   :wallet-assets                         "アセット"
   :wallet-add-asset                      "アセットの追加"
   :wallet-total-value                    "全体の価値"
   :wallet-settings                       "ウォレットの設定"
   :signing-phrase-description            "パスワードを入力しトランザクションにサインインして下さい。そして上記のワードがサインインフレーズにマッチすることを確認して下さい"
   :wallet-insufficient-funds             "不十分なファンド"
   :request-transaction                   "トランザクションをリクエスト"
   :send-request                          "リクエストを送信"
   :share                                 "共有"
   :eth                                   "ETH"
   :currency                              "貨幣"
   :usd-currency                          "USD"
   :transactions                          "トランザクション"
   :transaction-details                   "トランザクションの詳細"
   :transaction-failed                    "トランザクションが失敗"
   :transactions-sign                     "サイン"
   :transactions-sign-all                 "全てサイン"
   :transactions-sign-transaction         "トランザクションをサイン"
   :transactions-sign-later               "後ほどサイン"
   :transactions-delete                   "トランザクションを削除"
   :transactions-delete-content           "トランザクションは未サインのリストから削除されます"
   :transactions-history                  "履歴"
   :transactions-unsigned                 "未サイン"
   :transactions-history-empty            "あなたの履歴には未だトランザクションがありません"
   :transactions-unsigned-empty           "あなたには未サインのトランザクションはありません"
   :transactions-filter-title             "履歴のフィルター"
   :transactions-filter-tokens            "トークン"
   :transactions-filter-type              "タイプ"
   :transactions-filter-select-all        "全てを選択"
   :view-transaction-details              "トランザクションの詳細を閲覧"
   :transaction-description               "トランザクションが安全にプロセスされるために最低１２の確認作業をお待ちください"
   :transaction-sent                      "トランザクション送信済み"
   :transaction-moved-text                "トランザクションが５分間未サインのリストに残ります"
   :transaction-moved-title               "トランザクションが移動"
   :sign-later-title                      "トランザクションのサインを後ほど行いますか？"
   :sign-later-text                       "このトランザクションをサインするためにトランザクションの履歴をチェックしてください"
   :not-applicable                        "未サインのトランザクションには適用されません"

   ;; Wallet Send
   :wallet-choose-recipient               "受信者を選択"
   :wallet-choose-from-contacts           "連絡先から選択"
   :wallet-address-from-clipboard         "クリップボードからのアドレスを使用"
   :wallet-invalid-address                "無効なアドレス: \n {{data}}"
   :wallet-browse-photos                  "写真を閲覧"
   :validation-amount-invalid-number      "金額は無効な数字です"
   :validation-amount-is-too-precise      "金額が細かすぎます。送金可能なもっとも小さな単位は1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "新しいネットワーク"
   :add-network                           "ネットワークを追加"
   :add-new-network                       "新しいネットワークを追加"
   :existing-networks                     "既存のネットワーク"
   :add-json-file                         "JSONファイルを追加"
   :paste-json-as-text                    "JSONをテキストとしてペースト"
   :paste-json                            "JSONをペースト"
   :specify-rpc-url                       "RPC URLを特定してください"
   :edit-network-config                   "ネットワークの構成を編集してください"
   :connected                             "接続済み"
   :process-json                          "JSONを処理してください"
   :error-processing-json                 "JSONの処理に失敗しました"
   :rpc-url                               "RPC URL"
   :remove-network                        "ネットワークを削除"
   :network-settings                      "ネットワーク設定"
   :edit-network-warning                  "注意！　ネットワークデータの編集によりこのネットワークが利用できなくなるかもしれません"
   :connecting-requires-login             "他のネットワークに接続するのにはログインが必要です"
   :close-app-title                       "警告!"
   :close-app-content                     "アップは停止しクローズします。再度アップをオープンした際に選択されたネットワークが利用されます"
   :close-app-button                      "確認"})

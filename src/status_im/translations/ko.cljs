(ns status-im.translations.ko)

(def translations
  {
   ;;common
   :members-title                         "멤버"
   :not-implemented                       "!구현되지 않음"
   :chat-name                             "채팅 이름"
   :notifications-title                   "알림 및 소리"
   :offline                               "오프라인"
   :search-for                            "검색하기..."
   :cancel                                "취소"
   :next                                  "다음"
   :open                                  "열기"
   :description                           "설명"
   :url                                   "URL"
   :type-a-message                        "메시지 입력..."
   :type-a-command                        "명령 입력..."
   :error                                 "오류"
   :unknown-status-go-error               "알 수 없는 status-go 오류"
   :node-unavailable                      "사용 가능한 Ethereum 노드가 없습니다."
   :yes                                   "예"
   :no                                    "아니오"

   :camera-access-error                   "카메라 권한을 승인하려면 시스템 앱 설정에서 Status > 권한 > 카메라가 선택되어 있는지 확인해 주세요."
   :photos-access-error                   "사진 접근 권한을 승인하려면 시스템 앱 설정에서 Status > 권한 > 사진이 선택되어 있는지 확인해 주세요."

   ;;drawer
   :switch-users                          "사용자 전환"
   :current-network                       "현재 네트워크"

   ;;chat
   :is-typing                             "이(가) 입력 중입니다"
   :and-you                               "그리고 당신"
   :search-chat                           "채팅 검색"
   :members                               {:one   "멤버 1명"
                                           :other "멤버 {{count}}명"
                                           :zero  "멤버 없음"}
   :members-active                        {:one   "멤버 1명"
                                           :other "멤버 {{count}}명"
                                           :zero  "멤버 없음"}
   :public-group-status                   "공개"
   :active-online                         "온라인"
   :active-unknown                        "상태 알 수 없음"
   :available                             "이용 가능"
   :no-messages                           "메시지 없음"
   :suggestions-requests                  "요청"
   :suggestions-commands                  "명령"
   :faucet-success                        "Faucet 요청이 도착했습니다"
   :faucet-error                          "Faucet 요청 오류"

   ;;sync
   :sync-in-progress                      "동기화 중..."
   :sync-synced                           "동기화됨"

   ;;messages
   :status-pending                        "보류중"
   :status-sent                           "전송됨"
   :status-seen-by-everyone               "모두 읽음"
   :status-seen                           "읽음"
   :status-delivered                      "전송됨"
   :status-failed                         "실패"

   ;;datetime
   :datetime-ago-format                   "{{number}}{{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "초"
                                           :other "초"}
   :datetime-minute                       {:one   "분"
                                           :other "분"}
   :datetime-hour                         {:one   "시간"
                                           :other "시간"}
   :datetime-day                          {:one   "일"
                                           :other "일"}
   :datetime-ago                          "전"
   :datetime-yesterday                    "어제"
   :datetime-today                        "오늘"

   ;;profile
   :profile                               "프로필"
   :edit-profile                          "프로필 편집"
   :message                               "메시지"
   :not-specified                         "지정되지 않음"
   :public-key                            "공개 키"
   :phone-number                          "휴대전화 번호"
   :update-status                         "상태 메시지 업데이트..."
   :add-a-status                          "상태 메시지 추가..."
   :status-prompt                         "상태 메시지를 설정하세요. #해시태그를 사용하여 다른 사람이 날 찾는 것을 도울 수 있습니다."
   :add-to-contacts                       "연락처에 추가"
   :in-contacts                           "연락처에 있음"
   :remove-from-contacts                  "연락처에서 제거"
   :start-conversation                    "대화하기"
   :send-transaction                      "송금하기"
   :testnet-text                          "현재 {{testnet}} 테스트 네트워크에 있습니다. 이 주소로 진짜 ETH나 SNT를 보내지 마세요!"
   :mainnet-text                          "메인 네트워크에 있습니다. 진짜 ETH가 전송될 것입니다."

   ;;make_photo
   :image-source-title                    "프로필 이미지"
   :image-source-make-photo               "캡처"
   :image-source-gallery                  "갤러리에서 선택하기"

   ;;sharing
   :sharing-copy-to-clipboard             "클립보드로 복사"
   :sharing-share                         "공유하기"
   :sharing-cancel                        "취소"

   :browsing-title                        "브라우저"
   :browsing-open-in-web-browser          "웹 브라우저로 열기"
   :browsing-cancel                       "취소"

   ;;sign-up
   :contacts-syncronized                  "연락처가 동기화되었습니다"
   :confirmation-code                     (str "감사합니다! 확인 코드를 문자 메시지로 보내 드렸습니다. "
                                               "전화번호 검증을 위해 확인 코드를 입력해 주세요")
   :incorrect-code                        (str "잘못된 코드입니다. 다시 입력해 주세요")
   :phew-here-is-your-passphrase          "휴~ 꽤 어려웠어요. 이것이 당신의 암호문입니다. *이걸 적어두신 다음 안전한 곳에 보관하세요!* 계정을 복구할 때 필요합니다."
   :here-is-your-passphrase               "이것이 당신의 암호문입니다. *이걸 적어두신 다음 안전한 곳에 보관하세요!* 계정을 복구할 때 필요합니다."
   :here-is-your-signing-phrase           "이건 당신의 서명 문구입니다. 거래를 승인할 때 필요합니다. *이걸 적어두신 다음 안전한 곳에 보관하세요!*"
   :phone-number-required                 "여기를 눌러 전화번호를 검증하시면 친구들을 찾아드리겠습니다"
   :shake-your-phone                      "버그나 건의사항이 있나요? 폰을 ~흔들어~ 보세요!"
   :intro-status                          "계정을 설정하고 설정을 변경하려면 저와 채팅하세요!"
   :intro-message1                        "Status에 오신 것을 환영합니다!\n이 메시지를 탭하여 비밀번호를 설정하고 시작하세요!"
   :account-generation-message            "잠시만요, 계정을 만들려면 엄청 복잡한 수학 문제를 풀어야 합니다. 기다려 주세요!"
   :move-to-internal-failure-message      "몇몇 중요한 파일을 외부 저장소에서 내부 저장소로 옮기기 위해 권한이 필요합니다. 추후 버전에서는 외부 저장소를 사용하지 않을 것입니다."
   :debug-enabled                         "디버그 서버가 시작되었습니다! 같은 네트워크 상의 컴퓨터에서 *status-dev-cli scan*를 실행하여 디버그 서버를 확인하세요."

   ;;phone types
   :phone-e164                            "국제전화 1"
   :phone-international                   "국제전화 2"
   :phone-national                        "국내"
   :phone-significant                     "전국 전화번호"

   ;;chats
   :chats                                 "채팅"
   :delete-chat                           "채팅 삭제"
   :new-group-chat                        "새 그룹 채팅"
   :new-public-group-chat                 "공개 채팅에 참여하기"
   :edit-chats                            "채팅방 편집"
   :search-chats                          "채팅 검색하기"
   :empty-topic                           "토픽 없음"
   :topic-format                          "잘못된 포맷 [a-z0-9\\-]+"
   :public-group-topic                    "토픽"

   ;;discover
   :discover                              "찾아보기"
   :none                                  "없음"
   :search-tags                           "검색할 태그를 입력하세요"
   :popular-tags                          "인기 급상승 태그"
   :recent                                "최근 상태 메시지"
   :no-statuses-found                     "발견된 메시지 없음"
   :chat                                  "채팅"
   :all                                   "전체"
   :public-chats                          "공개 채팅방"
   :soon                                  "곧"
   :public-chat-user-count                "{{count}}명"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp 프로필"
   :no-statuses-discovered                "발견된 상태 메시지 없음"
   :no-statuses-discovered-body           "누군가가 상태 메시지를 올리면\n여기 나타납니다."
   :no-hashtags-discovered-title          "발견된 해시태그 없음"
   :no-hashtags-discovered-body           "유명세를 탄 해시태그가\n여기 나타납니다."

   ;;settings
   :settings                              "설정"

   ;;contacts
   :contacts                              "연락처"
   :new-contact                           "새 연락처"
   :delete-contact                        "연락처 삭제"
   :delete-contact-confirmation           "이 연락처가 삭제됩니다"
   :remove-from-group                     "그룹에서 삭제"
   :edit-contacts                         "연락처 편집"
   :search-contacts                       "연락처 검색"
   :contacts-group-new-chat               "새 채팅 시작"
   :choose-from-contacts                  "연락처에서 선택하기"
   :no-contacts                           "아직 연락처가 없습니다"
   :show-qr                               "QR코드 표시"
   :enter-address                         "주소 입력"
   :more                                  "더보기"

   ;;group-settings
   :remove                                "제거"
   :save                                  "저장"
   :delete                                "삭제"
   :clear-history                         "대화내역 삭제"
   :mute-notifications                    "알림 비활성화"
   :leave-chat                            "채팅방 나가기"
   :chat-settings                         "채팅방 설정"
   :edit                                  "편집"
   :add-members                           "멤버 추가"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "현재 위치"
   :places-nearby                         "주변 장소"
   :search-results                        "검색 결과"
   :dropped-pin                           "핀한 곳"
   :location                              "위치"
   :open-map                              "지도 열기"
   :sharing-copy-to-clipboard-address     "주소 복사"
   :sharing-copy-to-clipboard-coordinates "좌표 복사"

   ;;new-group
   :new-group                             "새 그룹"
   :reorder-groups                        "그룹 재정렬"
   :edit-group                            "그룹 편집"
   :delete-group                          "그룹 삭제"
   :delete-group-confirmation             "이 그룹을 그룹 목록에서 삭제합니다. 연락처에는 영향을 미치지 않습니다."
   :delete-group-prompt                   "연락처에는 영향이 없습니다"
   :contact-s                             {:one   "연락처"
                                           :other "연락처"}

   ;;protocol
   :received-invitation                   "채팅방 초대"
   :removed-from-chat                     "그룹 채팅방에서 쫓겨났습니다"
   :left                                  "떠남"
   :invited                               "초대됨"
   :removed                               "쫓겨남"
   :You                                   "당신"

   ;;new-contact
   :add-new-contact                       "새 연락처 추가"
   :scan-qr                               "QR코드 스캔"
   :name                                  "이름"
   :address-explication                   "공개 키는 Ethereum 주소를 생성하기 위해 사용되며 숫자와 알파벳으로 이루어져 있습니다. 당신의 프로필에서 쉽게 찾을 수 있습니다."
   :enter-valid-public-key                "올바른 공개 키를 입력하거나 QR 코드를 스캔하세요"
   :contact-already-added                 "이미 추가되어 있는 연락처입니다."
   :can-not-add-yourself                  "자기 자신을 추가할 수 없습니다"
   :unknown-address                       "알 수 없는 주소"

   ;;login
   :connect                               "접속"
   :address                               "주소"
   :password                              "비밀번호"
   :sign-in-to-status                     "Status에 로그인"
   :sign-in                               "로그인"
   :wrong-password                        "잘못된 비밀번호"
   :enter-password                        "비밀번호 입력"

   ;;recover
   :passphrase                            "암호문"
   :recover                               "복구"
   :twelve-words-in-correct-order         "12개 단어를 올바른 순서로 입력하세요"

   ;;accounts
   :recover-access                        "계정 복구"
   :create-new-account                    "새 계정 생성"

   ;;wallet-qr-code
   :done                                  "완료"

   ;;validation
   :invalid-phone                         "잘못된 전화번호"
   :amount                                "액수"

   ;;transactions
   :confirm                               "승인"
   :transaction                           "거래"
   :unsigned-transaction-expired          "승인되지 않은 거래 만료됨"
   :status                                "상태"
   :recipient                             "수신인"
   :to                                    "수신자"
   :from                                  "송금자"
   :data                                  "데이터"
   :got-it                                "알겠습니다"
   :block                                 "블록"
   :hash                                  "해시값"
   :gas-limit                             "최대 가스 사용량"
   :gas-price                             "가스 가격"
   :gas-used                              "가스 사용량"
   :cost-fee                              "비용"
   :nonce                                 "Nonce"
   :confirmations                         "이체확인(컨펌)"
   :confirmations-helper-text             "거래가 안전하게 처리된 것을 확실히 하기 위해 이체확인이 12개를 넘어갈 때 까지 기다려주세요"
   :copy-transaction-hash                 "거래 해시값 복사"
   :open-on-etherscan                     "Etherscan.io에서 보기"

   ;;webview
   :web-view-error                        "이런, 오류입니다"

   ;;testfairy warning
   :testfairy-title                       "경고!"
   :testfairy-message                     "나이틀리 빌드에서 설치된 앱을 사용중입니다. 이 빌드에는 테스트를 위해 세션 녹화 기능이 포함되어 있으며, Wi-Fi가 연결되어 있을 시 앱과의 모든 상호작용이 (비디오와 로그로) 저장됩니다. 이 데이터는 개발자들이 문제를 확인하기 위해 사용할 수 있습니다. 녹화된 정보에 비밀번호는 포함되어 있지 않습니다. 세션 녹화는 앱이 나이틀리 빌드에서 설치되었을 때만 진행됩니다. Play 스토어 또는 TestFlight를 통해 설치했을 시에는 녹화가 진행되지 않습니다."

   ;; wallet
   :wallet                                "지갑"
   :wallets                               "지갑들"
   :your-wallets                          "내 지갑"
   :main-wallet                           "주 지갑"
   :wallet-error                          "데이터 로딩 중 오류 발생"
   :wallet-send                           "송금"
   :wallet-request                        "요청"
   :wallet-exchange                       "거래소"
   :wallet-assets                         "자산"
   :wallet-add-asset                      "자산 추가"
   :wallet-total-value                    "총자산"
   :wallet-settings                       "지갑 설정"
   :signing-phrase-description            "거래를 승인하려면 비밀번호를 입력하세요. 위에 있는 단어들이 당신의 서명 문구와 일치하는지 확인하세요."
   :wallet-insufficient-funds             "자금 부족"
   :request-transaction                   "거래 요청"
   :send-request                          "요청 전송"
   :share                                 "공유"
   :eth                                   "ETH"
   :currency                              "통화"
   :usd-currency                          "USD"
   :transactions                          "거래"
   :transaction-details                   "거래 세부정보"
   :transaction-failed                    "거래 실패"
   :transactions-sign                     "승인"
   :transactions-sign-all                 "모두 승인"
   :transactions-sign-transaction         "거래 승인"
   :transactions-sign-later               "나중에 승인"
   :transactions-delete                   "거래 삭제"
   :transactions-delete-content           "거래가 '승인되지 않음' 목록에서 삭제됩니다"
   :transactions-history                  "거래 내역"
   :transactions-unsigned                 "승인되지 않음"
   :transactions-history-empty            "아직 거래 내역이 없습니다"
   :transactions-unsigned-empty           "승인되지 않은 거래가 없습니다"
   :transactions-filter-title             "거래 내역 필터"
   :transactions-filter-tokens            "토큰"
   :transactions-filter-type              "종류"
   :transactions-filter-select-all        "모두 선택"
   :view-transaction-details              "거래 세부정보 보기"
   :transaction-description               "거래가 안전하게 처리된 것을 확실히 하기 위해 이체확인이 12개를 넘어갈 때 까지 기다려주세요"
   :transaction-sent                      "거래 전송됨"
   :transaction-moved-text                "다음 5분간 거래가 '승인되지 않음' 목록에 남아있을 것입니다"
   :transaction-moved-title               "거래 옮겨짐"
   :sign-later-title                      "거래를 나중에 승인할까요?"
   :sign-later-text                       "이 거래를 승인하려면 거래 내역을 확인하세요"
   :not-applicable                        "승인되지 않은 거래에 사용할 수 없음"

   ;; Wallet Send
   :wallet-choose-recipient               "수신인 선택"
   :wallet-choose-from-contacts           "연락처에서 선택"
   :wallet-address-from-clipboard         "클립보드에서 가져오기"
   :wallet-invalid-address                "잘못된 주소: \n {{data}}"
   :wallet-browse-photos                  "사진 보기"
   :validation-amount-invalid-number      "잘못된 액수입니다"
   :validation-amount-is-too-precise      "액수가 너무 정확합니다. 최소 송금 단위는 1 Wei (1x10^-18 ETH) 입니다."



   ;; network settings
   :new-network                           "새 네트워크"
   :add-network                           "네트워크 추가"
   :add-new-network                       "새 네트워크 추가"
   :existing-networks                     "기존 네트워크"
   :add-json-file                         "JSON 파일 추가"
   :paste-json-as-text                    "JSON 텍스트로 붙여넣기"
   :paste-json                            "JSON 붙여넣기"
   :specify-rpc-url                       "RPC URL을 지정하세요"
   :edit-network-config                   "네트워크 설정 편집"
   :connected                             "연결됨"
   :process-json                          "JSON 처리"
   :error-processing-json                 "JSON 처리중 오류 발생"
   :rpc-url                               "RPC URL"
   :remove-network                        "네트워크 제거"
   :network-settings                      "네트워크 설정"
   :edit-network-warning                  "주의하십시오. 네트워크 설정을 편집하면 이 네트워크를 사용하지 못할 수 있습니다."
   :connecting-requires-login             "다른 네트워크에 연결하기 위해서는 로그인이 필요합니다"
   :close-app-title                       "경고!"
   :close-app-content                     "앱이 닫힐 것입니다. 앱을 다시 열었을 때 설정된 네트워크가 사용될 것입니다"
   :close-app-button                      "확인"})

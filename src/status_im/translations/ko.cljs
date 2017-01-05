(ns status-im.translations.ko)

(def translations
  {
   ;common
   :members-title                         "회원"
   :not-implemented                       "!구현되지 않음"
   :chat-name                             "채팅 이름"
   :notifications-title                   "알림 및 소리"
   :offline                               "오프라인"

   ;drawer
   :invite-friends                        "친구 초대"
   :faq                                   "자주묻는질문"
   :switch-users                          "사용자 전환"

   ;chat
   :is-typing                             "이(가) 입력 중"
   :and-you                               "그리고 귀하"
   :search-chat                           "채팅 검색"
   :members                               {:one   "회원 1명"
                                           :other "회원 {{count}}명"
                                           :zero  "회원 없음"}
   :members-active                        {:one   "회원 1명, 활성 1명"
                                           :other "회원 {{count}}명, 활성 {{count}}명"
                                           :zero  "회원 없음"}
   :active-online                         "온라인"
   :active-unknown                        "알 수 없음"
   :available                             "이용 가능"
   :no-messages                           "메시지 없음"
   :suggestions-requests                  "요청"
   :suggestions-commands                  "명령"

   ;sync
   :sync-in-progress                      "동기화 중..."
   :sync-synced                           "동기화됨"

   ;messages
   :status-sending                        "보내는 중"
   :status-pending                        "계류중인"
   :status-sent                           "전송됨"
   :status-seen-by-everyone               "모든 사람이 조회함"
   :status-seen                           "조회함"
   :status-delivered                      "배달됨"
   :status-failed                         "실패"

   ;datetime
   :datetime-second                       {:one   "초"
                                           :other "초"}
   :datetime-minute                       {:one   "분"
                                           :other "분"}
   :datetime-hour                         {:one   "시간"
                                           :other "시간"}
   :datetime-day                          {:one   "일"
                                           :other "일"}
   :datetime-multiple                     "초"
   :datetime-ago                          "전"
   :datetime-yesterday                    "어제"
   :datetime-today                        "오늘"

   ;profile
   :profile                               "프로필"
   :report-user                           "사용자 신고하기"
   :message                               "메시지"
   :username                              "사용자명"
   :not-specified                         "지정되지 않음"
   :public-key                            "공용 키"
   :phone-number                          "전화번호"
   :email                                 "이메일"
   :profile-no-status                     "상태 정보 없음"
   :add-to-contacts                       "연락처에 추가"
   :error-incorrect-name                  "다음 이름을 선택해 주세요"
   :error-incorrect-email                 "잘못된 이메일"

   ;;make_photo
   :image-source-title                    "프로필 이미지"
   :image-source-make-photo               "캡쳐"
   :image-source-gallery                  "갤러리에서 선택하기"
   :image-source-cancel                   "취소"

   ;sign-up
   :contacts-syncronized                  "연락처가 동기화되었습니다"
   :confirmation-code                     (str "감사합니다! 귀하께 확인 문자 메시지를 보내드렸습니다 "
                                               "코드. 귀하의 전화번호를 확인하려면 해당 코드를 입력해 주세요")
   :incorrect-code                        (str "죄송하지만 잘못된 코드입니다. 다시 입력해 주세요")
   :generate-passphrase                   (str "액세스를 복원하거나 다른 기기에서 로그인할 수 있도록 "
                                               "암호문을 생성해 드리겠습니다")
   :phew-here-is-your-passphrase          "*휴~*어려운 작업이었네요, 여기 암호문이 있습니다. *이걸 적어두시고 안전하게 보관하세요!* 계정을 복구할 때 필요합니다."
   :here-is-your-passphrase               "여기 암호문이 있습니다. *이걸 적어두시고 안전하게 보관하세요!* 계정을 복구할 때 필요합니다."
   :written-down                          "암호문을 안전하게 적어 두었는지 확인해 주세요"
   :phone-number-required                 "여기를 탭하여 귀하의 전화번호를 입력하시면 친구들을 찾아드리겠습니다"
   :intro-status                          "계정을 설정하고, 설정을 변경하려면 저와 채팅하세요!"
   :intro-message1                        "'상태'에 오신 것을 환영합니다\n이 메시지를 탭하여 비밀번호를 설정하고 시작하세요!"
   :account-generation-message            "잠깐만요, 귀하의 계정 생성을 위한 수학 연산 작업 좀 할게요!"

   ;chats
   :chats                                 "채팅"
   :new-chat                              "새 채팅"
   :new-group-chat                        "새 그룹 채팅"

   ;discover
   :discover                             "발견"
   :none                                  "없음"
   :search-tags                           "여기에 검색 태그를 입력하세요"
   :popular-tags                          "인기 태그"
   :recent                                "최근"
   :no-statuses-discovered                "발견된 상태 정보가 없습니다"

   ;settings
   :settings                              "설정"

   ;contacts
   :contacts                              "연락처"
   :new-contact                           "새 연락처"
   :show-all                              "모두 표시"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "사람들"
   :contacts-group-new-chat               "새 채팅 시작하기"
   :no-contacts                           "연락처가 아직 없습니다"
   :show-qr                               "QR 표시"

   ;group-settings
   :remove                                "제거"
   :save                                  "저장"
   :change-color                          "색상 변경"
   :clear-history                         "내역 삭제"
   :delete-and-leave                      "삭제하고 나가기"
   :chat-settings                         "채팅 설정"
   :edit                                  "편집"
   :add-members                           "회원 추가"
   :blue                                  "파란색"
   :purple                                "자주색"
   :green                                 "녹색"
   :red                                   "빨간색"

   ;commands
   :money-command-description             "송금하기"
   :location-command-description          "위치정보 보내기"
   :phone-command-description             "전화번호 보내기"
   :phone-request-text                    "전화번호 요청"
   :confirmation-code-command-description "확인 코드 보내기"
   :confirmation-code-request-text        "확인 코드 요청"
   :send-command-description              "위치정보 보내기"
   :request-command-description           "요청 보내기"
   :keypair-password-command-description  ""
   :help-command-description              "도움말"
   :request                               "요청"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH 송금인: {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH 수금인: {{chat-name}}"
   :command-text-location                 "위치: {{address}}"
   :command-text-browse                   "열람중인 웹페이지: {{webpage}}"
   :command-text-send                     "거래: {{amount}} ETH"
   :command-text-help                     "도움말"

   ;new-group
   :group-chat-name                       "채팅 이름"
   :empty-group-chat-name                 "이름을 입력해 주세요"
   :illegal-group-chat-name               "다른 이름을 선택해 주세요"

   ;participants
   :add-participants                      "참가자 추가"
   :remove-participants                   "참가자 제거"

   ;protocol
   :received-invitation                   "채팅 초대를 받았습니다"
   :removed-from-chat                     "귀하는 그룹 채팅에서 제거되었습니다"
   :left                                  "나감"
   :invited                               "초대됨"
   :removed                               "제거됨"
   :You                                   "귀하"

   ;new-contact
   :add-new-contact                       "새 연락처 추가하기"
   :import-qr                             "가져오기"
   :scan-qr                               "QR코드 스캔"
   :name                                  "이름"
   :whisper-identity                      "Whisper 아이디"
   :address-explication                   "여기에는 주소가 어떻게 되는지, 그리고 해당 주소를 어디에서 찾을지를 설명하는 글이 있어야 합니다"
   :enter-valid-address                   "유효한 주소를 입력하거나 QR 코드를 스캔해 주세요"
   :contact-already-added                 "해당 연락처는 이미 추가되었습니다"
   :can-not-add-yourself                  "나 자신을 추가할 수는 없습니다"
   :unknown-address                       "알 수 없는 주소"


   ;login
   :connect                               "연결"
   :address                               "주소"
   :password                              "비밀번호"
   :login                                 "로그인"
   :wrong-password                        "잘못된 비밀번호"

   ;recover
   :recover-from-passphrase               "암호문에서 복구하기"
   :recover-explain                       "액세스를 복구하려면 비밀번호에 대한 암호문을 입력해 주세요"
   :passphrase                            "암호문"
   :recover                               "복구하기"
   :enter-valid-passphrase                "암호문을 입력해 주세요"
   :enter-valid-password                  "비밀번호를 입력해 주세요"

   ;accounts
   :recover-access                        "액세스 복구하기"
   :add-account                           "계정 추가"

   ;wallet-qr-code
   :done                                  "완료"
   :main-wallet                           "주요 지갑"

   ;validation
   :invalid-phone                         "잘못된 전화번호"
   :amount                                "금액"
   :not-enough-eth                        (str "ETH 잔고가 부족합니다 "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "거래 확인"
                                           :other "{{count}}개의 거래 확인"
                                           :zero  "거래 없음"}
   :status                                "상태"
   :pending-confirmation                  "보류 중인 확인"
   :recipient                             "수취인"
   :one-more-item                         "항목 하나 더"
   :fee                                   "요금"
   :value                                 "값"

   ;:webview
   :web-view-error                        "이런, 오류입니다"})

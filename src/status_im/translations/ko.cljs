(ns status-im.translations.ko)

(def translations
  {
   ;common
   :members-title                         "회원"
   :not-implemented                       "!구현되지 않음"
   :chat-name                             "채팅 이름"
   :notifications-title                   "알림 및 소리"
   :offline                               "오프라인"
   :search-for                            "검색하기..."
   :cancel                                "취소"
   :next                                  "다음"
   :type-a-message                        "메시지 입력..."
   :type-a-command                        "명령 입력..."
   :error                                 "오류"

   :camera-access-error                   "카메라 권한을 승인하려면 시스템 설정에서 권한 > 카메라가 선택되어 있는지 확인해 주세요."
   :photos-access-error                   "사진 접근 권한을 승인하려면 시스템 설정에서 권한 > 사진이 선택되어 있는지 확인해 주세요."

   ;drawer
   :invite-friends                        "친구 초대"
   :faq                                   "FAQ"
   :switch-users                          "사용자 전환"
   :feedback                              "건의사항이 있나요?\n폰을 흔들어 보세요!"
   :view-all                              "모두 모기"
   :current-network                       "현재 네트워크"

   ;chat
   :is-typing                             "이(가) 입력 중"
   :and-you                               "그리고 나"
   :search-chat                           "채팅 검색"
   :members                               {:one   "회원 1명"
                                           :other "회원 {{count}}명"
                                           :zero  "회원 없음"}
   :members-active                        {:one   "회원 1명, 활성 1명"
                                           :other "회원 {{count}}명, 활성 {{count}}명"
                                           :zero  "회원 없음"}
   :public-group-status                   "공개"
   :active-online                         "온라인"
   :active-unknown                        "상태 알 수 없음"
   :available                             "이용 가능"
   :no-messages                           "메시지 없음"
   :suggestions-requests                  "요청"
   :suggestions-commands                  "명령"
   :faucet-success                        "Faucet 요청이 도착했습니다"
   :faucet-error                          "Faucet 요청 오류"

   ;sync
   :sync-in-progress                      "동기화 중..."
   :sync-synced                           "동기화됨"

   ;messages
   :status-sending                        "전송중"
   :status-pending                        "보류중"
   :status-sent                           "전송됨"
   :status-seen-by-everyone               "모두 읽음"
   :status-seen                           "읽음"
   :status-delivered                      "전송됨"
   :status-failed                         "실패"

   ;datetime
   :datetime-ago-format                   "{{number}}{{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "초"
                                           :other "초"}
   :datetime-minute                       {:one   "분"
                                           :other "분"}
   :datetime-hour                         {:one   "시간"
                                           :other "시간"}
   :datetime-day                          {:one   "일"
                                           :other "일"}
   :datetime-multiple                     ""
   :datetime-ago                          "전"
   :datetime-yesterday                    "어제"
   :datetime-today                        "오늘"

   ;profile
   :profile                               "프로필"
   :edit-profile                          "프로필 수정"
   :report-user                           "사용자 신고하기"
   :message                               "메시지"
   :username                              "사용자명"
   :not-specified                         "입력하지 않음"
   :public-key                            "공개키"
   :phone-number                          "전화번호"
   :email                                 "이메일"
   :update-status                         "상태 업데이트..."
   :add-a-status                          "상태 추가하기..."
   :status-prompt                         "상태 메시지를 추가하여 당신이 제공할 수 있는 것들에 대해 사람들에게 알려주세요. #해시태그를 이용할 수도 있습니다."
   :add-to-contacts                       "연락처에 추가"
   :in-contacts                           "연락처에 추가됨"
   :remove-from-contacts                  "연락처에서 삭제"
   :start-conversation                    "채팅 시작하기"
   :send-transaction                      "송금하기"
   :share-qr                              "QR코드 공유"
   :error-incorrect-name                  "다른 이름을 선택해 주세요"
   :error-incorrect-email                 "잘못된 이메일"

   ;;make_photo
   :image-source-title                    "프로필 이미지"
   :image-source-make-photo               "캡쳐"
   :image-source-gallery                  "갤러리에서 선택하기"
   :image-source-cancel                   "취소"

   ;;sharing
   :sharing-copy-to-clipboard             "클립보드로 복사"
   :sharing-share                         "공유하기"
   :sharing-cancel                        "취소"

   :browsing-title                        "브라우저"
   :browsing-open-in-web-browser          "웹 브라우저로 열기"
   :browsing-cancel                       "취소"

   ;sign-up
   :contacts-syncronized                  "연락처가 동기화되었습니다"
   :confirmation-code                     (str "감사합니다! 확인 코드 문자 메시지를 보내드렸습니다. "
                                               "전화번호를 확인하려면 해당 코드를 입력해 주세요")
   :incorrect-code                        (str "죄송하지만 잘못된 코드입니다. 다시 입력해 주세요")
   :generate-passphrase                   (str "계정을 복원하거나 다른 기기에서 로그인할 수 있도록 "
                                               "암호문을 생성해 드리겠습니다")
   :phew-here-is-your-passphrase          "*휴~*어려운 작업이었네요. 여기 암호문이 있습니다. *이걸 적어두시고 안전하게 보관하세요!* 계정을 복구할 때 필요합니다."
   :here-is-your-passphrase               "여기 암호문이 있습니다. *이걸 적어두시고 안전하게 보관하세요!* 계정을 복구할 때 필요합니다."
   :written-down                          "암호문을 안전하게 적어 두었는지 확인해 주세요"
   :phone-number-required                 "여기를 탭하여 전화번호를 입력하시면 친구들을 찾아드리겠습니다"
   :shake-your-phone                      "버그나 건의사항이 있나요? 폰을 ~흔들어~ 보세요!"
   :intro-status                          "계정을 설정하고 설정을 변경하려면 저와 채팅하세요!"
   :intro-message1                        "'Status'에 오신 것을 환영합니다\n이 메시지를 탭하여 비밀번호를 설정하고 시작하세요!"
   :account-generation-message            "잠깐만요, 계정 생성을 위해 수학 연산 작업 좀 할게요!"
   :move-to-internal-failure-message      "일부 중요 파일을 내부 저장소에서 외부 저장소로 이동해야 합니다. 이 작업은 권한이 필요합니다. 차기 버전에서는 외부 저장소를 사용하지 않을 예정입니다."
   :debug-enabled                         "디버그 서버가 공개되었습니다! 같은 네트워크에 있는 컴퓨터에서 *status-dev-cli scan*을 실행하여 서버를 찾을 수 있습니다."

   ;phone types
   :phone-e164                            "국제 번호 포맷 1"
   :phone-international                   "국제 번호 포맷 2"
   :phone-national                        "국내 번호 포맷"
   :phone-significant                     "0을 제외한 포맷"

   ;chats
   :chats                                 "채팅"
   :new-chat                              "새 채팅"
   :delete-chat                           "채팅 삭제"
   :new-group-chat                        "새 그룹 채팅"
   :new-public-group-chat                 "공개 채팅에 참여하기"
   :edit-chats                            "채팅방 편집"
   :search-chats                          "채팅 검색하기"
   :empty-topic                           "토픽 없음"
   :topic-format                          "잘못된 포맷 [a-z0-9\\-]+"
   :public-group-topic                    "토픽"

   ;discover
   :discover                              "찾아보기"
   :none                                  "없음"
   :search-tags                           "여기에 검색 태그를 입력하세요"
   :popular-tags                          "인기 태그"
   :recent                                "최근"
   :no-statuses-discovered                "발견된 상태 정보가 없습니다"
   :no-statuses-found                     "상태 정보가 없습니다"

   ;settings
   :settings                              "설정"

   ;contacts
   :contacts                              "연락처"
   :new-contact                           "새 연락처"
   :delete-contact                        "연락처 삭제"
   :delete-contact-confirmation           "이 연락처가 삭제됩니다"
   :remove-from-group                     "채팅에서 제거"
   :edit-contacts                         "연락처 편집"
   :search-contacts                       "연락처 검색"
   :show-all                              "모두 표시"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "사람들"
   :contacts-group-new-chat               "새 채팅 시작하기"
   :choose-from-contacts                  "연락처에서 선택하기"
   :no-contacts                           "연락처가 아직 없습니다"
   :show-qr                               "QR코드 표시"
   :enter-address                         "주소 입력"
   :more                                  "더보기"

   ;group-settings
   :remove                                "제거"
   :save                                  "저장"
   :delete                                "삭제"
   :change-color                          "색상 변경"
   :clear-history                         "대화내용 삭제"
   :mute-notifications                    "알림 받지 않기"
   :leave-chat                            "채팅방 나가기"
   :delete-and-leave                      "삭제하고 나가기"
   :chat-settings                         "채팅 설정"
   :edit                                  "편집"
   :add-members                           "대화상대 추가"
   :blue                                  "파란색"
   :purple                                "보라색"
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

   ;new-group
   :group-chat-name                       "채팅 이름"
   :empty-group-chat-name                 "이름을 입력해 주세요"
   :illegal-group-chat-name               "다른 이름을 선택해 주세요"
   :new-group                             "새로운 채팅 만들기"
   :reorder-groups                        "채팅 순서 바꾸기"
   :group-name                            "채팅 이름"
   :edit-group                            "채팅 편집"
   :delete-group                          "채팅 삭제"
   :delete-group-confirmation             "해당 채팅이 삭제됩니다. 연락처에는 영향을 주지 않습니다."
   :delete-group-prompt                   "연락처에는 영향을 주지 않습니다"
   :group-members                         "채팅 멤버"
   :contact-s                             {:one   "연락처"
                                           :other "연락처"}
   ;participants
   :add-participants                      "대화상대 추가"
   :remove-participants                   "대화상대 제거"

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
   :swow-qr                               "QR코드 보기"
   :name                                  "이름"
   :whisper-identity                      "Whisper 아이디"
   :address-explication                   "여기에는 주소가 어떻게 되는지, 그리고 해당 주소를 어디에서 찾을지를 설명하는 글이 있어야 합니다"
   :enter-valid-address                   "유효한 주소를 입력하거나 QR 코드를 스캔해 주세요"
   :enter-valid-public-key                "유효한 공개키를 입력하거나 QR코드를 스캔하세요"
   :contact-already-added                 "해당 연락처는 이미 추가되었습니다"
   :can-not-add-yourself                  "나 자신을 추가할 수는 없습니다"
   :unknown-address                       "알 수 없는 주소"


   ;login
   :connect                               "연결"
   :address                               "주소"
   :password                              "비밀번호"
   :login                                 "로그인"
   :sign-in-to-status                     "Status에 로그인"
   :sign-in                               "로그인"
   :wrong-password                        "잘못된 비밀번호"

   ;recover
   :recover-from-passphrase               "암호문에서 복구하기"
   :recover-explain                       "계정을 복구하려면 비밀번호에 대한 암호문을 입력해 주세요"
   :passphrase                            "암호문"
   :recover                               "복구하기"
   :enter-valid-passphrase                "암호문을 입력해 주세요"
   :enter-valid-password                  "비밀번호를 입력해 주세요"
   :twelve-words-in-correct-order         "12개의 단어를 올바른 순서로 입력하세요"

   ;accounts
   :recover-access                        "계정 복구하기"
   :add-account                           "계정 추가"
   :create-new-account                    "새 계정 생성"

   ;wallet-qr-code
   :done                                  "확인"
   :main-wallet                           "메인 지갑"

   ;validation
   :invalid-phone                         "잘못된 전화번호"
   :amount                                "금액"
   :not-enough-eth                        (str "ETH 잔고가 부족합니다 "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "승인"
   :confirm-transactions                  {:one   "거래 승인"
                                           :other "{{count}}개의 거래 승인"
                                           :zero  "거래 없음"}
   :transactions-confirmed                {:one   "거래 승인됨"
                                           :other "{{count}}개의 거래가 확인됨"
                                           :zero  "승인된 거래 없음"}
   :transaction                           "거래"
   :unsigned-transactions                 "서명되지 않은 거래"
   :no-unsigned-transactions              "서명되지 않은 거래 없음"
   :enter-password-transactions           {:one   "비밀번호를 입력하여 거래를 승인합니다."
                                           :other "비밀번호를 입력하여 거래들을 승인합니다."}
   :status                                "상태"
   :pending-confirmation                  "승인 보류중"
   :recipient                             "수취인"
   :one-more-item                         "항목 추가"
   :fee                                   "수수료"
   :estimated-fee                         "예상 수수료"
   :value                                 "금액"
   :to                                    "수신인"
   :from                                  "발신인"
   :data                                  "데이터"
   :got-it                                "확인함"
   :contract-creation                     "계약 생성"

   ;:webview
   :web-view-error                        "이런! 오류입니다"})

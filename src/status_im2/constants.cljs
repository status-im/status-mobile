(ns status-im2.constants)

(def ^:const ms-in-bg-for-require-bioauth 5000)

(def ^:const content-type-text 1)
(def ^:const content-type-sticker 2)
(def ^:const content-type-status 3)
(def ^:const content-type-emoji 4)
(def ^:const content-type-command 5)
(def ^:const content-type-system-text 6)
(def ^:const content-type-image 7)
(def ^:const content-type-audio 8)
(def ^:const content-type-community 9)
(def ^:const content-type-gap 10)
(def ^:const content-type-contact-request 11)
(def ^:const content-type-system-pinned-message 14)
(def ^:const content-type-system-message-mutual-event-sent 15)
(def ^:const content-type-system-message-mutual-event-accepted 16)
(def ^:const content-type-system-message-mutual-event-removed 17)

;; Not implemented in status-go, only used for testing/ui work
(def ^:const content-type-gif 100)
(def ^:const content-type-link 101)
(def ^:const content-type-album 102)


(def ^:const contact-request-state-none 0)
(def ^:const contact-request-state-mutual 1)
(def ^:const contact-request-state-sent 2)
(def ^:const contact-request-state-received 3)
(def ^:const contact-request-state-dismissed 4)

(def ^:const contact-verification-status-unknown 0)
(def ^:const contact-verification-status-pending 1)
(def ^:const contact-verification-status-accepted 2)
(def ^:const contact-verification-status-declined 3)
(def ^:const contact-verification-status-cancelled 4)
(def ^:const contact-verification-status-trusted 5)
(def ^:const contact-verification-status-untrustworthy 6)

(def ^:const activity-center-membership-status-idle 0)
(def ^:const activity-center-membership-status-pending 1)
(def ^:const activity-center-membership-status-accepted 2)
(def ^:const activity-center-membership-status-declined 3)

(def ^:const mute-for-15-mins-type 1)
(def ^:const mute-for-1-hour-type 2)
(def ^:const mute-for-8-hours-type 3)
(def ^:const mute-for-1-week 4)
(def ^:const mute-till-unmuted 5)
(def ^:const un-muted 0)

(def ^:const activity-center-mark-all-as-read-undo-time-limit-ms 4000)
(def ^:const activity-center-max-unread-count 99)

(def ^:const emoji-reaction-love 1)
(def ^:const emoji-reaction-thumbs-up 2)
(def ^:const emoji-reaction-thumbs-down 3)
(def ^:const emoji-reaction-laugh 4)
(def ^:const emoji-reaction-sad 5)
(def ^:const emoji-reaction-angry 6)

(def ^:const one-to-one-chat-type 1)
(def ^:const public-chat-type 2)
(def ^:const private-group-chat-type 3)
(def ^:const profile-chat-type 4)
(def ^:const timeline-chat-type 5)
(def ^:const community-chat-type 6)

(def ^:const contact-request-message-state-none 0)
(def ^:const contact-request-message-state-pending 1)
(def ^:const contact-request-message-state-accepted 2)
(def ^:const contact-request-message-state-declined 3)

(def request-to-join-pending-state 1)

(def reactions
  {emoji-reaction-love        :reaction/love
   emoji-reaction-thumbs-up   :reaction/thumbs-up
   emoji-reaction-thumbs-down :reaction/thumbs-down
   emoji-reaction-laugh       :reaction/laugh
   emoji-reaction-sad         :reaction/sad
   emoji-reaction-angry       :reaction/angry})

(def ^:const invitation-state-unknown 0)
(def ^:const invitation-state-requested 1)
(def ^:const invitation-state-rejected 2)
(def ^:const invitation-state-approved 3)
(def ^:const invitation-state-granted 4)
(def ^:const invitation-state-removed 5)

(def ^:const message-type-one-to-one 1)
(def ^:const message-type-public-group 2)
(def ^:const message-type-private-group 3)
(def ^:const message-type-private-group-system-message 4)
(def ^:const message-type-community-chat 5)
(def ^:const message-type-gap 6)

(def ^:const command-state-request-address-for-transaction 1)
(def ^:const command-state-request-address-for-transaction-declined 2)
(def ^:const command-state-request-address-for-transaction-accepted 3)
(def ^:const command-state-request-transaction 4)
(def ^:const command-state-request-transaction-declined 5)
(def ^:const command-state-transaction-pending 6)
(def ^:const command-state-transaction-sent 7)

(def ^:const profile-default-color :blue)
(def ^:const profile-name-max-length 24)

(def ^:const profile-pictures-show-to-contacts-only 1)
(def ^:const profile-pictures-show-to-everyone 2)
(def ^:const profile-pictures-show-to-none 3)

(def ^:const profile-pictures-visibility-contacts-only 1)
(def ^:const profile-pictures-visibility-everyone 2)
(def ^:const profile-pictures-visibility-none 3)

(def ^:const min-password-length 6)
(def ^:const max-group-chat-participants 20)
(def ^:const default-number-of-messages 20)
(def ^:const default-number-of-pin-messages 3)

(def ^:const mailserver-password "status-offline-inbox")

(def ^:const send-transaction-failed-parse-response 1)
(def ^:const send-transaction-failed-parse-params 2)
(def ^:const send-transaction-no-account-selected 3)
(def ^:const send-transaction-invalid-tx-sender 4)
(def ^:const send-transaction-err-decrypt 5)

(def ^:const web3-send-transaction "eth_sendTransaction")
(def ^:const web3-personal-sign "personal_sign")
(def ^:const web3-eth-sign "eth_sign")
(def ^:const web3-sign-typed-data "eth_signTypedData")
(def ^:const web3-sign-typed-data-v3 "eth_signTypedData_v3")
(def ^:const web3-sign-typed-data-v4 "eth_signTypedData_v4")

(def ^:const web3-keycard-sign-typed-data "keycard_signTypedData")

(def ^:const status-create-address "status_createaddress")

(def ^:const community-unknown-membership-access 0)
(def ^:const community-no-membership-access 1)
(def ^:const community-invitation-only-access 2)
(def ^:const community-on-request-access 3)

;; Community rules for joining
(def ^:const community-rule-ens-only "ens-only")

(def ^:const community-channel-access-no-membership 1)
(def ^:const community-channel-access-invitation-only 2)
(def ^:const community-channel-access-on-request 3)

(def ^:const community-request-to-join-state-pending 1)
(def ^:const community-request-to-join-state-declined 2)
(def ^:const community-request-to-join-state-accepted 3)
(def ^:const community-request-to-join-state-cancelled 4)

  ; BIP44 Wallet Root Key, the extended key from which any wallet can be derived
(def ^:const path-wallet-root "m/44'/60'/0'/0")
; EIP1581 Root Key, the extended key from which any whisper key/encryption key can be derived
(def ^:const path-eip1581 "m/43'/60'/1581'")
; BIP44-0 Wallet key, the default wallet key
(def ^:const path-default-wallet (str path-wallet-root "/0"))
; EIP1581 Chat Key 0, the default whisper key
(def ^:const path-whisper (str path-eip1581 "/0'/0"))

(def ^:const path-default-wallet-keyword (keyword path-default-wallet))
(def ^:const path-whisper-keyword (keyword path-whisper))
(def ^:const path-wallet-root-keyword (keyword path-wallet-root))
(def ^:const path-eip1581-keyword (keyword path-eip1581))

(def ^:const method-id-transfer "0xa9059cbb")
(def ^:const method-id-approve "0x095ea7b3")
(def ^:const method-id-approve-and-call "0xcae9ca51")

(def regx-emoji
  #"^((?:[\u261D\u26F9\u270A-\u270D]|\uD83C[\uDF85\uDFC2-\uDFC4\uDFC7\uDFCA-\uDFCC]|\uD83D[\uDC42\uDC43\uDC46-\uDC50\uDC66-\uDC69\uDC6E\uDC70-\uDC78\uDC7C\uDC81-\uDC83\uDC85-\uDC87\uDCAA\uDD74\uDD75\uDD7A\uDD90\uDD95\uDD96\uDE45-\uDE47\uDE4B-\uDE4F\uDEA3\uDEB4-\uDEB6\uDEC0\uDECC]|\uD83E[\uDD18-\uDD1C\uDD1E\uDD1F\uDD26\uDD30-\uDD39\uDD3D\uDD3E\uDDD1-\uDDDD])(?:\uD83C[\uDFFB-\uDFFF])?|(?:[\u231A\u231B\u23E9-\u23EC\u23F0\u23F3\u25FD\u25FE\u2614\u2615\u2648-\u2653\u267F\u2693\u26A1\u26AA\u26AB\u26BD\u26BE\u26C4\u26C5\u26CE\u26D4\u26EA\u26F2\u26F3\u26F5\u26FA\u26FD\u2705\u270A\u270B\u2728\u274C\u274E\u2753-\u2755\u2757\u2795-\u2797\u27B0\u27BF\u2B1B\u2B1C\u2B50\u2B55]|\uD83C[\uDC04\uDCCF\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE1A\uDE2F\uDE32-\uDE36\uDE38-\uDE3A\uDE50\uDE51\uDF00-\uDF20\uDF2D-\uDF35\uDF37-\uDF7C\uDF7E-\uDF93\uDFA0-\uDFCA\uDFCF-\uDFD3\uDFE0-\uDFF0\uDFF4\uDFF8-\uDFFF]|\uD83D[\uDC00-\uDC3E\uDC40\uDC42-\uDCFC\uDCFF-\uDD3D\uDD4B-\uDD4E\uDD50-\uDD67\uDD7A\uDD95\uDD96\uDDA4\uDDFB-\uDE4F\uDE80-\uDEC5\uDECC\uDED0-\uDED2\uDEEB\uDEEC\uDEF4-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])|(?:[#\*0-9\xA9\xAE\u203C\u2049\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE\u2600-\u2604\u260E\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640\u2642\u2648-\u2653\u2660\u2663\u2665\u2666\u2668\u267B\u267F\u2692-\u2697\u2699\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0-\u26F5\u26F7-\u26FA\u26FD\u2702\u2705\u2708-\u270D\u270F\u2712\u2714\u2716\u271D\u2721\u2728\u2733\u2734\u2744\u2747\u274C\u274E\u2753-\u2755\u2757\u2763\u2764\u2795-\u2797\u27A1\u27B0\u27BF\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55\u3030\u303D\u3297\u3299]|\uD83C[\uDC04\uDCCF\uDD70\uDD71\uDD7E\uDD7F\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE02\uDE1A\uDE2F\uDE32-\uDE3A\uDE50\uDE51\uDF00-\uDF21\uDF24-\uDF93\uDF96\uDF97\uDF99-\uDF9B\uDF9E-\uDFF0\uDFF3-\uDFF5\uDFF7-\uDFFF]|\uD83D[\uDC00-\uDCFD\uDCFF-\uDD3D\uDD49-\uDD4E\uDD50-\uDD67\uDD6F\uDD70\uDD73-\uDD7A\uDD87\uDD8A-\uDD8D\uDD90\uDD95\uDD96\uDDA4\uDDA5\uDDA8\uDDB1\uDDB2\uDDBC\uDDC2-\uDDC4\uDDD1-\uDDD3\uDDDC-\uDDDE\uDDE1\uDDE3\uDDE8\uDDEF\uDDF3\uDDFA-\uDE4F\uDE80-\uDEC5\uDECB-\uDED2\uDEE0-\uDEE5\uDEE9\uDEEB\uDEEC\uDEF0\uDEF3-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])\uFE0F|[\t-\r \xA0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF])+$")
(def regx-bold #"\*[^*]+\*")
(def regx-italic #"~[^~]+~")
(def regx-backquote #"`[^`]+`")
(def regx-universal-link #"((^https?://join.status.im/)|(^status-im://))[\x00-\x7F]+$")
(def regx-community-universal-link #"((^https?://join.status.im/)|(^status-im://))c/([\x00-\x7F]+)$")
(def regx-deep-link #"((^ethereum:.*)|(^status-im://[\x00-\x7F]+$))")
(def regx-ens #"^(?=.{5,255}$)([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.[a-zA-Z]{2,}$")

(def ^:const dapp-permission-contact-code "contact-code")
(def ^:const dapp-permission-web3 "web3")
(def ^:const dapp-permission-qr-code "qr-code")
(def ^:const api-response "api-response")
(def ^:const api-request "api-request")
(def ^:const history-state-changed "history-state-changed")
(def ^:const web3-send-async-read-only "web3-send-async-read-only")
(def ^:const web3-send-async-callback "web3-send-async-callback")
(def ^:const scan-qr-code "scan-qr-code")

(def ^:const faq "https://status.im/faq/")
(def ^:const faq-keycard (str faq "#keycard"))
(def ^:const keycard-integration-link "https://status.im/keycard-integration")

(def ^:const two-mins (* 2 60))
(def ^:const one-day (* 60 60 24))
(def ^:const three-days (* one-day 3))
(def ^:const one-week (* one-day 7))
(def ^:const one-month (* one-day 31))

(def ^:const privacy-policy-link "https://status.im/privacy-policy/")
(def ^:const terms-of-service-link "https://status.im/terms-of-use")
(def ^:const docs-link "https://status.im/docs/")
(def ^:const principles-link "https://our.status.im/our-principles/")

(def ^:const visibility-status-unknown 0)
(def ^:const visibility-status-automatic 1)
(def ^:const visibility-status-dnd 2)
(def ^:const visibility-status-always-online 3)
(def ^:const visibility-status-inactive 4)

(def ^:const wallet-connect-version-1 1)
(def ^:const wallet-connect-version-2 2)

(def ^:const sticker-pack-status-installed 1)
(def ^:const sticker-pack-status-pending 2)
(def ^:const sticker-pack-status-owned 3)

(def ^:const community-member-role-all 1)
(def ^:const community-member-role-manage-users 2)
(def ^:const community-member-role-moderator 3)

(def ^:const delete-message-undo-time-limit-ms 4000)
(def ^:const delete-message-for-me-undo-time-limit-ms 4000)

(def ^:const max-album-photos 6)
(def ^:const image-size 146)

(def album-image-sizes ; sometimes we subtract 1 or 0.5 for padding
  {2        {0 image-size
             1 image-size}
   3        {0 [(* image-size 2) (* image-size 1.25)]
             1 [(- image-size 0.5) (- (* image-size 0.75) 1)]
             2 [(- image-size 0.5) (- (* image-size 0.75) 1)]}
   4        {0 image-size
             1 image-size
             2 image-size
             3 image-size}
   5        {0 image-size
             1 image-size
             2 (- (* image-size 0.67) 1)
             3 (- (* image-size 0.67) 1)
             4 (- (* image-size 0.67) 1)}
   :default {0 image-size
             1 image-size
             2 (- (* image-size 0.5) 0.5)
             3 (- (* image-size 0.5) 0.5)
             4 (- (* image-size 0.5) 0.5)
             5 (- (* image-size 0.5) 0.5)}})

(def ^:const spam-message-frequency-threshold 4)
(def ^:const spam-interval-ms 1000)
(def ^:const default-cooldown-period-ms 10000)
(def ^:const cooldown-reset-threshold 3)
(def ^:const cooldown-periods-ms
  {1 2000
   2 5000
   3 10000})

;; any message that comes after this amount of ms will be grouped separately
(def ^:const group-ms 300000)

(def ^:const local-pairing-connection-string-identifier
  "If any string begins with cs we know its a connection string.
  This is useful when we read QR codes we know it is a connection string if it begins with this identifier.
  An example of a connection string is -> cs2:5vd6J6:Jfc:27xMmHKEYwzRGXcvTtuiLZFfXscMx4Mz8d9wEHUxDj4p7:EG7Z13QScfWBJNJ5cprszzDQ5fBVsYMirXo8MaQFJvpF:3 "
  "cs")

(def ^:const local-pairing-role-sender "sender")
(def ^:const local-pairing-role-receiver "receiver")

;; sender and receiver events
(def ^:const local-pairing-event-connection-success "connection-success")
(def ^:const local-pairing-event-connection-error "connection-error")
(def ^:const local-pairing-event-transfer-success "transfer-success")
(def ^:const local-pairing-event-transfer-error "transfer-error")

;; receiver events
(def ^:const local-pairing-event-received-account "received-account")
(def ^:const local-pairing-event-process-success "process-success")
(def ^:const local-pairing-event-process-error "process-error")
(def ^:const local-pairing-event-received-installation "received-installation")

(def ^:const local-pairing-event-errors
  #{local-pairing-event-connection-error
    local-pairing-event-transfer-error
    local-pairing-event-process-error})

(def ^:const local-pairing-action-connect 1)
(def ^:const local-pairing-action-pairing-account 2)
(def ^:const local-pairing-action-sync-device 3)
(def ^:const local-pairing-action-pairing-installation 4)

(def ^:const serialization-key
  "We pass this serialization key as a parameter to MultiformatSerializePublicKey
  function at status-go, This key determines the output base of the serialization.
  according to https://specs.status.im/spec/2#public-key-serialization we serialize
  keys with base58btc encoding"
  "z")

(def ^:const deserialization-key
  "We pass this deserialization key as a parameter to MultiformatDeserializePublicKey
  function at status-go, This key determines the output base of the deserialization.
  according to https://specs.status.im/spec/2#public-key-serialization we deserialize
  keys with base16 hexadecimal encoding"
  "f")

(def ^:const multi-code-prefix
  "We prefix our keys with 0xe701 prior to serialisation them"
  "0xe701")

(def ^:const theme-type-system 0)
(def ^:const theme-type-light 1)
(def ^:const theme-type-dark 2)
(def ^:const bottom-sheet-animation-delay 450)

(def ^:const local-pair-event-process-success "process-success")
(def ^:const local-pair-event-process-error "process-error")
(def ^:const local-pair-action-connect 1)
(def ^:const local-pair-action-pairing-account 2)
(def ^:const local-pair-action-sync-device 3)

(def ^:const everyone-mention-id "0x00001")

(def ^:const empty-category-id :communities/not-categorized)

(def ^:const seed-phrase-valid-length #{12 18 24})

(def ^:const auth-method-password "password")
(def ^:const auth-method-biometric "biometric")
(def ^:const auth-method-biometric-prepare "biometric-prepare")
(def ^:const auth-method-none "none")

(def ^:const onboarding-generating-keys-animation-duration-ms 7000)
(def ^:const onboarding-generating-keys-navigation-retry-ms 3000)

(def ^:const image-description-in-lightbox? false)

(def ^:const audio-max-duration-ms 120000)

(def ^:const onboarding-modal-animation-duration 300)
(def ^:const onboarding-modal-animation-delay 400)

(def ^:const initials-avatar-font-conf
  "we pass absolute font file path and uppercase ratio to status-go for media
  server to serve initials avatar image
  `:uppercase-ratio` is uppercase-height/line-height for Inter-Medium"
  {:ios             "Inter-Medium.otf"
   :android         "Inter-Medium.ttf"
   :uppercase-ratio 0.603861228044709})

(def ^:const ens-action-type-register 1)
(def ^:const ens-action-type-set-pub-key 2)

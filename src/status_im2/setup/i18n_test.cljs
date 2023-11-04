(ns status-im2.setup.i18n-test
  (:require
    [cljs.spec.alpha :as spec]
    [cljs.test :refer-macros [deftest is]]
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im2.setup.i18n-resources :as i18n-resources]
    [utils.i18n :as i18n]))

(defn- js-translations->namespaced-keys
  [js-translations]
  (->> js-translations
       js->clj
       keys
       (map (partial keyword "t"))
       set))

;; english as source of truth
(def labels
  (-> i18n-resources/translations-by-locale
      :en
      js-translations->namespaced-keys))

(spec/def ::label labels)
(spec/def ::labels (spec/coll-of ::label :kind set? :into #{}))

(defn labels-for-all-locales
  []
  (->> i18n-resources/translations-by-locale
       (mapcat (comp js-translations->namespaced-keys val))
       set))

;; checkpoints

;; Checkpoints specify milestones for locales.
;;
;; With milestones we can ensure that expected supported languages
;; are actually supported, and visualize the translation state for
;; the rest of locales according to these milestones.
;;
;; Checkpoints are defined by indicating the labels that need to be present
;; in a locale to achieve that checkpoint.
;;
;; We need to define the checkpoint that needs to be achieved for
;; a locale to be considered supported. This is why as we develop
;; we add translations, so we need to be defining a new target
;; for supported languages to achieve.
;;
;; Checkpoints are only used in dev and test. In dev when we want to
;; manually check the state of checkpoints for locales, and in test
;; to automatically check supported locales against the target checkpoint.

(spec/def ::checkpoint.id keyword?)
(spec/def ::checkpoint-defs (spec/map-of ::checkpoint.id ::labels))

;; We define here the labels for the first specified checkpoint.
(def checkpoint-1-0-0-rc1-labels
  #{:t/You
    :t/about-app
    :t/about-key-storage-content
    :t/about-key-storage-title
    :t/about-names-content
    :t/about-names-title
    :t/access-key
    :t/account-added
    :t/account-color
    :t/account-name
    :t/account-settings
    :t/accounts
    :t/active-online
    :t/active-unknown
    :t/add
    :t/add-a-watch-account
    :t/add-account
    :t/add-account-description
    :t/add-account-incorrect-password
    :t/add-an-account
    :t/add-bootnode
    :t/add-contact
    :t/add-custom-token
    :t/add-mailserver
    :t/add-members
    :t/add-network
    :t/add-to-contacts
    :t/address
    :t/advanced
    :t/advanced-settings
    :t/agree-by-continuing
    :t/all
    :t/allow
    :t/allowing-authorizes-this-dapp
    :t/already-have-asset
    :t/amount
    :t/are-you-sure-description
    :t/are-you-sure?
    :t/ask-in-status
    :t/at
    :t/authorize
    :t/available
    :t/available-participants
    :t/back
    :t/back-up-seed-phrase
    :t/back-up-your-seed-phrase
    :t/backup-recovery-phrase
    :t/balance
    :t/begin-set-up
    :t/biometric-auth-android-sensor-desc
    :t/biometric-auth-android-sensor-error-desc
    :t/biometric-auth-android-title
    :t/biometric-auth-confirm-logout
    :t/biometric-auth-confirm-message
    :t/biometric-auth-confirm-title
    :t/biometric-auth-confirm-try-again
    :t/biometric-auth-error
    :t/biometric-auth-login-error-title
    :t/biometric-auth-login-ios-fallback-label
    :t/biometric-auth-reason-login
    :t/biometric-auth-reason-verify
    :t/blank-keycard-text
    :t/blank-keycard-title
    :t/block
    :t/block-contact
    :t/block-contact-details
    :t/blocked-users
    :t/bootnode-address
    :t/bootnode-details
    :t/bootnode-format
    :t/bootnodes
    :t/bootnodes-enabled
    :t/bootnodes-settings
    :t/browsed-websites
    :t/browser
    :t/browser-not-secure
    :t/browser-secure
    :t/browsers
    :t/browsing-cancel
    :t/browsing-open-in-android-web-browser
    :t/browsing-open-in-ios-web-browser
    :t/browsing-open-in-status
    :t/browsing-site-blocked-description1
    :t/browsing-site-blocked-description2
    :t/browsing-site-blocked-go-back
    :t/browsing-site-blocked-title
    :t/browsing-title
    :t/camera-access-error
    :t/can-not-add-yourself
    :t/cancel
    :t/cancel-keycard-setup
    :t/cannot-read-card
    :t/cannot-use-default-pin
    :t/card-is-blank
    :t/card-reseted
    :t/card-unpaired
    :t/change-fleet
    :t/change-log-level
    :t/change-logging-enabled
    :t/change-passcode
    :t/change-password
    :t/change-pin
    :t/changed-amount-warning
    :t/changed-asset-warning
    :t/chaos-mode
    :t/chaos-unicorn-day
    :t/chaos-unicorn-day-details
    :t/chat
    :t/chat-key
    :t/chat-name
    :t/chat-settings
    :t/chats
    :t/check-your-recovery-phrase
    :t/choose-authentication-method
    :t/clear
    :t/clear-history
    :t/clear-history-action
    :t/clear-history-confirmation
    :t/clear-history-confirmation-content
    :t/clear-history-title
    :t/close-app-button
    :t/close-app-content
    :t/close-app-title
    :t/command-button-send
    :t/complete-hardwallet-setup
    :t/completed
    :t/confirm
    :t/confirmations
    :t/confirmations-helper-text
    :t/connect
    :t/connect-mailserver-content
    :t/connected
    :t/connecting
    :t/connecting-requires-login
    :t/connection-with-the-card-lost
    :t/connection-with-the-card-lost-setup-text
    :t/connection-with-the-card-lost-text
    :t/contact-code
    :t/contact-s
    :t/contacts
    :t/continue
    :t/contract-address
    :t/contract-interaction
    :t/copy-info
    :t/copy-qr
    :t/copy-to-clipboard
    :t/copy-transaction-hash
    :t/cost-fee
    :t/counter-9-plus
    :t/create
    :t/create-a-pin
    :t/create-group-chat
    :t/create-multiaccount
    :t/create-new-key
    :t/create-pin
    :t/create-pin-description
    :t/created-group-chat-description
    :t/cryptokitty-name
    :t/currency
    :t/currency-display-name-aed
    :t/currency-display-name-afn
    :t/currency-display-name-ars
    :t/currency-display-name-aud
    :t/currency-display-name-bbd
    :t/currency-display-name-bdt
    :t/currency-display-name-bgn
    :t/currency-display-name-bhd
    :t/currency-display-name-bnd
    :t/currency-display-name-bob
    :t/currency-display-name-brl
    :t/currency-display-name-btn
    :t/currency-display-name-cad
    :t/currency-display-name-chf
    :t/currency-display-name-clp
    :t/currency-display-name-cny
    :t/currency-display-name-cop
    :t/currency-display-name-crc
    :t/currency-display-name-czk
    :t/currency-display-name-dkk
    :t/currency-display-name-dop
    :t/currency-display-name-egp
    :t/currency-display-name-etb
    :t/currency-display-name-eur
    :t/currency-display-name-gbp
    :t/currency-display-name-gel
    :t/currency-display-name-ghs
    :t/currency-display-name-hkd
    :t/currency-display-name-hrk
    :t/currency-display-name-huf
    :t/currency-display-name-idr
    :t/currency-display-name-ils
    :t/currency-display-name-inr
    :t/currency-display-name-isk
    :t/currency-display-name-jmd
    :t/currency-display-name-jpy
    :t/currency-display-name-kes
    :t/currency-display-name-krw
    :t/currency-display-name-kwd
    :t/currency-display-name-kzt
    :t/currency-display-name-lkr
    :t/currency-display-name-mad
    :t/currency-display-name-mdl
    :t/currency-display-name-mur
    :t/currency-display-name-mwk
    :t/currency-display-name-mxn
    :t/currency-display-name-myr
    :t/currency-display-name-mzn
    :t/currency-display-name-nad
    :t/currency-display-name-ngn
    :t/currency-display-name-nok
    :t/currency-display-name-npr
    :t/currency-display-name-nzd
    :t/currency-display-name-omr
    :t/currency-display-name-pen
    :t/currency-display-name-pgk
    :t/currency-display-name-php
    :t/currency-display-name-pkr
    :t/currency-display-name-pln
    :t/currency-display-name-pyg
    :t/currency-display-name-qar
    :t/currency-display-name-ron
    :t/currency-display-name-rsd
    :t/currency-display-name-rub
    :t/currency-display-name-sar
    :t/currency-display-name-sek
    :t/currency-display-name-sgd
    :t/currency-display-name-thb
    :t/currency-display-name-try
    :t/currency-display-name-ttd
    :t/currency-display-name-twd
    :t/currency-display-name-tzs
    :t/currency-display-name-uah
    :t/currency-display-name-ugx
    :t/currency-display-name-usd
    :t/currency-display-name-uyu
    :t/currency-display-name-vef
    :t/currency-display-name-vnd
    :t/currency-display-name-zar
    :t/current-network
    :t/current-pin
    :t/current-pin-description
    :t/custom
    :t/custom-networks
    :t/dapp
    :t/dapp-would-like-to-connect-wallet
    :t/dapps
    :t/dapps-permissions
    :t/data
    :t/datetime-ago
    :t/datetime-ago-format
    :t/datetime-day
    :t/datetime-hour
    :t/datetime-minute
    :t/datetime-second
    :t/datetime-today
    :t/datetime-yesterday
    :t/decimals
    :t/decryption-failed-content
    :t/default
    :t/delete
    :t/delete-and-leave-group
    :t/delete-bootnode
    :t/delete-bootnode-are-you-sure
    :t/delete-bootnode-title
    :t/delete-chat
    :t/delete-chat-confirmation
    :t/delete-confirmation
    :t/delete-mailserver
    :t/delete-mailserver-are-you-sure
    :t/delete-mailserver-title
    :t/delete-message
    :t/delete-my-account
    :t/delete-network-confirmation
    :t/delete-network-error
    :t/delete-network-title
    :t/deny
    :t/description
    :t/dev-mode
    :t/dev-mode-settings
    :t/device-syncing
    :t/devices
    :t/disable
    :t/disabled
    :t/disconnected
    :t/discover
    :t/dismiss
    :t/done
    :t/edit
    :t/edit-profile
    :t/empty-chat-description
    :t/empty-chat-description-one-to-one
    :t/empty-chat-description-public
    :t/empty-chat-description-public-share-this
    :t/enable
    :t/encrypt-with-password
    :t/ens-10-SNT
    :t/ens-add-username
    :t/ens-agree-to
    :t/ens-chat-settings
    :t/ens-custom-domain
    :t/ens-custom-username-hints
    :t/ens-custom-username-taken
    :t/ens-deposit
    :t/ens-displayed-with
    :t/ens-get-name
    :t/ens-got-it
    :t/ens-locked
    :t/ens-network-restriction
    :t/ens-no-usernames
    :t/ens-powered-by
    :t/ens-primary-username
    :t/ens-register
    :t/ens-registration-failed
    :t/ens-registration-failed-title
    :t/ens-release-username
    :t/ens-remove-hints
    :t/ens-remove-username
    :t/ens-saved
    :t/ens-saved-title
    :t/ens-show-username
    :t/ens-terms-header
    :t/ens-terms-point-1
    :t/ens-terms-point-10
    :t/ens-terms-point-2
    :t/ens-terms-point-3
    :t/ens-terms-point-4
    :t/ens-terms-point-5
    :t/ens-terms-point-6
    :t/ens-terms-point-7
    :t/ens-terms-point-8
    :t/ens-terms-point-9
    :t/ens-terms-registration
    :t/ens-test-message
    :t/ens-transaction-pending
    :t/ens-understand
    :t/ens-username
    :t/ens-username-connected
    :t/ens-username-hints
    :t/ens-username-invalid
    :t/ens-username-owned
    :t/ens-username-available
    :t/ens-username-taken
    :t/ens-usernames
    :t/ens-usernames-details
    :t/wallet-address
    :t/ens-want-custom-domain
    :t/ens-want-domain
    :t/ens-welcome-hints
    :t/ens-welcome-point-customize
    :t/ens-welcome-point-customize-title
    :t/ens-welcome-point-simplify
    :t/ens-welcome-point-simplify-title
    :t/ens-welcome-point-receive
    :t/ens-welcome-point-receive-title
    :t/ens-welcome-point-register
    :t/ens-welcome-point-register-title
    :t/ens-welcome-point-verify
    :t/ens-welcome-point-verify-title
    :t/ens-your-username
    :t/ens-your-usernames
    :t/ens-your-your-name
    :t/enter-12-words
    :t/enter-contact-code
    :t/enter-pair-code
    :t/enter-pair-code-description
    :t/enter-password
    :t/enter-pin
    :t/enter-puk-code
    :t/enter-puk-code-description
    :t/enter-seed-phrase
    :t/enter-url
    :t/enter-word
    :t/enter-your-code
    :t/enter-your-password
    :t/error
    :t/error-unable-to-get-balance
    :t/error-unable-to-get-prices
    :t/error-unable-to-get-token-balance
    :t/errors
    :t/eth
    :t/ethereum-node-started-incorrectly-description
    :t/ethereum-node-started-incorrectly-title
    :t/etherscan-lookup
    :t/export-account
    :t/failed
    :t/faq
    :t/fetch-messages
    :t/find
    :t/finish
    :t/finishing-card-setup
    :t/fleet
    :t/fleet-settings
    :t/from
    :t/gas-limit
    :t/gas-price
    :t/gas-used
    :t/generate-a-key
    :t/generate-a-new-account
    :t/generate-a-new-key
    :t/generate-account
    :t/generate-new-key
    :t/generating-codes-for-pairing
    :t/generating-keys
    :t/generating-mnemonic
    :t/get-started
    :t/get-status-at
    :t/get-stickers
    :t/go-to-settings
    :t/got-it
    :t/group-chat
    :t/group-chat-admin
    :t/group-chat-admin-added
    :t/group-chat-created
    :t/group-chat-decline-invitation
    :t/group-chat-member-added
    :t/group-chat-member-joined
    :t/group-chat-member-removed
    :t/group-chat-members-count
    :t/group-chat-name-changed
    :t/group-chat-no-contacts
    :t/group-info
    :t/gwei
    :t/hash
    :t/help
    :t/help-capitalized
    :t/help-center
    :t/hide-content-when-switching-apps
    :t/history
    :t/history-nodes
    :t/hold-card
    :t/home
    :t/hooks
    :t/identifier
    :t/image-remove-current
    :t/image-source-gallery
    :t/image-source-make-photo
    :t/image-source-title
    :t/in-contacts
    :t/incoming
    :t/incorrect-code
    :t/initialization
    :t/install
    :t/intro-message1
    :t/intro-privacy-policy-note1
    :t/intro-privacy-policy-note2
    :t/intro-text
    :t/intro-text1
    :t/intro-text2
    :t/intro-text3
    :t/intro-title1
    :t/intro-title2
    :t/intro-title3
    :t/intro-wizard-text1
    :t/intro-wizard-text2
    :t/intro-wizard-text3
    :t/intro-wizard-text4
    :t/intro-wizard-text6
    :t/intro-wizard-title-alt4
    :t/intro-wizard-title-alt5
    :t/intro-wizard-title1
    :t/intro-wizard-title2
    :t/intro-wizard-title3
    :t/intro-wizard-title4
    :t/intro-wizard-title5
    :t/intro-wizard-title6
    :t/invalid-address-qr-code
    :t/invalid-format
    :t/invalid-key-confirm
    :t/invalid-key-content
    :t/invalid-number
    :t/invalid-pairing-password
    :t/invalid-range
    :t/invite-friends
    :t/invited
    :t/join-group-chat
    :t/join-group-chat-description
    :t/joined-group-chat-description
    :t/key
    :t/keycard
    :t/keycard-applet-install-instructions
    :t/keycard-blocked
    :t/keycard-cancel-setup-text
    :t/keycard-cancel-setup-title
    :t/keycard-desc
    :t/keycard-has-multiaccount-on-it
    :t/keycard-onboarding-finishing-header
    :t/keycard-onboarding-intro-header
    :t/keycard-onboarding-intro-text
    :t/keycard-onboarding-pairing-header
    :t/keycard-onboarding-preparing-header
    :t/keycard-onboarding-puk-code-header
    :t/keycard-onboarding-recovery-phrase-description
    :t/keycard-onboarding-recovery-phrase-header
    :t/keycard-onboarding-recovery-phrase-text
    :t/keycard-onboarding-start-header
    :t/keycard-onboarding-start-step1
    :t/keycard-onboarding-start-step1-text
    :t/keycard-onboarding-start-step2
    :t/keycard-onboarding-start-step2-text
    :t/keycard-onboarding-start-step3
    :t/keycard-onboarding-start-step3-text
    :t/keycard-onboarding-start-text
    :t/keycard-recovery-intro-button-text
    :t/keycard-recovery-intro-header
    :t/keycard-recovery-intro-text
    :t/keycard-recovery-no-key-header
    :t/keycard-recovery-no-key-text
    :t/keycard-recovery-phrase-confirm-header
    :t/keycard-recovery-phrase-confirmation-text
    :t/keycard-recovery-phrase-confirmation-title
    :t/keycard-recovery-success-header
    :t/keycard-unauthorized-operation
    :t/language
    :t/learn-more
    :t/learn-more-about-keycard
    :t/leave
    :t/leave-group
    :t/left
    :t/les-ulc
    :t/linked-on
    :t/load-messages-before
    :t/load-more-messages
    :t/loading
    :t/log-level
    :t/log-level-settings
    :t/logging
    :t/logging-enabled
    :t/login-pin-description
    :t/logout
    :t/logout-app-content
    :t/logout-are-you-sure
    :t/logout-title
    :t/mailserver-address
    :t/mailserver-automatic
    :t/mailserver-connection-error
    :t/mailserver-details
    :t/mailserver-error-content
    :t/mailserver-error-title
    :t/mailserver-format
    :t/mailserver-pick-another
    :t/mailserver-reconnect
    :t/mailserver-request-error-content
    :t/mailserver-request-error-status
    :t/mailserver-request-error-title
    :t/mailserver-request-retry
    :t/mailserver-retry
    :t/main-currency
    :t/main-networks
    :t/main-wallet
    :t/mainnet-network
    :t/make-admin
    :t/members
    :t/members-active
    :t/members-active-none
    :t/members-title
    :t/message
    :t/message-not-sent
    :t/message-options-cancel
    :t/message-reply
    :t/data-syncing
    :t/messages
    :t/might-break
    :t/migrations-failed-content
    :t/mobile-network-ask-me
    :t/mobile-network-continue-syncing
    :t/mobile-network-continue-syncing-details
    :t/mobile-network-go-to-settings
    :t/mobile-network-settings
    :t/mobile-network-sheet-configure
    :t/mobile-network-sheet-offline
    :t/mobile-network-sheet-offline-details
    :t/mobile-network-sheet-remember-choice
    :t/mobile-network-sheet-settings
    :t/mobile-network-start-syncing
    :t/mobile-network-stop-syncing
    :t/mobile-network-stop-syncing-details
    :t/mobile-network-use-mobile
    :t/mobile-network-use-mobile-data
    :t/mobile-network-use-wifi
    :t/mobile-syncing-sheet-details
    :t/mobile-syncing-sheet-title
    :t/more
    :t/multiaccounts-recover-enter-phrase-text
    :t/multiaccounts-recover-enter-phrase-title
    :t/name
    :t/name-of-token
    :t/need-help
    :t/network
    :t/network-chain
    :t/network-details
    :t/network-fee
    :t/network-id
    :t/network-invalid-network-id
    :t/network-invalid-status-code
    :t/network-invalid-url
    :t/network-settings
    :t/new
    :t/new-chat
    :t/new-contact
    :t/new-contract
    :t/new-group
    :t/new-group-chat
    :t/new-network
    :t/new-pin-description
    :t/new-public-group-chat
    :t/next
    :t/no
    :t/no-collectibles
    :t/no-contacts
    :t/no-keycard-applet-on-card
    :t/no-messages
    :t/no-pairing-slots-available
    :t/no-result
    :t/no-tokens-found
    :t/node-info
    :t/node-version
    :t/nonce
    :t/none
    :t/not-applicable
    :t/not-keycard-text
    :t/not-keycard-title
    :t/notifications
    :t/notify
    :t/off
    :t/offline
    :t/ok
    :t/ok-continue
    :t/ok-got-it
    :t/okay
    :t/on
    :t/open
    :t/open-dapp
    :t/open-dapp-store
    :t/open-nfc-settings
    :t/open-on-block-explorer
    :t/optional
    :t/outgoing
    :t/pair
    :t/pair-card
    :t/pair-code
    :t/pair-code-explanation
    :t/pair-this-card
    :t/pair-this-device
    :t/pair-this-device-description
    :t/paired-devices
    :t/pairing
    :t/pairing-card
    :t/pairing-go-to-installation
    :t/pairing-maximum-number-reached-content
    :t/pairing-maximum-number-reached-title
    :t/pairing-new-installation-detected-content
    :t/pairing-new-installation-detected-title
    :t/pairing-no-info
    :t/pairing-please-set-a-name
    :t/passphrase
    :t/password
    :t/password-description
    :t/password-placeholder2
    :t/password_error1
    :t/paste
    :t/paste-json
    :t/pay-to-chat
    :t/peers
    :t/pending
    :t/pending-confirmation
    :t/permissions
    :t/phone-e164
    :t/photos-access-error
    :t/pin-changed
    :t/pin-code
    :t/pin-mismatch
    :t/preview-privacy
    :t/privacy
    :t/privacy-and-security
    :t/privacy-policy
    :t/processing
    :t/product-information
    :t/profile
    :t/public-chat
    :t/public-chats
    :t/public-group-status
    :t/public-group-topic
    :t/public-key
    :t/puk-and-pairing-codes-displayed
    :t/puk-code
    :t/puk-code-explanation
    :t/puk-mismatch
    :t/quiet-days
    :t/quiet-hours
    :t/re-encrypt-key
    :t/receive
    :t/receive-transaction
    :t/recent
    :t/recent-recipients
    :t/recently-used-stickers
    :t/recipient
    :t/recipient-code
    :t/recover
    :t/recover-key
    :t/recover-keycard-multiaccount-not-supported
    :t/recover-with-keycard
    :t/recovering-key
    :t/recovery-confirm-phrase
    :t/recovery-phrase
    :t/recovery-success-text
    :t/recovery-typo-dialog-description
    :t/recovery-typo-dialog-title
    :t/remember-me
    :t/remind-me-later
    :t/remove
    :t/remove-from-chat
    :t/remove-network
    :t/remove-token
    :t/removed
    :t/repeat-pin
    :t/report-bug-email-template
    :t/request-transaction
    :t/required-field
    :t/resend-message
    :t/reset-card
    :t/reset-card-description
    :t/retry
    :t/revoke-access
    :t/rpc-url
    :t/save
    :t/save-password
    :t/save-password-unavailable
    :t/save-password-unavailable-android
    :t/scan-qr
    :t/scan-qr-code
    :t/search
    :t/secret-keys-confirmation-text
    :t/secret-keys-confirmation-title
    :t/security
    :t/see-details
    :t/see-it-again
    :t/select-chat
    :t/selected
    :t/send-logs
    :t/send-logs-to
    :t/send-message
    :t/send-request
    :t/send-request-amount
    :t/send-request-amount-max-decimals
    :t/send-request-unknown-token
    :t/send-sending-to
    :t/send-transaction
    :t/sending
    :t/sent-at
    :t/set-a-topic
    :t/set-currency
    :t/set-dapp-access-permissions
    :t/settings
    :t/share
    :t/share-address
    :t/share-chat
    :t/share-contact-code
    :t/share-dapp-text
    :t/share-link
    :t/share-my-profile
    :t/share-profile
    :t/share-profile-link
    :t/share-public-chat-text
    :t/sharing-copied-to-clipboard
    :t/sharing-copy-to-clipboard
    :t/sharing-share
    :t/show-less
    :t/show-more
    :t/show-qr
    :t/sign-in
    :t/sign-message
    :t/sign-out
    :t/sign-with
    :t/sign-with-password
    :t/sign-you-in
    :t/signing
    :t/signing-a-message
    :t/signing-phrase
    :t/something-went-wrong
    :t/soon
    :t/specify-address
    :t/specify-name
    :t/specify-network-id
    :t/specify-rpc-url
    :t/start-chat
    :t/start-conversation
    :t/start-group-chat
    :t/start-new-chat
    :t/status
    :t/status-confirmed
    :t/status-keycard
    :t/status-hardwallet
    :t/status-not-sent-click
    :t/status-not-sent-tap
    :t/status-pending
    :t/status-sent
    :t/status-tx-not-found
    :t/step-i-of-n
    :t/sticker-market
    :t/submit
    :t/submit-bug
    :t/success
    :t/symbol
    :t/sync-all-devices
    :t/sync-in-progress
    :t/sync-settings
    :t/sync-synced
    :t/syncing-devices
    :t/tag-was-lost
    :t/test-networks
    :t/text-input-disabled
    :t/this-device
    :t/this-device-desc
    :t/this-is-you-signing
    :t/this-will-take-few-seconds
    :t/three-words-description
    :t/three-words-description-2
    :t/to
    :t/to-block
    :t/to-encrypt-enter-password
    :t/to-see-this-message
    :t/token-auto-validate-decimals-error
    :t/token-auto-validate-name-error
    :t/token-auto-validate-symbol-error
    :t/token-details
    :t/topic-name-error
    :t/transaction
    :t/transaction-description
    :t/transaction-details
    :t/transaction-failed
    :t/transaction-history
    :t/transaction-request
    :t/transaction-sent
    :t/transactions
    :t/transactions-filter-select-all
    :t/transactions-filter-title
    :t/type
    :t/transactions-history
    :t/transactions-history-empty
    :t/transactions-sign
    :t/tribute-required-by-multiaccount
    :t/tribute-state-paid
    :t/tribute-state-pending
    :t/tribute-state-required
    :t/tribute-to-talk
    :t/tribute-to-talk-add-friends
    :t/tribute-to-talk-are-you-friends
    :t/tribute-to-talk-ask-to-be-added
    :t/tribute-to-talk-contact-received-your-tribute
    :t/tribute-to-talk-desc
    :t/tribute-to-talk-disabled
    :t/tribute-to-talk-disabled-note
    :t/tribute-to-talk-enabled
    :t/tribute-to-talk-finish-desc
    :t/tribute-to-talk-learn-more-1
    :t/tribute-to-talk-learn-more-2
    :t/tribute-to-talk-learn-more-3
    :t/tribute-to-talk-paywall-learn-more-1
    :t/tribute-to-talk-paywall-learn-more-2
    :t/tribute-to-talk-paywall-learn-more-3
    :t/tribute-to-talk-pending
    :t/tribute-to-talk-pending-note
    :t/tribute-to-talk-removing-note
    :t/tribute-to-talk-set-snt-amount
    :t/tribute-to-talk-signing
    :t/tribute-to-talk-transaction-failed-note
    :t/tribute-to-talk-tribute-received1
    :t/tribute-to-talk-tribute-received2
    :t/tribute-to-talk-you-require-snt
    :t/try-again
    :t/turn-nfc-on
    :t/type-a-message
    :t/ulc-enabled
    :t/unable-to-read-this-code
    :t/unblock-contact
    :t/unknown-status-go-error
    :t/unlock
    :t/unpair-card
    :t/unpair-card-confirmation
    :t/unpaired-keycard-text
    :t/unpaired-keycard-title
    :t/update
    :t/url
    :t/usd-currency
    :t/use-valid-contact-code
    :t/validation-amount-invalid-number
    :t/validation-amount-is-too-precise
    :t/version
    :t/view-cryptokitties
    :t/view-cryptostrikers
    :t/view-etheremon
    :t/view-gitcoin
    :t/view-profile
    :t/view-signing
    :t/view-superrare
    :t/waiting-for-wifi
    :t/waiting-for-wifi-change
    :t/waiting-to-sign
    :t/wallet
    :t/wallet-asset
    :t/wallet-assets
    :t/wallet-backup-recovery-title
    :t/wallet-choose-recipient
    :t/wallet-collectibles
    :t/wallet-insufficient-funds
    :t/wallet-insufficient-gas
    :t/wallet-invalid-address
    :t/wallet-invalid-address-checksum
    :t/wallet-invalid-chain-id
    :t/wallet-manage-assets
    :t/wallet-request
    :t/wallet-send
    :t/wallet-send-min-wei
    :t/wallet-settings
    :t/wallet-total-value
    :t/wallet-transaction-total-fee
    :t/wants-to-access-profile
    :t/warning
    :t/warning-message
    :t/web-view-error
    :t/welcome-screen-text
    :t/welcome-to-status
    :t/welcome-to-status-description
    :t/word-n
    :t/word-n-description
    :t/words-n
    :t/write-down-and-store-securely
    :t/wrong-address
    :t/wrong-card
    :t/wrong-card-text
    :t/wrong-contract
    :t/wrong-keycard-text
    :t/wrong-keycard-title
    :t/wrong-password
    :t/wrong-word
    :t/yes
    :t/you
    :t/you-already-have-an-asset
    :t/you-are-all-set
    :t/you-are-all-set-description
    :t/you-can-change-account
    :t/you-dont-have-stickers
    :t/your-contact-code
    :t/your-data-belongs-to-you
    :t/your-data-belongs-to-you-description
    :t/your-recovery-phrase
    :t/your-recovery-phrase-description
    :t/empty-keycard
    :t/user-keycard})

;; NOTE: the rest checkpoints are based on the previous one, defined
;;       like this:
;; (def checkpoint-2-labels (set/union checkpoint-1-labels #{:foo :bar})
;; (def checkpoint-3-labels (set/union checkpoint-2-labels #{:baz})

;; NOTE: This defines the scope of each checkpoint. To support a checkpoint,
;;       change the var `checkpoint-to-consider-locale-supported` a few lines
;;       below.
(def checkpoints-def
  (spec/assert ::checkpoint-defs
               {::checkpoint-1-0-0-rc1 checkpoint-1-0-0-rc1-labels}))
(def checkpoints (set (keys checkpoints-def)))

(spec/def ::checkpoint checkpoints)

(def checkpoint-to-consider-locale-supported ::checkpoint-1-0-0-rc1)

(defn checkpoint->labels
  [checkpoint]
  (get checkpoints-def checkpoint))

(defn checkpoint-val-to-compare
  [c]
  (-> c name (string/replace #"^.*\|" "") js/parseInt))

(defn >checkpoints
  [& cs]
  (apply > (map checkpoint-val-to-compare cs)))

;; locales

(def locales (set (keys i18n-resources/translations-by-locale)))

(spec/def ::locale locales)
(spec/def ::locales (spec/coll-of ::locale :kind set? :into #{}))

(defn locale->labels
  [locale]
  (-> i18n-resources/translations-by-locale
      (get locale)
      js-translations->namespaced-keys))

(defn locale->checkpoint
  [locale]
  (let [locale-labels (locale->labels locale)
        checkpoint    (->> checkpoints-def
                           (filter (fn [[_ checkpoint-labels]]
                                     (set/subset? checkpoint-labels locale-labels)))
                           ffirst)]
    checkpoint))

(defn locale-is-supported-based-on-translations?
  [locale]
  (let [c (locale->checkpoint locale)]
    (and c
         (or (= c checkpoint-to-consider-locale-supported)
             (>checkpoints checkpoint-to-consider-locale-supported c)))))

(defn actual-supported-locales
  []
  (->> locales
       (filter locale-is-supported-based-on-translations?)
       set))

;; NOTE: Add new locale keywords here to indicate support for them.
#_(def supported-locales
    (spec/assert ::locales
                 #{:fr
                   :zh
                   :zh-hans
                   :zh-hans-cn
                   :zh-hans-mo
                   :zh-hant
                   :zh-hant-sg
                   :zh-hant-hk
                   :zh-hant-tw
                   :zh-hant-mo
                   :zh-hant-cn
                   :sr-RS_#Cyrl
                   :el
                   :en
                   :de
                   :lt
                   :sr-RS_#Latn
                   :sr
                   :sv
                   :ja
                   :uk}))
(def supported-locales (spec/assert ::locales #{:en}))

(spec/def ::supported-locale supported-locales)
(spec/def ::supported-locales (spec/coll-of ::supported-locale :kind set? :into #{}))

(deftest label-options
  (is (not (nil? (:key (i18n/label-options {:key nil}))))))

(deftest locales-only-have-existing-tran-ids
  (is (spec/valid? ::labels (labels-for-all-locales))
      (->> locales
           (remove #(spec/valid? ::labels (locale->labels %)))
           (map (fn [l]
                  (str "Extra translations in locale "
                       l
                       "\n"
                       (set/difference (locale->labels l) labels)
                       "\n\n")))
           (apply str))))

(deftest supported-locales-are-actually-supported
  (is (set/subset? supported-locales (actual-supported-locales))
      (->> supported-locales
           (remove locale-is-supported-based-on-translations?)
           (map (fn [l]
                  (str "Missing translations in supported locale "
                       l
                       "\n"
                       (set/difference (checkpoint->labels checkpoint-to-consider-locale-supported)
                                       (locale->labels l))
                       "\n\n")))
           (apply str))))

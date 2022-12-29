(ns status-im2.setup.i18n-test
  (:require [cljs.spec.alpha :as spec]
            [cljs.test :refer-macros [deftest is]]
            [clojure.set :as set]
            [clojure.string :as string]
            [i18n.i18n :as i18n]
            [status-im2.setup.i18n-resources :as i18n-resources]))

;; english as source of truth
(def labels
  (set (keys (js->clj (:en i18n-resources/translations-by-locale)
                      :keywordize-keys
                      true))))

(spec/def ::label labels)
(spec/def ::labels (spec/coll-of ::label :kind set? :into #{}))

(defn labels-for-all-locales
  []
  (->> i18n-resources/translations-by-locale
       (mapcat #(-> % val (js->clj :keywordize-keys true) keys))
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
  #{:You
    :about-app
    :about-key-storage-content
    :about-key-storage-title
    :about-names-content
    :about-names-title
    :access-key
    :account-added
    :account-color
    :account-name
    :account-settings
    :accounts
    :active-online
    :active-unknown
    :add
    :add-a-watch-account
    :add-account
    :add-account-description
    :add-account-incorrect-password
    :add-an-account
    :add-bootnode
    :add-contact
    :add-custom-token
    :add-mailserver
    :add-members
    :add-network
    :add-to-contacts
    :address
    :advanced
    :advanced-settings
    :agree-by-continuing
    :all
    :allow
    :allowing-authorizes-this-dapp
    :already-have-asset
    :amount
    :are-you-sure-description
    :are-you-sure?
    :ask-in-status
    :at
    :authorize
    :available
    :available-participants
    :back
    :back-up-seed-phrase
    :back-up-your-seed-phrase
    :backup-recovery-phrase
    :balance
    :begin-set-up
    :biometric-auth-android-sensor-desc
    :biometric-auth-android-sensor-error-desc
    :biometric-auth-android-title
    :biometric-auth-confirm-logout
    :biometric-auth-confirm-message
    :biometric-auth-confirm-title
    :biometric-auth-confirm-try-again
    :biometric-auth-error
    :biometric-auth-login-error-title
    :biometric-auth-login-ios-fallback-label
    :biometric-auth-reason-login
    :biometric-auth-reason-verify
    :blank-keycard-text
    :blank-keycard-title
    :block
    :block-contact
    :block-contact-details
    :blocked-users
    :bootnode-address
    :bootnode-details
    :bootnode-format
    :bootnodes
    :bootnodes-enabled
    :bootnodes-settings
    :browsed-websites
    :browser
    :browser-not-secure
    :browser-secure
    :browsers
    :browsing-cancel
    :browsing-open-in-android-web-browser
    :browsing-open-in-ios-web-browser
    :browsing-open-in-status
    :browsing-site-blocked-description1
    :browsing-site-blocked-description2
    :browsing-site-blocked-go-back
    :browsing-site-blocked-title
    :browsing-title
    :camera-access-error
    :can-not-add-yourself
    :cancel
    :cancel-keycard-setup
    :cannot-read-card
    :cannot-use-default-pin
    :card-is-blank
    :card-reseted
    :card-unpaired
    :change-fleet
    :change-log-level
    :change-logging-enabled
    :change-passcode
    :change-password
    :change-pin
    :changed-amount-warning
    :changed-asset-warning
    :chaos-mode
    :chaos-unicorn-day
    :chaos-unicorn-day-details
    :chat
    :chat-key
    :chat-name
    :chat-settings
    :chats
    :check-your-recovery-phrase
    :choose-authentication-method
    :clear
    :clear-history
    :clear-history-action
    :clear-history-confirmation
    :clear-history-confirmation-content
    :clear-history-title
    :close-app-button
    :close-app-content
    :close-app-title
    :command-button-send
    :complete-hardwallet-setup
    :completed
    :confirm
    :confirmations
    :confirmations-helper-text
    :connect
    :connect-mailserver-content
    :connected
    :connecting
    :connecting-requires-login
    :connection-with-the-card-lost
    :connection-with-the-card-lost-setup-text
    :connection-with-the-card-lost-text
    :contact-code
    :contact-s
    :contacts
    :continue
    :contract-address
    :contract-interaction
    :copy-info
    :copy-qr
    :copy-to-clipboard
    :copy-transaction-hash
    :cost-fee
    :counter-9-plus
    :create
    :create-a-pin
    :create-group-chat
    :create-multiaccount
    :create-new-key
    :create-pin
    :create-pin-description
    :created-group-chat-description
    :cryptokitty-name
    :currency
    :currency-display-name-aed
    :currency-display-name-afn
    :currency-display-name-ars
    :currency-display-name-aud
    :currency-display-name-bbd
    :currency-display-name-bdt
    :currency-display-name-bgn
    :currency-display-name-bhd
    :currency-display-name-bnd
    :currency-display-name-bob
    :currency-display-name-brl
    :currency-display-name-btn
    :currency-display-name-cad
    :currency-display-name-chf
    :currency-display-name-clp
    :currency-display-name-cny
    :currency-display-name-cop
    :currency-display-name-crc
    :currency-display-name-czk
    :currency-display-name-dkk
    :currency-display-name-dop
    :currency-display-name-egp
    :currency-display-name-etb
    :currency-display-name-eur
    :currency-display-name-gbp
    :currency-display-name-gel
    :currency-display-name-ghs
    :currency-display-name-hkd
    :currency-display-name-hrk
    :currency-display-name-huf
    :currency-display-name-idr
    :currency-display-name-ils
    :currency-display-name-inr
    :currency-display-name-isk
    :currency-display-name-jmd
    :currency-display-name-jpy
    :currency-display-name-kes
    :currency-display-name-krw
    :currency-display-name-kwd
    :currency-display-name-kzt
    :currency-display-name-lkr
    :currency-display-name-mad
    :currency-display-name-mdl
    :currency-display-name-mur
    :currency-display-name-mwk
    :currency-display-name-mxn
    :currency-display-name-myr
    :currency-display-name-mzn
    :currency-display-name-nad
    :currency-display-name-ngn
    :currency-display-name-nok
    :currency-display-name-npr
    :currency-display-name-nzd
    :currency-display-name-omr
    :currency-display-name-pen
    :currency-display-name-pgk
    :currency-display-name-php
    :currency-display-name-pkr
    :currency-display-name-pln
    :currency-display-name-pyg
    :currency-display-name-qar
    :currency-display-name-ron
    :currency-display-name-rsd
    :currency-display-name-rub
    :currency-display-name-sar
    :currency-display-name-sek
    :currency-display-name-sgd
    :currency-display-name-thb
    :currency-display-name-try
    :currency-display-name-ttd
    :currency-display-name-twd
    :currency-display-name-tzs
    :currency-display-name-uah
    :currency-display-name-ugx
    :currency-display-name-usd
    :currency-display-name-uyu
    :currency-display-name-vef
    :currency-display-name-vnd
    :currency-display-name-zar
    :current-network
    :current-pin
    :current-pin-description
    :custom
    :custom-networks
    :dapp
    :dapp-would-like-to-connect-wallet
    :dapps
    :dapps-permissions
    :data
    :datetime-ago
    :datetime-ago-format
    :datetime-day
    :datetime-hour
    :datetime-minute
    :datetime-second
    :datetime-today
    :datetime-yesterday
    :decimals
    :decryption-failed-content
    :default
    :delete
    :delete-and-leave-group
    :delete-bootnode
    :delete-bootnode-are-you-sure
    :delete-bootnode-title
    :delete-chat
    :delete-chat-confirmation
    :delete-confirmation
    :delete-mailserver
    :delete-mailserver-are-you-sure
    :delete-mailserver-title
    :delete-message
    :delete-my-account
    :delete-network-confirmation
    :delete-network-error
    :delete-network-title
    :deny
    :description
    :dev-mode
    :dev-mode-settings
    :device-syncing
    :devices
    :disable
    :disabled
    :disconnected
    :discover
    :dismiss
    :done
    :edit
    :edit-profile
    :empty-chat-description
    :empty-chat-description-one-to-one
    :empty-chat-description-public
    :empty-chat-description-public-share-this
    :enable
    :encrypt-with-password
    :ens-10-SNT
    :ens-add-username
    :ens-agree-to
    :ens-chat-settings
    :ens-custom-domain
    :ens-custom-username-hints
    :ens-custom-username-taken
    :ens-deposit
    :ens-displayed-with
    :ens-get-name
    :ens-got-it
    :ens-locked
    :ens-network-restriction
    :ens-no-usernames
    :ens-powered-by
    :ens-primary-username
    :ens-register
    :ens-registration-failed
    :ens-registration-failed-title
    :ens-release-username
    :ens-remove-hints
    :ens-remove-username
    :ens-saved
    :ens-saved-title
    :ens-show-username
    :ens-terms-header
    :ens-terms-point-1
    :ens-terms-point-10
    :ens-terms-point-2
    :ens-terms-point-3
    :ens-terms-point-4
    :ens-terms-point-5
    :ens-terms-point-6
    :ens-terms-point-7
    :ens-terms-point-8
    :ens-terms-point-9
    :ens-terms-registration
    :ens-test-message
    :ens-transaction-pending
    :ens-understand
    :ens-username
    :ens-username-connected
    :ens-username-hints
    :ens-username-invalid
    :ens-username-owned
    :ens-username-available
    :ens-username-taken
    :ens-usernames
    :ens-usernames-details
    :wallet-address
    :ens-want-custom-domain
    :ens-want-domain
    :ens-welcome-hints
    :ens-welcome-point-customize
    :ens-welcome-point-customize-title
    :ens-welcome-point-simplify
    :ens-welcome-point-simplify-title
    :ens-welcome-point-receive
    :ens-welcome-point-receive-title
    :ens-welcome-point-register
    :ens-welcome-point-register-title
    :ens-welcome-point-verify
    :ens-welcome-point-verify-title
    :ens-your-username
    :ens-your-usernames
    :ens-your-your-name
    :enter-12-words
    :enter-contact-code
    :enter-pair-code
    :enter-pair-code-description
    :enter-password
    :enter-pin
    :enter-puk-code
    :enter-puk-code-description
    :enter-seed-phrase
    :enter-url
    :enter-word
    :enter-your-code
    :enter-your-password
    :error
    :error-unable-to-get-balance
    :error-unable-to-get-prices
    :error-unable-to-get-token-balance
    :errors
    :eth
    :ethereum-node-started-incorrectly-description
    :ethereum-node-started-incorrectly-title
    :etherscan-lookup
    :export-account
    :failed
    :faq
    :fetch-messages
    :find
    :finish
    :finishing-card-setup
    :fleet
    :fleet-settings
    :from
    :gas-limit
    :gas-price
    :gas-used
    :generate-a-key
    :generate-a-new-account
    :generate-a-new-key
    :generate-account
    :generate-new-key
    :generating-codes-for-pairing
    :generating-keys
    :generating-mnemonic
    :get-started
    :get-status-at
    :get-stickers
    :go-to-settings
    :got-it
    :group-chat
    :group-chat-admin
    :group-chat-admin-added
    :group-chat-created
    :group-chat-decline-invitation
    :group-chat-member-added
    :group-chat-member-joined
    :group-chat-member-removed
    :group-chat-members-count
    :group-chat-name-changed
    :group-chat-no-contacts
    :group-info
    :gwei
    :hash
    :help
    :help-capitalized
    :help-center
    :hide-content-when-switching-apps
    :history
    :history-nodes
    :hold-card
    :home
    :hooks
    :identifier
    :image-remove-current
    :image-source-gallery
    :image-source-make-photo
    :image-source-title
    :in-contacts
    :incoming
    :incorrect-code
    :initialization
    :install
    :intro-message1
    :intro-privacy-policy-note1
    :intro-privacy-policy-note2
    :intro-text
    :intro-text1
    :intro-text2
    :intro-text3
    :intro-title1
    :intro-title2
    :intro-title3
    :intro-wizard-text1
    :intro-wizard-text2
    :intro-wizard-text3
    :intro-wizard-text4
    :intro-wizard-text6
    :intro-wizard-title-alt4
    :intro-wizard-title-alt5
    :intro-wizard-title1
    :intro-wizard-title2
    :intro-wizard-title3
    :intro-wizard-title4
    :intro-wizard-title5
    :intro-wizard-title6
    :invalid-address-qr-code
    :invalid-format
    :invalid-key-confirm
    :invalid-key-content
    :invalid-number
    :invalid-pairing-password
    :invalid-range
    :invite-friends
    :invited
    :join-group-chat
    :join-group-chat-description
    :joined-group-chat-description
    :key
    :keycard
    :keycard-applet-install-instructions
    :keycard-blocked
    :keycard-cancel-setup-text
    :keycard-cancel-setup-title
    :keycard-desc
    :keycard-has-multiaccount-on-it
    :keycard-onboarding-finishing-header
    :keycard-onboarding-intro-header
    :keycard-onboarding-intro-text
    :keycard-onboarding-pairing-header
    :keycard-onboarding-preparing-header
    :keycard-onboarding-puk-code-header
    :keycard-onboarding-recovery-phrase-description
    :keycard-onboarding-recovery-phrase-header
    :keycard-onboarding-recovery-phrase-text
    :keycard-onboarding-start-header
    :keycard-onboarding-start-step1
    :keycard-onboarding-start-step1-text
    :keycard-onboarding-start-step2
    :keycard-onboarding-start-step2-text
    :keycard-onboarding-start-step3
    :keycard-onboarding-start-step3-text
    :keycard-onboarding-start-text
    :keycard-recovery-intro-button-text
    :keycard-recovery-intro-header
    :keycard-recovery-intro-text
    :keycard-recovery-no-key-header
    :keycard-recovery-no-key-text
    :keycard-recovery-phrase-confirm-header
    :keycard-recovery-phrase-confirmation-text
    :keycard-recovery-phrase-confirmation-title
    :keycard-recovery-success-header
    :keycard-unauthorized-operation
    :language
    :learn-more
    :learn-more-about-keycard
    :leave
    :leave-group
    :left
    :les-ulc
    :linked-on
    :load-messages-before
    :load-more-messages
    :loading
    :log-level
    :log-level-settings
    :logging
    :logging-enabled
    :login-pin-description
    :logout
    :logout-app-content
    :logout-are-you-sure
    :logout-title
    :mailserver-address
    :mailserver-automatic
    :mailserver-connection-error
    :mailserver-details
    :mailserver-error-content
    :mailserver-error-title
    :mailserver-format
    :mailserver-pick-another
    :mailserver-reconnect
    :mailserver-request-error-content
    :mailserver-request-error-status
    :mailserver-request-error-title
    :mailserver-request-retry
    :mailserver-retry
    :main-currency
    :main-networks
    :main-wallet
    :mainnet-network
    :make-admin
    :members
    :members-active
    :members-active-none
    :members-title
    :message
    :message-not-sent
    :message-options-cancel
    :message-reply
    :data-syncing
    :messages
    :might-break
    :migrations-failed-content
    :mobile-network-ask-me
    :mobile-network-continue-syncing
    :mobile-network-continue-syncing-details
    :mobile-network-go-to-settings
    :mobile-network-settings
    :mobile-network-sheet-configure
    :mobile-network-sheet-offline
    :mobile-network-sheet-offline-details
    :mobile-network-sheet-remember-choice
    :mobile-network-sheet-settings
    :mobile-network-start-syncing
    :mobile-network-stop-syncing
    :mobile-network-stop-syncing-details
    :mobile-network-use-mobile
    :mobile-network-use-mobile-data
    :mobile-network-use-wifi
    :mobile-syncing-sheet-details
    :mobile-syncing-sheet-title
    :more
    :multiaccounts-recover-enter-phrase-text
    :multiaccounts-recover-enter-phrase-title
    :name
    :name-of-token
    :need-help
    :network
    :network-chain
    :network-details
    :network-fee
    :network-id
    :network-invalid-network-id
    :network-invalid-status-code
    :network-invalid-url
    :network-settings
    :new
    :new-chat
    :new-contact
    :new-contract
    :new-group
    :new-group-chat
    :new-network
    :new-pin-description
    :new-public-group-chat
    :next
    :no
    :no-collectibles
    :no-contacts
    :no-keycard-applet-on-card
    :no-messages
    :no-pairing-slots-available
    :no-result
    :no-tokens-found
    :node-info
    :node-version
    :nonce
    :none
    :not-applicable
    :not-keycard-text
    :not-keycard-title
    :notifications
    :notify
    :off
    :offline
    :ok
    :ok-continue
    :ok-got-it
    :okay
    :on
    :open
    :open-dapp
    :open-dapp-store
    :open-nfc-settings
    :open-on-block-explorer
    :optional
    :outgoing
    :pair
    :pair-card
    :pair-code
    :pair-code-explanation
    :pair-this-card
    :pair-this-device
    :pair-this-device-description
    :paired-devices
    :pairing
    :pairing-card
    :pairing-go-to-installation
    :pairing-maximum-number-reached-content
    :pairing-maximum-number-reached-title
    :pairing-new-installation-detected-content
    :pairing-new-installation-detected-title
    :pairing-no-info
    :pairing-please-set-a-name
    :passphrase
    :password
    :password-description
    :password-placeholder2
    :password_error1
    :paste
    :paste-json
    :pay-to-chat
    :peers
    :pending
    :pending-confirmation
    :permissions
    :phone-e164
    :photos-access-error
    :pin-changed
    :pin-code
    :pin-mismatch
    :preview-privacy
    :privacy
    :privacy-and-security
    :privacy-policy
    :processing
    :product-information
    :profile
    :public-chat
    :public-chats
    :public-group-status
    :public-group-topic
    :public-key
    :puk-and-pairing-codes-displayed
    :puk-code
    :puk-code-explanation
    :puk-mismatch
    :quiet-days
    :quiet-hours
    :re-encrypt-key
    :receive
    :receive-transaction
    :recent
    :recent-recipients
    :recently-used-stickers
    :recipient
    :recipient-code
    :recover
    :recover-key
    :recover-keycard-multiaccount-not-supported
    :recover-with-keycard
    :recovering-key
    :recovery-confirm-phrase
    :recovery-phrase
    :recovery-success-text
    :recovery-typo-dialog-description
    :recovery-typo-dialog-title
    :remember-me
    :remind-me-later
    :remove
    :remove-from-chat
    :remove-network
    :remove-token
    :removed
    :repeat-pin
    :report-bug-email-template
    :request-transaction
    :required-field
    :resend-message
    :reset-card
    :reset-card-description
    :retry
    :revoke-access
    :rpc-url
    :save
    :save-password
    :save-password-unavailable
    :save-password-unavailable-android
    :scan-qr
    :scan-qr-code
    :search
    :secret-keys-confirmation-text
    :secret-keys-confirmation-title
    :security
    :see-details
    :see-it-again
    :select-chat
    :selected
    :send-logs
    :send-logs-to
    :send-message
    :send-request
    :send-request-amount
    :send-request-amount-max-decimals
    :send-request-unknown-token
    :send-sending-to
    :send-transaction
    :sending
    :sent-at
    :set-a-topic
    :set-currency
    :set-dapp-access-permissions
    :settings
    :share
    :share-address
    :share-chat
    :share-contact-code
    :share-dapp-text
    :share-link
    :share-my-profile
    :share-profile
    :share-profile-link
    :share-public-chat-text
    :sharing-copied-to-clipboard
    :sharing-copy-to-clipboard
    :sharing-share
    :show-less
    :show-more
    :show-qr
    :sign-in
    :sign-message
    :sign-out
    :sign-with
    :sign-with-password
    :sign-you-in
    :signing
    :signing-a-message
    :signing-phrase
    :something-went-wrong
    :soon
    :specify-address
    :specify-name
    :specify-network-id
    :specify-rpc-url
    :start-chat
    :start-conversation
    :start-group-chat
    :start-new-chat
    :status
    :status-confirmed
    :status-keycard
    :status-hardwallet
    :status-not-sent-click
    :status-not-sent-tap
    :status-pending
    :status-sent
    :status-tx-not-found
    :step-i-of-n
    :sticker-market
    :submit
    :submit-bug
    :success
    :symbol
    :sync-all-devices
    :sync-in-progress
    :sync-settings
    :sync-synced
    :syncing-devices
    :tag-was-lost
    :test-networks
    :text-input-disabled
    :this-device
    :this-device-desc
    :this-is-you-signing
    :this-will-take-few-seconds
    :three-words-description
    :three-words-description-2
    :to
    :to-block
    :to-encrypt-enter-password
    :to-see-this-message
    :token-auto-validate-decimals-error
    :token-auto-validate-name-error
    :token-auto-validate-symbol-error
    :token-details
    :topic-name-error
    :transaction
    :transaction-description
    :transaction-details
    :transaction-failed
    :transaction-history
    :transaction-request
    :transaction-sent
    :transactions
    :transactions-filter-select-all
    :transactions-filter-title
    :type
    :transactions-history
    :transactions-history-empty
    :transactions-sign
    :tribute-required-by-multiaccount
    :tribute-state-paid
    :tribute-state-pending
    :tribute-state-required
    :tribute-to-talk
    :tribute-to-talk-add-friends
    :tribute-to-talk-are-you-friends
    :tribute-to-talk-ask-to-be-added
    :tribute-to-talk-contact-received-your-tribute
    :tribute-to-talk-desc
    :tribute-to-talk-disabled
    :tribute-to-talk-disabled-note
    :tribute-to-talk-enabled
    :tribute-to-talk-finish-desc
    :tribute-to-talk-learn-more-1
    :tribute-to-talk-learn-more-2
    :tribute-to-talk-learn-more-3
    :tribute-to-talk-paywall-learn-more-1
    :tribute-to-talk-paywall-learn-more-2
    :tribute-to-talk-paywall-learn-more-3
    :tribute-to-talk-pending
    :tribute-to-talk-pending-note
    :tribute-to-talk-removing-note
    :tribute-to-talk-set-snt-amount
    :tribute-to-talk-signing
    :tribute-to-talk-transaction-failed-note
    :tribute-to-talk-tribute-received1
    :tribute-to-talk-tribute-received2
    :tribute-to-talk-you-require-snt
    :try-again
    :turn-nfc-on
    :type-a-message
    :ulc-enabled
    :unable-to-read-this-code
    :unblock-contact
    :unknown-status-go-error
    :unlock
    :unpair-card
    :unpair-card-confirmation
    :unpaired-keycard-text
    :unpaired-keycard-title
    :update
    :url
    :usd-currency
    :use-valid-contact-code
    :validation-amount-invalid-number
    :validation-amount-is-too-precise
    :version
    :view-cryptokitties
    :view-cryptostrikers
    :view-etheremon
    :view-gitcoin
    :view-profile
    :view-signing
    :view-superrare
    :waiting-for-wifi
    :waiting-for-wifi-change
    :waiting-to-sign
    :wallet
    :wallet-asset
    :wallet-assets
    :wallet-backup-recovery-title
    :wallet-choose-recipient
    :wallet-collectibles
    :wallet-insufficient-funds
    :wallet-insufficient-gas
    :wallet-invalid-address
    :wallet-invalid-address-checksum
    :wallet-invalid-chain-id
    :wallet-manage-assets
    :wallet-request
    :wallet-send
    :wallet-send-min-wei
    :wallet-settings
    :wallet-total-value
    :wallet-transaction-total-fee
    :wants-to-access-profile
    :warning
    :warning-message
    :web-view-error
    :welcome-screen-text
    :welcome-to-status
    :welcome-to-status-description
    :word-n
    :word-n-description
    :words-n
    :write-down-and-store-securely
    :wrong-address
    :wrong-card
    :wrong-card-text
    :wrong-contract
    :wrong-keycard-text
    :wrong-keycard-title
    :wrong-password
    :wrong-word
    :yes
    :you
    :you-already-have-an-asset
    :you-are-all-set
    :you-are-all-set-description
    :you-can-change-account
    :you-dont-have-stickers
    :your-contact-code
    :your-data-belongs-to-you
    :your-data-belongs-to-you-description
    :your-recovery-phrase
    :your-recovery-phrase-description})

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
  (-> i18n-resources/translations-by-locale (get locale) (js->clj :keywordize-keys true) keys set))

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

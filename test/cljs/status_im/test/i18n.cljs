(ns status-im.test.i18n
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.i18n :as i18n]
            [status-im.i18n-resources :as i18n-resources]
            [clojure.set :as set]
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]))

;; english as source of truth
(def labels (set (keys (:en i18n-resources/translations-by-locale))))

(spec/def ::label labels)
(spec/def ::labels (spec/coll-of ::label :kind set? :into #{}))

(defn labels-for-all-locales []
  (->> i18n-resources/translations-by-locale
       (mapcat #(-> % val keys))
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
  #{:biometric-auth-login-error-title :retry :ens-terms-point-6 :validation-amount-invalid-number :transaction-details :pin-code-description :pair-this-device :confirm :ens-terms-point-2 :ens-primary-username :public-chat :network-invalid-url :description :disable :currency-display-name-tzs :currency-display-name-brl :ens-no-usernames :write-down-and-store-securely :mobile-network-stop-syncing-details :mainnet-network :phone-national :ens-deposit :use-valid-contact-code-desktop :open-dapp :new-transaction :currency-display-name-bbd :amount :text-input-disabled :open :tribute-to-talk-learn-more-1 :name-placeholder :find :join-group-chat-description :close-app-title :agree-by-continuing :currency-display-name-sos :members-active :chat-name :wrong-contract :mobile-network-use-mobile-details :ens-username-unregistrable :currency-display-name-zar :ens-custom-username-unregistrable :offline-messaging :public-group-topic :currency-display-name-nad :new-pin-description :group-chat-no-contacts :save-password :currency-display-name-kes :messages-search-coming-soon :fetch-messages :view-etheremon :wallet-transaction-fee-details :mobile-network-use-mobile :leave-group :wallet-set-up-confirm-description :debug-enabled :device-to-device-warning-title :recovery-phrase-unknown-words :chat-settings :offline :recover-keycard-multiaccount-not-supported :update-status :send-request-unknown-token :ens-show-username :ens-want-custom-domain :no-keycard-applet-on-card :invited :you :no-pairing-slots-available :tribute-to-talk-add-friends :learn-more-about-keycard :token-auto-validate-symbol-error :default-dapps-social-networks :quiet-hours :share-link :empty-chat-description-public-share-this :create-new-multiaccount :errors :mailserver-connection-error :currency-display-name-kzt :no-recent-chats :install :chat-send-eth :address :require-my-permission :command-requesting :ens-names :login-pin-description :chaos-unicorn-day-details :mailserver-details :new-public-group-chat :tribute-to-talk-disabled :datetime-hour :add-account :tribute-to-talk-transaction-failed-note :contract-address :main-networks :wallet-settings :currency-display-name-myr :datetime-ago-format :biometric-auth-android-sensor-desc :no-messages-yet :token-auto-validate-decimals-error :delete-mailserver-are-you-sure :currency-display-name-yer :close-app-button :currency-display-name-mzn :block :wallet-set-up-title :empty-chat-description :camera-access-error :wallet-invalid-address :welcome-to-status :sending :pair-code-explanation :already-have-asset :tribute-to-talk-learn-more-3 :cryptokitty-name :address-explication :generating-mnemonic :remove :network-id :connection-problem :get-started :logging-enabled :contact-code :transactions-delete-content :home :browsing-site-blocked-description1 :transactions-unsigned-empty :status-confirmed :unable-to-read-this-code :currency-display-name-pyg :image-remove-current :message-options-cancel :transaction-moved-text :add-members :tribute-to-talk-pending :leave-public-chat :product-information :sign-later-title :manage-permissions :currency-display-name-pln :about-app :yes :dapps :ens-registration-failed :group-chat-members-count :hold-card :ropsten-network :save-password-unavailable-android :bootnode-details :secret-keys-confirmation-title :syncing-devices :ens-username-invalid :popular-tags :send-logs-to :invite-friends :card-is-empty :card-is-blank :pin-mismatch :completing-card-setup :network-settings :twelve-words-in-correct-order :permissions :extension-is-already-added :save-password-unavailable :currency-display-name-inr :transaction-moved-title :counter-9-plus :phone-number :currency-display-name-uyu :photos-access-error :ens-wallet-address :password-placeholder2 :taking-long-hold-phone-connected :request-feature :hash :new-chat :available-participants :removed-from-chat :show-more :token-details :revoke-access :done :network-fee :remove-from-contacts :paired-devices :selected :currency-display-name-pkr :currency-display-name-vnd :currency-display-name-bgn :delete-and-leave-group :biometric-auth-confirm-logout :keycard-recovery-phrase-confirm-header :tribute-to-talk-sample-text :pairing-please-set-a-name :contract-interaction :enter-url :decimals :enable :delete-network-title :delete-chat :bootnodes :ens-terms-point-5 :error-unable-to-get-transactions :pairing-new-installation-detected-title :members-active-none :new-contract :new-group-chat :desktop-alpha-release-warning :wrong-word :edit-chats :signing-phrase-warning :ens-got-it :account-settings :mailserver-request-error-content :check-your-recovery-phrase :processing :your-recovery-phrase-description :currency-display-name-gel :key :add-bootnode :complete-hardwallet-setup :currency-display-name-krw :wallet :pairing-maximum-number-reached-content :to-see-this-message :wallet-exchange :browser-secure :disabled :signing-phrase :keycard-applet-install-instructions :logout-app-content :command-button-sent :wallet-request :receive-transaction :pairing :sign-in :tribute-to-talk-you-require-snt :currency-display-name-mdl :datetime-yesterday :are-you-sure? :turn-nfc-on :disconnected :sign-in-to-status :leave-group-chat-confirmation :dapp-profile :connecting :sign-later-text :datetime-ago :wallet-transaction-fee :no-hashtags-discovered-body :currency-display-name-mnt :share-profile :contacts :log-level :search-chat :currency-display-name-ars :got-it :extensions-chain-id-not-found :ens-displayed-with :members-none :delete-group-confirmation :try-again :leave-group-chat :public-chats :specify-recipient :not-applicable :sent-at :move-to-internal-failure-message :need-help :active-online :biometric-auth-android-title :keycard-onboarding-start-step3 :complete-exclamation :continue :other-multiaccounts :joined-group-chat-description :authorize :currency-display-name-ngn :add-custom-token :extension-install-alert :delete-network-error :fleet-settings :secret-keys-confirmation-text :currency-display-name-npr :password :status-seen-by-everyone :send-logs :default-dapps-fun-games :backup-recovery-phrase :message-reply :edit-group :tribute-to-talk-paywall-learn-more-2 :mobile-network-sheet-remember-choice :wrong-address :not-specified :delete-group :send-request :use-valid-qr-code :ethereum-node-started-incorrectly-title :ens-get-name :pair-card :paste-json :browsing-title :syncing-enabled :wallet-add-asset :might-break :delete-message :browser :extensions-disclaimer :sign-message :ens-terms-point-3 :add-an-account :reorder-groups :wrong-card-text :connect-mailserver-content :currency-display-name-rub :cant-read-card :transactions-history-empty :leave-group-title :default :currency-display-name-bnd :tag-was-lost :pair :remove-from-chat :wallet-backup-recovery-description :wrong-card :discover :recovery-phrase :mobile-network-ask-me :mobile-network-sheet-settings :delete-bootnode :new :wallet-set-up-signing-explainer :make-sure-you-trust-dapp :wallet-error :currency-display-name-btn :ens-welcome-point-1 :create :already-have-multiaccount :currency-display-name-mxn :browsing-cancel :symbol :currency-display-name-jmd :ask-in-status :invalid-extension :bootnode-format :faucet-success :name :error-unable-to-get-balance :see-it-again :sign-you-in :created-group-chat-description :cannot-use-default-pin :report-bug-email-template :preview-privacy :gas-price :keycard-blocked :phone-number-required :view-transaction-details :wallet-insufficient-gas :reset-card :secure-your-assets :validation-amount-is-too-precise :pending :hooks :completed :copy-transaction-hash :unknown-address :received-invitation :show-qr :create-pin-description :group-chat-member-joined :start-chat :default-dapps-marketplaces :browsed-websites :mobile-network-sheet-offline :wallet-onboarding-title :ens-username-owned-continue :logout :mailserver-request-error-status :status-not-sent :tribute-to-talk-paywall-learn-more-1 :ens-welcome-point-2-title :group-chat-name-changed :edit-network-config :clear-history-confirmation :connect :choose-from-contacts :unpair-card-confirmation :wallet-deposit :wallet-offline :currency-display-name-gyd :tribute-state-pending :see-details :peers :quiet-days :mobile-network-sheet-offline-details :required-field :send-request-amount-must-be-specified :edit :wallet-address-from-clipboard :share-profile-link :currency-display-name-cad :remove-network :ens-release-username :recovery-typo-dialog-description :ens-welcome-point-2 :no-messages :passphrase :recipient :members-title :ens-powered-by :ens-saved :recovery-confirm-phrase :delete-group-chat-confirmation :default-dapps-media :new-group :tribute-to-talk-removing-note :sync-all-devices :sidechain-text :name-of-token :currency-display-name-vef :send-message :pay-to-chat :keycard-onboarding-finishing-header :no-extension :phone-e164 :sign-with-password :suggestions-requests :begin-keycard-setup-confirmation-text :currency-display-name-nok :connected :tribute-to-talk-sign-and-set-tribute :view-cryptokitties :tribute-to-talk-message-placeholder :changed-amount-warning :network-chain :rpc-url :currency-display-name-omr :make-admin :changed-asset-warning :ens-agree-to :wallet-onboarding-set-up :learn-more :settings :device-pairing :share-my-profile :remove-from-group :specify-rpc-url :secret-keys-confirmation-cancel :contacts-syncronized :enter-pair-code :pairing-go-to-installation :currency-display-name-aed :currency-display-name-egp :transactions-sign-all :begin-set-up :enter-contact-code :delete-chat-action :currency-display-name-twd :pin-unblocked-description :history :empty-chat-description-console :paste :tribute-to-talk-disabled-note :group-chat-admin :tribute-to-talk-tribute-received1 :tribute-to-talk-tribute-received2 :connection-with-the-card-lost-text :creating-your-multiaccount :postponed :load-messages-before :gas-limit :wallet-browse-photos :currency-display-name-kyd :keycard-onboarding-start-step3-text :add-new-contact :no-statuses-discovered-body :add-json-file :ens-10-SNT :browsing-open-in-status  :signing-a-message :network-invalid-status-code :mobile-network-use-mobile-data :waiting-for-wifi-change :wallet-onboarding-description :card-reseted :enter-pin-description :custom :dapps-can-access :update :delete :no-multiaccount-on-card-text :extension-url :search-contacts :chats :ens-registered :ens-terms-header :enter-puk-code-description :transaction-sent :currency-display-name-dkk :transaction :browsing-site-blocked-description2 :status-tx-not-found :extension-find :recover-multiaccount-warning :ens-saved-title :specify-address :currency-display-name-eur :keycard-applet-will-be-installed :public-group-status :leave-chat :status-not-sent-click :ens-registration-failed-title :transactions-delete :selected-dapps :dapp :mainnet-text :copy-info :receive :ok-got-it :main-currency :clear-history-title :image-source-make-photo :chat :puk-and-pair-codes :linked-on :ens-network-restriction :group-chat-admin-added :start-conversation :you-are-all-set-description :topic-format :specify-name :change-pin :syncing-disabled :tribute-to-talk-enabled :add-new-network :keycard-onboarding-intro-header :save :keycard-recovery-phrase-confirmation-title :enter-valid-public-key :mailserver-request-error-title :currency-display-name-bdt :initialization :keycard-onboarding-recovery-phrase-header :logout-title :faucet-error :or-choose-a-contact :card-already-linked :sharing-copied-to-clipboard :phone-significant :all :ens-terms-point-10 :status-not-sent-tap :search :tribute-to-talk-contact-received-your-tribute :confirmations-helper-text :enter-ens-or-contact-code :keycard-onboarding-start-step2-text :unblock-contact :reset-default :waiting-for-wifi :search-for :test-networks :sharing-copy-to-clipboard :your-wallets :send-command-payment :phone-international :error-unable-to-get-token-balance :keycard :web3-opt-in :next-step-generating-mnemonic :enter-word :sync-in-progress :enter-password :status-hardwallet-capitalized :tribute-to-talk-signing :logout-are-you-sure :allow :leave-group-confirmation :finishing-card-setup :keycard-cancel-setup-text :this-will-take-few-seconds :current-pin-description :enter-address :pairing-no-info :ens-welcome-point-4 :create-group-chat :signing-message-phrase-description :remove-token :default-dapps-social-utilities :block-contact :ens-locked :biometric-auth-android-sensor-error-desc :selected-for-you :send-request-amount-invalid-number :switch-users :currency-display-name-qar :command-button-send :cant-read-card-error-explanation :pair-this-device-description :currency-display-name-hkd :pin-unblocked :okay :ens-terms-point-1 :your-recovery-phrase :mailserver-request-retry :transaction-history :pair-code :send-transaction :currency-display-name-ltl :step-i-of-n :ens-terms-registration :confirmations :pairing-maximum-number-reached-title :ready-to-import-keycard-multiaccount :buy-with-snt :recover-access :currency-display-name-ron :repeat-pin :log-level-settings :ens-test-message :invalid-key-content :advanced-settings :preparing-card :group-info :currency-display-name-nio :incorrect-code :currency-display-name-ugx :image-source-gallery :sync-synced :currency :ens-transaction-pending :ens-add-username :currency-display-name-bmd :status-pending :delete-contact :currency-display-name-try :connecting-requires-login :biometric-auth-confirm-message :help-capitalized :logging :send-transaction-request :share-contact-code :use-valid-contact-code :dapps-permissions :no-hashtags-discovered-title :wallet-set-up-signing-explainer-warning :enter-dapp-url :tribute-state-required :browsers :wallet-transaction-total-fee :cannot-read-card :keycard-has-multiaccount-on-it :extension :datetime-day :request-transaction :warning :wallet-send :puk-code-explanation :tribute-to-talk-paywall-learn-more-3 :invalid-key-title :ethereum-node-started-incorrectly-description :group-chat-created :notifications :biometric-auth-confirm-title :balance :ens-chat-settings :enter-puk-code :currency-display-name-czk :ens-terms-point-9 :ens-remove-username :mute-notifications :keycard-onboarding-puk-code-header :you-dont-have-stickers :device-to-device-warning-content :currency-display-name-bob :invalid-phone :device-to-device :privacy-policy :scan-qr :install-the-extension :ens-terms-point-7 :messages :currency-display-name-lak :contact-s :recipient-code :view-my-wallet :fleet :unsigned-transaction-expired :recover-password-invalid :ens-want-domain :status-sending :send-request-amount :backup-your-recovery-phrase :gas-used :delete-chat-title :success :specify-bootnode-address :invalid-pairing-password :currency-display-name-thb :transactions-filter-type :ens-your-usernames :next :recent :wallet-send-token :importing-keycard-multiaccount :bootnodes-enabled :your-data-belongs-to-you :empty-chat-description-one-to-one :open-on-etherscan :loading :estimated-time :request-command-payment :create-pin :currency-display-name-lkr :mailserver-format :currency-display-name-lrd :browsing-open-in-ios-web-browser :browsing-open-in-android-web-browser :leave :offline-messaging-settings :ens-usernames :qr-code-public-key-hint :submit-bug :share :recent-recipients :delete-bootnode-are-you-sure :status :start-new-chat :from :extensions :leave-group-action :wrong-password :invalid-format :amount-placeholder :reset-card-description :export-account :search-chats :network-details :keycard-onboarding-recovery-phrase-description :currency-display-name-rsd :enter-pair-code-description :transactions-sign-later :pin-changed :in-contacts :resend-message :back :mobile-network-go-to-settings :tribute-to-talk-pending-note :currency-display-name-bhd :request-qr-legend :maintain-card-to-phone-contact :ok :currency-display-name-chf :start-group-chat :chaos-mode :keycard-cancel-setup-title :multiaccount-not-listed-text :share-public-chat-text :transactions-sign :decryption-failed-confirm :optional :open-dapp-store :pairing-card :show-less :wallet-set-up-confirm-title :sharing-share :tribute-to-talk-desc :card-setup-prepare-text :at :off :dev-mode :intro-text-description :go-to-settings :keycard-onboarding-start-step1 :currency-display-name-kwd :invalid-number :type-a-message :recover-password-too-short :rinkeby-network :faq :currency-display-name-sar :type-a-command :group-chat :keycard-onboarding-recovery-phrase-text :enter-pin :delete-bootnode-title :group-chat-decline-invitation :message-not-sent :tribute-state-paid :empty-chat-description-public :existing-mailservers :currency-display-name-dop :usd-currency :biometric-auth-reason-verify :add-contact :existing-networks :node-unavailable :wallet-set-up-safe-transactions-title :invalid-range :url :shake-your-phone :identifier :currency-display-name-mkd :no-pairing-on-device :currency-display-name-pen :currency-display-name-clp :currency-display-name-ghs :currency-display-name-isk :view-cryptostrikers :view-superrare :fetch-history :confirm-install :chaos-unicorn-day :token-auto-validate-name-error :add-network :etherscan-lookup :unknown-status-go-error :extensions-camera-send-picture :contacts-group-new-chat :extension-hooks-cannot-be-added :and-you :puk-and-pairing-codes-displayed :error-cant-send-transaction-offline :recently-used-stickers :wallets :clear-history :currency-display-name-sgd :default-dapps-exchanges :wallet-manage-assets :wallet-choose-from-contacts :send-sending-to :signing-phrase-description :no-contacts :currency-display-name-mad :here-is-your-signing-phrase :to-block :wants-to-access-profile :currency-display-name-huf :card-unpaired :keycard-onboarding-pairing-header :currency-display-name-pgk :group-chat-member-removed :currency-display-name-cop :decryption-failed-content :soon :wallet-asset :close-app-content :link-card :password-description :devices :currency-display-name-afn :word-n-description :status-sent :pin-retries-left :status-prompt :ens-register :join-group-chat :extensions-network-not-found :pending-confirmation :topic-name-error :tribute-to-talk-you-can-leave-a-message :delete-contact-confirmation :keycard-onboarding-intro-text :datetime-today :dapp-would-like-to-connect-wallet :specify-network-id :currency-display-name-aud :add-a-status :web-view-error :error-unable-to-get-prices :tribute-required-by-multiaccount :puk-code :set-a-topic :notifications-title :copy-qr :error :secret-keys-confirmation-confirm :open-nfc-settings :transactions-sign-transaction :wallet-backup-recovery-title :change-log-level :add-a-watch-account :block-contact-details :initialization-of-the-card :on :view-gitcoin :currency-display-name-mur :edit-contacts :more :cancel-keycard-setup :cancel :ens-understand :browsing-site-blocked-go-back :delete-network-confirmation :no-statuses-found :mailserver-automatic :share-chat :extension-address :mailserver-retry :ens-your-username :can-not-add-yourself :ens-registered-title :transaction-description :add-to-contacts :available :currency-display-name-jpy :sticker-market :intro-text :error-cant-sign-message-offline :paste-json-as-text :you-are-all-set :invalid-key-confirm :You :network :biometric-auth-login-ios-fallback-label :main-wallet :process-json :ens-usernames-details :testnet-text :browser-not-secure :group-chat-member-added :create-a-pin :notify :welcome-to-status-description :recovery-phrase-invalid :help :currency-display-name-cny :clear-history-confirmation-content :pin-code :mailserver-reconnect :transactions :change-logging-enabled :transactions-unsigned :network-invalid-network-id :mobile-network-sheet-configure :members :delete-mailserver-title :something-went-wrong :intro-message1 :ens-username-connected :public-chat-user-count :eth :finishing-card-setup-steps :transactions-history :fetching-messages :not-implemented :password_error1 :your-contact-code :send-request-invalid-asset :new-contact :keycard-onboarding-preparing-header :datetime-second :status-failed :password-placeholder :clear-history-action :is-typing :version :remaining-steps :specify-mailserver-address :scan-qr-code :status-not-sent-without-tap :recover :wallet-total-value :mobile-network-settings :currency-display-name-idr :mailserver-address :mailserver-error-content :add :bootnodes-settings :allowing-authorizes-this-dapp :currency-display-name-srd :transaction-request :choose-authentication-method :no-collectibles :load-more-messages :set-currency :decryption-failed-title :word-n :deny :command-sending :currency-display-name-hrk :you-already-have-an-asset :biometric-auth-setting-label :currency-display-name-gbp :ens-custom-domain :currency-display-name-etb :suggestions-commands :status-keycard :nonce :mobile-syncing-sheet-details :new-network :tribute-to-talk :biometric-auth-reason-login :migrations-failed-title :tribute-to-talk-are-you-friends :enter-12-words :contact-already-added :ens-welcome-hints :datetime-minute :ens-custom-username-hints :create-multiaccount :no-multiaccount-on-card :ok-continue :currency-display-name-gmd :migrations-erase-multiaccounts-data-button :view-signing :custom-networks :delete-group-prompt :ens-welcome-point-4-title :wallet-insufficient-funds :currency-display-name-ils :sign-in-to-another :ens-welcome-point-3-title :edit-profile :active-unknown :currency-display-name-crc :ens-welcome-point-1-title :help-center :always-allow :currency-display-name-mwk :wallet-collectibles :mailserver-error-title :pair-card-question :wallet-invalid-chain-id :search-tags :wallet-send-min-wei :biometric-auth-error :transaction-failed :wallet-invalid-address-checksum :keycard-onboarding-start-text :delete-mailserver :public-key :signing :status-hardwallet :biometric-auth-confirm-try-again :no-tokens-found :name-description :send-request-amount-max-decimals :error-processing-json :no-result :browsing-site-blocked-title :keycard-onboarding-start-step1-text :mobile-network-start-syncing :copy-to-clipboard :status-seen :get-stickers :transactions-filter-tokens :add-existing-multiaccount :incoming :keycard-recovery-phrase-confirmation-text :currency-display-name-sek :waiting-to-sign :ens-username-owned :status-delivered :recovery-typo-dialog-title :ens-username-registrable :unpair-card :share-dapp-text :bootnode-address :pairing-new-installation-detected-content :profile :wallet-choose-recipient :no-statuses-discovered :currency-display-name-nzd :none :removed :puk-mismatch :failed :current-pin :mailserver-pick-another :status-console :node-info :currency-display-name-bzd :network-mismatch :no-more-participants-available :empty-topic :back-up-your-seed-phrase :delete-confirmation :mobile-syncing-sheet-title :no :multiaccount-not-listed :generating-codes-for-pairing :transactions-filter-select-all :get-status-at :your-data-belongs-to-you-description :transactions-filter-title :ens-your-your-name :ens-remove-hints :view-profile :tribute-to-talk-finish-desc :message :ens-terms-point-8 :ens-welcome-point-3 :mobile-network-continue-syncing-details :keycard-onboarding-start-step2 :add-mailserver :currency-display-name-ttd :wallet-assets :are-you-sure-description :ens-terms-point-4 :ens-username-hints :notifications-new-message-body :next-step-entering-mnemonic :currency-display-name-php :image-source-title :leave-confirmation :mobile-network-continue-syncing :current-network :new-request :outgoing :blocked-users :card-is-paired :send-request-currency :mobile-network-stop-syncing :clear :dont-allow :left :warning-message :tribute-to-talk-learn-more-2 :tribute-to-talk-set-snt-amount :edit-network-warning :migrations-failed-content :to :keycard-onboarding-start-header :connection-with-the-card-lost :change-fleet :delete-chat-confirmation :data :gwei :keycard-unauthorized-operation :cost-fee :currency-display-name-usd :tribute-to-talk-ask-to-be-added :currency-display-name-uah})

;; NOTE: the rest checkpoints are based on the previous one, defined
;;       like this:
;; (def checkpoint-2-labels (set/union checkpoint-1-labels #{:foo :bar})
;; (def checkpoint-3-labels (set/union checkpoint-2-labels #{:baz})

;; NOTE: This defines the scope of each checkpoint. To support a checkpoint,
;;       change the var `checkpoint-to-consider-locale-supported` a few lines
;;       below.
(def checkpoints-def (spec/assert ::checkpoint-defs
                                  {::checkpoint-1-0-0-rc1 checkpoint-1-0-0-rc1-labels}))
(def checkpoints (set (keys checkpoints-def)))

(spec/def ::checkpoint checkpoints)

(def checkpoint-to-consider-locale-supported ::checkpoint-1-0-0-rc1)

(defn checkpoint->labels [checkpoint]
  (get checkpoints-def checkpoint))

(defn checkpoint-val-to-compare [c]
  (-> c name (string/replace #"^.*\|" "") int))

(defn >checkpoints [& cs]
  (apply > (map checkpoint-val-to-compare cs)))

;; locales

(def locales (set (keys i18n-resources/translations-by-locale)))

(spec/def ::locale locales)
(spec/def ::locales (spec/coll-of ::locale :kind set? :into #{}))

(defn locale->labels [locale]
  (-> i18n-resources/translations-by-locale (get locale) keys set))

(defn locale->checkpoint [locale]
  (let [locale-labels (locale->labels locale)
        checkpoint    (->> checkpoints-def
                           (filter (fn [[checkpoint checkpoint-labels]]
                                     (set/subset? checkpoint-labels locale-labels)))
                           ffirst)]
    checkpoint))

(defn locale-is-supported-based-on-translations? [locale]
  (let [c (locale->checkpoint locale)]
    (and c (or (= c checkpoint-to-consider-locale-supported)
               (>checkpoints checkpoint-to-consider-locale-supported c)))))

(defn actual-supported-locales []
  (->> locales
       (filter locale-is-supported-based-on-translations?)
       set))

;; NOTE: Add new locale keywords here to indicate support for them.
#_(def supported-locales (spec/assert ::locales #{:fr
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
                  (str "Extra translations in locale " l "\n"
                       (set/difference (locale->labels l) labels)
                       "\n\n")))
           (apply str))))

(deftest supported-locales-are-actually-supported
  (is (set/subset? supported-locales (actual-supported-locales))
      (->> supported-locales
           (remove locale-is-supported-based-on-translations?)
           (map (fn [l]
                  (str "Missing translations in supported locale " l "\n"
                       (set/difference (checkpoint->labels checkpoint-to-consider-locale-supported)
                                       (locale->labels l))
                       "\n\n")))
           (apply str))))

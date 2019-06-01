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
(def checkpoint-0-9-12-labels
  #{:validation-amount-invalid-number :transaction-details :confirm :description
    :phone-national :amount :open :close-app-title :members-active :chat-name
    :phew-here-is-your-passphrase :public-group-topic :debug-enabled
    :chat-settings :offline :update-status :invited :chat-send-eth :address
    :new-public-group-chat :datetime-hour :wallet-settings
    :datetime-ago-format :close-app-button :block :camera-access-error
    :wallet-invalid-address :wallet-invalid-address-checksum :address-explication :remove
    :transactions-delete-content :transactions-unsigned-empty
    :transaction-moved-text :add-members :sign-later-title
    :yes :dapps :popular-tags :network-settings :twelve-words-in-correct-order
    :transaction-moved-title :photos-access-error :hash
    :removed-from-chat :done :remove-from-contacts :delete-chat :new-group-chat
    :edit-chats :wallet :wallet-exchange :wallet-request :sign-in
    :datetime-yesterday :create-new-account :sign-in-to-status :save-password :save-password-unavailable :dapp-profile
    :sign-later-text :datetime-ago :no-hashtags-discovered-body :contacts
    :search-chat :got-it :delete-group-confirmation :public-chats
    :not-applicable :move-to-internal-failure-message :active-online
    :password :status-seen-by-everyone :edit-group :not-specified
    :delete-group :send-request :paste-json :browsing-title
    :wallet-add-asset :reorder-groups :transactions-history-empty :discover
    :browsing-cancel :faucet-success :intro-status :name :gas-price
    :view-transaction-details :wallet-error
    :validation-amount-is-too-precise :copy-transaction-hash :unknown-address
    :received-invitation :show-qr :edit-network-config :connect
    :choose-from-contacts :edit :wallet-address-from-clipboard
    :account-generation-message :remove-network :no-messages :passphrase
    :recipient :members-title :new-group :suggestions-requests
    :connected :rpc-url :settings :remove-from-group :specify-rpc-url
    :transactions-sign-all :gas-limit :wallet-browse-photos
    :add-new-contact :no-statuses-discovered-body :add-json-file :delete
    :search-contacts :chats :transaction-sent :transaction :public-group-status
    :leave-chat :transactions-delete :mainnet-text :image-source-make-photo
    :chat :start-conversation :topic-format :add-new-network :save
    :enter-valid-public-key :faucet-error :all
    :confirmations-helper-text :search-for :sharing-copy-to-clipboard
    :your-wallets :sync-in-progress :enter-password
    :enter-address :switch-users :send-transaction :confirmations
    :recover-access :image-source-gallery :sync-synced
    :currency :status-pending :delete-contact :connecting-requires-login
    :no-hashtags-discovered-title :datetime-day :request-transaction
    :wallet-send :mute-notifications :scan-qr :contact-s
    :unsigned-transaction-expired :status-sending :gas-used
    :transactions-filter-type :next :recent
    :open-on-etherscan :share :status :from
    :wrong-password :search-chats :transactions-sign-later :in-contacts
    :transactions-sign :sharing-share :type-a-message
    :usd-currency :existing-networks :node-unavailable :url :shake-your-phone
    :add-network :unknown-status-go-error :contacts-group-new-chat :and-you
    :wallets :clear-history :wallet-choose-from-contacts
    :signing-phrase-description :no-contacts :here-is-your-signing-phrase
    :soon :close-app-content :status-sent :status-prompt
    :delete-contact-confirmation :datetime-today :add-a-status
    :web-view-error :notifications-title :error :transactions-sign-transaction
    :edit-contacts :more :cancel :no-statuses-found :can-not-add-yourself
    :transaction-description :add-to-contacts :available
    :paste-json-as-text :You :main-wallet :process-json :testnet-text
    :transactions :transactions-unsigned :members :intro-message1
    :public-chat-user-count :eth :transactions-history :not-implemented
    :new-contact :datetime-second :status-failed :is-typing :recover
    :suggestions-commands :nonce :new-network :contact-already-added :datetime-minute
    :browsing-open-in-ios-web-browser :browsing-open-in-android-web-browser
    :delete-group-prompt :wallet-total-value
    :wallet-insufficient-funds :edit-profile :active-unknown
    :search-tags :transaction-failed :public-key :error-processing-json
    :status-seen :transactions-filter-tokens :status-delivered :profile
    :wallet-choose-recipient :no-statuses-discovered :none :removed :empty-topic
    :no :transactions-filter-select-all :transactions-filter-title :message
    :here-is-your-passphrase :wallet-assets :image-source-title :current-network
    :left :edit-network-warning :to :data :cost-fee})

;; NOTE: the rest checkpoints are based on the previous one, defined
;;       like this:
;; (def checkpoint-2-labels (set/union checkpoint-1-labels #{:foo :bar})
;; (def checkpoint-3-labels (set/union checkpoint-2-labels #{:baz})

;; NOTE: This defines the scope of each checkpoint. To support a checkpoint,
;;       change the var `checkpoint-to-consider-locale-supported` a few lines
;;       below.
(def checkpoints-def (spec/assert ::checkpoint-defs
                                  {::checkpoint-0-9-12 checkpoint-0-9-12-labels}))
(def checkpoints (set (keys checkpoints-def)))

(spec/def ::checkpoint checkpoints)

(def checkpoint-to-consider-locale-supported ::checkpoint-0-9-12)

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

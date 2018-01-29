(ns status-im.i18n
  (:require
    [cljs.spec.alpha :as spec]
    [status-im.react-native.js-dependencies :as rn-dependencies]
    [status-im.translations.af :as af]
    [status-im.translations.ar :as ar]
    [status-im.translations.bel :as be]
    [status-im.translations.cs :as cs]
    [status-im.translations.da :as da]
    [status-im.translations.de :as de]
    [status-im.translations.de-ch :as de-ch]
    [status-im.translations.en :as en]
    [status-im.translations.es :as es]
    [status-im.translations.es-ar :as es-ar]
    [status-im.translations.es-mx :as es-mx]
    [status-im.translations.fi :as fi]
    [status-im.translations.fr :as fr]
    [status-im.translations.fr-ch :as fr-ch]
    [status-im.translations.fy :as fy]
    [status-im.translations.he :as he]
    [status-im.translations.hi :as hi]
    [status-im.translations.hu :as hu]
    [status-im.translations.id :as id]
    [status-im.translations.it :as it]
    [status-im.translations.it-ch :as it-ch]
    [status-im.translations.ja :as ja]
    [status-im.translations.ko :as ko]
    [status-im.translations.la :as la]
    [status-im.translations.lt :as lt]
    [status-im.translations.lv :as lv]
    [status-im.translations.ms :as ms]
    [status-im.translations.nb :as nb]
    [status-im.translations.ne :as ne]
    [status-im.translations.nl :as nl]
    [status-im.translations.pl :as pl]
    [status-im.translations.pt-br :as pt-br]
    [status-im.translations.pt-pt :as pt-pt]
    [status-im.translations.ro :as ro]
    [status-im.translations.ru :as ru]
    [status-im.translations.sl :as sl]
    [status-im.translations.sr-rs-cyrl :as sr-rs-cyrl]
    [status-im.translations.sr-rs-latn :as sr-rs-latn]
    [status-im.translations.sv :as sv]
    [status-im.translations.sw :as sw]
    [status-im.translations.th :as th]
    [status-im.translations.tr :as tr]
    [status-im.translations.uk :as uk]
    [status-im.translations.ur :as ur]
    [status-im.translations.vi :as vi]
    [status-im.translations.zh-hans :as zh-hans]
    [status-im.translations.zh-hant :as zh-hant]
    [status-im.translations.zh-hant-tw :as zh-hant-tw]
    [status-im.translations.zh-hant-sg :as zh-hant-sg]
    [status-im.translations.zh-hant-hk :as zh-hant-hk]
    [status-im.translations.zh-wuu :as zh-wuu]
    [status-im.translations.zh-yue :as zh-yue]
    [status-im.utils.js-resources :refer [default-contacts]]
    [taoensso.timbre :as log]
    [clojure.string :as string]
    [clojure.set :as set]))

(set! (.-fallbacks rn-dependencies/i18n) true)
(set! (.-defaultSeparator rn-dependencies/i18n) "/")

;; translations

(def translations-by-locale {:af          af/translations
                             :ar          ar/translations
                             :be          be/translations
                             :cs          cs/translations
                             :da          da/translations
                             :de          de/translations
                             :de-ch       de-ch/translations
                             :en          en/translations
                             :es          es/translations
                             :es-ar       es-ar/translations
                             :es-mx       es-mx/translations
                             :fi          fi/translations
                             :fr          fr/translations
                             :fr-ch       fr-ch/translations
                             :fy          fy/translations
                             :he          he/translations
                             :hi          hi/translations
                             :hu          hu/translations
                             :id          id/translations
                             :it          it/translations
                             :it-ch       it-ch/translations
                             :ja          ja/translations
                             :ko          ko/translations
                             :la          la/translations
                             :lt          lt/translations
                             :lv          lv/translations
                             :ms          ms/translations
                             :nb          nb/translations
                             :ne          ne/translations
                             :nl          nl/translations
                             :pl          pl/translations
                             :pt-br       pt-br/translations
                             :pt-pt       pt-pt/translations
                             :ro          ro/translations
                             :ru          ru/translations
                             :sl          sl/translations
                             :sr          sr-rs-cyrl/translations
                             :sr-RS_#Latn sr-rs-latn/translations
                             :sr-RS_#Cyrl sr-rs-cyrl/translations
                             :sv          sv/translations
                             :sw          sw/translations
                             :th          th/translations
                             :tr          tr/translations
                             :uk          uk/translations
                             :ur          ur/translations
                             :vi          vi/translations
                             :zh          zh-hans/translations
                             :zh-hans     zh-hans/translations
                             :zh-hans-cn  zh-hans/translations
                             :zh-hans-mo  zh-hans/translations
                             :zh-hant     zh-hant/translations
                             :zh-hant-tw  zh-hant-tw/translations
                             :zh-hant-sg  zh-hant-sg/translations
                             :zh-hant-hk  zh-hant-hk/translations
                             :zh-hant-cn  zh-hant/translations
                             :zh-hant-mo  zh-hant/translations
                             :zh-wuu      zh-wuu/translations
                             :zh-yue      zh-yue/translations})

;; english as source of truth
(def labels (set (keys en/translations)))

(spec/def ::label labels)
(spec/def ::labels (spec/coll-of ::label :kind set? :into #{}))

(defn labels-for-all-locales []
  (->> translations-by-locale
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
    :wallet-invalid-address :address-explication :remove
    :transactions-delete-content :transactions-unsigned-empty
    :transaction-moved-text :add-members :sign-later-title :sharing-cancel
    :yes :dapps :popular-tags :network-settings :twelve-words-in-correct-order
    :transaction-moved-title :phone-number :photos-access-error :hash
    :removed-from-chat :done :remove-from-contacts :delete-chat :new-group-chat
    :edit-chats :wallet :wallet-exchange :wallet-request :sign-in
    :datetime-yesterday :create-new-account :sign-in-to-status :dapp-profile
    :sign-later-text :datetime-ago :no-hashtags-discovered-body :contacts
    :search-chat :got-it :delete-group-confirmation :public-chats
    :not-applicable :move-to-internal-failure-message :active-online
    :password :status-seen-by-everyone :edit-group :not-specified
    :delete-group :send-request :paste-json :browsing-title
    :wallet-add-asset :reorder-groups :transactions-history-empty :discover
    :browsing-cancel :faucet-success :intro-status :name :gas-price
    :phone-number-required :view-transaction-details :wallet-error
    :validation-amount-is-too-precise :copy-transaction-hash :unknown-address
    :received-invitation :show-qr :edit-network-config :connect
    :choose-from-contacts :edit :wallet-address-from-clipboard
    :account-generation-message :remove-network :no-messages :passphrase
    :recipient :members-title :new-group :phone-e164 :suggestions-requests
    :connected :rpc-url :settings :remove-from-group :specify-rpc-url
    :contacts-syncronized :transactions-sign-all :gas-limit :wallet-browse-photos
    :add-new-contact :no-statuses-discovered-body :add-json-file :delete
    :search-contacts :chats :transaction-sent :transaction :public-group-status
    :leave-chat :transactions-delete :mainnet-text :image-source-make-photo
    :chat :start-conversation :topic-format :add-new-network :save
    :enter-valid-public-key :faucet-error :phone-significant :all
    :confirmations-helper-text :search-for :sharing-copy-to-clipboard
    :your-wallets :phone-international :sync-in-progress :enter-password
    :enter-address :switch-users :send-transaction :confirmations
    :recover-access :incorrect-code :image-source-gallery :sync-synced
    :currency :status-pending :delete-contact :connecting-requires-login
    :no-hashtags-discovered-title :datetime-day :request-transaction
    :wallet-send :mute-notifications :invalid-phone :scan-qr :contact-s
    :unsigned-transaction-expired :status-sending :gas-used
    :transactions-filter-type :next :recent :confirmation-code
    :open-on-etherscan :share :status :from
    :wrong-password :search-chats :transactions-sign-later :in-contacts
    :transactions-sign :sharing-share :type-a-message :type-a-command
    :usd-currency :existing-networks :node-unavailable :url :shake-your-phone
    :add-network :unknown-status-go-error :contacts-group-new-chat :and-you
    :wallets :clear-history :wallet-choose-from-contacts
    :signing-phrase-description :no-contacts :here-is-your-signing-phrase
    :soon :close-app-content :status-sent :status-prompt :testfairy-title
    :delete-contact-confirmation :datetime-today :add-a-status
    :web-view-error :notifications-title :error :transactions-sign-transaction
    :edit-contacts :more :cancel :no-statuses-found :can-not-add-yourself
    :transaction-description :add-to-contacts :available
    :paste-json-as-text :You :main-wallet :process-json :testnet-text
    :transactions :transactions-unsigned :members :intro-message1
    :public-chat-user-count :eth :transactions-history :not-implemented
    :new-contact :datetime-second :status-failed :is-typing :recover
    :suggestions-commands :nonce :new-network :contact-already-added :datetime-minute
    :browsing-open-in-web-browser :delete-group-prompt :wallet-total-value
    :wallet-insufficient-funds :edit-profile :active-unknown :testfairy-message
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

(defn labels-that-are-not-in-current-checkpoint []
  (set/difference labels (checkpoint->labels checkpoint-to-consider-locale-supported)))

;; locales

(def locales (set (keys translations-by-locale)))

(spec/def ::locale locales)
(spec/def ::locales (spec/coll-of ::locale :kind set? :into #{}))

;; NOTE: Add new locale keywords here to indicate support for them.
(def supported-locales (spec/assert ::locales #{:fr
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
                                                :en
                                                :de
                                                :lt
                                                :sr-RS_#Latn
                                                :sr
                                                :sv
                                                :ja
                                                :uk}))

(spec/def ::supported-locale supported-locales)
(spec/def ::supported-locales (spec/coll-of ::supported-locale :kind set? :into #{}))

(defn locale->labels [locale]
  (-> translations-by-locale (get locale) keys set))

(defn locale->checkpoint [locale]
  (let [locale-labels (locale->labels locale)
        checkpoint    (->> checkpoints-def
                           (filter (fn [[checkpoint checkpoint-labels]]
                                     (set/subset? checkpoint-labels locale-labels)))
                           ffirst)]
    checkpoint))

(defn locales-with-checkpoint []
  (->> locales
       (map (fn [locale]
              [locale (locale->checkpoint locale)]))
       (into {})))

(defn locale-is-supported-based-on-translations? [locale]
  (let [c (locale->checkpoint locale)]
    (and c (or (= c checkpoint-to-consider-locale-supported)
               (>checkpoints checkpoint-to-consider-locale-supported c)))))

(defn actual-supported-locales []
  (->> locales
       (filter locale-is-supported-based-on-translations?)
       set))

(defn locales-with-full-support []
  (->> locales
       (filter (fn [locale]
                 (set/subset? labels (locale->labels locale))))
       set))

(defn supported-locales-that-are-not-considered-supported []
  (set/difference (actual-supported-locales) supported-locales))


(set! (.-translations rn-dependencies/i18n)
      (clj->js translations-by-locale))

;;:zh, :zh-hans-xx, :zh-hant-xx have been added until this bug will be fixed https://github.com/fnando/i18n-js/issues/460

(def delimeters
  "This function is a hack: mobile Safari doesn't support toLocaleString(), so we need to pass
  this map to WKWebView to make number formatting work."
  (let [n          (.toLocaleString (js/Number 1000.1))
        delimiter? (= (count n) 7)]
    (if delimiter?
      {:delimiter (subs n 1 2)
       :separator (subs n 5 6)}
      {:delimiter ""
       :separator (subs n 4 5)})))

(defn label-number [number]
  (when number
    (let [{:keys [delimiter separator]} delimeters]
      (.toNumber rn-dependencies/i18n
                 (string/replace number #"," ".")
                 (clj->js {:precision                 10
                           :strip_insignificant_zeros true
                           :delimiter                 delimiter
                           :separator                 separator})))))

(def default-option-value "<no value>")

(defn label-options [options]
  ;; i18n ignores nil value, leading to misleading messages
  (into {} (for [[k v] options] [k (or v default-option-value)])))

(defn label
  ([path] (label path {}))
  ([path options]
   (if (exists? rn-dependencies/i18n.t)
     (let [options (update options :amount label-number)]
       (.t rn-dependencies/i18n (name path) (clj->js (label-options options))))
     (name path))))

(defn label-pluralize [count path & options]
  (if (exists? rn-dependencies/i18n.t)
    (.p rn-dependencies/i18n count (name path) (clj->js options))
    (name path)))

(defn message-status-label [status]
  (->> status
       (name)
       (str "t/status-")
       (keyword)
       (label)))

(def locale
  (.-locale rn-dependencies/i18n))

(defn get-contact-translated [contact-id key fallback]
  (let [translation #(get-in default-contacts [(keyword contact-id) key (keyword %)])]
    (or (translation locale)
        (translation (subs locale 0 2))
        fallback)))

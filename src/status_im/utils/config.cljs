(ns status-im.utils.config
  (:require ["react-native-config" :default react-native-config]
            [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]))

(def config
  (memoize
   (fn []
     (js->clj react-native-config :keywordize-keys true))))

(defn get-config
  ([k] (get (config) k))
  ([k not-found] (get (config) k not-found)))

;; TODO(oskarth): Extend this to deal with true/false for Jenkins parameter builds
(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.

(goog-define INFURA_TOKEN "800c641949d64d768a5070a1b0511938")
(goog-define OPENSEA_API_KEY "")

(def mainnet-rpc-url (str "https://mainnet.infura.io/v3/" INFURA_TOKEN))
(def testnet-rpc-url (str "https://ropsten.infura.io/v3/" INFURA_TOKEN))
(def goerli-rpc-url  (str "https://goerli.infura.io/v3/" INFURA_TOKEN))
(def opensea-api-key OPENSEA_API_KEY)
(def bootnodes-settings-enabled? (enabled? (get-config :BOOTNODES_SETTINGS_ENABLED "1")))
(def rpc-networks-only? (enabled? (get-config :RPC_NETWORKS_ONLY "1")))
(def mailserver-confirmations-enabled? (enabled? (get-config :MAILSERVER_CONFIRMATIONS_ENABLED)))
(def pairing-popup-disabled? (enabled? (get-config :PAIRING_POPUP_DISABLED "0")))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def snoopy-enabled? (enabled? (get-config :SNOOPY 0)))
(def dev-build? (enabled? (get-config :DEV_BUILD 0)))
(def tr-to-talk-enabled? (enabled? (get-config :TRIBUTE_TO_TALK 0)))
(def max-message-delivery-attempts (js/parseInt (get-config :MAX_MESSAGE_DELIVERY_ATTEMPTS "6")))
(def max-images-batch (js/parseInt (get-config :MAX_IMAGES_BATCH "1")))
;; NOTE: only disabled in releases
(def local-notifications? (enabled? (get-config :LOCAL_NOTIFICATIONS "1")))
(def blank-preview? (enabled? (get-config :BLANK_PREVIEW "1")))
(def group-chat-enabled? (enabled? (get-config :GROUP_CHATS_ENABLED "0")))
(def tooltip-events? (enabled? (get-config :TOOLTIP_EVENTS "0")))
(def commands-enabled? (enabled? (get-config :COMMANDS_ENABLED "0")))
(def keycard-test-menu-enabled? (enabled? (get-config :KEYCARD_TEST_MENU "1")))
(def qr-test-menu-enabled? (enabled? (get-config :QR_READ_TEST_MENU "0")))
(def quo-preview-enabled? (enabled? (get-config :ENABLE_QUO_PREVIEW "0")))
(def communities-enabled? (enabled? (get-config :COMMUNITIES_ENABLED "0")))
(def database-management-enabled? (enabled? (get-config :DATABASE_MANAGEMENT_ENABLED "0")))
(def debug-webview? (enabled? (get-config :DEBUG_WEBVIEW "0")))
(def delete-message-enabled? (enabled? (get-config :DELETE_MESSAGE_ENABLED "0")))
(def collectibles-enabled? (enabled? (get-config :COLLECTIBLES_ENABLED "1")))
(def test-stateofus? (enabled? (get-config :TEST_STATEOFUS "0")))
(def two-minutes-syncing? (enabled? (get-config :TWO_MINUTES_SYNCING "0")))
(def swap-enabled? (enabled? (get-config :SWAP_ENABLED "0")))
(def stickers-test-enabled? (enabled? (get-config :STICKERS_TEST_ENABLED "0")))

;; CONFIG VALUES
(def log-level
  (string/upper-case (get-config :LOG_LEVEL "")))
(def fleet (get-config :FLEET "eth.staging"))
(def apn-topic (get-config :APN_TOPIC "im.status.ethereum"))
(def default-network (get-config :DEFAULT_NETWORK "goerli_rpc"))
(def pow-target (js/parseFloat (get-config :POW_TARGET "0.0001")))
(def pow-time (js/parseInt (get-config :POW_TIME "1")))
(def max-installations 2)
; currently not supported in status-go
(def enable-remove-profile-picture? false)

(def verify-transaction-chain-id (js/parseInt (get-config :VERIFY_TRANSACTION_CHAIN_ID "1")))
(def verify-transaction-url (if (= :mainnet (ethereum/chain-id->chain-keyword verify-transaction-chain-id))
                              mainnet-rpc-url
                              goerli-rpc-url))

(def verify-ens-chain-id (js/parseInt (get-config :VERIFY_ENS_CHAIN_ID "1")))
(def verify-ens-url (if (= :mainnet (ethereum/chain-id->chain-keyword verify-ens-chain-id))
                      mainnet-rpc-url
                      goerli-rpc-url))
(def verify-ens-contract-address (get-config :VERIFY_ENS_CONTRACT_ADDRESS ((ethereum/chain-id->chain-keyword verify-ens-chain-id) ens/ens-registries)))

(def default-multiaccount
  {:preview-privacy?      blank-preview?
   :wallet/visible-tokens {:mainnet #{:SNT}}
   :currency :usd
   :appearance 0
   :profile-pictures-show-to 1
   :profile-pictures-visibility 1
   :log-level log-level
   :webview-allow-permission-requests? false
   :opensea-enabled?                   false
   :link-previews-enabled-sites        #{}
   :link-preview-request-enabled       true})

(defn default-visible-tokens [chain]
  (get-in default-multiaccount [:wallet/visible-tokens chain]))

(def mainnet-networks
  [{:id                  "mainnet_rpc",
    :chain-explorer-link "https://etherscan.io/address/",
    :name                "Mainnet with upstream RPC",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :mainnet)
                          :DataDir        "/ethereum/mainnet_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     mainnet-rpc-url}}}])

(def sidechain-networks
  [{:id                  "xdai_rpc",
    :name                "xDai Chain",
    :chain-explorer-link "https://blockscout.com/xdai/mainnet/address/",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :xdai)
                          :DataDir        "/ethereum/xdai_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     "https://gnosischain-rpc.gateway.pokt.network"}}}
   {:id                  "bsc_rpc",
    :chain-explorer-link "https://bscscan.com/address/",
    :name                "BSC Network",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :bsc)
                          :DataDir        "/ethereum/bsc_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     "https://bsc-dataseed.binance.org"}}}])

(def testnet-networks
  [{:id                  "testnet_rpc",
    :chain-explorer-link "https://ropsten.etherscan.io/address/",
    :name                "Ropsten with upstream RPC",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :testnet)
                          :DataDir        "/ethereum/testnet_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     testnet-rpc-url}}}
   {:id                  "rinkeby_rpc",
    :chain-explorer-link "https://rinkeby.etherscan.io/address/",
    :name                "Rinkeby with upstream RPC",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :rinkeby)
                          :DataDir        "/ethereum/rinkeby_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     (str "https://rinkeby.infura.io/v3/" INFURA_TOKEN)}}}
   {:id                  "goerli_rpc",
    :chain-explorer-link "https://goerli.etherscan.io/address/",
    :name                "Goerli with upstream RPC",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :goerli)
                          :DataDir        "/ethereum/goerli_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     goerli-rpc-url}}}
   {:id                  "bsc_testnet_rpc",
    :chain-explorer-link "https://testnet.bscscan.com/address/",
    :name                "BSC testnet",
    :config              {:NetworkId      (ethereum/chain-keyword->chain-id :bsc-testnet)
                          :DataDir        "/ethereum/bsc_testnet_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     "https://data-seed-prebsc-1-s1.binance.org:8545/"}}}])

(def default-networks
  (concat testnet-networks mainnet-networks sidechain-networks))

(def default-networks-by-id
  (into {}
        (map (fn [{:keys [id] :as network}]
               [id network])
             default-networks)))

(def link-preview-enabled-site?
  #{"youtube.com"
    "youtu.be"
    "our.status.im"
    "github.com"
    "giphy.com"
    "gph.is"
    "media.giphy.com"})

(def default-relay-provider "https://relay.walletconnect.com")

(def default-wallet-connect-metadata {:name "Status Wallet"
                                      :description "Status is a secure messaging app, crypto wallet, and Web3 browser built with state of the art technology."
                                      :url "#"
                                      :icons ["https://statusnetwork.com/img/press-kit-status-logo.svg"]})

(def wallet-connect-project-id "87815d72a81d739d2a7ce15c2cfdefb3")

(def new-ui-enabled? (atom false))

;; TODO: Remove this (highly) temporary flag once the new Activity Center is
;; usable enough to replace the old one **in the new UI**.
(def new-activity-center-enabled?
  (atom false))

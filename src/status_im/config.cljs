(ns status-im.config
  (:require
    [clojure.string :as string]
    [react-native.config :as react-native-config]
    [utils.ens.core :as utils.ens]
    [utils.ethereum.chain :as chain]))

(def get-config react-native-config/get-config)

(defn enabled? [v] (= "1" v))

(goog-define INFURA_TOKEN "e5193fde096b4a5d92433107ee22cf75")
(goog-define POKT_TOKEN "3ef2018191814b7e1009b8d9")
(goog-define OPENSEA_API_KEY "")
(goog-define RARIBLE_MAINNET_API_KEY "")
(goog-define RARIBLE_TESTNET_API_KEY "")
(goog-define ALCHEMY_ETHEREUM_MAINNET_TOKEN "")
(goog-define ALCHEMY_ETHEREUM_GOERLI_TOKEN "")
(goog-define ALCHEMY_ETHEREUM_SEPOLIA_TOKEN "")
(goog-define ALCHEMY_ARBITRUM_MAINNET_TOKEN "")
(goog-define ALCHEMY_ARBITRUM_GOERLI_TOKEN "")
(goog-define ALCHEMY_ARBITRUM_SEPOLIA_TOKEN "")
(goog-define ALCHEMY_OPTIMISM_MAINNET_TOKEN "")
(goog-define ALCHEMY_OPTIMISM_GOERLI_TOKEN "")
(goog-define ALCHEMY_OPTIMISM_SEPOLIA_TOKEN "")

(def mainnet-rpc-url (str "https://eth-archival.gateway.pokt.network/v1/lb/" POKT_TOKEN))
(def goerli-rpc-url (str "https://goerli-archival.gateway.pokt.network/v1/lb/" POKT_TOKEN))
(def mainnet-chain-explorer-link "https://etherscan.io/address/")
(def optimism-mainnet-chain-explorer-link "https://optimistic.etherscan.io/address/")
(def arbitrum-mainnet-chain-explorer-link "https://arbiscan.io/address/")
(def opensea-api-key OPENSEA_API_KEY)
(def bootnodes-settings-enabled? (enabled? (get-config :BOOTNODES_SETTINGS_ENABLED "1")))
(def mailserver-confirmations-enabled? (enabled? (get-config :MAILSERVER_CONFIRMATIONS_ENABLED)))
(def pairing-popup-disabled? (enabled? (get-config :PAIRING_POPUP_DISABLED "0")))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def snoopy-enabled? (enabled? (get-config :SNOOPY 0)))
(def dev-build? (enabled? (get-config :DEV_BUILD 0)))
(def max-message-delivery-attempts (js/parseInt (get-config :MAX_MESSAGE_DELIVERY_ATTEMPTS "6")))
;; NOTE: only disabled in releases
(def local-notifications? (enabled? (get-config :LOCAL_NOTIFICATIONS "1")))
(def blank-preview? (enabled? (get-config :BLANK_PREVIEW "1")))
(def group-chat-enabled? (enabled? (get-config :GROUP_CHATS_ENABLED "0")))
(def tooltip-events? (enabled? (get-config :TOOLTIP_EVENTS "0")))
(def commands-enabled? (enabled? (get-config :COMMANDS_ENABLED "0")))
(def keycard-test-menu-enabled? (enabled? (get-config :KEYCARD_TEST_MENU "1")))
(def qr-test-menu-enabled? (enabled? (get-config :QR_READ_TEST_MENU "0")))
(def quo-preview-enabled? (enabled? (get-config :ENABLE_QUO_PREVIEW "0")))
(def database-management-enabled? (enabled? (get-config :DATABASE_MANAGEMENT_ENABLED "0")))
(def debug-webview? (enabled? (get-config :DEBUG_WEBVIEW "0")))
(def collectibles-enabled? (enabled? (get-config :COLLECTIBLES_ENABLED "1")))
(def test-stateofus? (enabled? (get-config :TEST_STATEOFUS "0")))
(def two-minutes-syncing? (enabled? (get-config :TWO_MINUTES_SYNCING "0")))
(def swap-enabled? (enabled? (get-config :SWAP_ENABLED "0")))
(def stickers-test-enabled? (enabled? (get-config :STICKERS_TEST_ENABLED "0")))
(def local-pairing-mode-enabled? (enabled? (get-config :LOCAL_PAIRING_ENABLED "1")))

;; CONFIG VALUES
(def log-level (string/upper-case (get-config :LOG_LEVEL "")))
(def fleet (get-config :FLEET "eth.staging"))
(def apn-topic (get-config :APN_TOPIC "im.status.ethereum"))
(def default-network (get-config :DEFAULT_NETWORK "goerli_rpc"))
(def max-installations 2)
; currently not supported in status-go
(def enable-remove-profile-picture? false)

(def verify-transaction-chain-id (js/parseInt (get-config :VERIFY_TRANSACTION_CHAIN_ID "1")))
(def verify-transaction-url
  (if (= :mainnet (chain/chain-id->chain-keyword verify-transaction-chain-id))
    mainnet-rpc-url
    goerli-rpc-url))

(def verify-ens-chain-id (js/parseInt (get-config :VERIFY_ENS_CHAIN_ID "1")))
(def verify-ens-url
  (if (= :mainnet (chain/chain-id->chain-keyword verify-ens-chain-id))
    mainnet-rpc-url
    goerli-rpc-url))
(def verify-ens-contract-address
  (get-config :VERIFY_ENS_CONTRACT_ADDRESS
              ((chain/chain-id->chain-keyword verify-ens-chain-id) utils.ens/ens-registries)))

(def fast-create-community-enabled?
  (enabled? (get-config :FAST_CREATE_COMMUNITY_ENABLED "0")))

(def default-multiaccount
  {:preview-privacy?                   blank-preview?
   :wallet-legacy/visible-tokens       {:mainnet #{:SNT}}
   :currency                           :usd
   :appearance                         0
   :profile-pictures-show-to           2
   :profile-pictures-visibility        2
   :log-level                          log-level
   :webview-allow-permission-requests? false
   :opensea-enabled?                   false
   :link-previews-enabled-sites        #{}
   :link-preview-request-enabled       true})

(defn default-visible-tokens
  [chain]
  (get-in default-multiaccount [:wallet-legacy/visible-tokens chain]))

(def mainnet-networks
  [{:id                  "mainnet_rpc"
    :chain-explorer-link mainnet-chain-explorer-link
    :name                "Mainnet with upstream RPC"
    :config              {:NetworkId      (chain/chain-keyword->chain-id :mainnet)
                          :DataDir        "/ethereum/mainnet_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     mainnet-rpc-url}}}])

(def sidechain-networks
  [{:id                  "xdai_rpc"
    :name                "xDai Chain"
    :chain-explorer-link "https://blockscout.com/xdai/mainnet/address/"
    :config              {:NetworkId      (chain/chain-keyword->chain-id :xdai)
                          :DataDir        "/ethereum/xdai_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     "https://gnosischain-rpc.gateway.pokt.network"}}}
   {:id                  "bsc_rpc"
    :chain-explorer-link "https://bscscan.com/address/"
    :name                "BSC Network"
    :config              {:NetworkId      (chain/chain-keyword->chain-id :bsc)
                          :DataDir        "/ethereum/bsc_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     "https://bsc-dataseed.binance.org"}}}])

(def testnet-networks
  [{:id                  "goerli_rpc"
    :chain-explorer-link "https://goerli.etherscan.io/address/"
    :name                "Goerli with upstream RPC"
    :config              {:NetworkId      (chain/chain-keyword->chain-id :goerli)
                          :DataDir        "/ethereum/goerli_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL     goerli-rpc-url}}}
   {:id                  "bsc_testnet_rpc"
    :chain-explorer-link "https://testnet.bscscan.com/address/"
    :name                "BSC testnet"
    :config              {:NetworkId      (chain/chain-keyword->chain-id :bsc-testnet)
                          :DataDir        "/ethereum/bsc_testnet_rpc"
                          :UpstreamConfig {:Enabled true
                                           :URL "https://data-seed-prebsc-1-s1.binance.org:8545/"}}}])

(def default-networks
  (concat testnet-networks mainnet-networks sidechain-networks))

(def default-networks-by-id
  (into {}
        (map (fn [{:keys [id] :as network}]
               [id network])
             default-networks)))

(def default-network-id
  (get-in default-networks-by-id [default-network :config :NetworkId]))

(def default-network-rpc-url
  (get-in default-networks-by-id [default-network :config :UpstreamConfig :URL]))

(def waku-nodes-config
  {:status.prod
   ["enrtree://AL65EKLJAUXKKPG43HVTML5EFFWEZ7L4LOKTLZCLJASG4DSESQZEC@prod.status.nodes.status.im"]
   :status.test
   ["enrtree://AIO6LUM3IVWCU2KCPBBI6FEH2W42IGK3ASCZHZGG5TIXUR56OGQUO@test.status.nodes.status.im"]
   :wakuv2.prod
   ["enrtree://ANEDLO25QVUGJOUTQFRYKWX6P4Z4GKVESBMHML7DZ6YK4LGS5FC5O@prod.wakuv2.nodes.status.im"]
   :wakuv2.test
   ["enrtree://AO47IDOLBKH72HIZZOXQP6NMRESAN7CHYWIBNXDXWRJRZWLODKII6@test.wakuv2.nodes.status.im"]})

(def default-kdf-iterations 3200)

(def community-accounts-selection-enabled? true)
(def fetch-messages-enabled? (enabled? (get-config :FETCH_MESSAGES_ENABLED "1")))
(def test-networks-enabled? (enabled? (get-config :TEST_NETWORKS_ENABLED "0")))

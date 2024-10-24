(ns status-im.config
  (:require
    [clojure.string :as string]
    [react-native.config :as react-native-config]
    [status-im.constants :as constants]
    [utils.ens.core :as utils.ens]
    [utils.ethereum.chain :as chain]))

(def get-config react-native-config/get-config)

(defn enabled? [v] (= "1" v))

(goog-define INFURA_TOKEN "")
(goog-define POKT_TOKEN "3ef2018191814b7e1009b8d9")
(goog-define STATUS_BUILD_PROXY_USER "dev")
(goog-define STATUS_BUILD_PROXY_PASSWORD "Crypto_is_a_scam")
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
(goog-define WALLET_CONNECT_PROJECT_ID "87815d72a81d739d2a7ce15c2cfdefb3")
(goog-define MIXPANEL_APP_ID "3350627")
(goog-define MIXPANEL_TOKEN "5c73bda2d36a9f688a5ee45641fb6775")

(def mainnet-rpc-url (str "https://eth-archival.rpc.grove.city/v1/" POKT_TOKEN))
(def goerli-rpc-url (str "https://goerli-archival.gateway.pokt.network/v1/lb/" POKT_TOKEN))
(def mainnet-chain-explorer-link "https://etherscan.io/address/")
(def optimism-mainnet-chain-explorer-link "https://optimistic.etherscan.io/address/")
(def arbitrum-mainnet-chain-explorer-link "https://arbiscan.io/address/")
(def sepolia-chain-explorer-link "https://sepolia.etherscan.io/address/")
(def optimism-sepolia-chain-explorer-link "https://sepolia-optimistic.etherscan.io/address/")
(def arbitrum-sepolia-chain-explorer-link "https://sepolia.arbiscan.io/address/")
(def goerli-chain-explorer-link "https://goerli.etherscan.io/address/")
(def optimism-goerli-chain-explorer-link "https://goerli-optimistic.etherscan.io/address/")
(def opensea-link "https://opensea.io")
(def opensea-tesnet-link "https://testnets.opensea.io")

(def mixpanel-app-id MIXPANEL_APP_ID)
(def mixpanel-token MIXPANEL_TOKEN)

(def opensea-api-key OPENSEA_API_KEY)
(def status-proxy-enabled? true)
(def status-proxy-stage-name (get-config :STATUS_PROXY_STAGE_NAME "test"))
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
(def test-stateofus? (enabled? (get-config :TEST_STATEOFUS "0")))
(def two-minutes-syncing? (enabled? (get-config :TWO_MINUTES_SYNCING "0")))
(def swap-enabled? (enabled? (get-config :SWAP_ENABLED "0")))
(def stickers-test-enabled? (enabled? (get-config :STICKERS_TEST_ENABLED "0")))
(def local-pairing-mode-enabled? (enabled? (get-config :LOCAL_PAIRING_ENABLED "1")))
(def show-not-implemented-features? (enabled? (get-config :SHOW_NOT_IMPLEMENTED_FEATURES "0")))

;; CONFIG VALUES
(def log-level (string/upper-case (get-config :LOG_LEVEL "")))
(def log-request-go (enabled? (get-config :LOG_REQUEST_GO "0")))
(def fleet (get-config :FLEET ""))
(def apn-topic (get-config :APN_TOPIC "im.status.ethereum"))
(def default-network (get-config :DEFAULT_NETWORK "goerli_rpc"))
(def max-installations 2)
; currently not supported in status-go
(def enable-remove-profile-picture? false)

(defn env-variable->int
  [env-var-name default-value]
  (js/parseInt (get-config env-var-name default-value)))

(def delete-message-for-me-undo-time-limit-ms
  (env-variable->int :DELETE_MESSAGE_FOR_ME_UNDO_TIME_LIMIT
                     constants/delete-message-for-me-undo-time-limit-ms))

(def delete-message-undo-time-limit-ms
  (env-variable->int :DELETE_MESSAGE_UNDO_TIME_LIMIT
                     constants/delete-message-undo-time-limit-ms))

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

(def waku-nodes-config
  {:status.prod
   ["enrtree://AL65EKLJAUXKKPG43HVTML5EFFWEZ7L4LOKTLZCLJASG4DSESQZEC@prod.status.nodes.status.im"]
   :status.test
   ["enrtree://AIO6LUM3IVWCU2KCPBBI6FEH2W42IGK3ASCZHZGG5TIXUR56OGQUO@test.status.nodes.status.im"]
   :waku.sandbox
   ["enrtree://AIRVQ5DDA4FFWLRBCHJWUWOO6X6S4ZTZ5B667LQ6AJU6PEYDLRD5O@sandbox.waku.nodes.status.im"]
   :waku.test
   ["enrtree://AOGYWMBYOUIMOENHXCHILPKY3ZRFEULMFI4DOM442QSZ73TT2A7VI@test.waku.nodes.status.im"]})

(def community-accounts-selection-enabled? true)
(def fetch-messages-enabled? (enabled? (get-config :FETCH_MESSAGES_ENABLED "1")))
(def test-networks-enabled? (enabled? (get-config :TEST_NETWORKS_ENABLED "0")))

(def mobile-data-syncing-toggle-enabled?
  (enabled? (get-config :MOBILE_DATA_SYNCING_TOGGLE_ENABLE "1")))

;; Alert banners are disabled for debug builds because alert banners overlay
;; interfere with react-native debug tools, such as inspector and Perf monitor
(def enable-alert-banner? (enabled? (get-config :ENABLE_ALERT_BANNER "0")))

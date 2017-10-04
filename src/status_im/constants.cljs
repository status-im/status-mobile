(ns status-im.constants
  (:require [status-im.utils.types :as types]))

(def ethereum-rpc-url "http://localhost:8545")

(def server-address "http://api.status.im/")
;; (def server-address "http://10.0.3.2:3000/")
;; (def server-address "http://localhost:3000/")

(def text-content-type "text/plain")
(def content-type-log-message "log-message")
(def content-type-command "command")
(def content-type-command-request "command-request")
(def content-type-wallet-command "wallet-command")
(def content-type-wallet-request "wallet-request")
(def content-type-status "status")

(def max-chat-name-length 20)

(def response-input-hiding-duration 100)
(def response-suggesstion-resize-duration 100)

(def default-number-of-messages 20)
(def blocks-per-hour 120)

(def default-number-of-discover-search-results 20)

(def console-chat-id "console")
(def wallet-chat-id "wallet")

(def default-network "testnet_rpc")

(defn- transform-config [networks]
  (->> networks
       (map (fn [[network-name {:keys [config] :as data}]]
              [network-name (assoc data
                              :config (types/clj->json config)
                              :raw-config config)]))
       (into {})))

(def default-networks
  (transform-config
    {"testnet"     {:id     "testnet",
                    :name   "Ropsten",
                    :config {:NetworkId 3
                             :DataDir   "/ethereum/testnet"}}
     "testnet_rpc" {:id     "testnet_rpc",
                    :name   "Ropsten with upstream RPC",
                    :config {:NetworkId      3
                             :DataDir        "/ethereum/testnet_rpc"
                             :UpstreamConfig {:Enabled true
                                              :URL     "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"}}}
     "rinkeby"     {:id     "rinkeby",
                    :name   "Rinkeby",
                    :config {:NetworkId 4
                             :DataDir   "/ethereum/rinkeby"}}
     "rinkeby_rpc" {:id     "rinkeby_rpc",
                    :name   "Rinkeby with upstream RPC",
                    :config {:NetworkId      4
                             :DataDir        "/ethereum/rinkeby_rpc"
                             :UpstreamConfig {:Enabled true
                                              :URL     "https://rinkeby.infura.io/z6GCTmjdP3FETEJmMBI4"}}}
     "mainnet"     {:id     "mainnet",
                    :name   "Mainnet",
                    :config {:NetworkId 1
                             :DataDir   "/ethereum/mainnet"}}
     "mainnet_rpc" {:id     "mainnet_rpc",
                    :name   "Mainnet with upstream RPC",
                    :config {:NetworkId      1
                             :DataDir        "/ethereum/mainnet_rpc"
                             :UpstreamConfig {:Enabled true
                                              :URL     "https://mainnet.infura.io/z6GCTmjdP3FETEJmMBI4 "}}}}))

(def ^:const send-transaction-no-error-code        "0")
(def ^:const send-transaction-default-error-code   "1")
(def ^:const send-transaction-password-error-code  "2")
(def ^:const send-transaction-timeout-error-code   "3")
(def ^:const send-transaction-discarded-error-code "4")

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
(def default-networks
  {"mainnet"     {:id     "mainnet",
                  :name   "Mainnet",
                  :config (types/clj->json
                            {:NetworkId 1
                             :DataDir   "/ethereum/mainnet"})}
   "testnet"     {:id     "testnet",
                  :name   "Ropsten",
                  :config (types/clj->json
                            {:NetworkId 3
                             :DataDir   "/ethereum/testnet"})}
   "testnet_rpc" {:id     "testnet_rpc",
                  :name   "Ropsten with RPC",
                  :config (types/clj->json
                            {:NetworkId      3
                             :DataDir        "/ethereum/testnet_rpc"
                             :UpstreamConfig {:Enabled true
                                              :URL     "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"}})}
   "rinkeby"     {:id     "rinkeby",
                  :name   "Rinkeby",
                  :config (types/clj->json
                            {:NetworkId 4
                             :DataDir   "/ethereum/rinkeby"})}})

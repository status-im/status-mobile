(ns status-im.constants
  (:require [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.config :as config]))

(def ethereum-rpc-url "http://localhost:8545")

(def server-address "http://api.status.im/")
;; (def server-address "http://10.0.3.2:3000/")
;; (def server-address "http://localhost:3000/")

(def text-content-type "text/plain")
(def content-type-log-message "log-message")
(def content-type-command "command")
(def content-type-command-request "command-request") 
(def content-type-status "status")

(def min-password-length 6)
(def max-chat-name-length 20)
(def response-suggesstion-resize-duration 100)
(def default-number-of-messages 20)
(def blocks-per-hour 120)

(def console-chat-id "console")

(def default-network "testnet_rpc")

(def default-wallet-transactions
  {:filters
   {:type [{:id :inbound   :label (i18n/label :t/incoming)  :checked? true}
           {:id :outbound  :label (i18n/label :t/outgoing)  :checked? true}
           {:id :pending   :label (i18n/label :t/pending)   :checked? true}
           ;; TODO(jeluard) Restore once we support postponing transaction
           #_
           {:id :postponed :label (i18n/label :t/postponed) :checked? true}]}})

(defn- transform-config [networks]
  (->> networks
       (map (fn [[network-name {:keys [config] :as data}]]
              [network-name (assoc data
                              :config (types/clj->json config)
                              :raw-config config)]))
       (into {})))

(def mainnet-networks
  {"mainnet"     {:id     "mainnet",
                  :name   "Mainnet",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :mainnet)
                           :DataDir   "/ethereum/mainnet"}}
   "mainnet_rpc" {:id     "mainnet_rpc",
                  :name   "Mainnet with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :mainnet)
                           :DataDir        "/ethereum/mainnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://mainnet.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})

(def testnet-networks
  {"testnet"     {:id     "testnet",
                  :name   "Ropsten",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :testnet)
                           :DataDir   "/ethereum/testnet"}}
   "testnet_rpc" {:id     "testnet_rpc",
                  :name   "Ropsten with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :testnet)
                           :DataDir        "/ethereum/testnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"}}}
   "rinkeby"     {:id     "rinkeby",
                  :name   "Rinkeby",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :rinkeby)
                           :DataDir   "/ethereum/rinkeby"}}
   "rinkeby_rpc" {:id     "rinkeby_rpc",
                  :name   "Rinkeby with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :rinkeby)
                           :DataDir        "/ethereum/rinkeby_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://rinkeby.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})
(def default-networks
  (transform-config
   (merge testnet-networks
          (when config/mainnet-networks-enabled? mainnet-networks))))

;; adamb's status-cluster enode
(def default-wnode {:name "Status mailserver"
                    :address "enode://08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404"})

(def default-wnodes [default-wnode])

(def ^:const send-transaction-no-error-code        "0")
(def ^:const send-transaction-default-error-code   "1")
(def ^:const send-transaction-password-error-code  "2")
(def ^:const send-transaction-timeout-error-code   "3")
(def ^:const send-transaction-discarded-error-code "4")

(ns status-im.constants
  (:require [status-im.i18n :as i18n]
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
(def content-type-wallet-command "wallet-command")
(def content-type-wallet-request "wallet-request")
(def content-type-status "status")

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

(def mainnet-id 1)
(def ropsten-id 3)
(def rinkeby-id 4)

(defn get-testnet-name [testnet-id]
  (cond
    (= testnet-id ropsten-id) "Ropsten"
    (= testnet-id rinkeby-id) "Rinkeby"
    :else "Unknown"))

(def mainnet-networks
  {"mainnet"     {:id     "mainnet",
                  :name   "Mainnet",
                  :config {:NetworkId mainnet-id
                           :DataDir   "/ethereum/mainnet"}}
   "mainnet_rpc" {:id     "mainnet_rpc",
                  :name   "Mainnet with upstream RPC",
                  :config {:NetworkId      mainnet-id
                           :DataDir        "/ethereum/mainnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://mainnet.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})

(def testnet-networks
  {"testnet"     {:id     "testnet",
                  :name   "Ropsten",
                  :config {:NetworkId ropsten-id
                           :DataDir   "/ethereum/testnet"}}
   "testnet_rpc" {:id     "testnet_rpc",
                  :name   "Ropsten with upstream RPC",
                  :config {:NetworkId      ropsten-id
                           :DataDir        "/ethereum/testnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"}}}
   "rinkeby"     {:id     "rinkeby",
                  :name   "Rinkeby",
                  :config {:NetworkId rinkeby-id
                           :DataDir   "/ethereum/rinkeby"}}
   "rinkeby_rpc" {:id     "rinkeby_rpc",
                  :name   "Rinkeby with upstream RPC",
                  :config {:NetworkId      rinkeby-id
                           :DataDir        "/ethereum/rinkeby_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://rinkeby.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})
(def default-networks
  (transform-config
   (merge testnet-networks
          (when config/mainnet-networks-enabled? mainnet-networks))))

(def ^:const send-transaction-no-error-code        "0")
(def ^:const send-transaction-default-error-code   "1")
(def ^:const send-transaction-password-error-code  "2")
(def ^:const send-transaction-timeout-error-code   "3")
(def ^:const send-transaction-discarded-error-code "4")

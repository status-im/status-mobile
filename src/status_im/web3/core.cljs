(ns status-im.web3.core
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as dependencies]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [status-im.data-store.core :as data-store]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(defn make-internal-web3
  "This Web3 object will allow access to private RPC calls
  It should be only used for internal application needs and never provided to any
  3rd parties (DApps, etc)"
  []
  (let [Web3 (dependencies/Web3)]
    (Web3.
     #js {:sendAsync (fn [payload callback]
                       (status/call-private-rpc
                        (.stringify js/JSON payload)
                        (fn [response]
                          (if (= "" response)
                            (log/warn :web3-response-error)
                            (callback nil (.parse js/JSON response))))))})))

(fx/defn initialization-success
  {:events [::initialization-success]}
  [{:keys [db]} web3 address password login?]
  {:db (assoc db :web3 web3)
   (if login?
     ::data-store/change-multiaccount
     ::data-store/create-multiaccount) [address password]})

(re-frame/reg-fx
 ::initialize
 (fn [[address password login?]]
   (let [web3 (make-internal-web3)]
     (set! (.-defaultAccount (.-eth web3))
           (ethereum/normalized-address address))
     (re-frame/dispatch [::initialization-success web3 address password login?]))))

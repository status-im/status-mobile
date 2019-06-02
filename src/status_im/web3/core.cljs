(ns status-im.web3.core
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as dependencies]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]))

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

(defn get-web3 [cofx]
  (let [web3 (make-internal-web3)]
    (assoc cofx :web3 web3)))

;;; FX
(defn set-default-account
  [web3 address]
  (set! (.-defaultAccount (.-eth web3))
        (ethereum/normalized-address address)))

(defn fetch-node-version
  [web3 cb]
  (.. web3
      -version
      (getNode
       (fn [err resp]
         (if-not err
           (cb resp)
           (log/warn (str "unable to obtain web3 version:" err)))))))

(defn fetch-node-version-callback
  [resp {:keys [db]}]
  (if-let [node-version (second (re-find #"StatusIM/v(.*)/.*/.*" resp))]
    {:db (assoc db :web3-node-version node-version)}
    (log/warn (str "unexpected web3 version format: " "'" resp "'"))))

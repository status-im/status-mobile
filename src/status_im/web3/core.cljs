(ns status-im.web3.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.node.core :as node]
            [status-im.protocol.core :as protocol]
            [taoensso.timbre :as log]
            [status-im.native-module.core :as status]))

(defn make-internal-web3
  "This Web3 object will allow access to private RPC calls
  It should be only used for internal application needs and never provided to any
  3rd parties (DApps, etc)"
  []
  (dependencies/Web3.
   #js {:sendAsync (fn [payload callback]
                     (status/call-private-rpc
                      (.stringify js/JSON payload)
                      (fn [response]
                        (if (= "" response)
                          (log/warn :web3-response-error)
                          (callback nil (.parse js/JSON response))))))}))

(defn get-web3 [cofx]
  (let [web3 (make-internal-web3)]
    (assoc cofx :web3 web3)))

(fx/defn update-syncing-progress [cofx error sync]
  (fx/merge cofx
            (protocol/update-sync-state error sync)
            (node/update-sync-state error sync)))

;;; FX
(defn get-syncing [web3]
  (when web3
    (.getSyncing
     (.-eth web3)
     (fn [error sync]
       (re-frame/dispatch [:web3.callback/get-syncing-success error sync])))))

(defn get-block-number-fx [web3]
  (when web3
    (.getBlockNumber
     (.-eth web3)
     (fn [error block-number]
       (re-frame/dispatch [:web3.callback/get-block-number error block-number])))))

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

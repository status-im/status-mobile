(ns status-im.contexts.browser.api
  (:require [camel-snake-kebab.extras :as cske]
            [cljs.pprint :as pprint]
            [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc]
            [utils.transforms :as transforms]))

(defn- process-dapp-permissions
  [permissions]
  (->> permissions
       (cske/transform-keys transforms/->kebab-case-keyword)
       (into {} (map (juxt :url identity)))))

(defn get-dapp-permissions
  []
  (-> (rpc/call-async :connector_getPermittedDAppsList false)
      (promesa/then process-dapp-permissions)))

(defn call-connector-rpc
  [json-rpc dapp]
  (let [arg (-> {:params  []
                 :jsonrpc "2.0"}
                (merge json-rpc)
                (merge dapp)
                (dissoc :topic))]
    (rpc/call-async :connector_callRPC false (transforms/clj->json arg))))

(defn approve-accounts-request
  [response]
  (->> response
       clj->js
       (rpc/call-async :connector_requestAccountsAccepted false)))

(defn reject-accounts-request
  [response]
  (->> response
       clj->js
       (rpc/call-async :connector_requestAccountsRejected false)))

(defn approve-transaction
  [response]
  (->> response
       clj->js
       (rpc/call-async :connector_sendTransactionAccepted false)))

(defn reject-transaction
  [response]
  (->> response
       clj->js
       (rpc/call-async :connector_sendTransactionRejected false)))

(defn get-dapp-browsers
  []
  (-> (rpc/call-async :wakuext_getBrowsers false)
      (promesa/then process-dapp-permissions)))

(defn get-dapp-bookmarks
  []
  (-> (rpc/call-async :wakuext_getBookmarks false)
      (promesa/then process-dapp-permissions)))

(defn save-browser
  [browser]
  (-> (rpc/call-async :wakuext_addBrowser browser false)
      (promesa/then process-dapp-permissions)))

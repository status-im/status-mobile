(ns status-im.protocol.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.models.protocol :as models]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.web3-provider :as web3-provider]))

;;;; COFX
(re-frame/reg-cofx
 :protocol/get-web3
 (fn [cofx _]
   (let [web3 (web3-provider/make-internal-web3)]
     (assoc cofx :web3 web3))))

;;; FX
(re-frame/reg-fx
 :protocol/web3-get-syncing
 (fn [web3]
   (when web3
     (.getSyncing
      (.-eth web3)
      (fn [error sync]
        (re-frame/dispatch [:update-sync-state error sync]))))))

(re-frame/reg-fx
 :protocol/set-default-account
 (fn [[web3 address]]
   (set! (.-defaultAccount (.-eth web3))
         (ethereum/normalized-address address))))

;;; NODE SYNC STATE

(handlers/register-handler-db
 :update-sync-state
 (fn [cofx [_ error sync]]
   (models/update-sync-state cofx error sync)))

(handlers/register-handler-fx
 :check-sync-state
 (fn [cofx _]
   (models/check-sync-state cofx)))

(handlers/register-handler-fx
 :start-check-sync-state
 (fn [cofx _]
   (models/start-check-sync-state cofx)))

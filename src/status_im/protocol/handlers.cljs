(ns status-im.protocol.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.models.protocol :as models]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :as utils]))

;;;; FX
(re-frame/reg-fx
 :protocol/assert-correct-network
 (fn [{:keys [web3 network-id]}]
   ;; ensure that node was started correctly
   (when (and network-id web3) ; necessary because of the unit tests
     (.getNetwork (.-version web3)
                  (fn [error fetched-network-id]
                    (when (and (not error) ; error most probably means we are offline
                               (not= network-id fetched-network-id))
                      (utils/show-popup
                       "Ethereum node started incorrectly"
                       "Ethereum node was started with incorrect configuration, application will be stopped to recover from that condition."
                       #(re-frame/dispatch [:close-application]))))))))

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

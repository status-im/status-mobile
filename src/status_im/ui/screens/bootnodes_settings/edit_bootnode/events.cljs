(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.models.bootnode :as models.bootnode]))

(handlers/register-handler-fx
 :save-new-bootnode
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx _]
   (models.bootnode/upsert cofx)))

(handlers/register-handler-fx
 :bootnode-set-input
 (fn [cofx [_ input-key value]]
   (models.bootnode/set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-bootnode
 (fn [cofx [_ bootnode-id]]
   (models.bootnode/edit bootnode-id cofx)))

(handlers/register-handler-fx
 :delete-bootnode
 (fn [cofx [_ bootnode-id]]
   (assoc (models.bootnode/delete bootnode-id cofx)
          :dispatch [:navigate-back])))

(handlers/register-handler-fx
 :set-bootnode-from-qr
 (fn [cofx [_ _ url]]
   (assoc (models.bootnode/set-input :url url cofx)
          :dispatch [:navigate-back])))

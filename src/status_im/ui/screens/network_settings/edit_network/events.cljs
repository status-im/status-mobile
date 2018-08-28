(ns status-im.ui.screens.network-settings.edit-network.events
  (:require [re-frame.core :as re-frame]
            [status-im.models.network :as models.network]
            [status-im.utils.handlers :refer [register-handler] :as handlers]))

(handlers/register-handler-fx
 :save-new-network
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx]
   (models.network/save cofx
                        {:data       (get-in cofx [:db :network/manage])
                         :on-success (fn []
                                       {:dispatch [:navigate-back]})})))

(handlers/register-handler-fx
 :network-set-input
 (fn [cofx [_ input-key value]]
   (models.network/set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-network
 (fn [cofx]
   (models.network/edit cofx)))

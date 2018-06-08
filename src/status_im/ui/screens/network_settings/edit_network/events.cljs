(ns status-im.ui.screens.network-settings.edit-network.events
  (:require [re-frame.core :as re-frame]
            [status-im.models.network :as models.network]
            [status-im.utils.handlers :refer [register-handler] :as handlers]))

(handlers/register-handler-fx
 :save-new-network
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx _]
   (models.network/save cofx)))

(handlers/register-handler-fx
 :network-set-input
 (fn [cofx [_ input-key value]]
   (models.network/set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-network
 (fn [cofx _]
   (models.network/edit cofx)))

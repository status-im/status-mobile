(ns status-im.ui.screens.extensions.events
  (:require [re-frame.core :as re-frame]
            [pluto.registry :as registry]
            [status-im.utils.handlers :as handlers]
            status-im.ui.screens.extensions.add.events))

(handlers/register-handler-fx
 :extensions/toggle-activation
 (fn [{:keys [db]} [_ id m]]
   nil))

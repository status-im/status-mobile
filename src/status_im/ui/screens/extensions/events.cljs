(ns status-im.ui.screens.extensions.events
  (:require [re-frame.core :as re-frame]
            [pluto.registry :as registry] 
            [status-im.utils.handlers :as handlers] 
            status-im.ui.screens.extensions.add.events))

(handlers/register-handler-db
 :extensions/toggle-activation
 [re-frame/trim-v]
 (fn [db [id m]]
   nil))


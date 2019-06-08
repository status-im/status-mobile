(ns status-im.extensions.ui.add.events
  (:require [status-im.extensions.registry :as extensions.registry]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(handlers/register-handler-fx
 :extensions/stage
 (fn [cofx [_ url extension-data]]
   (extensions.registry/stage-extension cofx url extension-data false)))

(handlers/register-handler-fx
 :extensions/stage-modal
 (fn [cofx [_ url extension-data]]
   (extensions.registry/stage-extension cofx url extension-data true)))

(handlers/register-handler-fx
 :extensions/add-to-registry
 (fn [cofx [_ extension-key {:keys [data]} active?]]
   (extensions.registry/add-to-registry cofx extension-key data active?)))

(handlers/register-handler-fx
 :extensions/disable-all-hooks
 (fn [cofx [_ extensions]]
   (apply fx/merge cofx (map (fn [{:keys [id]}]
                               (extensions.registry/disable-hooks cofx id))
                             extensions))))

(ns status-im.ui.screens.extensions.add.events
  (:require [re-frame.core :as re-frame]
            [status-im.extensions.core :as extensions]
            [status-im.extensions.registry :as extensions.registry]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            [status-im.i18n :as i18n]))

(re-frame/reg-fx
 :extensions/load
 (fn [{:keys [extensions follow-up]}]
   (doseq [{:keys [url active?]} extensions]
     (extensions/load-from url #(re-frame/dispatch [follow-up url (extensions/parse-extension % url) active?])))))

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
 :extensions/update-hooks
 (fn [cofx [_ extensions]]
   (apply fx/merge cofx (map (fn [{:keys [url]}]
                               (extensions.registry/update-hooks cofx url))
                             extensions))))

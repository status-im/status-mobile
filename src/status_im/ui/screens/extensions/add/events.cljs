(ns status-im.ui.screens.extensions.add.events
  (:require [pluto.registry :as registry]
            [re-frame.core :as re-frame]
            [status-im.extensions.core :as extensions]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(re-frame/reg-fx
 :extensions/load
 (fn [{:keys [extensions follow-up]}]
   (doseq [{:keys [url active?]} extensions]
     (extensions/load-from url #(re-frame/dispatch [follow-up (-> % extensions/read-extension extensions/parse) active?])))))

(handlers/register-handler-fx
 :extensions/stage
 (fn [{:keys [db] :as cofx} [_ extension-data]]
   (fx/merge cofx
             {:db (assoc db :staged-extension extension-data)}
             (navigation/navigate-to-cofx :show-extension nil))))

(handlers/register-handler-fx
 :extensions/add
 (fn [cofx [_ {:keys [data]} active?]]
   (extensions/add cofx data active?)))

(handlers/register-handler-fx
 :extensions/deactivate-all
 (fn [cofx [_ extensions]]
   (apply fx/merge cofx (map (fn [{:keys [name]}]
                               (partial registry/deactivate name))
                             extensions))))
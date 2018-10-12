(ns status-im.ui.screens.extensions.add.events
  (:require [re-frame.core :as re-frame]
            [status-im.extensions.core :as extensions]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(re-frame/reg-fx
 :extension/load
 (fn [[url follow-up-event]]
   (extensions/load-from url #(re-frame/dispatch [follow-up-event (-> % extensions/read-extension extensions/parse)]))))

(handlers/register-handler-fx
 :extension/stage
 (fn [{:keys [db] :as cofx} [_ extension-data]]
   (fx/merge cofx
             {:db (assoc db :staged-extension extension-data)}
             (navigation/navigate-to-cofx :show-extension nil))))

(handlers/register-handler-fx
 :extension/add
 (fn [cofx [_ data active?]]
   (extensions/add cofx data active?)))
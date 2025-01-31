(ns status-im.contexts.shell.events
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-event-fx :shell/change-tab
 (fn [{:keys [db]} [stack-id]]
   {:db (-> db
            (assoc :view-id stack-id)
            (assoc :shell/selected-stack-id stack-id))
    :fx [[:effects.shell/change-tab stack-id]]}))

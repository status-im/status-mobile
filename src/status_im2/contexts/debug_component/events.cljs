(ns status-im2.contexts.debug-component.events
  (:require
    [re-frame.core :as re-frame]))

(def ^:const debug-screen-name :dev-debug-component-sheet)

(when js/goog.DEBUG
  (re-frame/reg-event-fx :debug-component
   (fn [{:keys [db]} [view]]
     (let [view-id   (:view-id db)
           navigate? (not= view-id debug-screen-name)]
       (cond-> {:db (assoc db :debug/component view)}
         navigate? (assoc :fx [[:dispatch [:navigate-to debug-screen-name]]]))))))

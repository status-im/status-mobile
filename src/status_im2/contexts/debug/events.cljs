(ns status-im2.contexts.debug.events
  (:require
    [utils.re-frame :as rf]))

(rf/defn debug-view-event
  {:events [:debug/debug-view]}
  [{:keys [db]} view]
  {:db (assoc db :debug/view view)})

(defn debug-view
  [view]
  (let [view-id (rf/sub [:view-id])]
    (rf/dispatch [:debug/debug-view view])
    (when (not= view-id :debug)
      (rf/dispatch [:navigate-to :debug]))))

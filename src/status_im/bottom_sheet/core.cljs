(ns status-im.bottom-sheet.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:db (assoc db
              :bottom-sheet/show? true
              :bottom-sheet/view view
              :bottom-sheet/options options)})

(fx/defn show-bottom-sheet-event
  {:events [:bottom-sheet/show-sheet]}
  [cofx view options]
  (show-bottom-sheet
   cofx
   {:view    view
    :options options}))

(fx/defn hide-bottom-sheet
  {:events [:bottom-sheet/hide]}
  [{:keys [db]}]
  {:db (assoc db :bottom-sheet/show? false)})

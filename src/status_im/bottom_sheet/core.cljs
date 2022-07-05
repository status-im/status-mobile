(ns status-im.bottom-sheet.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:show-bottom-sheet nil
   :db (assoc db
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
  {:hide-bottom-sheet nil
   :db (assoc db :bottom-sheet/show? false)})

(fx/defn show-bottom-sheet-redesign
  [{:keys [db]} {:keys [view options]}]
  {:show-bottom-sheet-redesign nil
   :db (assoc db
              :bottom-sheet-redesign/show? true
              :bottom-sheet-redesign/view view
              :bottom-sheet-redesign/options options)})

(fx/defn show-bottom-sheet-event-redesign
  {:events [:bottom-sheet-redesign/show-sheet]}
  [cofx view options]
  (show-bottom-sheet-redesign
   cofx
   {:view    view
    :options options}))

(fx/defn hide-bottom-sheet-redesign
  {:events [:bottom-sheet-redesign/hide]}
  [{:keys [db]}]
  {:hide-bottom-sheet-redesign nil
   :db (assoc db :bottom-sheet-redesign/show? false)})

(ns status-im.bottom-sheet.core
  (:require [utils.re-frame :as rf]))

(rf/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:show-bottom-sheet nil
   :db                (assoc db
                             :bottom-sheet/show?   true
                             :bottom-sheet/view    view
                             :bottom-sheet/options options)})

(rf/defn show-bottom-sheet-event
  {:events [:bottom-sheet/show-sheet]}
  [cofx view options]
  (show-bottom-sheet
   cofx
   {:view    view
    :options options}))

(rf/defn hide-bottom-sheet
  {:events [:bottom-sheet/hide]}
  [{:keys [db]}]
  {:hide-bottom-sheet nil
   :db                (assoc db :bottom-sheet/show? false)})

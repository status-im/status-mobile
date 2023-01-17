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
  {:db (-> db
           (assoc :bottom-sheet/show? false)
           (assoc-in [:bottom-sheet/config :show-bottom-sheet?] nil))})

(rf/defn hide-bottom-sheet-navigation-overlay
  {:events [:bottom-sheet/hide-navigation-overlay]}
  [{}]
  {:hide-bottom-sheet nil})

(ns status-im2.common.bottom-sheet.events
  (:require [utils.re-frame :as rf]))

(rf/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:dispatch-n                [[:dismiss-keyboard]]
   :show-bottom-sheet-overlay nil
   :db                        (assoc db
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
  {:db                           (assoc db :bottom-sheet/show? false)
   :dismiss-bottom-sheet-overlay nil})

(rf/defn hide-bottom-sheet-navigation-overlay
  {:events [:bottom-sheet/hide-navigation-overlay]}
  [{}]
  {:dismiss-bottom-sheet-overlay nil})

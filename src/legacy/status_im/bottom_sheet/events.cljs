(ns legacy.status-im.bottom-sheet.events
  (:require
    [utils.re-frame :as rf]))

(rf/defn show-bottom-sheet-old
  [{:keys [db]} {:keys [view options]}]
  {:dismiss-keyboard              nil
   :show-bottom-sheet-overlay-old nil
   :db                            (assoc db
                                         :bottom-sheet/show?   true
                                         :bottom-sheet/view    view
                                         :bottom-sheet/options options)})

(rf/defn show-bottom-sheet-event
  {:events [:bottom-sheet/show-sheet-old]}
  [cofx view options]
  (show-bottom-sheet-old
   cofx
   {:view    view
    :options options}))

(rf/defn hide-bottom-sheet-old
  {:events [:bottom-sheet/hide-old]}
  [{:keys [db]}]
  {:db                               (assoc db :bottom-sheet/show? false)
   :dismiss-bottom-sheet-overlay-old nil})

(rf/defn hide-bottom-sheet-navigation-overlay
  {:events [:bottom-sheet/hide-old-navigation-overlay]}
  [{}]
  {:dismiss-bottom-sheet-overlay-old nil})

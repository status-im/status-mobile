(ns legacy.status-im.visibility-status-popover.core
  (:require
    [utils.re-frame :as rf]))

(rf/defn show-visibility-status-popover
  {:events [:show-visibility-status-popover]}
  [_ value]
  {:show-visibility-status-popover nil
   :dispatch-later                 [{:ms       250
                                     :dispatch [:show-visibility-status-popover-db value]}]
   :dismiss-keyboard               nil})

(rf/defn show-visibility-status-popover-db
  {:events [:show-visibility-status-popover-db]}
  [{:keys [db]} value]
  {:db (assoc db :visibility-status-popover/popover value)})

(rf/defn hide-visibility-status-popover
  {:events [:hide-visibility-status-popover]}
  [{:keys [db]}]
  {:db                             (dissoc db :visibility-status-popover/popover)
   :hide-visibility-status-popover nil})

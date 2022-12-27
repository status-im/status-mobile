(ns status-im.popover.core
  (:require [utils.re-frame :as rf]))

(rf/defn show-popover
  {:events [:show-popover]}
  [_ value]
  {:show-popover     nil
   ;;TODO refactor popover just start animation on mount
   :dispatch-later   [{:ms 250 :dispatch [:show-popover-db value]}]
   :dismiss-keyboard nil})

(rf/defn show-popover-db
  {:events [:show-popover-db]}
  [{:keys [db]} value]
  {:db (assoc db :popover/popover value)})

(rf/defn hide-popover
  {:events [:hide-popover]}
  [{:keys [db]}]
  {:db           (dissoc db :popover/popover)
   :hide-popover nil})

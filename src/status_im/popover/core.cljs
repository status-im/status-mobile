(ns status-im.popover.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn show-popover
  {:events [:show-popover]}
  [_ value]
  {:rnn-show-popover nil
   ;;TODO refactor popover just start animation on mount
   :dispatch-later   [{:ms 250 :dispatch [:show-popover-db value]}]
   :dismiss-keyboard nil})

(fx/defn show-popover-db
  {:events [:show-popover-db]}
  [{:keys [db]} value]
  {:db (assoc db :popover/popover value)})

(fx/defn hide-popover
  {:events [:hide-popover]}
  [{:keys [db]}]
  {:db               (dissoc db :popover/popover)
   :rnn-hide-popover nil})

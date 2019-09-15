(ns status-im.popover.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn show-popover
  {:events [:show-popover]}
  [{:keys [db]} value]
  {:db               (assoc db :popover/popover value)
   :dismiss-keyboard nil})

(fx/defn hide-popover
  {:events [:hide-popover]}
  [{:keys [db]}]
  {:db (dissoc db :popover/popover)})

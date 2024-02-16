(ns legacy.status-im.popover.core
  (:require
    [utils.re-frame :as rf]))

(rf/defn show-popover
  {:events [:show-popover]}
  [_ value]
  (let [delay-ms (or (:delay-ms value) 250)
        value    (dissoc value :delay-ms)]
    {:show-popover     nil
     ;; We should probably refactor to start the animation on mount, so that the
     ;; delay can be removed. See comment for more details:
     ;; https://github.com/status-im/status-mobile/pull/15222#issuecomment-1450162137
     :dispatch-later   [{:ms delay-ms :dispatch [:show-popover-db value]}]
     :dismiss-keyboard nil}))

(rf/defn show-popover-db
  {:events [:show-popover-db]}
  [{:keys [db]} value]
  {:db (assoc db :popover/popover value)})

(rf/defn hide-popover
  {:events [:hide-popover]}
  [{:keys [db]}]
  {:db           (dissoc db :popover/popover)
   :hide-popover nil})

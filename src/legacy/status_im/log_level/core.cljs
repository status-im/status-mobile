(ns legacy.status-im.log-level.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/defn save-log-level
  {:events [:log-level.ui/change-log-level-confirmed]}
  [{:keys [db] :as cofx} log-level]
  (let [old-log-level (get-in db [:profile/profile :log-level])]
    (when (not= old-log-level log-level)
      (let [need-set-log-enabled? (or (empty? old-log-level) (empty? log-level))
            log-enabled?          (boolean (seq log-level))]
        (when log-enabled?
          (native-module/set-log-level log-level))

        (when need-set-log-enabled?
          (native-module/set-log-enabled log-enabled?))

        (multiaccounts.update/multiaccount-update
         cofx
         :log-level
         log-level
         {:on-success #()})))))

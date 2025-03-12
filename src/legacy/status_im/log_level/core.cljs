(ns legacy.status-im.log-level.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :log-level/set-log-level
 (fn [log-level]
   (when (seq log-level)
     (native-module/set-log-level log-level))))

(rf/reg-fx
 :log-level/set-log-enabled
 (fn [enabled?]
   (native-module/set-log-enabled enabled?)))

(rf/defn save-log-level
  {:events [:log-level.ui/change-log-level-confirmed]}
  [{:keys [db] :as cofx} log-level]
  (let [old-log-level (get-in db [:profile/profile :log-level])]
    (if (not= old-log-level log-level)
      (let [need-set-log-enabled? (or (empty? old-log-level) (empty? log-level))
            log-enabled?          (boolean (seq log-level))]
        (merge
         (multiaccounts.update/multiaccount-update
          cofx
          :log-level
          log-level
          {:on-success #()})
         {:fx [[:log-level/set-log-level log-level]
               (when need-set-log-enabled?
                 [:log-level/set-log-enabled log-enabled?])
               ;; update log level in taoensso.timbre
               [:logs/set-level log-level]]})))))

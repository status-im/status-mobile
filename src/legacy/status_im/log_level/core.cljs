(ns legacy.status-im.log-level.core
  (:require
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :log-level/set-profile-log-level
 (fn [log-level]
   (when (seq log-level)
     (native-module/set-profile-log-level log-level))))

(rf/reg-fx
 :log-level/set-profile-log-enabled
 (fn [enabled?]
   (native-module/set-profile-log-enabled enabled?)))

(rf/defn save-log-level
  {:events [:log-level.ui/change-log-level-confirmed]}
  [{:keys [db]} log-level]
  (let [old-log-level (get-in db [:profile/profile :log-level])]
    (when (not= old-log-level log-level)
      (let [need-set-profile-log-enabled? (or (empty? old-log-level) (empty? log-level))
            profile-log-enabled?          (boolean (seq log-level))]
        {:fx [[:log-level/set-profile-log-level log-level]
              (when need-set-profile-log-enabled?
                [:log-level/set-profile-log-enabled profile-log-enabled?])
              ;; update log level in taoensso.timbre
              [:logs/set-level log-level]
              [:dispatch [:multiaccounts.ui/update :log-level log-level]]]}))))

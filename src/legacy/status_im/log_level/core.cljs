(ns legacy.status-im.log-level.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn save-log-level
  {:events [:log-level.ui/change-log-level-confirmed]}
  [{:keys [db]} log-level]
  (let [old-log-level (get-in db [:profile/profile :log-level])]
    (when (not= old-log-level log-level)
      (let [need-set-log-enabled? (or (empty? old-log-level) (empty? log-level))
            log-enabled?          (boolean (seq log-level))
            rpc-calls             (cond-> []
                                    log-enabled?
                                    (conj {:method   "wakuext_setLogLevel"
                                           :params   [{:logLevel log-level}]
                                           :on-error #(log/error "Failed to set log level" %)})

                                    need-set-log-enabled?
                                    (conj {:method   "wakuext_setLogEnabled"
                                           :params   [log-enabled?]
                                           :on-error #(log/error "Failed to set log enabled" %)}))]
        {:fx [[:json-rpc/call
               (conj (vec (map #(assoc % :on-success nil) (butlast rpc-calls)))
                     (assoc (last rpc-calls)
                            :on-success
                            #(rf/dispatch [:log-level/update-multiaccount log-level])))]]}))))

(rf/defn update-multiaccount
  {:events [:log-level/update-multiaccount]}
  [cofx log-level]
  (multiaccounts.update/multiaccount-update
   cofx
   :log-level
   log-level
   {:on-success #(rf/dispatch [:profile/logout])}))

(rf/defn show-change-log-level-confirmation
  {:events [:log-level.ui/log-level-selected]}
  [_ {:keys [name value]}]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/change-log-level
                                     {:log-level name})
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept           #(re-frame/dispatch
                           [:log-level.ui/change-log-level-confirmed value])
    :on-cancel           nil}})

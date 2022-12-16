(ns status-im.visibility-status-updates.core
  (:require [status-im.data-store.visibility-status-updates :as visibility-status-updates-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.datetime :as datetime]
            [status-im.ui.screens.profile.visibility-status.utils :as utils]))

(defn valid-status-type? [status-type]
  (some #(= status-type %)
        (list constants/visibility-status-always-online
              constants/visibility-status-inactive
              constants/visibility-status-automatic)))

(defn process-visibility-status-update [acc visibility-status-update]
  (let [real-status-type (utils/calculate-real-status-type visibility-status-update)]
    (assoc-in
     acc [:visibility-status-updates (:public-key visibility-status-update)]
     (assoc visibility-status-update :status-type real-status-type))))

(fx/defn load-visibility-status-updates
  {:events [:visibility-status-updates/visibility-status-updates-loaded]}
  [{:keys [db]} visibility-status-updates-loaded]
  (let [{:keys [visibility-status-updates]}
        (reduce (fn [acc visibility-status-update-loaded]
                  (let [visibility-status-update (visibility-status-updates-store/<-rpc
                                                  visibility-status-update-loaded)]
                    (process-visibility-status-update acc visibility-status-update)))
                {} visibility-status-updates-loaded)]
    {:db (assoc db :visibility-status-updates visibility-status-updates)}))

(defn handle-my-visibility-status-updates
  [acc my-current-status clock visibility-status-update]
  (let [status-type (:status-type visibility-status-update)]
    (if (and (valid-status-type? status-type)
             (or
              (nil? my-current-status)
              (> clock (:clock my-current-status))))
      (-> acc
          (update :current-user-visibility-status
                  merge {:clock clock :status-type status-type})
          (assoc :dispatch [:visibility-status-updates/send-visibility-status-updates?
                            (not= status-type constants/visibility-status-inactive)]))
      acc)))

(defn handle-other-visibility-status-updates
  [acc public-key clock visibility-status-update]
  (let [status-type (:status-type visibility-status-update)
        visibility-status-update-old
        (get-in acc [:visibility-status-updates public-key])]
    (if (and (valid-status-type? status-type)
             (or
              (nil? visibility-status-update-old)
              (> clock (:clock visibility-status-update-old))))
      (process-visibility-status-update acc visibility-status-update)
      acc)))

(fx/defn handle-visibility-status-updates
  [{:keys [db]} visibility-status-updates-received]
  (let [visibility-status-updates-old (get db :visibility-status-updates {})
        my-public-key                 (get-in
                                       db [:multiaccount :public-key])
        my-current-status             (get-in
                                       db [:multiaccount :current-user-visibility-status])
        {:keys [visibility-status-updates current-user-visibility-status dispatch]}
        (reduce (fn [acc visibility-status-update-received]
                  (let [{:keys [public-key clock] :as visibility-status-update}
                        (visibility-status-updates-store/<-rpc
                         visibility-status-update-received)]
                    (if (= public-key my-public-key)
                      (handle-my-visibility-status-updates
                       acc my-current-status clock visibility-status-update)
                      (handle-other-visibility-status-updates
                       acc public-key clock visibility-status-update))))
                {:visibility-status-updates      visibility-status-updates-old
                 :current-user-visibility-status my-current-status}
                visibility-status-updates-received)]
    (merge {:db (-> db
                    (update-in [:visibility-status-updates]
                               merge visibility-status-updates)
                    (update-in [:multiaccount :current-user-visibility-status]
                               merge current-user-visibility-status))}
           (when dispatch {:dispatch dispatch}))))

(fx/defn update-visibility-status
  {:events [:visibility-status-updates/update-visibility-status]}
  [{:keys [db] :as cofx} status-type]
  {:db (update-in db [:multiaccount :current-user-visibility-status]
                  merge {:status-type status-type
                         :clock       (datetime/timestamp-sec)})
   ::json-rpc/call [{:method     "wakuext_setUserStatus"
                     :params     [status-type ""]
                     :on-success #()}]})

(fx/defn send-visibility-status-updates?
  {:events [:visibility-status-updates/send-visibility-status-updates?]}
  [cofx val]
  (multiaccounts.update/multiaccount-update cofx
                                            :send-status-updates? val
                                            {}))

(fx/defn visibility-status-option-pressed
  {:events [:visibility-status-updates/visibility-status-option-pressed]}
  [{:keys [db] :as cofx} status-type]
  (let [events-to-dispatch-later
        (cond-> [{:ms 10 :dispatch
                  [:visibility-status-updates/update-visibility-status
                   status-type]}]
          (and
           (= status-type constants/visibility-status-inactive)
           (> (:peers-count db) 0))
          ;; Disable broadcasting further updates
          (conj {:ms 1000
                 :dispatch
                 [:visibility-status-updates/send-visibility-status-updates? false]}))]
    (fx/merge cofx
              {:dispatch-later events-to-dispatch-later}
              ;; Enable broadcasting for current broadcast
              (send-visibility-status-updates? true))))

(fx/defn delayed-visibility-status-update
  {:events [:visibility-status-updates/delayed-visibility-status-update]}
  [{:keys [db]} status-type]
  {:dispatch-later
   [{:ms 200
     :dispatch
     [:visibility-status-updates/visibility-status-option-pressed status-type]}]})

(fx/defn peers-summary-change
  [{:keys [db] :as cofx} peers-count]
  (let [send-visibility-status-updates?
        (get-in db [:multiaccount :send-status-updates?])
        status-type
        (get-in db [:multiaccount :current-user-visibility-status :status-type])]
    (when (and
           (> peers-count 0)
           send-visibility-status-updates?
           (= status-type constants/visibility-status-inactive))
      (fx/merge cofx
                {:dispatch-later [{:ms 1000 :dispatch
                                   [:visibility-status-updates/send-visibility-status-updates? false]}]
                 :db (assoc-in db [:multiaccount :send-status-updates?] false)}
                (update-visibility-status status-type)))))

(fx/defn sync-visibility-status-update
  [{:keys [db] :as cofx} visibility-status-update-received]
  (let [my-current-status           (get-in db [:multiaccount :current-user-visibility-status])
        {:keys [status-type clock]} (visibility-status-updates-store/<-rpc
                                     visibility-status-update-received)]
    (when (and (valid-status-type? status-type)
               (or
                (nil? my-current-status)
                (> clock (:clock my-current-status))))
      (fx/merge cofx
                {:db (update-in db [:multiaccount :current-user-visibility-status]
                                merge {:clock clock :status-type status-type})}
                (send-visibility-status-updates?
                 (not= status-type constants/visibility-status-inactive))))))

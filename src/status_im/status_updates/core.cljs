(ns status-im.status-updates.core
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.status-updates :as status-updates-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts.update]))

(fx/defn load-status-updates
  {:events [::status-updates-loaded]}
  [{:keys [db]} status-updates-loaded]
  (let [{:keys [status-updates]}
        (reduce (fn [prev-map status-update-loaded]
                  (let [{:keys [public-key] :as status-update} (status-updates-store/<-rpc status-update-loaded)]
                    (assoc-in prev-map [:status-updates public-key] status-update)))
                {:status-updates {}}
                status-updates-loaded)]
    {:db (assoc db :status-updates status-updates)}))

(fx/defn handle-status-updates
  [{:keys [db]} status-updates-received]
  (let [status-updates-old (if (nil? (:status-updates db)) {} (:status-updates db))
        {:keys [status-updates]}
        (reduce (fn [prev-map status-update-received]
                  (let [{:keys [public-key clock] :as status-update} (status-updates-store/<-rpc status-update-received)
                        status-update-old (get-in prev-map [:status-updates public-key])]
                    (when (or
                           (nil? status-update-old)
                           (> clock (:clock status-update-old)))
                      (assoc-in prev-map [:status-updates public-key] status-update))))
                {:status-updates status-updates-old}
                status-updates-received)]
    {:db (update-in db [:status-updates] merge status-updates)}))

(fx/defn initialize-status-updates [cofx]
  (status-updates-store/fetch-status-updates-rpc cofx #(re-frame/dispatch [::status-updates-loaded %])))

(fx/defn visibility-status-option-pressed
  {:events [:status-updates/visibility-status-option-pressed]}
  [_ status-type]
  (let [events-to-dispatch-later (atom [])]
    (swap! events-to-dispatch-later conj {:ms 10 :dispatch [:status-updates/update-visibility-status status-type]})
    (when
     (not= status-type constants/visibility-status-online)
      ;; Disable broadcasting further updates
      (swap! events-to-dispatch-later conj {:ms 1000 :dispatch [:status-updates/send-status-updates? false]}))
    {:dispatch        [:status-updates/send-status-updates? true] ;; Enable broadcasting for current update
     :dispatch-later  @events-to-dispatch-later}))

(fx/defn update-visibility-status
  {:events [:status-updates/update-visibility-status]}
  [{:keys [db] :as cofx} status-type]
  {:db (update-in db [:multiaccount :current-user-status] merge {:statusType status-type})
   ::json-rpc/call [{:method     "wakuext_setUserStatus"
                     :params     [status-type ""]
                     :on-success #()}]})

(fx/defn send-status-updates?
  {:events [:status-updates/send-status-updates?]}
  [cofx val]
  (multiaccounts.update/multiaccount-update cofx
                                            :send-status-updates? val
                                            {}))

(fx/defn timeout-user-online-status
  {:events [:status-updates/timeout-user-online-status]}
  [{:keys [db]} public-key clock]
  (let [current-clock (get-in db [:status-updates public-key :clock] 0)]
    (when (= current-clock clock)
      {:db (update-in db [:status-updates public-key] merge {:status-type constants/visibility-status-invisible})})))

(fx/defn countdown-for-online-user
  {:events [:status-updates/countdown-for-online-user]}
  [{:keys [db]} public-key clock ms]
  {:dispatch-later [{:ms ms
                     :dispatch [:status-updates/timeout-user-online-status public-key clock]}]})

(fx/defn delayed-visibility-status-update
  {:events [:status-updates/delayed-visibility-status-update]}
  [{:keys [db]} status-type]
  {:dispatch-later [{:ms 200
                     :dispatch [:status-updates/visibility-status-option-pressed status-type]}]})

(ns status-im.common.pairing.events
  (:require
    [status-im.constants :as constants]
    [status-im.contexts.communities.discover.events]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn handle-local-pairing-signals
  [{:keys [db]} {:keys [type action data error] :as event}]
  (log/info "local pairing signal received"
            {:event event})
  (let [{:keys [account password]}      data
        role                            (get-in db [:syncing :role])
        receiver?                       (= role constants/local-pairing-role-receiver)
        sender?                         (= role constants/local-pairing-role-sender)
        connection-success?             (and (= type
                                                constants/local-pairing-event-connection-success)
                                             (= action
                                                constants/local-pairing-action-connect))
        connection-error?               (and (= type
                                                constants/local-pairing-event-connection-error))
        error-on-pairing?               (contains? constants/local-pairing-event-errors type)
        completed-pairing?              (and (= type
                                                constants/local-pairing-event-transfer-success)
                                             (= action
                                                constants/local-pairing-action-pairing-installation))
        received-account?               (and (= type
                                                constants/local-pairing-event-received-account)
                                             (= action
                                                constants/local-pairing-action-pairing-account)
                                             (and (some? account) (some? password)))
        multiaccount-data               (when received-account?
                                          (merge account {:password password}))
        navigate-to-syncing-devices?    (and (or connection-success? connection-error?) receiver?)
        user-in-syncing-devices-screen? (or (= (:view-id db) :syncing-progress)
                                            (= (:view-id db) :profiles)
                                            (= (:view-id db) :syncing-progress-intro))
        user-in-sign-in-intro-screen?   (= (:view-id db) :sign-in-intro)]
    (merge {:db (cond-> db
                  connection-success?
                  (assoc-in [:syncing :pairing-status] :connected)

                  received-account?
                  (assoc-in [:syncing :profile] multiaccount-data)

                  error-on-pairing?
                  (assoc-in [:syncing :pairing-status] :error)

                  completed-pairing?
                  (assoc-in [:syncing :pairing-status] :completed))}
           (cond
             (and navigate-to-syncing-devices? (not user-in-syncing-devices-screen?))
             {:dispatch (if user-in-sign-in-intro-screen?
                          [:navigate-to-within-stack [:syncing-progress-intro :sign-in-intro]]
                          [:navigate-to :syncing-progress])}

             (and completed-pairing? sender?)
             {:dispatch [:syncing/clear-states]}

             (and completed-pairing? receiver?)
             {:dispatch [:profile.login/local-paired-user]}

             (and error-on-pairing? (some? error))
             {:dispatch [:toasts/upsert
                         {:type :negative
                          :text error}]}))))

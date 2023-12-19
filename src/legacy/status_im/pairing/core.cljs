(ns legacy.status-im.pairing.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [re-frame.core :as re-frame]
    [react-native.platform :as utils.platform]
    [status-im2.common.json-rpc.events :as json-rpc]
    [status-im2.config :as config]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn enable-installation-rpc
  [installation-id on-success on-error]
  (json-rpc/call {:method     "wakuext_enableInstallation"
                  :params     [installation-id]
                  :on-success on-success
                  :on-error   on-error}))

(defn disable-installation-rpc
  [installation-id on-success on-error]
  (json-rpc/call {:method     "wakuext_disableInstallation"
                  :params     [installation-id]
                  :on-success on-success
                  :on-error   on-error}))

(defn set-installation-metadata-rpc
  [installation-id metadata on-success on-error]
  (json-rpc/call {:method     "wakuext_setInstallationMetadata"
                  :params     [installation-id metadata]
                  :on-success on-success
                  :on-error   on-error}))

(defn get-our-installations-rpc
  [on-success on-error]
  (json-rpc/call {:method     "wakuext_getOurInstallations"
                  :params     []
                  :on-success on-success
                  :on-error   on-error}))

(defn compare-installation
  "Sort installations, first by our installation-id, then on whether is
  enabled, and last on timestamp value"
  [our-installation-id a b]
  (cond
    (= our-installation-id (:installation-id a))
    -1
    (= our-installation-id (:installation-id b))
    1
    :else
    (let [enabled-compare (compare (:enabled? b) (:enabled? a))]
      (if (not= 0 enabled-compare)
        enabled-compare
        (compare (:timestamp a) (:timestamp b))))))

(defn sort-installations
  [our-installation-id installations]
  (sort (partial compare-installation our-installation-id) installations))

(rf/defn send-pair-installation
  {:events [:pairing.ui/pair-devices-pressed]}
  [_]
  {:json-rpc/call [{:method     "wakuext_sendPairInstallation"
                    :params     []
                    :on-success #(log/info "sent pair installation message")}]})

(rf/defn prompt-dismissed
  {:events [:pairing.ui/prompt-dismissed]}
  [{:keys [db]}]
  {:db (assoc-in db [:pairing/prompt-user-pop-up] false)})

(rf/defn prompt-accepted
  {:events [:pairing.ui/prompt-accepted]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc-in db [:pairing/prompt-user-pop-up] false)}
            (navigation/navigate-to :installations nil)))

(rf/defn prompt-user-on-new-installation
  [{:keys [db]}]
  (when-not config/pairing-popup-disabled?
    {:db                   (assoc-in db [:pairing/prompt-user-pop-up] true)
     :ui/show-confirmation {:title               (i18n/label :t/pairing-new-installation-detected-title)
                            :content             (i18n/label
                                                  :t/pairing-new-installation-detected-content)
                            :confirm-button-text (i18n/label :t/pairing-go-to-installation)
                            :cancel-button-text  (i18n/label :t/cancel)
                            :on-cancel           #(re-frame/dispatch [:pairing.ui/prompt-dismissed])
                            :on-accept           #(re-frame/dispatch [:pairing.ui/prompt-accepted])}}))

(rf/defn set-name
  "Set the name of the device"
  {:events [:pairing.ui/set-name-pressed]}
  [{:keys [db]} installation-name]
  (let [our-installation-id (get-in db [:profile/profile :installation-id])]
    {:pairing/set-installation-metadata [our-installation-id
                                         {:name       installation-name
                                          :deviceType utils.platform/os}]}))

(rf/defn init
  [cofx]
  {:pairing/get-our-installations nil})

(rf/defn enable
  [{:keys [db]} installation-id]
  {:db (assoc-in db
        [:pairing/installations installation-id :enabled?]
        true)})

(rf/defn disable
  [{:keys [db]} installation-id]
  {:db (assoc-in db
        [:pairing/installations installation-id :enabled?]
        false)})

(defn handle-enable-installation-response-success
  "Callback to dispatch on enable signature response"
  [installation-id]
  (re-frame/dispatch [:pairing.callback/enable-installation-success installation-id]))

(defn handle-disable-installation-response-success
  "Callback to dispatch on disable signature response"
  [installation-id]
  (re-frame/dispatch [:pairing.callback/disable-installation-success installation-id]))

(defn handle-set-installation-metadata-response-success
  "Callback to dispatch on set-installation-metadata response"
  [installation-id metadata]
  (re-frame/dispatch [:pairing.callback/set-installation-metadata-success installation-id metadata]))

(defn handle-get-our-installations-response-success
  "Callback to dispatch on get-our-installation response"
  [result]
  (re-frame/dispatch [:pairing.callback/get-our-installations-success result]))

(defn enable-installation!
  [installation-id]
  (enable-installation-rpc
   installation-id
   (partial handle-enable-installation-response-success installation-id)
   nil))

(defn disable-installation!
  [installation-id]
  (disable-installation-rpc
   installation-id
   (partial handle-disable-installation-response-success installation-id)
   nil))

(defn set-installation-metadata!
  [installation-id metadata]
  (set-installation-metadata-rpc
   installation-id
   metadata
   (partial handle-set-installation-metadata-response-success installation-id metadata)
   nil))

(defn get-our-installations
  []
  (get-our-installations-rpc handle-get-our-installations-response-success nil))

(rf/defn enable-fx
  {:events [:pairing.ui/enable-installation-pressed]}
  [cofx installation-id]
  (if (< (count (filter :enabled? (vals (get-in cofx [:db :pairing/installations]))))
         (inc config/max-installations))
    {:pairing/enable-installation [installation-id]}
    {:effects.utils/show-popup {:title   (i18n/label :t/pairing-maximum-number-reached-title)

                                :content (i18n/label :t/pairing-maximum-number-reached-content)}}))

(rf/defn disable-fx
  {:events [:pairing.ui/disable-installation-pressed]}
  [_ installation-id]
  {:pairing/disable-installation [installation-id]})

(re-frame/reg-fx
 :pairing/enable-installation
 (fn [[installation-id]]
   (enable-installation! installation-id)))

(re-frame/reg-fx
 :pairing/disable-installation
 (fn [[installation-id]]
   (disable-installation! installation-id)))

(re-frame/reg-fx
 :pairing/set-installation-metadata
 (fn [[installation-id metadata]]
   (set-installation-metadata! installation-id metadata)))

(re-frame/reg-fx
 :pairing/get-our-installations
 get-our-installations)

(rf/defn send-installation-messages
  {:events [:pairing.ui/synchronize-installation-pressed]}
  [{:keys [db]}]
  (let [multiaccount                  (:profile/profile db)
        {:keys [name preferred-name]} multiaccount]
    {:json-rpc/call [{:method     "wakuext_syncDevices"
                      :params     [(or preferred-name name)]
                      :on-success #(log/debug "successfully synced devices")}]}))

(defn installation<-rpc
  [{:keys [metadata id enabled timestamp]}]
  {:installation-id id
   :name            (:name metadata)
   :timestamp       timestamp
   :device-type     (:deviceType metadata)
   :enabled?        enabled})

(rf/defn update-installation
  {:events [:pairing.callback/set-installation-metadata-success]}
  [{:keys [db]} installation-id metadata]
  {:db (update-in db
                  [:pairing/installations installation-id]
                  assoc
                  :installation-id installation-id
                  :name            (:name metadata)
                  :device-type     (:deviceType metadata))})

(rf/defn handle-installations
  [{:keys [db]} installations]
  {:db (update db
               :pairing/installations
               #(reduce
                 (fn [acc {:keys [id] :as i}]
                   (update acc id merge (installation<-rpc i)))
                 %
                 installations))})

(rf/defn load-installations
  {:events [:pairing.callback/get-our-installations-success]}
  [{:keys [db]} installations]
  {:db (assoc db
              :pairing/installations
              (reduce
               (fn [acc {:keys [id] :as i}]
                 (assoc acc
                        id
                        (installation<-rpc i)))
               {}
               installations))})

(rf/defn enable-installation-success
  {:events [:pairing.callback/enable-installation-success]}
  [cofx installation-id]
  (rf/merge cofx
            (enable installation-id)
            (multiaccounts.update/send-contact-update)))

(rf/defn disable-installation-success
  {:events [:pairing.callback/disable-installation-success]}
  [cofx installation-id]
  (rf/merge cofx
            (disable installation-id)
            (multiaccounts.update/send-contact-update)))

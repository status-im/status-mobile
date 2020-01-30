(ns status-im.pairing.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as models.chat]
            [status-im.contact.core :as contact]
            [status-im.contact.db :as contact.db]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.transport.message.protocol :as protocol]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.pairing :as pairing.utils]
            [status-im.utils.platform :as utils.platform]
            [status-im.utils.types :as types]))

(defn enable-installation-rpc [installation-id on-success on-failure]
  (json-rpc/call {:method (if config/waku-enabled?
                            "wakuext_enableInstallation"
                            "shhext_enableInstallation")
                  :params [installation-id]
                  :on-success on-success
                  :on-failure on-failure}))

(defn disable-installation-rpc [installation-id on-success on-failure]
  (json-rpc/call {:method (if config/waku-enabled?
                            "wakuext_disableInstallation"
                            "shhext_disableInstallation")
                  :params [installation-id]
                  :on-success on-success
                  :on-failure on-failure}))

(defn set-installation-metadata-rpc [installation-id metadata on-success on-failure]
  (json-rpc/call {:method (if config/waku-enabled?
                            "wakuext_setInstallationMetadata"
                            "shhext_setInstallationMetadata")
                  :params                 [installation-id metadata]
                  :on-success                 on-success
                  :on-failure                 on-failure}))

(defn get-our-installations-rpc [on-success on-failure]
  (json-rpc/call {:method (if config/waku-enabled?
                            "wakuext_getOurInstallations"
                            "shhext_getOurInstallations")
                  :params  []
                  :on-success       on-success
                  :on-failure       on-failure}))

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
    (let [enabled-compare (compare (:enabled? b)
                                   (:enabled? a))]
      (if (not= 0 enabled-compare)
        enabled-compare
        (compare (:timestamp b)
                 (:timestamp a))))))

(defn sort-installations
  [our-installation-id installations]
  (sort (partial compare-installation our-installation-id) installations))

(defn send-pair-installation
  [cofx]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_sendPairInstallation"
                               "shhext_sendPairInstallation")
                     :params []
                     :on-success #(log/info "sent pair installation message")}]})

(fx/defn prompt-dismissed [{:keys [db]}]
  {:db (assoc-in db [:pairing/prompt-user-pop-up] false)})

(fx/defn prompt-accepted [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:pairing/prompt-user-pop-up] false)}
            (navigation/navigate-to-cofx :installations nil)))

(fx/defn prompt-user-on-new-installation [{:keys [db]}]
  (when-not config/pairing-popup-disabled?
    {:db               (assoc-in db [:pairing/prompt-user-pop-up] true)
     :ui/show-confirmation {:title      (i18n/label :t/pairing-new-installation-detected-title)
                            :content    (i18n/label :t/pairing-new-installation-detected-content)
                            :confirm-button-text (i18n/label :t/pairing-go-to-installation)
                            :cancel-button-text  (i18n/label :t/cancel)
                            :on-cancel  #(re-frame/dispatch [:pairing.ui/prompt-dismissed])
                            :on-accept #(re-frame/dispatch [:pairing.ui/prompt-accepted])}}))

(fx/defn set-name
  "Set the name of the device"
  [{:keys [db] :as cofx} installation-name]
  (let [our-installation-id (get-in db [:multiaccount :installation-id])]
    {:pairing/set-installation-metadata [[our-installation-id {:name installation-name
                                                               :deviceType utils.platform/os}]]}))

(fx/defn init [cofx]
  {:pairing/get-our-installations nil})

(defn handle-bundles-added [{:keys [db] :as cofx} bundle]
  (let [installation-id  (:installationID bundle)]
    (when
     (and (= (:identity bundle)
             (multiaccounts.model/current-public-key cofx))
          (not= (get-in db [:multiaccount :installation-id]) installation-id))
      (fx/merge cofx
                (init)
                #(when-not (or (:pairing/prompt-user-pop-up db)
                               (= :installations (:view-id db)))
                   (prompt-user-on-new-installation %))))))

(fx/defn enable [{:keys [db]} installation-id]
  {:db (assoc-in db
                 [:pairing/installations installation-id :enabled?]
                 true)})

(fx/defn disable [{:keys [db]} installation-id]
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

(defn enable-installation! [installation-id]
  (enable-installation-rpc installation-id
                           (partial handle-enable-installation-response-success installation-id)
                           nil))

(defn disable-installation! [installation-id]
  (disable-installation-rpc installation-id
                            (partial handle-disable-installation-response-success installation-id)
                            nil))

(defn set-installation-metadata! [installation-id metadata]
  (set-installation-metadata-rpc installation-id
                                 metadata
                                 (partial handle-set-installation-metadata-response-success installation-id metadata)
                                 nil))

(defn get-our-installations []
  (get-our-installations-rpc handle-get-our-installations-response-success nil))

(defn enable-fx [cofx installation-id]
  (if (< (count (filter :enabled? (vals (get-in cofx [:db :pairing/installations])))) (inc config/max-installations))
    {:pairing/enable-installation installation-id}
    {:utils/show-popup {:title (i18n/label :t/pairing-maximum-number-reached-title)

                        :content (i18n/label :t/pairing-maximum-number-reached-content)}}))

(defn disable-fx [_ installation-id]
  {:pairing/disable-installation installation-id})

(re-frame/reg-fx
 :pairing/enable-installation
 enable-installation!)

(re-frame/reg-fx
 :pairing/disable-installation
 disable-installation!)

(re-frame/reg-fx
 :pairing/set-installation-metadata
 (fn [pairs]
   (doseq [[installation-id metadata] pairs]
     (set-installation-metadata! installation-id metadata))))

(re-frame/reg-fx
 :pairing/get-our-installations
 get-our-installations)

(defn send-installation-messages [{:keys [db]}]
  (let [multiaccount (:multiaccount db)
        {:keys [name preferred-name photo-path]} multiaccount]
    {::json-rpc/call [{:method (if config/waku-enabled?
                                 "wakuext_syncDevices"
                                 "shhext_syncDevices")
                       :params [(or preferred-name name) photo-path]
                       :on-success #(log/debug "successfully synced devices")}]}))

(defn installation<-rpc [{:keys [metadata id enabled]}]
  {:installation-id id
   :name (:name metadata)
   :timestamp (:timestamp metadata)
   :device-type (:deviceType metadata)
   :enabled? enabled})

(fx/defn update-installation [{:keys [db]} installation-id metadata]
  {:db (update-in db [:pairing/installations installation-id]
                  assoc
                  :installation-id installation-id
                  :name (:name metadata)
                  :device-type (:deviceType metadata))})

(fx/defn handle-installation [{:keys [db]} {:keys [id] :as i}]
  {:db (assoc-in db [:pairing/installations id] (installation<-rpc i))})

(fx/defn load-installations [{:keys [db]} installations]
  {:db (assoc db :pairing/installations (reduce
                                         (fn [acc {:keys [id] :as i}]
                                           (assoc acc id
                                                  (installation<-rpc i)))
                                         {}
                                         installations))})

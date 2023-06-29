(ns status-im.browser.permissions
  (:require [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [utils.re-frame :as rf]
            [status-im2.navigation.events :as navigation]))

(declare process-next-permission)
(declare send-response-to-bridge)

(def supported-permissions
  {constants/dapp-permission-qr-code      {:yield-control? true
                                           :allowed?       true}
   constants/dapp-permission-contact-code {:type        :profile
                                           :title       (i18n/label :t/wants-to-access-profile)
                                           :description (i18n/label :t/your-contact-code)
                                           :icon        :main-icons/profile}
   constants/dapp-permission-web3         {:type        :wallet
                                           :title       (i18n/label :t/dapp-would-like-to-connect-wallet)
                                           :description (i18n/label :t/allowing-authorizes-this-dapp)
                                           :icon        :main-icons/wallet}})

(rf/defn permission-yield-control
  [{:keys [db] :as cofx} dapp-name permission message-id params]
  (cond
    (= permission constants/dapp-permission-qr-code)
    (rf/merge (assoc-in cofx [:db :browser/options :yielding-control?] true)
              (qr-scanner/scan-qr-code {:handler        :browser.bridge.callback/qr-code-scanned
                                        :cancel-handler :browser.bridge.callback/qr-code-canceled
                                        :data           {:dapp-name  dapp-name
                                                         :permission permission
                                                         :message-id message-id}}))))

(rf/defn permission-show-permission
  [{:keys [db] :as cofx} dapp-name permission message-id yield-control?]
  {:db (assoc-in db
        [:browser/options :show-permission]
        {:requested-permission permission
         :message-id           message-id
         :dapp-name            dapp-name
         :yield-control?       yield-control?})})

(defn get-permission-data
  [cofx allowed-permission]
  (let [multiaccount (get-in cofx [:db :profile/profile])]
    (get {constants/dapp-permission-contact-code (:public-key multiaccount)
          constants/dapp-permission-web3         [(:dapps-address multiaccount)]}
         allowed-permission)))

(rf/defn send-response-to-bridge
  "Send response to the bridge. If the permission is allowed, send data associated
   with the permission"
  [{:keys [db] :as cofx} permission message-id allowed? data]
  {:browser/send-to-bridge (cond-> {:type       constants/api-response
                                    :isAllowed  allowed?
                                    :permission permission
                                    :messageId  message-id}
                             allowed?
                             (assoc :data data))})

(rf/defn update-dapp-permissions
  [{:keys [db]} dapp-name permission allowed?]
  (let [dapp-permissions-set    (set (get-in db [:dapps/permissions dapp-name :permissions]))
        allowed-permissions-set (if allowed?
                                  (conj dapp-permissions-set permission)
                                  (disj dapp-permissions-set permission))
        allowed-permissions     {:dapp        dapp-name
                                 :permissions (vec allowed-permissions-set)}]
    {:db            (assoc-in db [:dapps/permissions dapp-name] allowed-permissions)
     :json-rpc/call [{:method     "permissions_addDappPermissions"
                      :params     [allowed-permissions]
                      :on-success #()}]}))

(rf/defn revoke-permissions
  {:events [:browser/revoke-dapp-permissions]}
  [{:keys [db] :as cofx} dapp]
  (rf/merge cofx
            {:db            (update-in db [:dapps/permissions] dissoc dapp)
             :json-rpc/call [{:method     "permissions_deleteDappPermissions"
                              :params     [dapp]
                              :on-success #()}]}))

(rf/defn revoke-dapp-permissions
  {:events [:dapps/revoke-access]}
  [cofx dapp]
  (rf/merge cofx
            (revoke-permissions dapp)
            (navigation/navigate-back)))

(rf/defn clear-dapps-permissions
  [{:keys [db]}]
  (let [dapp-permissions (keys (:dapps/permissions db))]
    {:db            (dissoc db :dapps/permissions)
     :json-rpc/call (for [dapp dapp-permissions]
                      {:method     "permissions_deleteDappPermissions"
                       :params     [dapp]
                       :on-success #()})}))

(rf/defn process-next-permission
  "Process next permission by removing it from pending permissions and prompting user
  if there is no pending permissions left, save all granted permissions
  and return the result to the bridge"
  [{:keys [db] :as cofx} dapp-name]
  (let [{:keys [show-permission yielding-control?]} (get db :browser/options)]
    (if (or show-permission yielding-control?)
      {:db db}
      (let [pending-permissions (get-in db [:browser/options :pending-permissions])
            next-permission     (last pending-permissions)
            new-cofx            (update-in cofx [:db :browser/options :pending-permissions] butlast)]
        (when-let [{:keys [yield-control? permission message-id allowed? params]} next-permission]
          (if (and yield-control? allowed?)
            (permission-yield-control new-cofx dapp-name permission message-id params)
            (permission-show-permission new-cofx dapp-name permission message-id yield-control?)))))))

(rf/defn send-response-and-process-next-permission
  [{:keys [db] :as cofx} dapp-name requested-permission message-id]
  (rf/merge cofx
            (send-response-to-bridge requested-permission
                                     message-id
                                     true
                                     (get-permission-data cofx requested-permission))
            (process-next-permission dapp-name)))

(rf/defn allow-permission
  "Add permission to set of allowed permission and process next permission"
  {:events [:browser.permissions.ui/dapp-permission-allowed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [requested-permission message-id dapp-name yield-control? params]}
        (get-in db [:browser/options :show-permission])]
    (rf/merge (assoc-in cofx [:db :browser/options :show-permission] nil)
              (update-dapp-permissions dapp-name requested-permission true)
              (if yield-control?
                (permission-yield-control dapp-name requested-permission message-id params)
                (send-response-and-process-next-permission dapp-name requested-permission message-id)))))

(rf/defn deny-permission
  "Add permission to set of allowed permission and process next permission"
  {:events [:browser.permissions.ui/dapp-permission-denied]}
  [{:keys [db] :as cofx}]
  (let [{:keys [requested-permission message-id dapp-name]} (get-in db
                                                                    [:browser/options :show-permission])]
    (rf/merge (assoc-in cofx [:db :browser/options :show-permission] nil)
              (send-response-to-bridge requested-permission
                                       message-id
                                       false
                                       (get-permission-data cofx requested-permission))
              (process-next-permission dapp-name))))

(rf/defn process-permission
  "Process the permission requested by a dapp
  If supported permission is already granted, return the result immediatly to the bridge
  Otherwise process the first permission which will prompt user"
  [cofx dapp-name permission message-id params]
  (let [allowed-permissions  (set (get-in cofx [:db :dapps/permissions dapp-name :permissions]))
        permission-allowed?  (boolean (allowed-permissions permission))
        supported-permission (get supported-permissions permission)]
    (cond
      (not supported-permission)
      (send-response-to-bridge cofx permission message-id false nil)

      (and (or permission-allowed? (:allowed? supported-permission))
           (not (:yield-control? supported-permission)))
      (send-response-to-bridge cofx permission message-id true (get-permission-data cofx permission))

      :else
      (process-next-permission (update-in cofx
                                          [:db :browser/options :pending-permissions]
                                          conj
                                          {:permission     permission
                                           :allowed?       (or permission-allowed?
                                                               (:allowed? supported-permission))
                                           :yield-control? (:yield-control? supported-permission)
                                           :params         params
                                           :message-id     message-id})
                               dapp-name))))

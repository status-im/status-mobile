(ns status-im.browser.permissions
  (:require [status-im.constants :as constants]
            [status-im.data-store.dapp-permissions :as dapp-permissions]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]))

(def supported-permissions
  {constants/dapp-permission-contact-code {:title       (i18n/label :t/wants-to-access-profile)
                                           :description (i18n/label :t/your-contact-code)
                                           :icon        :icons/profile-active}
   constants/dapp-permission-web3         {:title       (i18n/label :t/dapp-would-like-to-connect-wallet)
                                           :description (i18n/label :t/allowing-authorizes-this-dapp)
                                           :icon        :icons/wallet-active}})

(defn get-permission-data [cofx allowed-permission]
  (let [account (get-in cofx [:db :account/account])]
    (get {constants/dapp-permission-contact-code (:public-key account)
          constants/dapp-permission-web3         (ethereum/normalized-address (:address account))}
         allowed-permission)))

(fx/defn send-response-to-bridge
  "Send response to the bridge. If the permission is allowed, send data associated
   with the permission"
  [{:keys [db] :as cofx} permission message-id allowed?]
  {:browser/send-to-bridge {:message (cond-> {:type       constants/api-response
                                              :isAllowed  allowed?
                                              :permission permission
                                              :messageId  message-id}
                                       allowed?
                                       (assoc :data (get-permission-data cofx permission)))
                            :webview (:webview-bridge db)}})

(fx/defn update-dapp-permissions
  [{:keys [db]} dapp-name permission allowed?]
  (let [dapp-permissions-set    (set (get-in db [:dapps/permissions dapp-name :permissions]))
        allowed-permissions-set (if allowed?
                                  (conj dapp-permissions-set permission)
                                  (disj dapp-permissions-set permission))
        allowed-permissions     {:dapp        dapp-name
                                 :permissions (vec allowed-permissions-set)}]
    {:db            (assoc-in db [:dapps/permissions dapp-name] allowed-permissions)
     :data-store/tx [(dapp-permissions/save-dapp-permissions allowed-permissions)]}))

(fx/defn process-next-permission
  "Process next permission by removing it from pending permissions and prompting user
  if there is no pending permissions left, save all granted permissions
  and return the result to the bridge"
  [{:keys [db] :as cofx} dapp-name]
  (if (get-in db [:browser/options :show-permission])
    {:db db}
    (let [pending-permissions (get-in db [:browser/options :pending-permissions])
          next-permission     (last pending-permissions)]
      (when next-permission
        {:db (-> db
                 (update-in [:browser/options :pending-permissions] butlast)
                 (assoc-in [:browser/options :show-permission]
                           {:requested-permission (:permission next-permission)
                            :message-id           (:message-id next-permission)
                            :dapp-name            dapp-name}))}))))

(fx/defn allow-permission
  "Add permission to set of allowed permission and process next permission"
  [{:keys [db] :as cofx}]
  (let [{:keys [requested-permission message-id dapp-name]} (get-in db [:browser/options :show-permission])]
    (fx/merge (assoc-in cofx [:db :browser/options :show-permission] nil)
              (update-dapp-permissions dapp-name requested-permission true)
              (send-response-to-bridge requested-permission message-id true)
              (process-next-permission dapp-name))))

(fx/defn deny-permission
  "Add permission to set of allowed permission and process next permission"
  [{:keys [db] :as cofx}]
  (let [{:keys [requested-permission message-id dapp-name]} (get-in db [:browser/options :show-permission])]
    (fx/merge (assoc-in cofx [:db :browser/options :show-permission] nil)
              (send-response-to-bridge requested-permission message-id false)
              (process-next-permission dapp-name))))

(fx/defn process-permission
  "Process the permission requested by a dapp
  If supported permission is already granted, return the result immediatly to the bridge
  Otherwise process the first permission which will prompt user"
  [cofx dapp-name permission message-id]
  (let [allowed-permissions   (set (get-in cofx [:db :dapps/permissions dapp-name :permissions]))
        permission-allowed?   (boolean (allowed-permissions permission))
        permission-supported? ((set (keys supported-permissions)) permission)]
    (if (or permission-allowed? (not permission-supported?))
      (send-response-to-bridge cofx permission message-id permission-allowed?)
      (process-next-permission (update-in cofx [:db :browser/options :pending-permissions]
                                          conj {:permission permission
                                                :message-id message-id})
                               dapp-name))))

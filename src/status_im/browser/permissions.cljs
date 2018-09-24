(ns status-im.browser.permissions
  (:require [clojure.set :as set]
            [status-im.constants :as constants]
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

(defn get-pending-permissions [db]
  (get-in db [:browser/options :pending-permissions]))

(defn remove-pending-permission [db pending-permission]
  (update-in db [:browser/options :pending-permissions] disj pending-permission))

(defn get-allowed-permissions [db]
  (get-in db [:browser/options :allowed-permissions]))

(defn get-requested-permissions [db]
  (get-in db [:browser/options :requested-permissions]))

(defn add-allowed-permission [db allowed-permission]
  (update-in db [:browser/options :allowed-permissions] conj allowed-permission))

(defn get-permissions-data [allowed-permissions cofx]
  (let [account (get-in cofx [:db :account/account])]
    (select-keys {constants/dapp-permission-contact-code (:public-key account)
                  constants/dapp-permission-web3         (ethereum/normalized-address
                                                          (:address account))}
                 (vec allowed-permissions))))

(fx/defn send-permissions-data-to-bridge
  "If there is granted permissions, return the data to the bridge
  If no permission were granted and dapp requested web3 permission,
  return `web3-permission-request-denied` message type
  Otherwise do nothing"
  ;;TODO(yenda): this was the behavior of the code prior to refactoring
  ;;if this is not the intended behavior please create an issue for that
  [{:keys [db] :as cofx}]
  (let [allowed-permissions (get-allowed-permissions db)
        requested-permissions (get-requested-permissions db)
        new-db (update db :browser/options dissoc
                       :pending-permissions
                       :allowed-permissions
                       :requested-permissions)]
    (cond
      (not-empty allowed-permissions)
      {:db new-db
       :browser/send-to-bridge {:message {:type constants/status-api-success
                                          :data (get-permissions-data allowed-permissions cofx)
                                          :keys (vec allowed-permissions)}
                                :webview (:webview-bridge db)}}
      (and (empty? allowed-permissions)
           (requested-permissions constants/dapp-permission-web3))
      {:db new-db
       :browser/send-to-bridge {:message {:type constants/web3-permission-request-denied}
                                :webview (:webview-bridge db)}}

      :else
      {:db new-db})))

(fx/defn update-dapp-permissions
  [{:keys [db]} dapp-name]
  (let [allowed-permissions-set (get-allowed-permissions db)
        allowed-permissions     {:dapp dapp-name
                                 :permissions (vec allowed-permissions-set)}]
    (when (not-empty allowed-permissions-set)
      {:db            (assoc-in db [:dapps/permissions dapp-name] allowed-permissions)
       :data-store/tx [(dapp-permissions/save-dapp-permissions allowed-permissions)]})))

(fx/defn process-next-permission
  "Process next permission by removing it from pending permissions
  and prompting user
  if there is no pending permissions left, save all granted permissions
  and return the result to the bridge"
  [{:keys [db] :as cofx} dapp-name]
  (let [pending-permissions (get-pending-permissions db)
        next-permission (first pending-permissions)]
    (if next-permission
      {:db (-> db
               (remove-pending-permission next-permission)
               (assoc-in [:browser/options :show-permission]
                         {:requested-permission next-permission
                          :dapp-name dapp-name}))}
      (fx/merge cofx
                {:db (assoc-in db [:browser/options :show-permission] nil)}
                (update-dapp-permissions dapp-name)
                (send-permissions-data-to-bridge)))))

(fx/defn allow-permission
  "Add permission to set of allowed permission and process next permission"
  [{:keys [db] :as cofx} dapp-name permission]
  (fx/merge cofx
            {:db (add-allowed-permission db permission)}
            (process-next-permission dapp-name)))

(fx/defn process-permissions
  "Process the permissions requested by a dapp
  If all supported permissions are already granted, return the result immediatly
  to the bridge
  Otherwise process the first permission which will prompt user"
  [cofx dapp-name requested-permissions]
  (let [requested-permissions-set    (set requested-permissions)
        current-dapp-permissions     (get-in cofx [:db :dapps/permissions dapp-name :permissions])
        current-dapp-permissions-set (set current-dapp-permissions)
        supported-permissions-set    (set (keys supported-permissions))
        allowed-permissions          (set/intersection requested-permissions-set
                                                       current-dapp-permissions-set)
        pending-permissions          (-> requested-permissions-set
                                         (set/intersection supported-permissions-set)
                                         (set/difference current-dapp-permissions-set))
        new-cofx                     (update-in cofx [:db :browser/options] merge
                                                {:pending-permissions pending-permissions
                                                 :allowed-permissions allowed-permissions
                                                 :requested-permissions requested-permissions-set})]
    (if (empty? pending-permissions)
      (send-permissions-data-to-bridge new-cofx)
      (process-next-permission new-cofx dapp-name))))

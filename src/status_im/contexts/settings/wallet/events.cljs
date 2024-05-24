(ns status-im.contexts.settings.wallet.events
  (:require
    [native-module.core :as native-module]
    [status-im.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(defn- update-keypair
  [keypairs key-uid update-fn]
  (mapcat (fn [keypair]
            (if (= (keypair :key-uid) key-uid)
              (if-let [updated (update-fn keypair)]
                [updated]
                [])
              [keypair]))
   keypairs))

(rf/reg-event-fx
 :wallet/rename-keypair-success
 (fn [{:keys [db]} [key-uid name]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(update-keypair % key-uid (fn [keypair] (assoc keypair :name name))))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/key-pair-name-updated)}]]]}))

(defn rename-keypair
  [_ [{:keys [key-uid keypair-name]}]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_updateKeypairName"
           :params     [key-uid keypair-name]
           :on-success [:wallet/rename-keypair-success key-uid keypair-name]
           :on-error   #(log/info "failed to rename keypair " %)}]]]})

(rf/reg-event-fx :wallet/rename-keypair rename-keypair)

(defn get-key-pair-export-connection
  [{:keys [db]} [{:keys [sha3-pwd keypair-key-uid callback]}]]
  (let [key-uid           (get-in db [:profile/profile :key-uid])
        config-map        (transforms/clj->json {:senderConfig {:loggedInKeyUid key-uid
                                                                :keystorePath ""
                                                                :keypairsToExport [keypair-key-uid]
                                                                :password (security/safe-unmask-data
                                                                           sha3-pwd)}
                                                 :serverConfig {:timeout 0}})
        handle-connection (fn [response]
                            (when (sync-utils/valid-connection-string? response)
                              (callback response)
                              (rf/dispatch [:hide-bottom-sheet])))]
    (native-module/get-connection-string-for-exporting-keypairs-keystores
     config-map
     handle-connection)))

(rf/reg-event-fx :wallet/get-key-pair-export-connection get-key-pair-export-connection)

(rf/reg-event-fx
 :wallet/remove-keypair-success
 (fn [{:keys [db]} [key-uid]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(update-keypair % key-uid (fn [_] nil)))
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/key-pair-removed)}]]]}))

(defn remove-keypair
  [_ [key-uid]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_deleteKeypair"
           :params     [key-uid]
           :on-success [:wallet/remove-keypair-success key-uid]
           :on-error   #(log/info "failed to remove keypair " %)}]]]})

(rf/reg-event-fx :wallet/remove-keypair remove-keypair)

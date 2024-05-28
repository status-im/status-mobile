(ns status-im.contexts.settings.wallet.events
  (:require
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [status-im.contexts.settings.wallet.data-store :as data-store]
    [status-im.contexts.syncing.events :as syncing-events]
    [status-im.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet/rename-keypair-success
 (fn [{:keys [db]} [key-uid name]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(data-store/update-keypair % key-uid (fn [keypair] (assoc keypair :name name))))
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

(rf/reg-fx :effects.connection-string/export-keypair
 (fn [{:keys [key-uid sha3-pwd keypair-key-uid on-success on-fail]}]
   (let [config-map (transforms/clj->json {:senderConfig {:loggedInKeyUid   key-uid
                                                          :keystorePath     ""
                                                          :keypairsToExport [keypair-key-uid]
                                                          :password         (security/safe-unmask-data
                                                                             sha3-pwd)}
                                           :serverConfig {:timeout 0}})]
     (-> (native-module/get-connection-string-for-exporting-keypairs-keystores
          config-map)
         (promesa/then (fn [response]
                         (if (sync-utils/valid-connection-string? response)
                           (on-success response)
                           (on-fail "generic-error: failed to get connection string"))))
         (promesa/catch on-fail)))))

(defn get-keypair-export-connection
  [{:keys [db]} [{:keys [sha3-pwd keypair-key-uid callback]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.connection-string/export-keypair
           {:key-uid         key-uid
            :sha3-pwd        sha3-pwd
            :keypair-key-uid keypair-key-uid
            :on-success      (fn [connect-string]
                               (callback connect-string)
                               (rf/dispatch [:hide-bottom-sheet]))
            :on-fail         (fn [error]
                               (rf/dispatch [:toasts/upsert
                                             {:type :negative
                                              :text error}]))}]]}))

(rf/reg-event-fx :wallet/get-keypair-export-connection get-keypair-export-connection)

(rf/reg-event-fx :wallet/remove-keypair-success
 (fn [{:keys [db]} [key-uid]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(data-store/update-keypair % key-uid (fn [_] nil)))
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

(defn make-keypairs-accounts-fully-operable
  [{:keys [db]} [key-uids-to-update]]
  (let [key-uids-set (set key-uids-to-update)
        keypair-name (data-store/extract-keypair-name db key-uids-set)]
    {:db (-> db
             (update-in [:wallet :accounts] #(data-store/make-accounts-fully-operable % key-uids-set))
             (update-in [:wallet :keypairs] #(data-store/make-keypairs-fully-operable % key-uids-set)))
     :fx [[:dispatch
           [:toasts/upsert
            {:type  :positive
             :theme :dark
             :text  (if (= (count key-uids-to-update) 1)
                      (i18n/label :t/key-pair-imported-successfully {:name keypair-name})
                      (i18n/label :t/key-pairs-successfully-imported
                                  {:count (count key-uids-to-update)}))}]]]}))

(rf/reg-event-fx :wallet/make-keypairs-accounts-fully-operable make-keypairs-accounts-fully-operable)

(rf/reg-fx :effects.connection-string/import-keypair
 (fn [{:keys [key-uid sha3-pwd keypairs-key-uids connection-string on-success on-fail]}]
   (let [config-map (.stringify js/JSON
                                (clj->js
                                 {:receiverConfig
                                  {:loggedInKeyUid   key-uid
                                   :keystorePath     ""
                                   :password         (security/safe-unmask-data
                                                      sha3-pwd)
                                   :keypairsToImport keypairs-key-uids}}))]
     (-> (native-module/input-connection-string-for-importing-keypairs-keystores
          connection-string
          config-map)
         (promesa/then (fn [res]
                         (let [error (when (syncing-events/extract-error res)
                                       (str "generic-error: " res))]
                           (if-not (some? error)
                             (on-success)
                             (on-fail error)))))
         (promesa/catch #(log/error "error import-keypair/connection-string " %))))))

(defn connection-string-for-import-keypair
  [{:keys [db]} [{:keys [sha3-pwd keypairs-key-uids connection-string]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.connection-string/import-keypair
           {:key-uid           key-uid
            :sha3-pwd          sha3-pwd
            :keypairs-key-uids keypairs-key-uids
            :connection-string connection-string
            :on-success        #(rf/dispatch [:wallet/make-keypairs-accounts-fully-operable
                                              keypairs-key-uids])
            :on-fail           #(rf/dispatch [:toasts/upsert
                                              {:type :negative
                                               :text %}])}]]}))

(rf/reg-event-fx :wallet/connection-string-for-import-keypair connection-string-for-import-keypair)

(defn success-keypair-qr-scan
  [_ [connection-string keypairs-key-uids]]
  {:fx [(if (sync-utils/valid-connection-string? connection-string)
          [:dispatch
           [:standard-auth/authorize-with-password
            {:blur?             true
             :theme             :dark
             :auth-button-label (i18n/label :t/confirm)
             :on-auth-success   (fn [password]
                                  (rf/dispatch [:hide-bottom-sheet])
                                  (rf/dispatch
                                   [:wallet/connection-string-for-import-keypair
                                    {:connection-string connection-string
                                     :keypairs-key-uids keypairs-key-uids
                                     :sha3-pwd          password}]))}]]
          [:dispatch
           [:toasts/upsert
            {:type  :negative
             :theme :dark
             :text  (i18n/label :t/invalid-qr)}]])]})

(rf/reg-event-fx :wallet/success-keypair-qr-scan success-keypair-qr-scan)

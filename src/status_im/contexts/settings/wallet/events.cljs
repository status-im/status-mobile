(ns status-im.contexts.settings.wallet.events
  (:require
    [status-im.contexts.settings.wallet.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

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

(defn get-keypair-export-connection
  [{:keys [db]} [{:keys [sha3-pwd keypair-key-uid callback]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.syncing/export-keypairs-keystores
           {:key-uid         key-uid
            :sha3-pwd        sha3-pwd
            :keypair-key-uid keypair-key-uid
            :on-success      (fn [connect-string]
                               (callback connect-string)
                               (rf/dispatch [:hide-bottom-sheet]))
            :on-fail         (fn [error]
                               (rf/dispatch [:toasts/upsert
                                             {:type :negative
                                              :text (.-message error)}]))}]]}))

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
           :on-error   #(log/error "failed to remove keypair " {:error %})}]]]})

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

(defn connection-string-for-import-keypair
  [{:keys [db]} [{:keys [sha3-pwd keypairs-key-uids connection-string]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.syncing/import-keypairs-keystores
           {:key-uid           key-uid
            :sha3-pwd          sha3-pwd
            :keypairs-key-uids keypairs-key-uids
            :connection-string connection-string
            :on-success        #(rf/dispatch [:wallet/make-keypairs-accounts-fully-operable %])
            :on-fail           #(rf/dispatch [:toasts/upsert
                                              {:type  :negative
                                               :theme :dark
                                               :text  %}])}]]}))

(rf/reg-event-fx :wallet/connection-string-for-import-keypair connection-string-for-import-keypair)

(defn success-keypair-qr-scan
  [_ [connection-string keypairs-key-uids]]
  {:fx [[:dispatch
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
                                   :sha3-pwd          password}]))}]]]})

(rf/reg-event-fx :wallet/success-keypair-qr-scan success-keypair-qr-scan)

(ns status-im.contexts.settings.wallet.events
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [react-native.platform :as platform]
    [status-im.contexts.settings.wallet.data-store :as data-store]
    [status-im.contexts.settings.wallet.network-settings.testnet-mode.view :as testnet]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx
 :wallet/rename-keypair-success
 (fn [{:keys [db]} [key-uid name]]
   {:db (update-in db [:wallet :keypairs key-uid] assoc :name name)
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
   {:db (update-in db [:wallet :keypairs] dissoc key-uid)
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
  (let [key-uids-set              (set key-uids-to-update)
        single-keypair-to-update? (= (count key-uids-to-update) 1)
        keypair-name              (when single-keypair-to-update?
                                    (let [key-uid (first key-uids-set)]
                                      (get-in db [:wallet :keypairs key-uid :name])))]
    {:db (->
           db
           (update-in [:wallet :accounts]
                      #(data-store/make-accounts-fully-operable {:accounts     %
                                                                 :key-uids-set key-uids-set}))
           (update-in [:wallet :keypairs]
                      #(data-store/make-keypairs-fully-operable {:keypairs     %
                                                                 :key-uids-set key-uids-set})))
     :fx [[:dispatch
           [:toasts/upsert
            {:type  :positive
             :theme :dark
             :text  (if single-keypair-to-update?
                      (i18n/label :t/key-pair-imported-successfully {:name keypair-name})
                      (i18n/label :t/key-pairs-successfully-imported
                                  {:count (count key-uids-to-update)}))}]]]}))

(rf/reg-event-fx :wallet/make-keypairs-accounts-fully-operable make-keypairs-accounts-fully-operable)


(rf/reg-event-fx :wallet/connection-string-for-import-keypairs-failed
 (fn [{:keys [db]} [keypairs-key-uids error]]
   (let [error-message (-> error ex-data :error)
         incorrect-keypair? (string/includes?
                             error-message
                             "one or more expected keystore files are not found among the sent files")
         single-keypair-to-update? (= (count keypairs-key-uids) 1)
         keypair-name (when single-keypair-to-update?
                        (let [key-uid (first keypairs-key-uids)]
                          (get-in db [:wallet :keypairs key-uid :name])))
         toast-message (cond
                         (and single-keypair-to-update? incorrect-keypair?)
                         (i18n/label
                          :t/this-qr-does-not-contain-key-pair
                          {:name keypair-name})

                         (and (not single-keypair-to-update?) incorrect-keypair?)
                         (i18n/label
                          :t/this-qr-does-not-contain-any-missing-key-pair)

                         :else
                         error-message)]
     (log/error "failed to import missing key pairs with connection string"
                {:error error-message})
     (rf/dispatch [:toasts/upsert
                   {:type  :negative
                    :theme :dark
                    :text  toast-message}]))))

(defn connection-string-for-import-keypairs
  [{:keys [db]} [{:keys [sha3-pwd keypairs-key-uids connection-string]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.syncing/import-keypairs-keystores
           {:key-uid           key-uid
            :sha3-pwd          sha3-pwd
            :keypairs-key-uids keypairs-key-uids
            :connection-string connection-string
            :on-success        (fn [key-uids]
                                 (rf/dispatch [:wallet/make-keypairs-accounts-fully-operable key-uids]))
            :on-fail           (fn [error]
                                 (rf/dispatch [:wallet/connection-string-for-import-keypairs-failed
                                               keypairs-key-uids
                                               error]))}]]}))

(rf/reg-event-fx :wallet/connection-string-for-import-keypairs connection-string-for-import-keypairs)

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
                                 [:wallet/connection-string-for-import-keypairs
                                  {:connection-string connection-string
                                   :keypairs-key-uids keypairs-key-uids
                                   :sha3-pwd          password}]))}]]]})

(rf/reg-event-fx :wallet/success-keypair-qr-scan success-keypair-qr-scan)

(defn wallet-validate-seed-phrase
  [_ [seed-phrase on-success on-error]]
  {:fx [[:multiaccount/validate-mnemonic [seed-phrase on-success on-error]]]})

(rf/reg-event-fx :wallet/validate-seed-phrase wallet-validate-seed-phrase)

(defn make-seed-phrase-keypair-fully-operable
  [_ [mnemonic password on-success on-error]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_makeSeedPhraseKeypairFullyOperable"
           :params     [(security/safe-unmask-data mnemonic)
                        (-> password security/safe-unmask-data native-module/sha3)]
           :on-success on-success
           :on-error   on-error}]]]})

(rf/reg-event-fx :wallet/make-seed-phrase-keypair-fully-operable make-seed-phrase-keypair-fully-operable)

(defn make-private-key-keypair-fully-operable
  [_ [private-key password on-success on-error]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_makePrivateKeyKeypairFullyOperable"
           :params     [(security/safe-unmask-data private-key)
                        (security/safe-unmask-data password)]
           :on-success on-success
           :on-error   on-error}]]]})

(rf/reg-event-fx :wallet/make-private-key-keypair-fully-operable make-private-key-keypair-fully-operable)

(defn create-account-from-private-key
  [_ [private-key on-success on-error]]
  {:fx [[:effects.wallet/create-account-from-private-key
         {:private-key (security/safe-unmask-data private-key)
          :on-success  on-success
          :on-error    on-error}]]})

(rf/reg-event-fx :wallet/create-account-from-private-key create-account-from-private-key)

(defn verify-private-key-for-keypair
  [_ [keypair-key-uid private-key on-success on-error]]
  {:fx [[:effects.wallet/verify-private-key-for-keypair
         {:keypair-key-uid keypair-key-uid
          :private-key     (security/safe-unmask-data private-key)
          :on-success      on-success
          :on-error        on-error}]]})

(rf/reg-event-fx :wallet/verify-private-key-for-keypair verify-private-key-for-keypair)

(defn import-missing-keypair-by-seed-phrase
  [_ [{:keys [keypair-key-uid seed-phrase password on-success on-error]}]]
  {:fx [[:effects.wallet/import-missing-keypair-by-seed-phrase
         {:keypair-key-uid keypair-key-uid
          :seed-phrase     seed-phrase
          :password        password
          :on-success      (fn []
                             (rf/dispatch [:wallet/make-keypairs-accounts-fully-operable
                                           #{keypair-key-uid}])
                             (rf/call-continuation on-success))
          :on-error        (fn [error]
                             (rf/dispatch [:wallet/import-missing-keypair-by-seed-phrase-failed error])
                             (log/error "failed to import missing keypair with seed phrase"
                                        {:error error})
                             (rf/call-continuation on-error error))}]]})

(rf/reg-event-fx :wallet/import-missing-keypair-by-seed-phrase import-missing-keypair-by-seed-phrase)

(defn import-missing-keypair-by-seed-phrase-failed
  [_ [error]]
  (let [error-type (-> error ex-message keyword)
        error-data (ex-data error)]
    (when-not (= error-type :import-missing-keypair-by-seed-phrase/import-error)
      {:fx [[:dispatch
             [:toasts/upsert
              {:type  :negative
               :theme :dark
               :text  (:error error-data)}]]]})))

(rf/reg-event-fx :wallet/import-missing-keypair-by-seed-phrase-failed
 import-missing-keypair-by-seed-phrase-failed)

(defn import-missing-keypair-by-private-key
  [_ [{:keys [keypair-key-uid private-key password on-success on-error]}]]
  {:fx [[:dispatch
         [:wallet/make-private-key-keypair-fully-operable private-key password
          (fn []
            (rf/dispatch [:wallet/make-keypairs-accounts-fully-operable #{keypair-key-uid}])
            (rf/call-continuation on-success))
          (fn [error]
            (rf/dispatch [:wallet/import-missing-keypair-by-private-key-failed error])
            (log/error "failed to import missing keypair with private key" {:error error})
            (rf/call-continuation on-error error))]]]})

(rf/reg-event-fx :wallet/import-missing-keypair-by-private-key import-missing-keypair-by-private-key)

(defn import-missing-keypair-by-private-key-failed
  [_ [error]]
  {:fx [[:dispatch
         [:toasts/upsert
          {:type  :negative
           :theme :dark
           :text  error}]]]})

(rf/reg-event-fx :wallet/import-missing-keypair-by-private-key-failed
 import-missing-keypair-by-private-key-failed)

(defn make-partially-operable-accounts-fully-operable-success
  [{:keys [db]} [addresses]]
  (let [key-uids-to-update (data-store/get-keypair-key-uids-set-from-addresses db addresses)]
    {:db (-> db
             (update-in [:wallet :accounts]
                        #(data-store/make-accounts-fully-operable {:accounts           %
                                                                   :key-uids-set       key-uids-to-update
                                                                   :operable-condition :partially}))
             (update-in [:wallet :keypairs]
                        #(data-store/make-keypairs-fully-operable {:keypairs           %
                                                                   :key-uids-set       key-uids-to-update
                                                                   :operable-condition :partially})))}))

(rf/reg-event-fx :wallet/make-partially-operable-accounts-fully-operable-success
 make-partially-operable-accounts-fully-operable-success)

(defn make-partially-operable-accounts-fully-operable
  [_ [{:keys [password on-success on-error]}]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_makePartiallyOperableAccoutsFullyOperable"
           :params     [(security/safe-unmask-data password)]
           :on-success (fn [addresses]
                         (when addresses
                           (rf/dispatch [:wallet/make-partially-operable-accounts-fully-operable-success
                                         addresses]))
                         (on-success))
           :on-error   (fn [error]
                         (log/error "failed to make partially accounts fully operable" {:error error})
                         (on-error error))}]]]})

(rf/reg-event-fx :wallet/make-partially-operable-accounts-fully-operable
 make-partially-operable-accounts-fully-operable)

(rf/reg-event-fx :wallet/show-disable-testnet-mode-confirmation
 (fn [{:keys [db]} _]
   (let [theme (:theme db)
         blur? (and (not platform/android?) (= theme :dark))]
     {:fx [[:dispatch
            [:show-bottom-sheet
             {:content (fn [] [testnet/view
                               {:enable? false
                                :blur?   blur?}])
              :shell?  blur?
              :theme   (when-not platform/android? theme)}]]]})))

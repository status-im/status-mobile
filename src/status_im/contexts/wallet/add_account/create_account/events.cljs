(ns status-im.contexts.wallet.add-account.create-account.events
  (:require [camel-snake-kebab.extras :as cske]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.add-account.create-account.utils :as create-account.utils]
            [status-im.contexts.wallet.data-store :as data-store]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

(defn get-keypairs-success
  [{:keys [db]} [keypairs]]
  (let [parsed-keypairs (data-store/parse-keypairs keypairs)
        default-key-uid (:key-uid (first parsed-keypairs))]
    {:db (-> db
             (assoc-in [:wallet :keypairs] parsed-keypairs)
             (assoc-in [:wallet :ui :create-account :selected-keypair-uid] default-key-uid))}))

(rf/reg-event-fx :wallet/get-keypairs-success get-keypairs-success)

(defn confirm-account-origin
  [{:keys [db]} [key-uid]]
  {:db (assoc-in db [:wallet :ui :create-account :selected-keypair-uid] key-uid)
   :fx [[:dispatch [:navigate-back]]]})

(rf/reg-event-fx :wallet/confirm-account-origin confirm-account-origin)

(defn store-new-seed-phrase
  [{:keys [db]} [{:keys [seed-phrase random-phrase]}]]
  {:db (update-in db
                  [:wallet :ui :create-account :new-keypair]
                  assoc
                  :seed-phrase   seed-phrase
                  :random-phrase random-phrase)
   :fx [[:dispatch-later
         [{:ms       20
           :dispatch [:navigate-to :screen/wallet.confirm-backup]}]]]})

(rf/reg-event-fx :wallet/store-new-seed-phrase store-new-seed-phrase)

(defn seed-phrase-validated
  [{:keys [db]} [seed-phrase]]
  {:db (assoc-in db [:wallet :ui :create-account :seed-phrase] seed-phrase)
   :fx [[:dispatch [:navigate-to :screen/wallet.keypair-name]]]})

(rf/reg-event-fx :wallet/seed-phrase-validated seed-phrase-validated)

(defn seed-phrase-entered
  [_ [seed-phrase on-error]]
  {:fx [[:multiaccount/validate-mnemonic
         [seed-phrase
          (fn [mnemonic key-uid]
            (rf/dispatch [:wallet/seed-phrase-validated mnemonic key-uid]))
          on-error]]]})

(rf/reg-event-fx :wallet/seed-phrase-entered seed-phrase-entered)

(defn store-account-generated
  [{:keys [db]} [{:keys [new-account-data keypair-name]}]]
  (let [new-account (update new-account-data :mnemonic security/mask-data)]
    {:db (-> db
             (update-in [:wallet :ui :create-account :new-keypair]
                        assoc
                        :new-account-data new-account
                        :keypair-name     keypair-name)
             (update-in [:wallet :ui :create-account :new-keypair]
                        dissoc
                        :seed-phrase
                        :random-phrase))
     :fx [[:dispatch [:navigate-back-to :screen/wallet.create-account]]]}))

(rf/reg-event-fx :wallet/store-account-generated store-account-generated)

(defn generate-account-for-keypair
  [{:keys [db]} [{:keys [keypair-name]}]]
  (let [seed-phrase (-> db :wallet :ui :create-account :new-keypair :seed-phrase)]
    {:fx [[:effects.wallet/create-account-from-mnemonic
           {:mnemonic-phrase (security/safe-unmask-data seed-phrase)
            :paths           [constants/path-default-wallet]
            :on-success      (fn [new-account-data]
                               (rf/dispatch [:wallet/store-account-generated
                                             {:new-account-data new-account-data
                                              :keypair-name     keypair-name}]))}]]}))

(rf/reg-event-fx :wallet/generate-account-for-keypair generate-account-for-keypair)

(defn clear-create-account-data
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui :create-account] dissoc :new-keypair)})

(rf/reg-event-fx :wallet/clear-create-account clear-create-account-data)

(defn get-derived-addresses
  [{:keys [db]} [{:keys [password derived-from paths]}]]
  {:db            (assoc-in db [:wallet :ui :create-account :derivation-path-state] :scanning)
   :json-rpc/call [{:method     "wallet_getDerivedAddresses"
                    :params     [password derived-from paths]
                    :on-success [:wallet/get-derived-addresses-success]}]})

(rf/reg-event-fx :wallet/get-derived-addresses get-derived-addresses)

(rf/reg-event-fx
 :wallet/next-derivation-path
 (fn [_ [{:keys [on-success keypair-uid]}]]
   {:fx [[:json-rpc/call
          [{:method     "accounts_resolveSuggestedPathForKeypair"
            :params     [keypair-uid]
            :on-success on-success
            :on-error   (fn [error]
                          (log/error
                           "Failed to resolve next path derivation path"
                           {:event  :wallet/next-derivation-path
                            :method "accounts_resolveSuggestedPathForKeypair"
                            :error  error
                            :params keypair-uid}))}]]]}))

(defn get-derived-addresses-success
  [{:keys [db]} [response]]
  (let [derived-address (first response)]
    {:db (-> db
             (assoc-in [:wallet :ui :create-account :derivation-path-state]
                       (if (:has-activity derived-address) :has-activity :no-activity))
             (assoc-in [:wallet :ui :create-account :derivation-path]
                       (cske/transform-keys transforms/->kebab-case-keyword derived-address)))}))

(rf/reg-event-fx :wallet/get-derived-addresses-success get-derived-addresses-success)

(rf/reg-event-fx
 :wallet/set-private-key
 (fn [{:keys [db]} [value]]
   {:db (assoc-in db [:wallet :ui :create-account :private-key] (security/mask-data value))}))

(rf/reg-event-fx
 :wallet/set-public-address
 (fn [{:keys [db]} [value]]
   {:db (assoc-in db [:wallet :ui :create-account :public-address] value)}))

(rf/reg-event-fx
 :wallet/clear-private-key-data
 (fn [{:keys [db]} _]
   {:db (update-in db [:wallet :ui :create-account] dissoc :private-key :public-address)}))

(rf/reg-event-fx
 :wallet/create-keypair-with-account
 (fn [{db :db} [password account-preferences]]
   (let [{:keys [keypair-name
                 new-account-data]} (-> db :wallet :ui :create-account :new-keypair)
         keypair-with-account       (create-account.utils/prepare-new-account
                                     {:keypair-name        keypair-name
                                      :account-data        new-account-data
                                      :account-preferences account-preferences})
         new-address                (some-> new-account-data
                                            (create-account.utils/first-derived-account)
                                            (:address)
                                            (string/lower-case))
         unmasked-password          (security/safe-unmask-data password)]
     {:fx [[:json-rpc/call
            [{:method     "accounts_addKeypair"
              :params     [unmasked-password keypair-with-account]
              :on-success [:wallet/add-account-success new-address]
              :on-error   #(log/error "Failed to add Keypair and create account" %)}]]]})))

(defn import-and-create-keypair-with-account
  [{db :db} [{:keys [password account-preferences]}]]
  (let [account-data      (-> db :wallet :ui :create-account :new-keypair :new-account-data)
        unmasked-mnemonic (security/safe-unmask-data (:mnemonic account-data))
        unmasked-password (security/safe-unmask-data password)]
    {:fx [[:json-rpc/call
           [{:method     "accounts_importMnemonic"
             :params     [unmasked-mnemonic unmasked-password]
             :on-success #(rf/dispatch
                           [:wallet/create-keypair-with-account password account-preferences])}]]]}))

(rf/reg-event-fx :wallet/import-and-create-keypair-with-account import-and-create-keypair-with-account)

(rf/reg-event-fx
 :wallet/derive-address-and-add-account
 (fn [_ [{:keys [password derived-from-address derivation-path account-preferences]}]]
   {:fx [[:json-rpc/call
          [{:method     "wallet_getDerivedAddresses"
            :params     [(security/safe-unmask-data password)
                         derived-from-address
                         [derivation-path]]
            :on-success (fn [[derived-account]]
                          (rf/dispatch [:wallet/add-account
                                        (assoc account-preferences :password password)
                                        derived-account]))
            :on-error   #(log/info "Failed to get derived addresses"
                                   derived-from-address
                                   %)}]]]}))


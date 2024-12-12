(ns status-im.contexts.wallet.add-account.create-account.events
  (:require [camel-snake-kebab.extras :as cske]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.add-account.create-account.utils :as create-account.utils]
            [status-im.contexts.wallet.data-store :as data-store]
            [taoensso.timbre :as log]
            [utils.collection]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

(defn get-keypairs-success
  [{:keys [db]} [keypairs]]
  (let [parsed-keypairs (data-store/rpc->keypairs keypairs)
        default-key-uid (->> parsed-keypairs
                             (some #(when (= (:type %) :profile) %))
                             :key-uid)
        keycard?        (get-in db [:profile/profile :keycard-pairing])
        keypair         (first parsed-keypairs)
        keycard         (when (and keycard? (= (count parsed-keypairs) 1) (empty? (:keycards keypair)))
                          {:keycard-uid        (get-in db [:profile/profile :keycard-instance-uid])
                           :key-uid            default-key-uid
                           :keycard-name       ""
                           :accounts-addresses [(some #(when (:wallet %) (:address %))
                                                      (:accounts keypair))]})]
    {:db (-> db
             (assoc-in [:wallet :keypairs]
                       (cond-> (utils.collection/index-by :key-uid parsed-keypairs)
                         keycard
                         (assoc-in [default-key-uid :keycards] [keycard])))
             (assoc-in [:wallet :ui :create-account :selected-keypair-uid] default-key-uid))
     ;; this is a temporary solution, should be done properly in statu-go instead
     :fx [(when keycard
            [:json-rpc/call
             [{:method "accounts_saveOrUpdateKeycard"
               :params [keycard true]}]])]}))
(rf/reg-event-fx :wallet/get-keypairs-success get-keypairs-success)

(defn confirm-account-origin
  [{:keys [db]} [key-uid]]
  {:db (assoc-in db [:wallet :ui :create-account :selected-keypair-uid] key-uid)
   :fx [[:dispatch [:navigate-back]]]})

(rf/reg-event-fx :wallet/confirm-account-origin confirm-account-origin)

(defn store-new-seed-phrase
  [{:keys [db]} [{:keys [seed-phrase]}]]
  {:db (update-in db
                  [:wallet :ui :create-account :new-keypair]
                  assoc
                  :seed-phrase
                  seed-phrase)
   :fx [[:dispatch-later
         [{:ms       20
           :dispatch [:navigate-to :screen/wallet.confirm-backup]}]]]})

(rf/reg-event-fx :wallet/store-new-seed-phrase store-new-seed-phrase)

(defn seed-phrase-validated
  [{:keys [db]} [seed-phrase key-uid on-error]]
  (let [keypair-already-added? (-> db
                                   :wallet
                                   :keypairs
                                   (contains? key-uid))]
    (if keypair-already-added?
      (do
        (on-error)
        {:fx [[:dispatch
               [:toasts/upsert
                {:id   :already-existing-keypair
                 :type :negative
                 :text "This keypair already exists in the device"}]]]})
      {:db (assoc-in db [:wallet :ui :create-account :new-keypair :seed-phrase] seed-phrase)
       :fx [[:dispatch [:navigate-to :screen/wallet.keypair-name {:workflow :recovery-phrase}]]]})))

(rf/reg-event-fx :wallet/seed-phrase-validated seed-phrase-validated)

(defn store-account-generated-with-mnemonic
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

(rf/reg-event-fx :wallet/store-account-generated-with-mnemonic store-account-generated-with-mnemonic)

(defn generate-account-for-keypair-with-mnemonic
  [{:keys [db]} [{:keys [keypair-name]}]]
  (let [seed-phrase (-> db :wallet :ui :create-account :new-keypair :seed-phrase)]
    {:fx [[:effects.wallet/create-account-from-mnemonic
           {:mnemonic-phrase (security/safe-unmask-data seed-phrase)
            :paths           [constants/path-default-wallet]
            :on-success      (fn [new-account-data]
                               (rf/dispatch [:wallet/store-account-generated-with-mnemonic
                                             {:new-account-data new-account-data
                                              :keypair-name     keypair-name}]))}]]}))

(rf/reg-event-fx :wallet/generate-account-for-keypair-with-mnemonic
 generate-account-for-keypair-with-mnemonic)

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
                           "Failed to resolve next derivation path"
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
   (let [{:keys [workflow
                 keypair-name
                 new-account-data]} (-> db :wallet :ui :create-account :new-keypair)
         keypair-type               (if (= workflow :workflow/new-keypair.import-private-key)
                                      :key
                                      :seed)
         keypair-with-account       (create-account.utils/prepare-new-account
                                     {:keypair-name        keypair-name
                                      :keypair-type        keypair-type
                                      :account-data        new-account-data
                                      :account-preferences account-preferences})
         new-address                (some-> new-account-data
                                            (create-account.utils/first-derived-account keypair-type)
                                            (:address)
                                            (string/lower-case))
         unmasked-password          (security/safe-unmask-data password)]
     {:fx [[:json-rpc/call
            [{:method     "accounts_addKeypair"
              :params     [unmasked-password keypair-with-account]
              :on-success [:wallet/add-account-success new-address]
              :on-error   #(log/error "Failed to add Keypair and create account" %)}]]]})))

(defn import-mnemonic-and-create-keypair-with-account
  [{db :db} [{:keys [password account-preferences]}]]
  (let [account-data      (-> db :wallet :ui :create-account :new-keypair :new-account-data)
        unmasked-mnemonic (security/safe-unmask-data (:mnemonic account-data))
        unmasked-password (security/safe-unmask-data password)]
    {:fx [[:json-rpc/call
           [{:method     "accounts_importMnemonic"
             :params     [unmasked-mnemonic unmasked-password]
             :on-success #(rf/dispatch
                           [:wallet/create-keypair-with-account password account-preferences])}]]]}))

(rf/reg-event-fx :wallet/import-mnemonic-and-create-keypair-with-account
 import-mnemonic-and-create-keypair-with-account)

(rf/reg-event-fx
 :wallet/derive-address-and-add-account
 (fn [_ [{:keys [password derived-from-address derivation-path account-preferences key-uid]}]]
   {:fx [[:json-rpc/call
          [{:method     "wallet_getDerivedAddresses"
            :params     [(security/safe-unmask-data password)
                         derived-from-address
                         [derivation-path]]
            :on-success (fn [[derived-account]]
                          (rf/dispatch [:wallet/add-account
                                        (assoc account-preferences
                                               :key-uid  key-uid
                                               :password password)
                                        derived-account]))
            :on-error   [:wallet/log-rpc-error
                         {:event  :wallet/derive-address-and-add-account
                          :params [derived-from-address [derivation-path]]}]}]]]}))

(defn import-private-key-and-create-keypair-with-account
  [{:keys [db]} [{:keys [password account-preferences]}]]
  (let [private-key          (get-in db
                                     [:wallet :ui :create-account :new-keypair
                                      :new-account-data :private-key])
        unmasked-private-key (security/safe-unmask-data private-key)
        unmasked-password    (security/safe-unmask-data password)]
    {:json-rpc/call
     [{:method     "accounts_importPrivateKey"
       :params     [unmasked-private-key unmasked-password]
       :on-success [:wallet/create-keypair-with-account password account-preferences]
       :on-error   #(log/error "Failed to import private key" %)}]}))

(rf/reg-event-fx
 :wallet/import-private-key-and-create-keypair-with-account
 import-private-key-and-create-keypair-with-account)

(defn store-account-generated-with-private-key
  [{:keys [db]} [{:keys [new-account-data keypair-name]}]]
  {:db (-> db
           (update-in [:wallet :ui :create-account :new-keypair]
                      assoc
                      :new-account-data new-account-data
                      :keypair-name     keypair-name)
           (update-in [:wallet :ui :create-account :new-keypair]
                      dissoc
                      :private-key))
   :fx [[:dispatch [:navigate-back-to :screen/wallet.create-account]]]})

(rf/reg-event-fx :wallet/store-account-generated-with-private-key
 store-account-generated-with-private-key)

(rf/reg-event-fx
 :wallet/import-private-key-and-generate-account-for-keypair
 (fn [{:keys [db]} [{:keys [keypair-name]}]]
   (let [private-key (get-in db [:wallet :ui :create-account :private-key])]
     {:db (assoc-in db
           [:wallet :ui :create-account :new-keypair :workflow]
           :workflow/new-keypair.import-private-key)
      :fx [[:effects.wallet/create-account-from-private-key
            {:private-key private-key
             :on-success  (fn [new-account-data]
                            (rf/dispatch [:wallet/store-account-generated-with-private-key
                                          {:keypair-name     keypair-name
                                           :new-account-data new-account-data}]))}]]})))

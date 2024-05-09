(ns status-im.contexts.wallet.add-account.create-account.events
  (:require [camel-snake-kebab.extras :as cske]
            [status-im.contexts.wallet.data-store :as data-store]
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

(defn store-seed-phrase
  [{:keys [db]} [{:keys [seed-phrase random-phrase]}]]
  {:db (-> db
           (assoc-in [:wallet :ui :create-account :seed-phrase] seed-phrase)
           (assoc-in [:wallet :ui :create-account :random-phrase] random-phrase))
   :fx [[:dispatch-later [{:ms 20 :dispatch [:navigate-to :screen/wallet.check-your-backup]}]]]})

(rf/reg-event-fx :wallet/store-seed-phrase store-seed-phrase)

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
            (rf/dispatch [:wallet/seed-phrase-validated
                          mnemonic key-uid]))
          on-error]]]})

(rf/reg-event-fx :wallet/seed-phrase-entered seed-phrase-entered)

(defn new-keypair-created
  [{:keys [db]} [{:keys [new-keypair]}]]
  {:db (assoc-in db [:wallet :ui :create-account :new-keypair] new-keypair)
   :fx [[:dispatch [:navigate-back-to :screen/wallet.create-account]]]})

(rf/reg-event-fx :wallet/new-keypair-created new-keypair-created)

(defn new-keypair-continue
  [{:keys [db]} [{:keys [keypair-name]}]]
  (let [seed-phrase (get-in db [:wallet :ui :create-account :seed-phrase])]
    {:fx [[:effects.wallet/create-account-from-mnemonic
           {:seed-phrase  (security/safe-unmask-data seed-phrase)
            :keypair-name keypair-name}]]}))

(rf/reg-event-fx :wallet/new-keypair-continue new-keypair-continue)

(defn clear-new-keypair
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui :create-account] dissoc :new-keypair)})

(rf/reg-event-fx :wallet/clear-new-keypair clear-new-keypair)

(defn get-derived-addresses
  [{:keys [db]} [{:keys [password derived-from paths]}]]
  {:db            (assoc-in db [:wallet :ui :create-account :derivation-path-state] :scanning)
   :json-rpc/call [{:method     "wallet_getDerivedAddresses"
                    :params     [password derived-from paths]
                    :on-success [:wallet/get-derived-addresses-success]}]})

(rf/reg-event-fx :wallet/get-derived-addresses get-derived-addresses)

(defn get-derived-addresses-success
  [{:keys [db]} [response]]
  (let [derived-address (first response)]
    {:db (-> db
             (assoc-in [:wallet :ui :create-account :derivation-path-state]
                       (if (:has-activity derived-address) :has-activity :no-activity))
             (assoc-in [:wallet :ui :create-account :derivation-path]
                       (cske/transform-keys transforms/->kebab-case-keyword derived-address)))}))

(rf/reg-event-fx :wallet/get-derived-addresses-success get-derived-addresses-success)

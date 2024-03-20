(ns status-im.contexts.wallet.create-account.events
  (:require [status-im.contexts.wallet.data-store :as data-store]
            [utils.re-frame :as rf]))

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

(defn store-secret-phrase
  [{:keys [db]} [{:keys [secret-phrase random-phrase]}]]
  {:db (-> db
           (assoc-in [:wallet :ui :create-account :secret-phrase] secret-phrase)
           (assoc-in [:wallet :ui :create-account :random-phrase] random-phrase))
   :fx [[:dispatch-later [{:ms 20 :dispatch [:navigate-to :screen/wallet.check-your-backup]}]]]})

(rf/reg-event-fx :wallet/store-secret-phrase store-secret-phrase)

(defn new-keypair-created
  [{:keys [db]} [{:keys [new-keypair]}]]
  {:db (assoc-in db [:wallet :ui :create-account :new-keypair] new-keypair)
   :fx [[:dispatch [:navigate-back-to :wallet-create-account]]]})

(rf/reg-event-fx :wallet/new-keypair-created new-keypair-created)

(defn new-keypair-continue
  [{:keys [db]} [{:keys [keypair-name]}]]
  (let [secret-phrase (get-in db [:wallet :ui :create-account :secret-phrase])]
    {:fx [[:effects.wallet/create-account-from-mnemonic
           {:secret-phrase secret-phrase
            :keypair-name  keypair-name}]]}))

(rf/reg-event-fx :wallet/new-keypair-continue new-keypair-continue)

(defn clear-new-keypair
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui :create-account] dissoc :new-keypair)})

(rf/reg-event-fx :wallet/clear-new-keypair clear-new-keypair)

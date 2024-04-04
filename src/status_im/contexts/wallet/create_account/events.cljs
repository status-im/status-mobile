(ns status-im.contexts.wallet.create-account.events
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [status-im.contexts.wallet.data-store :as data-store]
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
   :fx [[:dispatch [:navigate-back-to :screen/wallet.create-account]]]})

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

(defn get-derived-addresses
  [{:keys [db]} [{:keys [password derived-from paths]}]]
  {:db            (assoc-in db [:wallet :ui :create-account :derivation-path-state] :scanning)
   :json-rpc/call [{:method     "wallet_getDerivedAddresses"
                    :params     [password derived-from paths]
                    :on-success [:wallet/get-derived-addresses-success]
                    :on-error   (fn [error]
                                  (println "error" error))}]})

(rf/reg-event-fx :wallet/get-derived-addresses get-derived-addresses)

(defn get-derived-addresses-success
  [{:keys [db]} [response]]
  {:db (-> db
           (assoc-in [:wallet :ui :create-account :derivation-path-state]
                     (if (:has-activity (first response)) :has-activity :no-activity))
           (assoc-in [:wallet :ui :create-account :derivation-path]
                     (cske/transform-keys csk/->kebab-case-keyword (first response))))})

(rf/reg-event-fx :wallet/get-derived-addresses-success get-derived-addresses-success)

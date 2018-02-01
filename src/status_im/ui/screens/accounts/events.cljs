(ns status-im.ui.screens.accounts.events
  (:require [status-im.data-store.accounts :as accounts-store]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.protocol.core :as protocol]
            [status-im.native-module.core :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.random :as random]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.statuses :as statuses]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.hex :as utils.hex]))

(handlers/register-handler-fx
  :error-event
  (fn [_ [_ error]]
    (println "There was an error:" error)))

;; Account creation

(defn create-account-flow [password]
  {:first-dispatch [::create-account password]
   :rules [{:when :seen-both?
            :events [::account-created-success
                     ::get-public-key-success]
            :dispatch [::save-created-account password]}]})

(handlers/register-handler-fx
  ::create-account
  [(re-frame/inject-cofx ::get-signing-phrase)
   (re-frame/inject-cofx ::get-status)
   (re-frame/inject-cofx :protocol/get-web3)]
  (fn [{:keys [db web3 signing-phrase status]} [_ password]]
    {:db (assoc db :accounts/new-account {:signing-phrase signing-phrase
                                          :status status})
     :status/create-account {:password password
                             :success-event ::account-created-success}
     :shh/get-new-key-pair {:web3 web3
                            :success-event ::get-new-key-pair-success
                            :error-event   :error-event}}))

(handlers/register-handler-fx
  ::account-created-success
  (fn [{{:keys [network] :networks/keys [networks] :accounts/keys [new-account] :as db} :db}
       [_ {:keys [pubkey address mnemonic]}]]
    (let [normalized-address (utils.hex/normalize-hex address)]
      {:db (update db :accounts/new-account merge {:network             network
                                                   :networks            networks
                                                   :public-key          pubkey
                                                   :address             normalized-address
                                                   :name                (generate-gfy pubkey)
                                                   :signed-up?          true
                                                   :photo-path          (identicon pubkey)
                                                   :settings            {:wallet {:visible-tokens {:testnet #{:STT}
                                                                                                   :mainnet #{:SNT}}}}})
       :dispatch [:show-mnemonic mnemonic (:signing-phrase new-account)]})))

(handlers/register-handler-fx
  ::get-new-key-pair-success
  [(re-frame/inject-cofx :protocol/get-web3)]
  (fn [{:keys [db web3]} [_ key-pair-id]]
    {:db (assoc-in db [:accounts/new-account :updates-key-pair-id] key-pair-id)
     :shh/get-public-key {:web3 web3
                          :key-pair-id key-pair-id
                          :success-event ::get-public-key-success}}))

(handlers/register-handler-fx
  ::get-public-key-success
  (fn [{:keys [db]} [_ public-key]]
    {:db (assoc-in db [:accounts/new-account :updates-public-key] public-key)}))

(handlers/register-handler-fx
  ::save-created-account
  (fn [{{:accounts/keys [new-account] :as db} :db} [_ password]]
    (let [{:keys [address]} new-account]
      {:db (-> db
               (assoc-in [:accounts/accounts address] new-account)
               (dissoc :accounts/new-account))
       :data-store.accounts/save new-account
       :dispatch [:login-account address password true]})))

;;;; COFX
(re-frame/reg-cofx
 ::get-signing-phrase
 (fn [coeffects _]
   (assoc coeffects :signing-phrase (signing-phrase/generate))))

(re-frame/reg-cofx
 ::get-status
 (fn [coeffects _]
   (assoc coeffects :status (rand-nth statuses/data))))

;;;; FX

(re-frame/reg-fx
  ::broadcast-account-update
  (fn [{:keys [current-public-key web3 name photo-path status
               updates-public-key updates-private-key]}]
    (when web3
      (protocol/broadcast-profile!
       {:web3    web3
        :message {:from       current-public-key
                  :message-id (random/id)
                  :keypair    {:public  updates-public-key
                               :private updates-private-key}
                  :payload    {:profile {:name          name
                                         :status        status
                                         :profile-image photo-path}}}}))))

(re-frame/reg-fx
  ::send-keys-update
  (fn [{:keys [web3 current-public-key contacts
               updates-public-key updates-private-key]}]
    (doseq [id (handlers/identities contacts)]
      (protocol/update-keys!
       {:web3    web3
        :message {:from       current-public-key
                  :to         id
                  :message-id (random/id)
                  :payload    {:keypair {:public  updates-public-key
                                         :private updates-private-key}}}}))))
;;;; Handlers

(handlers/register-handler-fx
  :create-new-account-handler
  (fn [_ _]
    {:dispatch [:check-console-chat true]}))

(handlers/register-handler-fx
  :load-accounts
  [(re-frame/inject-cofx :data-store/accounts)]
  (fn [{:keys [db all-accounts]} _]
    (let [accounts (->> all-accounts
                        (map (fn [{:keys [address] :as account}]
                               [address account]))
                        (into {}))
          ;;workaround for realm bug, migrating account v4
          events (mapv #(when (empty? (:networks %)) [:account-update-networks (:address %)]) (vals accounts))]
      (merge
       {:db (assoc db :accounts/accounts accounts)}
       (when-not (empty? events)
         {:dispatch-n events})))))

(handlers/register-handler-fx
  :account-update-networks
  (fn [{{:accounts/keys [accounts] :networks/keys [networks] :as db} :db} [_ id]]
    (let [current-account (get accounts id)
          new-account (assoc current-account :networks networks)]
      {:db            (assoc-in db [:accounts/accounts id] new-account)
       :data-store.accounts/save new-account})))

(defn update-wallet-settings [{:accounts/keys [account] :as db} settings]
  {:db            (assoc-in db [:accounts/account :settings] settings)
   :data-store.accounts/saveccount (assoc account :settings settings)})

(defn account-update
  "Takes effects (containing :db) + new account fields, adds all effects necessary for account update."
  [{{:accounts/keys [account] :as db} :db :as fx} new-account-fields]
  (let [new-account (merge account new-account-fields)]
    (-> fx
        (update-in [:db :accounts/account] merge new-account-fields)
        (assoc :data-store.accounts/save new-account
               ::broadcast-account-update (merge (select-keys db [:current-public-key :web3])
                                                 (select-keys new-account [:name :photo-path :status
                                                                           :updates-public-key :updates-private-key]))))))

(handlers/register-handler-fx
  :account-update-keys
  (fn [{:keys [db keypair now]} _]
    ;; TODO get new keypair
    (let [{:accounts/keys [account]} db
          {:keys [public private]} keypair
          new-account     (merge account {:updates-public-key  public
                                          :updates-private-key private
                                          :last-updated        now})]
      {:db                (assoc db :accounts/account new-account)
       :data-store.accounts/save     new-account
       ::send-keys-update (merge
                           (select-keys db [:web3 :current-public-key :contacts])
                           (select-keys new-account [:updates-public-key
                                                     :updates-private-key]))})))

(handlers/register-handler-fx
  :send-account-update-if-needed
  (fn [{{:accounts/keys [account] :as db} :db now :now} _]
    (let [{:keys [last-updated]} account
          needs-update?          (> (- now last-updated) time/week)]
      (log/info "Need to send account-update: " needs-update?)
      (when needs-update?
        ;; TODO(janherich): this is very strange and misleading, need to figure out why it'd necessary to update
        ;; account with network update when last update was more then week ago
        (account-update {:db db} nil)))))

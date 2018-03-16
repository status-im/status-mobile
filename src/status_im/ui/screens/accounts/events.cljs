(ns status-im.ui.screens.accounts.events
  (:require [status-im.data-store.accounts :as accounts-store]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.protocol.core :as protocol]
            [status-im.native-module.core :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.random :as random]
            [clojure.string :as str]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.statuses :as statuses]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.hex :as utils.hex]
            status-im.ui.screens.accounts.create.navigation))

;;;; COFX

(re-frame/reg-cofx
  :get-new-keypair!
  (fn [coeffects _]
    (assoc coeffects :keypair (protocol/new-keypair!))))

(re-frame/reg-cofx
  ::get-all-accounts
  (fn [coeffects _]
    (assoc coeffects :all-accounts (accounts-store/get-all))))

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
  ::save-account
  (fn [account]
    (accounts-store/save account true)))

(re-frame/reg-fx
  ::create-account
  (fn [password]
    (status/create-account
     password
     #(re-frame/dispatch [::account-created (json->clj %) password]))))

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
  :create-account
  (fn [{{:accounts/keys [create] :as db} :db} _]
    {:db (update db :accounts/create assoc :step :account-creating :error nil)
     ::create-account (:password create)}))

(defn add-account
  "Takes db and new account, creates map of effects describing adding account to database and realm"
  [{:keys [network inbox/wnode] :networks/keys [networks] :as db} {:keys [address] :as account}]
  (let [enriched-account (assoc account
                                :network  network
                                :networks networks
                                :wnode    wnode
                                :address  address)]
    {:db            (assoc-in db [:accounts/accounts address] enriched-account)
     ::save-account enriched-account}))

;; TODO(janherich) we have this handler here only because of the tests, refactor/improve tests ASAP
(handlers/register-handler-fx
  :add-account
  (fn [{:keys [db]} [_ new-account]]
    (add-account db new-account)))

(handlers/register-handler-fx
  ::account-created
  [re-frame/trim-v (re-frame/inject-cofx :get-new-keypair!)
   (re-frame/inject-cofx ::get-signing-phrase) (re-frame/inject-cofx ::get-status)]
  (fn [{:keys [keypair signing-phrase status db] :as cofx} [{:keys [pubkey address mnemonic]} password]]
    (let [normalized-address (utils.hex/normalize-hex address)
          account            {:public-key          pubkey
                              :address             normalized-address
                              :name                (generate-gfy pubkey)
                              :status              status
                              :signed-up?          true
                              :updates-public-key  (:public keypair)
                              :updates-private-key (:private keypair)
                              :photo-path          (identicon pubkey)
                              :signing-phrase      signing-phrase
                              :mnemonic            mnemonic
                              :settings            {:wallet {:visible-tokens {:testnet #{:STT} :mainnet #{:SNT}}}}}]
      (log/debug "account-created")
      (when-not (str/blank? pubkey)
        (-> (add-account db account)
            (assoc :dispatch [:login-account normalized-address password]))))))

(handlers/register-handler-fx
  :load-accounts
  [(re-frame/inject-cofx ::get-all-accounts)]
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
       ::save-account new-account})))

(defn update-wallet-settings [{:accounts/keys [current-account-id accounts] :as db} settings]
  (let [new-account (-> (get accounts current-account-id)
                        (assoc :settings settings))]
    {:db            (assoc-in db [:accounts/accounts current-account-id] new-account)
     ::save-account new-account}))

(defn account-update
  "Takes effects (containing :db) + new account fields, adds all effects necessary for account update."
  [{{:accounts/keys [accounts current-account-id] :as db} :db :as fx} new-account-fields]
  (let [current-account (get accounts current-account-id)
        new-account     (merge current-account new-account-fields)
        broadcast-fields [:name :photo-path :status
                          :updates-public-key :updates-private-key]]
    (cond-> fx
            true
            (assoc-in [:db :accounts/accounts current-account-id] new-account)
            true
            (assoc ::save-account new-account)

            (seq (clojure.set/intersection (set (keys new-account-fields)) (set broadcast-fields)))
            (assoc ::broadcast-account-update (merge (select-keys db [:current-public-key :web3])
                                                     (select-keys new-account broadcast-fields))))))

(handlers/register-handler-fx
  :account-update-keys
  [(re-frame/inject-cofx :get-new-keypair!)]
  (fn [{:keys [db keypair now]} _]
    (let [{:accounts/keys [accounts current-account-id]} db
          {:keys [public private]} keypair
          current-account (get accounts current-account-id)
          new-account     (merge current-account {:updates-public-key  public
                                                  :updates-private-key private
                                                  :last-updated        now})]
      {:db                (assoc-in db [:accounts/accounts current-account-id] new-account)
       ::save-account     new-account
       ::send-keys-update (merge
                           (select-keys db [:web3 :current-public-key :contacts])
                           (select-keys new-account [:updates-public-key
                                                     :updates-private-key]))})))

(handlers/register-handler-fx
  :send-account-update-if-needed
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db now :now} _]
    (let [{:keys [last-updated]} (get accounts current-account-id)
          needs-update? (> (- now last-updated) time/week)]
      (log/info "Need to send account-update: " needs-update?)
      (when needs-update?
        ;; TODO(janherich): this is very strange and misleading, need to figure out why it'd necessary to update
        ;; account with network update when last update was more then week ago
        (account-update {:db db} nil)))))

(handlers/register-handler-fx
  :account-set-name
  (fn [{{:accounts/keys [create] :as db} :db} _]
    (-> {:db (assoc-in db [:accounts/create :show-welcome?] true)
         :dispatch [:navigate-to-clean :usage-data]}
        (account-update {:name (:name create)}))))

(handlers/register-handler-fx
  :update-sign-in-time
  (fn [{db :db now :now} _]
    (account-update {:db db} {:last-sign-in now})))

(handlers/register-handler-fx
  :reset-account-creation
  (fn [{db :db} _]
    {:db (update db :accounts/create assoc :step :enter-password :password nil :password-confirm nil :error nil)}))

(handlers/register-handler-fx
  :switch-dev-mode
  (fn [{db :db} [_ dev-mode]]
    (account-update {:db db} {:dev-mode? dev-mode})))
(ns status-im.ui.screens.accounts.events
  (:require
    status-im.ui.screens.accounts.login.events
    status-im.ui.screens.accounts.recover.events

    [status-im.data-store.accounts :as accounts-store]
    [re-frame.core :refer [reg-cofx reg-fx dispatch inject-cofx]]
    [taoensso.timbre :as log]
    [status-im.protocol.core :as protocol]
    [status-im.native-module.core :as status]
    [status-im.utils.types :refer [json->clj]]
    [status-im.utils.identicon :refer [identicon]]
    [status-im.utils.random :as random]
    [clojure.string :as str]
    [status-im.utils.datetime :as time]
    [status-im.utils.handlers :refer [register-handler-db register-handler-fx get-hashtags] :as handlers]
    [status-im.ui.screens.accounts.statuses :as statuses]
    [status-im.utils.signing-phrase.core :as signing-phrase]
    [status-im.utils.gfycat.core :refer [generate-gfy]]))

;;;; COFX

(reg-cofx
  :get-new-keypair!
  (fn [coeffects _]
    (assoc coeffects :keypair (protocol/new-keypair!))))

(reg-cofx
  ::get-all-accounts
  (fn [coeffects _]
    (assoc coeffects :all-accounts (accounts-store/get-all))))

;;;; FX

(reg-fx
  ::save-account
  (fn [account]
    (accounts-store/save account true)))

(defn account-created [result password]
  (let [data       (json->clj result)
        public-key (:pubkey data)
        address    (:address data)
        mnemonic   (:mnemonic data)
        phrase     (signing-phrase/generate)
        {:keys [public private]} (protocol/new-keypair!)
        account {:public-key          public-key
                 :address             address
                 :name                (generate-gfy public-key)
                 :status              (rand-nth statuses/data)
                 :signed-up?          true
                 :updates-public-key  public
                 :updates-private-key private
                 :photo-path          (identicon public-key)
                 :signing-phrase      phrase}]
    (log/debug "account-created")
    (when-not (str/blank? public-key)
      (dispatch [:show-mnemonic mnemonic phrase])
      (dispatch [:add-account account password]))))

(reg-fx
  ::create-account
  (fn [password]
    (status/create-account
      password
      #(account-created % password))))

(reg-fx
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

(reg-fx
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

(register-handler-fx
  :add-account
  (fn [{{:keys          [network]
         :networks/keys [networks]
         :as            db} :db} [_ {:keys [address] :as account} password]]
    (let [account' (assoc account :network network
                                  :networks networks)]
      (merge
        {:db            (assoc-in db [:accounts/accounts address] account')
         ::save-account account'}
        (when password
          {:dispatch-later [{:ms 400 :dispatch [:login-account address password true]}]})))))

(register-handler-fx
  :create-account
  (fn [{db :db} [_ password]]
    {:db              (assoc db :accounts/creating-account? true)
     ::create-account password
     :dispatch-later  [{:ms 400 :dispatch [:account-generation-message]}]}))

(register-handler-fx
  :create-new-account-handler
  (fn [_ _]
    {:dispatch-n [[:initialize-db]
                  [:load-accounts]
                  [:check-console-chat true]]}))

(register-handler-fx
  :load-accounts
  [(inject-cofx ::get-all-accounts)]
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

(register-handler-fx
  :account-update-networks
  (fn [{{:accounts/keys [accounts] :networks/keys [networks] :as db} :db} [_ id]]
    (let [current-account (get accounts id)
          new-account (assoc current-account :networks networks)]
      {:db            (assoc-in db [:accounts/accounts id] new-account)
       ::save-account new-account})))

(register-handler-fx
  :check-status-change
  (fn [{{:accounts/keys [accounts current-account-id]} :db} [_ status]]
    (let [{old-status :status} (get accounts current-account-id)
          status-updated? (and (not= status nil)
                               (not= status old-status))]
      (when status-updated?
        (let [hashtags (get-hashtags status)]
          (when (seq hashtags)
            {:dispatch [:broadcast-status status hashtags]}))))))

(defn update-account [current-account new-fields]
  (merge current-account (assoc new-fields :last-updated (time/now-ms))))

(register-handler-fx
  :account-update
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ new-account-fields]]
    (let [current-account     (get accounts current-account-id)
          new-account         (update-account current-account new-account-fields)]
      {:db                        (assoc-in db [:accounts/accounts current-account-id] new-account)
       ::save-account             new-account
       ::broadcast-account-update (merge
                                   (select-keys db [:current-public-key :web3])
                                   (select-keys new-account [:name :photo-path :status
                                                             :updates-public-key :updates-private-key]))})))

(register-handler-fx
  :account-update-keys
  [(inject-cofx :get-new-keypair!)]
  (fn [{:keys [db keypair]} _]
    (let [{:accounts/keys [accounts current-account-id]} db
          {:keys [public private]} keypair
          current-account (get accounts current-account-id)
          new-account     (update-account current-account {:updates-public-key  public
                                                           :updates-private-key private})]
      {:db                (assoc-in db [:accounts/accounts current-account-id] new-account)
       ::save-account     new-account
       ::send-keys-update (merge
                           (select-keys db [:web3 :current-public-key :contacts])
                           (select-keys new-account [:updates-public-key
                                                     :updates-private-key]))})))

(register-handler-fx
  :send-account-update-if-needed
  (fn [{{:accounts/keys [accounts current-account-id]} :db} _]
    (let [{:keys [last-updated]} (get accounts current-account-id)
          now           (time/now-ms)
          needs-update? (> (- now last-updated) time/week)]
      (log/info "Need to send account-update: " needs-update?)
      (when needs-update?
        (dispatch [:account-update])))))

(register-handler-db
  :set-current-account
  (fn [{:accounts/keys [accounts] :as db} [_ address]]
    (let [key (:public-key (accounts address))]
      (assoc db
             :accounts/current-account-id address
             :current-public-key key))))

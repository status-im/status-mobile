(ns status-im.ui.screens.accounts.events
  (:require [status-im.data-store.accounts :as accounts-store]
            [status-im.data-store.processed-messages :as processed-messages]
            [re-frame.core :refer [reg-event-db after dispatch dispatch-sync debug]]
            [taoensso.timbre :as log]
            [status-im.protocol.core :as protocol]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.random :as random]
            [status-im.i18n :refer [label]]
            [status-im.constants :refer [content-type-command-request console-chat-id]]
            status-im.ui.screens.accounts.login.events
            status-im.ui.screens.accounts.recover.events
            [clojure.string :as str]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as u :refer [get-hashtags]]
            [status-im.ui.screens.accounts.statuses :as statuses]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.scheduler :as s]
            [status-im.protocol.message-cache :as cache]
            [status-im.ui.screens.navigation :as nav]))

(defn save-account
  [{:keys [network]} [_ account]]
  (accounts-store/save (assoc account :network network) true))

(defn update-account
  [{:keys [network] :as db} [_ {:keys [address] :as account}]]
  (let [account' (assoc account :network network)]
    (update db :accounts assoc address account')))

(reg-event-db
  :add-account
  (u/handlers->
    update-account
      save-account))

(defn account-created [result password]
  (let [data       (json->clj result)
        public-key (:pubkey data)
        address    (:address data)
        mnemonic   (:mnemonic data)
        {:keys [public private]} (protocol/new-keypair!)
        account    {:public-key          public-key
                    :address             address
                    :name                (generate-gfy)
                    :status              (rand-nth statuses/data)
                    :signed-up?          true
                    :updates-public-key  public
                    :updates-private-key private
                    :photo-path          (identicon public-key)}]
    (log/debug "account-created")
    (when-not (str/blank? public-key)
      (dispatch [:show-mnemonic mnemonic])
      (dispatch [:add-account account])
      (dispatch [:login-account address password true]))))

(reg-event-db :create-account
  (u/side-effect!
    (fn [_ [_ password]]
      (dispatch [:set :creating-account? true])
      (s/execute-later #(dispatch [:account-generation-message]) 400)
      (status/create-account
        password
        #(account-created % password)))))

(defn save-account!
  [{:keys [current-account-id accounts network]} _]
  (let [{acc-network :network :as account}
        (get accounts current-account-id)

        account' (assoc account :network (or acc-network network))]
    (accounts-store/save account' true)))

(defn broadcast-account-update
  [{:keys [current-account-id current-public-key web3 accounts]} _]
  (let [{:keys [name photo-path status]} (get accounts current-account-id)
        {:keys [updates-public-key updates-private-key]} (accounts current-account-id)]
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

(defn send-keys-update
  [{:keys [current-account-id current-public-key web3 accounts contacts]} _]
  (let [{:keys [name photo-path status]} (get accounts current-account-id)
        {:keys [updates-public-key updates-private-key]} (accounts current-account-id)]
    (doseq [id (u/identities contacts)]
      (protocol/update-keys!
        {:web3    web3
         :message {:from       current-public-key
                   :to         id
                   :message-id (random/id)
                   :payload    {:keypair {:public  updates-public-key
                                          :private updates-private-key}}}}))))

(reg-event-db
  :check-status-change
  (u/side-effect!
    (fn [{:keys [current-account-id accounts]} [_ status]]
      (let [{old-status :status} (get accounts current-account-id)
            status-updated? (and (not= status nil)
                                 (not= status old-status))]
        (when status-updated?
          (let [hashtags (get-hashtags status)]
            (when (seq hashtags)
              (dispatch [:broadcast-status status hashtags]))))))))

(defn account-update
  [{:keys [current-account-id accounts] :as db} data]
  (let [data    (assoc data :last-updated (time/now-ms))
        account (merge (get accounts current-account-id) data)]
    (assoc-in db [:accounts current-account-id] account)))

(defn account-update-keys
  [db _]
  (let [{:keys [public private]} (protocol/new-keypair!)]
    (account-update db {:updates-public-key  public
                        :updates-private-key private})))

(reg-event-db
  :account-update
  (u/handlers->
    (fn [db [_ data]]
      (account-update db data))
    save-account!
    broadcast-account-update))

(reg-event-db
  :account-update-keys
  (u/handlers->
    save-account!
    send-keys-update
    account-update-keys))

(reg-event-db
  :send-account-update-if-needed
  (u/side-effect!
    (fn [{:keys [current-account-id accounts]} _]
      (let [{:keys [last-updated]} (get accounts current-account-id)
            now           (time/now-ms)
            needs-update? (> (- now last-updated) time/week)]
        (log/info "Need to send account-update: " needs-update?)
        (when needs-update?
          (dispatch [:account-update]))))))

(defn set-current-account
  [{:keys [accounts] :as db} [_ address]]
  (let [key (:public-key (accounts address))]
    (assoc db :current-account-id address
              :current-public-key key)))

(reg-event-db :set-current-account set-current-account)

(defn load-accounts! [db _]
  (let [accounts (->> (accounts-store/get-all)
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    (assoc db :accounts accounts)))

(reg-event-db :load-accounts load-accounts!)

(defn console-create-account [db _]
  (let [message-id (random/id)]
    (dispatch [:received-message
               {:message-id   message-id
                :content      {:command (name :keypair)
                               :content (label :t/keypair-generated)}
                :content-type content-type-command-request
                :outgoing     false
                :from         console-chat-id
                :to           "me"}])
    db))

(reg-event-db :console-create-account console-create-account)

(reg-event-db
  :load-processed-messages
  (u/side-effect!
    (fn [_]
      (let [now      (time/now-ms)
            messages (processed-messages/get-filtered (str "ttl > " now))]
        (cache/init! messages)
        (processed-messages/delete (str "ttl <=" now))))))

(ns status-im.accounts.handlers
  (:require [status-im.data-store.accounts :as accounts-store]
            [re-frame.core :refer [register-handler after dispatch dispatch-sync debug]]
            [taoensso.timbre :as log]
            [status-im.protocol.core :as protocol]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.db :refer [default-view]]
            [status-im.utils.random :as random]
            [status-im.i18n :refer [label]]
            [status-im.constants :refer [content-type-command-request]]
            status-im.accounts.login.handlers
            status-im.accounts.recover.handlers
            [clojure.string :as str]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as u]
            [status-im.constants :refer [console-chat-id]]))


(defn save-account [_ [_ account]]
  (accounts-store/save account true))

(register-handler
  :add-account
  (-> (fn [db [_ {:keys [address] :as account}]]
        (update db :accounts assoc address account))
      ((after save-account))))

(defn account-created [result password]
  (let [data (json->clj result)
        public-key (:pubkey data)
        address (:address data)
        mnemonic (:mnemonic data)
        {:keys [public private]} (protocol/new-keypair!)
        account {:public-key          public-key
                 :address             address
                 :name                address
                 :signed-up?          true
                 :updates-public-key  public
                 :updates-private-key private
                 :photo-path          (identicon public-key)}]
    (log/debug "account-created")
    (when (not (str/blank? public-key))
      (do
        (dispatch [:add-account account])
        (dispatch [:show-mnemonic mnemonic])
        (dispatch [:login-account address password])))))

(register-handler :create-account
  (u/side-effect!
    (fn [_ [_ password]]
      (status/create-account
        password
        #(account-created % password)))))

(defn save-account!
  [{:keys [current-account-id accounts]} _]
  (accounts-store/save (get accounts current-account-id) true))

(defn send-account-update
  [{:keys [current-account-id current-public-key web3 accounts]} _]
  (let [{:keys [name photo-path status]} (get accounts current-account-id)
        {:keys [updates-public-key updates-private-key]} (accounts current-account-id)]
    (protocol/broadcats-profile!
      {:web3    web3
       :message {:from       current-public-key
                 :message-id (random/id)
                 :keypair    {:public  updates-public-key
                              :private updates-private-key}
                 :payload    {:profile {:name          name
                                        :status        status
                                        :profile-image photo-path}}}})))

(register-handler
  :account-update
  (-> (fn [{:keys [current-account-id accounts] :as db} [_ data]]
        (let [data (assoc data :last-updated (time/now-ms))
              account (-> (get accounts current-account-id)
                          (merge data))]
          (assoc-in db [:accounts current-account-id] account)))
      ((after save-account!))
      ((after send-account-update))))

(register-handler
  :send-account-update-if-needed
  (u/side-effect!
    (fn [{:keys [current-account-id accounts]} _]
      (let [{:keys [last-updated]} (get accounts current-account-id)
            now (time/now-ms)
            needs-update? (> (- now last-updated) time/week)]
        (log/info "Need to send account-update: " needs-update?)
        (when needs-update?
          (dispatch [:account-update]))))))

(defn set-current-account
  [{:keys [accounts] :as db} [_ address]]
  (let [key (:public-key (accounts address))]
    (assoc db :current-account-id address
              :current-public-key key)))

(register-handler :set-current-account set-current-account)

(defn load-accounts! [db _]
  (let [accounts (->> (accounts-store/get-all)
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    (assoc db :accounts accounts
              :view-id (if (empty? accounts)
                         :chat
                         :accounts))))

(register-handler :load-accounts load-accounts!)

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

(register-handler :console-create-account console-create-account)

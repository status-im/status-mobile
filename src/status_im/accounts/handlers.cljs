(ns status-im.accounts.handlers
  (:require [status-im.models.accounts :as accounts]
            [re-frame.core :refer [register-handler after dispatch dispatch-sync debug]]
            [status-im.utils.logging :as log]
            [status-im.components.geth :as geth]
            [status-im.utils.types :refer [json->clj]]
            [status-im.persistence.simple-kv-store :as kv]
            [status-im.protocol.state.storage :as storage]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.db :refer [default-view]]
            [status-im.utils.random :as random]
            [status-im.persistence.realm.core :as realm]
            [status-im.i18n :refer [label]]
            [status-im.constants :refer [content-type-command-request]]
            status-im.accounts.login.handlers
            [clojure.string :as str]))


(defn save-account [_ [_ account]]
  (accounts/save-accounts [account]))

(register-handler :add-account
  (-> (fn [db [_ {:keys [address] :as account}]]
          (update db :accounts assoc address account))
      ((after save-account))))

(defn save-password [password]
  (storage/put kv/kv-store :password password))

(defn account-created [db result password]
  (let [data (json->clj result)
        public-key (:pubkey data)
        address (:address data)
        account {:public-key public-key
                 :address address
                 :name address
                 :photo-path (identicon address)}
        ]
    (log/debug "account-created: " account)
    (when (not (str/blank? public-key))
      (do
        ;(save-password password)
        (dispatch-sync [:add-account account])
        (dispatch [:login-account address password])))))

(register-handler :create-account
  (-> (fn [db [_ password]]
          (geth/create-account password (fn [result] (account-created db result password)))
        db)))

(defn initialize-account [db account]
  (let [is-login-screen? (= (:view-id db) :login)]
    (dispatch [:set :login {}])
    (dispatch [:set :is-logged-in true])
    (dispatch [:set :user-identity account])
    (dispatch [:initialize-account account])
    (when is-login-screen? (dispatch [:navigate-to-clean default-view]))))

(defn logged-in [db address]
  (let [account (get-in db [:accounts address])
        is-login-screen? (= (:view-id db) :login)
        new-account? (not is-login-screen?)]
    (log/debug "Logged in: " address account)
    (realm/change-account-realm address new-account?
                                #(if (nil? %)
                                   (initialize-account db account)
                                   (log/debug "Error changing acount realm: " %)))))

(register-handler :login-account
  (-> (fn [db [_ address password]]
        (geth/login address password (fn [result]
                                       (let [data (json->clj result)
                                             error (:error data)
                                             success (zero? (count error))]
                                         (log/debug "Logged in account: " address result)
                                         (if success
                                           (logged-in db address)
                                           (dispatch [:set-in [:login :error] error])))))
        db)))

(defn load-accounts! [db _]
  (let [accounts (->> (accounts/get-accounts)
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    (assoc db :accounts accounts)))

(register-handler :load-accounts load-accounts!)

(defn console-create-account [db _]
  (let [msg-id (random/id)]
    (dispatch [:received-msg
               {:msg-id       msg-id
                :content      {:command (name :keypair)
                               :content (label :t/keypair-generated)}
                :content-type content-type-command-request
                :outgoing     false
                :from         "console"
                :to           "me"}])
    db))

(register-handler :console-create-account console-create-account)
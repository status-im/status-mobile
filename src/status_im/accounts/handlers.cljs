(ns status-im.accounts.handlers
  (:require [status-im.models.accounts :as accounts]
            [re-frame.core :refer [register-handler after dispatch dispatch-sync debug]]
            [status-im.utils.logging :as log]
            [status-im.protocol.api :as api]
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
            [clojure.string :as str]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as u]))


(defn save-account [_ [_ account]]
  (accounts/save-accounts [account] true))

(register-handler
  :add-account
  (-> (fn [db [_ {:keys [address] :as account}]]
        (update db :accounts assoc address account))
      ((after save-account))))

(defn save-password [password]
  (storage/put kv/kv-store :password password))

(defn account-created [result password]
  (let [data       (json->clj result)
        public-key (:pubkey data)
        address    (:address data)
        account    {:public-key public-key
                    :address    address
                    :name       address
                    :photo-path (identicon public-key)}]
    (log/debug "account-created")
    (when (not (str/blank? public-key))
      (do
        (dispatch-sync [:add-account account])
        (dispatch [:login-account address password])))))

(register-handler :create-account
  (after #(dispatch [:init-wallet-chat]))
  (u/side-effect!
    (fn [_ [_ password]]
      (geth/create-account
        password
        #(account-created % password)))))

(defn save-account-to-realm!
  [{:keys [current-account-id accounts]} _]
  (accounts/save-accounts [(get accounts current-account-id)] true))

(defn send-account-update
  [{:keys [current-account-id accounts]} _]
  (api/send-account-update (get accounts current-account-id)))

(register-handler
  :account-update
  (-> (fn [{:keys [current-account-id accounts] :as db} [_ data]]
        (let [data               (assoc data :last-updated (time/now-ms))
              account            (-> (get accounts current-account-id)
                                     (merge data))]
          (assoc-in db [:accounts current-account-id] account)))
      ((after save-account-to-realm!))
      ((after send-account-update))))

(register-handler
  :send-account-update-if-needed
  (u/side-effect!
    (fn [{:keys [current-account-id accounts]} _]
      (let [{:keys [last-updated]} (get accounts current-account-id)
            now           (time/now-ms)
            needs-update? (> (- now last-updated) time/week)]
        (log/info "Need to send account-update: " needs-update?)
        (when needs-update?
          (dispatch [:account-update]))))))

(defn initialize-account [db address]
  (let [is-login-screen? (= (:view-id db) :login)]
    (dispatch [:set :login {}])
    (dispatch [:set :current-account-id address])
    (dispatch [:initialize-account address])
    (when is-login-screen? (dispatch [:navigate-to-clean default-view]))))

(defn logged-in [db address]
  (let [is-login-screen? (= (:view-id db) :login)
        new-account? (not is-login-screen?)]
    (log/debug "Logged in: ")
    (realm/change-account-realm address new-account?
                                #(if (nil? %)
                                   (initialize-account db address)
                                   (log/debug "Error changing acount realm: " %)))))

(register-handler
  :login-account
  (u/side-effect!
    (fn [db [_ address password]]
      (geth/login address password
                  (fn [result]
                    (let [data (json->clj result)
                          error (:error data)
                          success (zero? (count error))]
                      (log/debug "Logged in account: ")
                      (if success
                        (logged-in db address)
                        (dispatch [:set-in [:login :error] error]))))))))

(defn load-accounts! [db _]
  (let [accounts (->> (accounts/get-accounts)
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    (assoc db :accounts accounts)))

(register-handler :load-accounts load-accounts!)

(defn console-create-account [db _]
  (let [msg-id (random/id)]
    (dispatch [:received-message
               {:msg-id       msg-id
                :content      {:command (name :keypair)
                               :content (label :t/keypair-generated)}
                :content-type content-type-command-request
                :outgoing     false
                :from         "console"
                :to           "me"}])
    db))

(register-handler :console-create-account console-create-account)

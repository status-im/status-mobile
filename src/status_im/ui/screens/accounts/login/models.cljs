(ns status-im.ui.screens.accounts.login.models
  (:require status-im.ui.screens.accounts.login.navigation
            [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.notifications :as notifications]
            [status-im.utils.universal-links.core :as universal-links]))

;;;; FX

(defn stop-node! [] (status/stop-node))

(defn login! [address password]
  (status/login address password #(re-frame/dispatch [:login-handler % address])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn- change-account! [address encryption-key]
  (data-store/change-account address encryption-key)
  (re-frame/dispatch [:change-account-handler address]))

(defn handle-change-account! [address]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then (partial change-account! address))
      (catch (fn [error]
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [:initialize-app "" :decryption-failed])))))

;;;; Handlers

(defn open-login [address photo-path name {db :db}]
  {:db       (update db
                     :accounts/login assoc
                     :address address
                     :photo-path photo-path
                     :name name)
   :dispatch [:navigate-to :login]})

(defn wrap-with-login-account-fx [db address password]
  {:db    db
   :login [address password]})

(defn login-account-internal [address password {db :db}]
  (wrap-with-login-account-fx
   (assoc db :node/after-start nil)
   address password))

(defn- add-custom-bootnodes [config network all-bootnodes]
  (let [bootnodes (as-> all-bootnodes $
                    (get $ network)
                    (vals $)
                    (map :address $))]
    (if (seq bootnodes)
      (assoc config :ClusterConfig {:Enabled   true
                                    :BootNodes bootnodes})
      config)))

(defn- get-network-by-address [db address]
  (let [accounts (get db :accounts/accounts)
        {:keys [network
                settings
                bootnodes
                networks]} (get accounts address)
        use-custom-bootnodes (get-in settings [:bootnodes network])
        config   (cond-> (get-in networks [network :config])
                   (and
                    config/bootnodes-settings-enabled?
                    use-custom-bootnodes)
                   (add-custom-bootnodes network bootnodes))]
    {:use-custom-bootnodes use-custom-bootnodes
     :network              network
     :config               config}))

(defn- wrap-with-initialize-geth-fx [db address password]
  (let [{:keys [network config]} (get-network-by-address db address)]
    {:initialize-geth-fx config
     :db                 (assoc db
                                :network network
                                :node/after-start [:login-account-internal address password])}))

(defn start-node [address password {db :db}]
  (wrap-with-initialize-geth-fx
   (assoc db :node/after-stop nil)
   address password))

(defn- wrap-with-stop-node-fx [db address password]
  {:db        (assoc db :node/after-stop [:start-node address password])
   :stop-node nil})

(defn- restart-node? [account-network network use-custom-bootnodes]
  (or (not= account-network network)
      (and config/bootnodes-settings-enabled?
           use-custom-bootnodes)))

(defn login-account [address password {{:keys [network status-node-started?] :as db} :db}]
  (let [{use-custom-bootnodes :use-custom-bootnodes
         account-network :network} (get-network-by-address db address)
        db'     (-> db
                    (assoc-in [:accounts/login :processing] true))
        wrap-fn (cond (not status-node-started?)
                      wrap-with-initialize-geth-fx

                      (not (restart-node? account-network
                                          network
                                          use-custom-bootnodes))
                      wrap-with-login-account-fx

                      :else
                      wrap-with-stop-node-fx)]
    (wrap-fn db' address password)))

(defn login-handler [login-result address {db :db}]
  (let [data    (types/json->clj login-result)
        error   (:error data)
        success (zero? (count error))
        db'     (assoc-in db [:accounts/login :processing] false)]
    (if success
      {:db             db
       :clear-web-data nil
       :change-account [address]}
      {:db (assoc-in db' [:accounts/login :error] error)})))

(defn change-account-handler [address {{:keys [view-id] :as db} :db :as cofx}]
  {:db       (cond-> (dissoc db :accounts/login)
               (= view-id :create-account)
               (assoc-in [:accounts/create :step] :enter-name))
   :dispatch [:initialize-account address
              (when (not= view-id :create-account)
                [[:navigate-to-clean :home]
                 (universal-links/stored-url-event cofx)
                 (notifications/stored-event address cofx)])]})

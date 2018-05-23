(ns status-im.ui.screens.accounts.login.events
  (:require status-im.ui.screens.accounts.login.navigation
            [re-frame.core :refer [dispatch reg-fx]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [taoensso.timbre :as log]
            [status-im.utils.types :refer [json->clj]]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.keychain :as keychain]
            [status-im.utils.utils :as utils]))

;;;; FX

(reg-fx ::stop-node (fn [] (status/stop-node)))

(reg-fx
 ::login
 (fn [[address password]]
   (status/login address password #(dispatch [:login-handler % address]))))

(reg-fx
 ::clear-web-data
 (fn []
   (status/clear-web-data)))

(reg-fx
 ::change-account
 (fn [[address]]
   ;; if we don't add delay when running app without status-go
   ;; "null is not an object (evaluating 'realm.schema')" error appears
   (keychain/get-encryption-key-then
    (fn [encryption-key]
      (let [change-account-fn (fn [] (data-store/change-account address
                                                                false
                                                                encryption-key
                                                                #(dispatch [:change-account-handler % address])))]
        (if config/stub-status-go?
          (utils/set-timeout change-account-fn
                             300)
          (change-account-fn)))))))

;;;; Handlers

(register-handler-fx
 :open-login
 (fn [{db :db} [_ address photo-path name]]
   {:db       (update db
                      :accounts/login assoc
                      :address address
                      :photo-path photo-path
                      :name name)
    :dispatch [:navigate-to :login]}))

(defn wrap-with-login-account-fx [db address password]
  {:db     db
   ::login [address password]})

(register-handler-fx
 ::login-account
 (fn [{db :db} [_ address password]]
   (wrap-with-login-account-fx
    (assoc db :node/after-start nil)
    address password)))

(defn add-custom-bootnodes [config network all-bootnodes]
  (let [bootnodes (as-> all-bootnodes $
                    (get $ network)
                    (vals $)
                    (map :address $))]
    (if (seq bootnodes)
      (assoc config :ClusterConfig {:Enabled   true
                                    :BootNodes bootnodes})
      config)))

(defn get-network-by-address [db address]
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

(defn wrap-with-initialize-geth-fx [db address password]
  (let [{:keys [network config]} (get-network-by-address db address)]
    {:initialize-geth-fx config
     :db                 (assoc db :network network
                                :node/after-start [::login-account address password])}))

(register-handler-fx
 ::start-node
 (fn [{db :db} [_ address password]]
   (wrap-with-initialize-geth-fx
    (assoc db :node/after-stop nil)
    address password)))

(defn wrap-with-stop-node-fx [db address password]
  {:db         (assoc db :node/after-stop [::start-node address password])
   ::stop-node nil})

(defn- restart-node? [account-network network use-custom-bootnodes]
  (or (not= account-network network)
      (and config/bootnodes-settings-enabled?
           use-custom-bootnodes)))

(defn login-account [{{:keys [network status-node-started?] :as db} :db} [_ address password]]
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

(register-handler-fx
 :login-account
 login-account)

(register-handler-fx
 :login-handler
 (fn [{db :db} [_ login-result address]]
   (let [data    (json->clj login-result)
         error   (:error data)
         success (zero? (count error))
         db'     (assoc-in db [:accounts/login :processing] false)]
     (if success
       {:db              db
        ::clear-web-data nil
        ::change-account [address]}
       {:db (assoc-in db' [:accounts/login :error] error)}))))

(register-handler-fx
 :change-account-handler
 (fn [{{:keys [view-id] :as db} :db} [_ error address]]
   (if (nil? error)
     {:db       (cond-> (dissoc db :accounts/login)
                  (= view-id :create-account)
                  (assoc-in [:accounts/create :step] :enter-name))
      :dispatch [:initialize-account address
                 (when (not= view-id :create-account)
                   [[:navigate-to-clean :home]])]}
     (log/debug "Error changing acount: " error))))

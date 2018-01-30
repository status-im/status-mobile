(ns status-im.ui.screens.accounts.login.events
  (:require status-im.ui.screens.accounts.login.navigation
            [re-frame.core :refer [dispatch reg-fx]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [taoensso.timbre :as log]
            [status-im.chat.console :as console-chat]
            [status-im.utils.types :refer [json->clj]]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.constants :refer [console-chat-id]]
            [status-im.utils.config :as config]))

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
  (fn [[address new-account?]]
    ;; if we don't add delay when running app without status-go
    ;; "null is not an object (evaluating 'realm.schema')" error appears
    (if config/stub-status-go?
      (js/setTimeout
        (fn []
          (data-store/change-account address new-account?
                                     #(dispatch [:change-account-handler % address new-account?])))
        300)
      (data-store/change-account address new-account?
                                 #(dispatch [:change-account-handler % address new-account?])))))

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

(defn get-network-by-address [db address]
  (let [accounts (get db :accounts/accounts)
        {:keys [network networks]} (get accounts address)
        config   (get-in networks [network :config])]
    {:network network
     :config  config}))

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

(register-handler-fx
  :login-account
  (fn [{{:keys [network status-node-started?] :as db} :db}
       [_ address password account-creation?]]
    (let [{account-network :network} (get-network-by-address db address)
          db' (-> db
                  (assoc :accounts/account-creation? account-creation?)
                  (assoc-in [:accounts/login :processing] true))
          wrap-fn (cond (not status-node-started?)
                        wrap-with-initialize-geth-fx

                        (= account-network network)
                        wrap-with-login-account-fx

                        :else
                        wrap-with-stop-node-fx)]
      (wrap-fn db' address password))))

(register-handler-fx
  :login-handler
  (fn [{{:accounts/keys [account-creation?] :as db} :db} [_ login-result address]]
    (let [data    (json->clj login-result)
          error   (:error data)
          success (zero? (count error))
          db'     (assoc-in db [:accounts/login :processing] false)]
      (log/debug "Logging result: " login-result)
      (merge
       {:db (if success db' (assoc-in db' [:accounts/login :error] error))}
       (when success
         (log/debug "Logged in" (when account-creation? " new account") ":" address)
         {::clear-web-data nil
          ::change-account [address account-creation?]})))))

(register-handler-fx
  :change-account-handler
  (fn [{db :db} [_ error address new-account?]]
    (let [recover-in-progress? (:accounts/recover db)]
      (if (nil? error)
        {:db         (dissoc db :accounts/login)
         :dispatch-n [[:stop-debugging]
                      [:initialize-account
                       address
                       (when (or new-account? recover-in-progress?)
                         [[:chat-received-message/add console-chat/shake-your-phone-message]])]
                      [:navigate-to-clean :home]
                      (if new-account?
                        [:navigate-to-chat console-chat-id]
                        [:navigate-to :home])]}
        (log/debug "Error changing acount: " error)))))

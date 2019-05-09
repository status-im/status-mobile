(ns status-im.extensions.capacities.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [clojure.string :as string]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ipfs.core :as ipfs]
            [status-im.browser.core :as browser]
            [status-im.utils.fx :as fx]
            [status-im.accounts.update.core :as accounts.update]
            status-im.extensions.capacities.ethereum
            status-im.extensions.capacities.camera.events
            status-im.extensions.capacities.network))

(defn- empty-value? [o]
  (cond
    (seqable? o) (empty? o)
    :else (nil? o)))

(defn- put-or-dissoc [db id key value]
  (if (empty-value? value)
    (update-in db [:extensions/store id] dissoc key)
    (assoc-in db [:extensions/store id key] value)))

(defn- append [acc k v]
  (let [o (get acc k)
        ve (cond (vector? o) o
                 (not (nil? o)) (vector o)
                 :else [])]
    (assoc acc k (conj ve v))))

(defn- json? [res]
  (when-let [type (get-in res [:headers "content-type"])]
    (string/starts-with? type "application/json")))

(defn- parse-json [o]
  (when o
    (js->clj (js/JSON.parse o) :keywordize-keys true)))

(defn- parse-result [o on-success]
  (let [res (if (json? o) (update o :body parse-json) o)]
    (on-success res)))

(defn operation->fn [k]
  (case k
    :plus +
    :minus -
    :times *
    :divide /))

(defn update-account-data [{db :db :as cofx} id data-fn]
  (let [{:account/keys [account]} db
        extensions     (get account :extensions)]
    (accounts.update/account-update
     cofx
     {:extensions (update-in extensions [id :data] data-fn)}
     {})))

(defn- put-or-dissoc-persis [current-data key value]
  (if value
    (assoc current-data key value)
    (dissoc current-data key)))

(fx/defn put-persistent
  [cofx id {:keys [key value]}]
  (update-account-data cofx id
                       (fn [current-data]
                         (put-or-dissoc-persis current-data key value))))

(fx/defn put-in-persistent
  [cofx id {:keys [keys value]}]
  (update-account-data cofx id
                       (fn [current-data]
                         (assoc-in current-data keys value))))

(fx/defn puts-persistent
  [cofx id {:keys [value]}]
  (update-account-data cofx id
                       (fn [current-data]
                         (reduce #(put-or-dissoc-persis %1 (:key %2) (:value %2)) current-data value))))

(fx/defn append-persistent
  [cofx id {:keys [key value]}]
  (update-account-data cofx id
                       (fn [current-data]
                         (append current-data key value))))

(fx/defn clear-persistent
  [{db :db :as cofx} id {:keys [key]}]
  (let [{:account/keys [account]} db
        extensions     (get account :extensions)]
    (when (get-in extensions [id :data key])
      (accounts.update/account-update
       cofx
       {:extensions (update-in extensions [id :data] dissoc key)}
       {}))))

(fx/defn clear-all-persistent
  [{db :db :as cofx} id]
  (let [{:account/keys [account]} db
        extensions     (get account :extensions)]
    (when (get-in extensions [id :data])
      (accounts.update/account-update
       cofx
       {:extensions (update extensions id dissoc :data)}
       {}))))

;;FX

(re-frame/reg-fx
 ::identity-event
 (fn [{:keys [cb]}] (cb {})))

(re-frame/reg-fx
 ::alert
 (fn [value] (js/alert value)))

(re-frame/reg-fx
 ::log
 (fn [value] (js/console.log value)))

(re-frame/reg-fx
 ::schedule-start
 (fn [{:keys [interval on-created on-result]}]
   (let [id (js/setInterval #(on-result {}) interval)]
     (on-created {:value id}))))

(re-frame/reg-fx
 ::schedule-cancel
 (fn [{:keys [value]}]
   (js/clearInterval value)))

(re-frame/reg-fx
 ::json-parse
 (fn [{:keys [value on-result]}]
   (on-result {:value (parse-json value)})))

(re-frame/reg-fx
 ::json-stringify
 (fn [value on-result]
   (on-result {:value (js/JSON.stringify (clj->js value))})))

(re-frame/reg-fx
 ::arithmetic
 (fn [{:keys [operation values on-result]}]
   (on-result {:value (apply (operation->fn operation) values)})))

;;EVENTS

(handlers/register-handler-fx
 :extensions/identity-event
 (fn [_ [_ _ m]]
   {::identity-event m}))

(handlers/register-handler-fx
 :alert
 (fn [_ [_ _ {:keys [value]}]]
   {::alert value}))

(handlers/register-handler-fx
 :log
 (fn [_ [_ _ {:keys [value]}]]
   {::log value}))

(handlers/register-handler-fx
 :extensions/schedule-start
 (fn [_ [_ _ m]]
   {::schedule-start m}))

(handlers/register-handler-fx
 :extensions/schedule-cancel
 (fn [_ [_ _ m]]
   {::schedule-cancel m}))

(handlers/register-handler-fx
 :store/put
 (fn [{:keys [db] :as cofx} [_ {id :id} {:keys [key value persistent] :as arguments}]]
   (fx/merge cofx
             {:db (put-or-dissoc db id key value)}
             (when persistent
               (put-persistent id arguments)))))

(re-frame/reg-event-fx
 :store/put-in
 (fn [{:keys [db] :as cofx} [_ {id :id} {:keys [keys value persistent] :as arguments}]]
   (fx/merge cofx
             {:db (assoc-in db (into [] (concat [:extensions/store id] keys)) value)}
             (when persistent
               (put-in-persistent id arguments)))))

(handlers/register-handler-fx
 :store/puts
 (fn [{:keys [db] :as cofx} [_ {id :id} {:keys [value persistent] :as arguments}]]
   (fx/merge cofx
             {:db (reduce #(put-or-dissoc %1 id (:key %2) (:value %2)) db value)}
             (when persistent
               (puts-persistent id arguments)))))

(handlers/register-handler-fx
 :store/append
 (fn [{:keys [db] :as cofx} [_ {id :id} {:keys [key value persistent] :as arguments}]]
   (fx/merge cofx
             {:db (update-in db [:extensions/store id] append key value)}
             (when persistent
               (append-persistent id arguments)))))

(handlers/register-handler-fx
 :store/clear
 (fn [{:keys [db] :as cofx} [_ {id :id} {:keys [key]}]]
   (fx/merge cofx
             {:db (update-in db [:extensions/store id] dissoc key)}
             (clear-persistent id key))))

(handlers/register-handler-fx
 :store/clear-all
 (fn [{:keys [db] :as cofx} [_ {id :id} _]]
   (fx/merge cofx
             {:db (update db :extensions/store dissoc id)}
             (clear-all-persistent id))))

(handlers/register-handler-fx
 :extensions/json-parse
 (fn [_ [_ _ m]]
   {::json-parse m}))

(handlers/register-handler-fx
 :extensions/json-stringify
 (fn [_ [_ _ {:keys [value]}]]
   {::json-stringify value}))

(handlers/register-handler-fx
 :http/get
 (fn [_ [_ _ {:keys [url on-success on-failure timeout]}]]
   {:http-raw-get (merge {:url                   url
                          :success-event-creator #(parse-result % on-success)}
                         (when on-failure
                           {:failure-event-creator on-failure})
                         (when timeout
                           {:timeout-ms timeout}))}))

(handlers/register-handler-fx
 :ipfs/cat
 (fn [cofx [_ _ args]]
   (ipfs/cat cofx args)))

(handlers/register-handler-fx
 :ipfs/add
 (fn [cofx [_ _ args]]
   (ipfs/add cofx args)))

(handlers/register-handler-fx
 :http/post
 (fn [_ [_ _ {:keys [url body on-success on-failure timeout]}]]
   {:http-raw-post (merge {:url                   url
                           :body                  (clj->js body)
                           :success-event-creator #(parse-result % on-success)}
                          (when on-failure
                            {:failure-event-creator on-failure})
                          (when timeout
                            {:timeout-ms timeout}))}))

(handlers/register-handler-fx
 :extensions.chat.command/set-parameter
 (fn [_ [_ _ {:keys [value]}]]
   {:dispatch [:chat.ui/set-command-parameter value]}))

(handlers/register-handler-fx
 :extensions.chat.command/set-custom-parameter
 (fn [{{:keys [current-chat-id] :as db} :db} [_ _ {:keys [key value]}]]
   {:db (assoc-in db [:chats current-chat-id :custom-params key] value)}))

(handlers/register-handler-fx
 :extensions.chat.command/set-parameter-with-custom-params
 (fn [{{:keys [current-chat-id] :as db} :db} [_ _ {:keys [value params]}]]
   {:db       (update-in db [:chats current-chat-id :custom-params] merge params)
    :dispatch [:chat.ui/set-command-parameter value]}))

(handlers/register-handler-fx
 :extensions.chat.command/send-plain-text-message
 (fn [_ [_ _ {:keys [value]}]]
   {:dispatch [:chat/send-plain-text-message value]}))

(handlers/register-handler-fx
 :extensions.chat.command/send-message
 (fn [{{:keys [current-chat-id] :as db} :db :as cofx} [_ {:keys [hook-id]} {:keys [params]}]]
   (when hook-id
     (when-let [command (last (first (filter #(= (ffirst %) (name hook-id)) (:id->command db))))]
       (commands-sending/send cofx current-chat-id command params)))))

(handlers/register-handler-fx
 :extensions.chat.command/open-public-chat
 (fn [_ [_ _ {:keys [topic navigate-to]}]]
   {:dispatch [:chat.ui/start-public-chat topic {:dont-navigate? (not navigate-to) :navigation-reset? true}]}))

(handlers/register-handler-fx
 :extensions/show-selection-screen
 (fn [cofx [_ _ {:keys [on-select] :as params}]]
   (navigation/navigate-to-cofx cofx
                                :selection-modal-screen
                                (assoc params :on-select #(do
                                                            (re-frame/dispatch [:navigate-back])
                                                            (on-select %))))))

(handlers/register-handler-fx
 :extensions/arithmetic
 (fn [_ [_ _ m]]
   {::arithmetic m}))

(handlers/register-handler-fx
 :extensions/open-url
 (fn [cofx [_ _ {:keys [url]}]]
   (browser/open-url cofx url)))

(handlers/register-handler-fx
 :extensions/profile-settings-close
 (fn [{:keys [db] :as cofx} [_ _ {:keys [on-close on-failure]}]]
   (let [view-id (:view-id db)]
     ;; ensure the current view-id is the profile settings view
     (if (= view-id :my-profile-ext-settings)
       (fx/merge cofx
                 (when on-close (on-close))
                 (navigation/navigate-back))
       (if on-failure
         (on-failure {:value "extension is not currently showing profile settings"})
         {})))))

(handlers/register-handler-fx
 :extensions/wallet-settings-close
 (fn [{:keys [db] :as cofx} [_ _ {:keys [on-close on-failure]}]]
   (let [view-id (:view-id db)]
     ;; ensure the current view-id is the wallet settings view
     (if (= view-id :wallet-settings-hook)
       (fx/merge cofx
                 (when on-close (on-close))
                 (navigation/navigate-back))
       (if on-failure
         (on-failure {:value "extension is not currently showing wallet settings"})
         {})))))

(handlers/register-handler-fx
 :extensions/screen-open
 (fn [cofx [_ _ {:keys [view on-open]}]]
   (fx/merge cofx
             (when on-open (on-open))
             (navigation/navigate-to-cofx :extension-screen-holder {:view view}))))

(handlers/register-handler-fx
 :extensions/screen-close
 (fn [{:keys [db] :as cofx} [_ _ {:keys [on-close on-failure]}]]
   (let [view-id (:view-id db)]
     ;; ensure the current view-id is the extension fullscreen holder
     (if (= view-id :extension-screen-holder)
       (fx/merge cofx
                 (when on-close (on-close))
                 (navigation/navigate-back))
       (if on-failure
         (on-failure {:value "extension is not currently showing a fullscreen view"})
         {})))))

;;CAPACITIES

(def all
  {'identity
   {:permissions [:read]
    :data        :extensions/identity-event
    :arguments   {:cb :event}}
   'alert
   {:permissions [:read]
    :data        :alert
    :arguments   {:value :string}}
   'selection-screen
   {:permissions [:read]
    :data        :extensions/show-selection-screen
    :arguments   {:items :vector :on-select :event :render :view :title :string :extractor-key :keyword}}
   'chat.command/set-parameter
   {:permissions [:read]
    :data        :extensions.chat.command/set-parameter
    :arguments   {:value :any}}
   'chat.command/set-custom-parameter
   {:permissions [:read]
    :data        :extensions.chat.command/set-custom-parameter
    :arguments   {:key :keyword :value :any}}
   'chat.command/set-parameter-with-custom-params
   {:permissions [:read]
    :data        :extensions.chat.command/set-parameter-with-custom-params
    :arguments   {:value :string :params :map}}
   'chat.command/send-plain-text-message
   {:permissions [:read]
    :data        :extensions.chat.command/send-plain-text-message
    :arguments   {:value :string}}
   'chat.command/send-message
   {:permissions [:read]
    :data        :extensions.chat.command/send-message
    :arguments   {:params :map}}
   'chat.command/open-public-chat
   {:permissions [:read]
    :data       :extensions.chat.command/open-public-chat
    :arguments   {:topic :string :navigate-to :boolean}}
   'log
   {:permissions [:read]
    :data        :log
    :arguments   {:value :string}}
   'arithmetic
   {:permissions [:read]
    :data        :extensions/arithmetic
    :arguments   {:values    :vector
                  :operation {:one-of #{:plus :minus :times :divide}}
                  :on-result :event}}
   'browser/open-url
   {:permissions [:read]
    :data       :extensions/open-url
    :arguments   {:url :string}}
   'camera/picture
   {:permissions [:read]
    :data       :extensions/camera-picture
    :arguments   {:on-success  :event
                  :on-failure? :event}}
   'camera/qr-code
   {:permissions [:read]
    :data       :extensions/camera-qr-code
    :arguments   {:on-success  :event
                  :on-failure? :event}}
   'schedule/start
   {:permissions [:read]
    :data        :extensions/schedule-start
    :arguments   {:interval   :number
                  :on-created :event
                  :on-result  :event}}
   'schedule/cancel
   {:permissions [:read]
    :data        :extensions/schedule-cancel
    :arguments   {:value      :number}}
   'json/parse
   {:permissions [:read]
    :data        :extensions/json-parse
    :arguments   {:value     :string
                  :on-result :event}}
   'json/stringify
   {:permissions [:read]
    :data        :extensions/json-stringify
    :arguments   {:value     :string
                  :on-result :event}}
   'store/put
   {:permissions [:read]
    :data        :store/put
    :arguments   {:key :string :value :any :persistent :boolean}}
   'store/put-in
   {:permissions [:read]
    :data        :store/put-in
    :arguments   {:keys :vector :value :any :persistent :boolean}}
   'store/puts
   {:permissions [:read]
    :data        :store/puts
    :arguments   {:value :vector :persistent :boolean}}
   'store/append
   {:permissions [:read]
    :data        :store/append
    :arguments   {:key :string :value :any :persistent :boolean}}
   'store/clear
   {:permissions [:read]
    :data        :store/clear
    :arguments   {:key :string}}
   'store/clear-all
   {:permissions [:read]
    :data       :store/clear-all}
   'network/add
   {:permissions [:read]
    :data       :network/add
    :arguments   {:chain-id    :number
                  :name        :string
                  :url         :string
                  :on-success? :event
                  :on-failure? :event}}
   'network/select
   {:permissions [:read]
    :data       :network/select
    :arguments   {:chain-id    :number
                  :on-success? :event
                  :on-failure? :event}}
   'network/remove
   {:permissions [:read]
    :data       :network/remove
    :arguments   {:chain-id    :number
                  :on-success? :event
                  :on-failure? :event}}
   'screen/open
   {:permissions [:read]
    :data        :extensions/screen-open
    :arguments   {:view        :view
                  :on-open?    :event}}
   'screen/close
   {:permissions [:read]
    :data        :extensions/screen-close
    :arguments   {:on-close?   :event
                  :on-failure? :event}}
   'http/get
   {:permissions [:read]
    :data        :http/get
    :arguments   {:url         :string
                  :timeout?    :string
                  :on-success  :event
                  :on-failure? :event}}
   'http/post
   {:permissions [:read]
    :data        :http/post
    :arguments   {:url         :string
                  :body        :string
                  :timeout?    :string
                  :on-success  :event
                  :on-failure? :event}}
   'ipfs/cat
   {:permissions [:read]
    :data        :ipfs/cat
    :arguments   {:hash        :string
                  :on-success  :event
                  :on-failure? :event}}
   'ipfs/add
   {:permissions [:read]
    :data        :ipfs/add
    :arguments   {:value       :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/transaction-receipt
   {:permissions [:read]
    :data        :extensions/ethereum-transaction-receipt
    :arguments   {:value        :string
                  :topics-hints :vector
                  :on-success   :event
                  :on-failure?  :event}}
   'ethereum/await-transaction-receipt
   {:permissions [:read]
    :data        :extensions/ethereum-await-transaction-receipt
    :arguments   {:value        :string
                  :interval     :number
                  :topics-hints :vector
                  :on-success   :event
                  :on-failure?  :event}}
   'ethereum/sign
   {:permissions [:read]
    :data        :extensions/ethereum-sign
    :arguments   {:message?    :string
                  :data?       :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/create-address
   {:permissions [:read]
    :data        :extensions/ethereum-create-address
    :arguments   {:on-result  :event}}
   'ethereum/send-transaction
   {:permissions [:read]
    :data        :extensions/ethereum-send-transaction
    :arguments   {:to          :string
                  :gas?        :string
                  :gas-price?  :string
                  :value?      :string
                  :method?     :string
                  :params?     :vector
                  :nonce?      :string
                  :on-success? :event
                  :on-failure? :event}}
   'ethereum/logs
   {:permissions [:read]
    :data        :extensions/ethereum-logs
    :arguments   {:from?       :string
                  :to?         :string
                  :address?    :vector
                  :topics?     :vector
                  :block-hash? :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/create-filter
   {:permissions [:read]
    :data        :extensions/ethereum-create-filter
    :arguments   {:type        {:one-of #{:filter :block :pending-transaction}}
                  :from?       :string
                  :to?         :string
                  :address?    :vector
                  :topics?     :vector
                  :block-hash? :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/logs-changes
   {:permissions [:read]
    :data        :extensions/ethereum-logs-changes
    :arguments   {:id           :string
                  :topics-hints :vector}}
   'ethereum/cancel-filter
   {:permissions [:read]
    :data        :extensions/ethereum-cancel-filter
    :arguments   {:id  :string}}
   'ethereum.ens/resolve
   {:permissions [:read]
    :data        :extensions/ethereum-resolve-ens
    :arguments   {:name        :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc20/total-supply
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-total-supply
    :arguments   {:contract     :string
                  :on-success   :event
                  :on-failure?  :event}}
   'ethereum.erc20/balance-of
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-balance-of
    :arguments   {:contract     :string
                  :token-owner  :string
                  :on-success   :event
                  :on-failure?  :event}}
   'ethereum.erc20/transfer
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-transfer
    :arguments   {:contract    :string
                  :to          :string
                  :value       :number
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc20/transfer-from
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-transfer-from
    :arguments   {:contract    :string
                  :from        :string
                  :to          :string
                  :value       :number
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc20/approve
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-approve
    :arguments   {:contract    :string
                  :spender     :string
                  :value       :number
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc20/allowance
   {:permissions [:read]
    :data        :extensions/ethereum-erc20-allowance
    :arguments   {:contract     :string
                  :token-owner  :string
                  :spender      :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc721/owner-of
   {:permissions [:read]
    :data        :extensions/ethereum-erc721-owner-of
    :arguments   {:contract    :string
                  :token-id    :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc721/is-approved-for-all
   {:permissions [:read]
    :data        :extensions/ethereum-erc721-is-approved-for-all
    :arguments   {:contract    :string
                  :owner       :string
                  :operator    :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc721/get-approved
   {:permissions [:read]
    :data        :extensions/ethereum-erc721-get-approved
    :arguments   {:contract    :string
                  :token-id    :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc721/set-approval-for-all
   {:permissions [:read]
    :data        :extensions/ethereum-erc721-set-approval-for-all
    :arguments   {:contract    :string
                  :operator    :string
                  :approved    :boolean
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum.erc721/safe-transfer-from
   {:permissions [:read]
    :data        :extensions/ethereum-erc721-safe-transfer-from
    :arguments   {:contract    :string
                  :from        :string
                  :to          :string
                  :token-id    :string
                  :data?       :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/call
   {:permissions [:read]
    :data        :extensions/ethereum-call
    :arguments   {:to          :string
                  :method      :string
                  :params?     :vector
                  :outputs?    :vector
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/shh_post
   {:permissions [:read]
    :data        :extensions/shh-post
    :arguments   {:from?       :string
                  :to?         :string
                  :topics      :vector
                  :payload     :string
                  :priority    :string
                  :ttl         :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/shh-new-identity
   {:permissions [:read]
    :data        :extensions/shh-new-identity
    :arguments   {:on-success  :event
                  :on-failure? :event}}
   'ethereum/shh-has-identity
   {:permissions [:read]
    :value       :extensions/shh-has-identity
    :arguments   {:address     :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/shh-new-group
   {:permissions [:read]
    :data        :extensions/shh-new-group
    :arguments   {:on-success  :event
                  :on-failure? :event}}
   'ethereum/shh-add-to-group
   {:permissions [:read]
    :data        :extensions/shh-add-to-group
    :arguments   {:address     :string
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/shh_new-filter
   {:permissions [:read]
    :data        :extensions/shh-new-filter
    :arguments   {:to?         :string
                  :topics      :vector
                  :on-success  :event
                  :on-failure? :event}}
   'ethereum/shh-uninstall-filter
   {:permissions [:read]
    :data        :extensions/shh-uninstall-filter
    :arguments   {:id  :string}}
   'ethereum/shh-get-filter-changes
   {:permissions [:read]
    :data        :extensions/shh-get-filter-changes
    :arguments   {:id :string}}
   'ethereum/shh-get-messages
   {:permissions [:read]
    :data        :extensions/shh-get-messages
    :arguments   {:id :string}}
   'profile.settings/close
   {:permissions [:read]
    :data        :extensions/profile-settings-close
    :arguments   {:on-close?   :event
                  :on-failure? :event}}
   'wallet.settings/close
   {:permissions [:read]
    :data        :extensions/wallet-settings-close
    :arguments   {:on-close?   :event
                  :on-failure? :event}}})
(ns status-im.extensions.core
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as string]
            [pluto.reader :as reader]
            [pluto.registry :as registry]
            [pluto.storages :as storages]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.button.view :as button]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))
; TODO add list, links, radio buttons
; wallet/balance
; wallet/tokens
; http/ post, put, delete

(re-frame/reg-fx
 ::alert
 (fn [value] (js/alert value)))

(re-frame/reg-event-fx
 :alert
 (fn [_ [_ {:keys [value]}]]
   {::alert value}))

(re-frame/reg-fx
 ::log
 (fn [value] (js/console.log value)))

(re-frame/reg-event-fx
 :log
 (fn [_ [_ {:keys [value]}]]
   {::log value}))

(re-frame/reg-sub
 :store/get
 (fn [db [_ {:keys [key]}]]
   (get-in db [:extensions-store :collectible key])))

(handlers/register-handler-fx
 :store/put
 (fn [{:keys [db]} [_ {:keys [key value]}]]
   {:db (assoc-in db [:extensions-store :collectible key] value)}))

(defn- append [acc k v]
  (let [o (get acc k)]
    (assoc acc k (conj (if (vector? o) o (vector o)) v))))

(handlers/register-handler-fx
 :store/append
 (fn [{:keys [db]} [_ {:keys [key value]}]]
   {:db (update-in db [:extensions-store :collectible] append key value)}))

(handlers/register-handler-fx
 :store/clear
 (fn [{:keys [db]} [_ {:keys [key]}]]
   {:db (update-in db [:extensions-store :collectible] dissoc key)}))

(defn- json? [res]
  (string/starts-with? (get-in res [:headers "content-type"]) "application/json"))

(re-frame/reg-event-fx
 :http/get
 (fn [_ [_ {:keys [url on-success on-failure timeout]}]]
   {:http-raw-get (merge {:url url
                          :success-event-creator
                          (fn [o]
                            (let [res (if (json? o) (update o :body #(js->clj (js/JSON.parse %) :keywordize-keys true) o))]
                              (on-success res)))}
                         (when on-failure
                           {:failure-event-creator on-failure})
                         (when timeout
                           {:timeout-ms timeout}))}))

(defn button [{:keys [on-click]} label]
  [button/secondary-button {:on-press #(re-frame/dispatch (on-click {}))} label])

(defn input [{:keys [on-change placeholder]}]
  [react/text-input {:on-change-text #(re-frame/dispatch (on-change {})) :placeholder placeholder}])

(defn touchable-opacity [{:keys [on-press]}]
  [react/touchable-opacity {:on-press #(re-frame/dispatch (on-press {}))}])

(defn image [{:keys [uri]}]
  [react/image {:source {:uri uri}}])

(def capacities
  {:components {'view               {:value react/view}
                'text               {:value react/text}
                'touchable-opacity  {:value touchable-opacity :properties {:on-press :event}}
                'image              {:value image :properties {:uri :string}}
                'input              {:value input :properties {:on-change :event :placeholder :string}}
                'button             {:value button :properties {:on-click :event}}
                'nft-token-viewer   {:value transactions/nft-token :properties {:token :string}}
                'transaction-status {:value transactions/transaction-status :properties {:outgoing :string :tx-hash :string}}
                'asset-selector     {:value transactions/choose-nft-asset-suggestion}
                'token-selector     {:value transactions/choose-nft-token-suggestion}}
   :queries    {'store/get {:value :store/get :arguments {:key :string}}
                'wallet/collectibles {:value :get-collectible-token :arguments {:token :string :symbol :string}}}
   :events     {'alert
                {:permissions [:read]
                 :value       :alert
                 :arguments   {:value :string}}
                'log
                {:permissions [:read]
                 :value       :log
                 :arguments   {:value :string}}
                'store/put
                {:permissions [:read]
                 :value       :store/put
                 :arguments   {:key :string :value :string}}
                'store/append
                {:permissions [:read]
                 :value       :store/append
                 :arguments   {:key :string :value :string}}
                'store/clear
                {:permissions [:read]
                 :value       :store/put
                 :arguments   {:key :string}}
                'http/get
                {:permissions [:read]
                 :value       :http/get
                 :arguments   {:url         :string
                               :timeout?    :string
                               :on-success  :event
                               :on-failure? :event}}
                'browser/open {:value  :browser/open :arguments {:url :string}}
                'chat/open {:value  :chat/open :arguments {:url :string}}
                'ethereum/sign
                {:arguments
                 {:account   :string
                  :message   :string
                  :on-result :event}}
                'ethereum/send-raw-transaction
                {:arguments {:data :string}}
                'ethereum/send-transaction
                {:arguments
                 {:from       :string
                  :to         :string
                  :gas?       :string
                  :gas-price? :string
                  :value?     :string
                  :data?      :string
                  :nonce?     :string}}
                'ethereum/new-contract
                {:arguments
                 {:from       :string
                  :gas?       :string
                  :gas-price? :string
                  :value?     :string
                  :data?      :string
                  :nonce?     :string}}
                'ethereum/call
                {:arguments
                 {:from?      :string
                  :to         :string
                  :gas?       :string
                  :gas-price? :string
                  :value?     :string
                  :data?      :string
                  :block      :string}}
                'ethereum/logs
                {:arguments
                 {:from?     :string
                  :to        :string
                  :address   :string
                  :topics    :string
                  :blockhash :string}}}
   :hooks {:commands commands/command-hook}})

(defn read-extension [{:keys [value]}]
  (when (seq value)
    (let [{:keys [content]} (first value)]
      (reader/read content))))

(defn parse [{:keys [data]}]
  (try
    (let [{:keys [errors] :as extension-data} (reader/parse {:capacities capacities} data)]
      (when errors
        (println "Failed to parse status extensions" errors))
      extension-data)
    (catch :default e (println "EXC" e))))

(def uri-prefix "https://get.status.im/extension/")

(defn valid-uri? [s]
  (boolean
   (when s
     (re-matches (re-pattern (str "^" uri-prefix "\\w+@\\w+")) (string/trim s)))))

(defn url->uri [s]
  (when s
    (string/replace s uri-prefix "")))

(defn load-from [url f]
  (when-let [uri (url->uri url)]
    (storages/fetch uri f)))

(fx/defn set-extension-url-from-qr
  [cofx url]
  (fx/merge (assoc-in cofx [:db :extension-url] url)
            (navigation/navigate-back)))

(fx/defn set-input
  [{:keys [db]} input-key value]
  {:db (update db :extensions/manage assoc input-key {:value value})})

(fx/defn fetch [cofx id]
  (get-in cofx [:db :account/account :extensions id]))

(fx/defn edit
  [cofx id]
  (let [{:keys [url]} (fetch cofx id)]
    (fx/merge (set-input cofx :url (str url))
              (navigation/navigate-to-cofx :edit-extension nil))))

(fx/defn add
  [cofx extension-data active?]
  (when-let [extension-key (get-in extension-data ['meta :name])]
    (fx/merge cofx
              #(registry/add extension-data %)
              (when active?
                #(registry/activate extension-key %)))))

(fx/defn install
  [{{:extensions/keys [manage] :account/keys [account] :as db} :db
    random-id-generator :random-id-generator :as cofx}
   extension-data]
  (let [extension-key  (get-in extension-data ['meta :name])
        {:keys [url id]} manage
        extension      {:id      (-> (:value id)
                                     (or (random-id-generator))
                                     (string/replace "-" ""))
                        :name    (str extension-key)
                        :url     (:value url)
                        :active? true}
        new-extensions (assoc (:extensions account) (:id extension) extension)]
    (fx/merge cofx
              {:ui/show-confirmation {:title     (i18n/label :t/success)
                                      :content   (i18n/label :t/extension-installed)
                                      :on-accept #(re-frame/dispatch [:navigate-to-clean :my-profile])
                                      :on-cancel nil}}
              (accounts.update/account-update {:extensions new-extensions} {})
              (add extension-data true))))

(fx/defn toggle-activation
  [cofx id state]
  (let [toggle-fn      (get {true  registry/activate
                             false registry/deactivate}
                            state)
        extensions     (get-in cofx [:db :account/account :extensions])
        new-extensions (assoc-in extensions [id :active?] state)
        extension-key  (get-in extensions [id :name])]
    (fx/merge cofx
              (accounts.update/account-update {:extensions new-extensions} {:success-event nil})
              #(toggle-fn extension-key %))))

(defn load-active-extensions
  [{:keys [db]}]
  (let [extensions (vals (get-in db [:account/account :extensions]))]
    (doseq [{:keys [url active?]} extensions]
      (load-from url #(re-frame/dispatch [:extension/add (-> % read-extension parse :data) active?])))))

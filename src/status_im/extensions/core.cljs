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
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            status-im.extensions.ethereum))

(re-frame/reg-fx
 ::alert
 (fn [value] (js/alert value)))

(handlers/register-handler-fx
 :alert
 (fn [_ [_ {:keys [value]}]]
   {::alert value}))

(re-frame/reg-fx
 ::log
 (fn [value] (js/console.log value)))

(handlers/register-handler-fx
 :log
 (fn [_ [_ {:keys [value]}]]
   {::log value}))

(re-frame/reg-sub
 :store/get
 (fn [db [_ {:keys [key]}]]
   (get-in db [:extensions-store :collectible key])))

(re-frame/reg-sub :extensions/identity
                  (fn [_ [_ {:keys [value]}]] value))

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

(defn- parse-json [o]
  (js->clj (js/JSON.parse o) :keywordize-keys true))

(re-frame/reg-fx
 ::json-parse
 (fn [value on-result] (re-frame/dispatch (on-result {:value (parse-json value)}))))

(handlers/register-handler-fx
 :extensions/json-parse
 (fn [_ [_ {:keys [value]}]]
   {::json-parse value}))

(re-frame/reg-fx
 ::json-stringify
 (fn [value on-result] (re-frame/dispatch (on-result {:value (js/JSON.stringify (clj->js value))}))))

(handlers/register-handler-fx
 :extensions/json-stringify
 (fn [_ [_ {:keys [value]}]]
   {::json-stringify value}))

(re-frame/reg-event-fx
 :http/get
 (fn [_ [_ {:keys [url on-success on-failure timeout]}]]
   {:http-raw-get (merge {:url url
                          :success-event-creator
                          (fn [o]
                            (let [res (if (json? o) (update o :body parse-json o))]
                              (on-success res)))}
                         (when on-failure
                           {:failure-event-creator on-failure})
                         (when timeout
                           {:timeout-ms timeout}))}))

(re-frame/reg-event-fx
 :ipfs/cat
 (fn [_ [_ {:keys [hash on-success on-failure]}]]
   {:http-raw-get (merge {:url (str "https://ipfs.infura.io/ipfs/" hash)
                          :success-event-creator
                          (fn [o]
                            (let [res (if (json? o) (update o :body parse-json o))]
                              (on-success res)))}
                         (when on-failure
                           {:failure-event-creator on-failure})
                         {:timeout-ms 5000})}))

(re-frame/reg-event-fx
 :http/post
 (fn [_ [_ {:keys [url body on-success on-failure timeout]}]]
   {:http-raw-post (merge {:url  url
                           :body body
                           :success-event-creator
                           (fn [o]
                             (let [res (if (json? o) (update o :body parse-json o))]
                               (on-success res)))}
                          (when on-failure
                            {:failure-event-creator on-failure})
                          (when timeout
                            {:timeout-ms timeout}))}))

(defn operation->fn [k]
  (case k
    :plus   +
    :minus  -
    :times  *
    :divide /))

(re-frame/reg-fx
 ::arithmetic
 (fn [{:keys [operation values on-result]}]
   (re-frame/dispatch (on-result {:value (apply (operation->fn operation) values)}))))

(handlers/register-handler-fx
 :extensions/arithmetic
 (fn [_ [_ m]]
   {::arithmetic m}))

(defn button [{:keys [on-click]} label]
  [button/secondary-button {:on-press #(re-frame/dispatch (on-click {}))} label])

(defn input [{:keys [on-change placeholder]}]
  [react/text-input {:placeholder placeholder
                     :style {:width "100%"}
                     :on-change-text #(re-frame/dispatch (on-change {:value %}))}])

(defn touchable-opacity [{:keys [style on-press]} & children]
  (into [react/touchable-opacity (merge {:on-press #(re-frame/dispatch (on-press {}))}
                                        (when style {:style style}))] children))

(defn image [{:keys [uri style]}]
  [react/image (merge {:style (merge {:width 100 :height 100} style)} {:source {:uri uri}})])

(defn link [{:keys [uri]}]
  [react/text
   {:style    {:color                colors/white
               :text-decoration-line :underline}
    :on-press #(re-frame/dispatch [:browser.ui/message-link-pressed uri])}
   uri])

(defn text [o & children]
  (if (map? o)
    [react/text o children]
    (into [react/text {} o] children)))

(defn list [{:keys [data item-view]}]
  [list/flat-list {:data data :key-fn (fn [_ i] (str i)) :render-fn item-view}])

(defn checkbox [{:keys [on-change checked]}]
  [react/view {:style {:background-color colors/white}}
   [checkbox/checkbox {:checked?        checked
                       :style           {:padding 0}
                       :on-value-change #(re-frame/dispatch (on-change {:value %}))}]])

(def capacities
  {:components {'view               {:value react/view}
                'text               {:value text}
                'touchable-opacity  {:value touchable-opacity :properties {:on-press :event}}
                'image              {:value image :properties {:uri :string}}
                'input              {:value input :properties {:on-change :event :placeholder :string}}
                'button             {:value button :properties {:on-click :event}}
                'link               {:value link :properties {:uri :string}}
                ;'list               {:value list :properties {:data :vector :item-view :view}}
                'checkbox           {:value checkbox :properties {:on-change :event :checked :boolean}}
                'nft-token-viewer   {:value transactions/nft-token :properties {:token :string}}
                'transaction-status {:value transactions/transaction-status :properties {:outgoing :string :tx-hash :string}}
                'asset-selector     {:value transactions/choose-nft-asset-suggestion}
                'token-selector     {:value transactions/choose-nft-token-suggestion}}
   :queries    {'identity            {:value :extensions/identity :arguments {:value :map}}
                'store/get           {:value :store/get :arguments {:key :string}}
                'wallet/collectibles {:value :get-collectible-token :arguments {:token :string :symbol :string}}}
   :events     {'alert
                {:permissions [:read]
                 :value       :alert
                 :arguments   {:value :string}}
                'log
                {:permissions [:read]
                 :value       :log
                 :arguments   {:value :string}}
                'arithmetic
                {:permissions [:read]
                 :value       :extensions/arithmetic
                 :arguments   {:values    #{:plus :minus :times :divide}
                               :operation :keyword
                               :on-result :event}}
                'json/parse
                {:permissions [:read]
                 :value       :extensions/json-parse
                 :arguments   {:value     :string
                               :on-result :event}}
                'json/stringify
                {:permissions [:read]
                 :value       :extensions/json-stringify
                 :arguments   {:value     :string
                               :on-result :event}}
                'store/put
                {:permissions [:read]
                 :value       :store/put
                 :arguments   {:key :string :value :map}}
                'store/append
                {:permissions [:read]
                 :value       :store/append
                 :arguments   {:key :string :value :map}}
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
                'http/post
                {:permissions [:read]
                 :value       :http/post
                 :arguments   {:url         :string
                               :body        :string
                               :timeout?    :string
                               :on-success  :event
                               :on-failure? :event}}
                'ipfs/cat
                {:permissions [:read]
                 :value       :ipfs/cat
                 :arguments   {:hash        :string
                               :on-success  :event
                               :on-failure? :event}}
                'ethereum/send-transaction
                {:permissions [:read]
                 :value       :extensions/ethereum-send-transaction
                 :arguments   {:to         :string
                               :gas?       :string
                               :gas-price? :string
                               :value?     :string
                               :method?    :string
                               :params?    :vector
                               :nonce?     :string
                               :on-result? :event}}
                'ethereum/call
                {:permissions [:read]
                 :value       :extensions/ethereum-call
                 :arguments   {:to         :string
                               :method     :string
                               :params?    :vector
                               :outputs?   :vector
                               :on-result? :event}}}
   :hooks      {:commands commands/command-hook}})

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
     (re-matches (re-pattern (str "^" uri-prefix "\\w+@.+")) (string/trim s)))))

(defn url->uri [s]
  (when s
    (string/replace s uri-prefix "")))

(defn load-from [url f]
  (when-let [uri (url->uri url)]
    (storages/fetch uri f)))

(fx/defn set-extension-url-from-qr
  [cofx url]
  (fx/merge (assoc-in cofx [:db :extensions/manage :url] {:value url
                                                          :error false})
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
  (let [extension-key  "extension"
        extension-data (assoc-in extension-data ['meta :name] extension-key)]
    (fx/merge cofx
              #(registry/deactivate extension-key %)
              #(registry/add extension-data %)
              (when active?
                #(registry/activate extension-key %)))))

(fx/defn install
  [{{:extensions/keys [manage] :account/keys [account] :as db} :db
    random-id-generator :random-id-generator :as cofx}
   extension-data]
  (let [extension-key  (get-in extension-data ['meta :name])
        {:keys [url id]} manage
        extension      {:id      (or (:value id) "extension")
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
        new-extensions (assoc-in extensions [id :active?] state)]
    (fx/merge cofx
              (accounts.update/account-update {:extensions new-extensions} {:success-event nil})
              #(toggle-fn id %))))

(fx/defn load
  [_ url]
  {:extensions/load {:extensions [{:url     (string/trim url)
                                   :active? true}]
                     :follow-up  :extensions/stage}})

(fx/defn activate-extensions
  [{{:account/keys [account]} :db}]
  (let [{:keys [extensions dev-mode?]} account]
    (when dev-mode?
      {:extensions/load {:extensions (vals extensions)
                         :follow-up  :extensions/add}})))

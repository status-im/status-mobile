(ns status-im.extensions.core
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as string]
            [pluto.core :as pluto]
            [pluto.storages :as storages]
            [re-frame.core :as re-frame]
            [re-frame.registrar :as registrar]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.settings.views :as settings]
            [status-im.i18n :as i18n]
            [status-im.ipfs.core :as ipfs]
            [status-im.utils.money :as money]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            status-im.extensions.ethereum
            status-im.extensions.camera
            [status-im.extensions.map :as map]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.browser.core :as browser]
            [status-im.utils.platform :as platform]))

(re-frame/reg-fx
 ::identity-event
 (fn [{:keys [cb]}] (cb {})))

(re-frame/reg-event-fx
 :extensions/identity-event
 (fn [_ [_ _ m]]
   {::identity-event m}))

(re-frame/reg-fx
 ::alert
 (fn [value] (js/alert value)))

(handlers/register-handler-fx
 :alert
 (fn [_ [_ _ {:keys [value]}]]
   {::alert value}))

(re-frame/reg-fx
 ::log
 (fn [value] (js/console.log value)))

(handlers/register-handler-fx
 :log
 (fn [_ [_ _ {:keys [value]}]]
   {::log value}))

(re-frame/reg-fx
 ::schedule-start
 (fn [{:keys [interval on-created on-result]}]
   (let [id (js/setInterval #(on-result {}) interval)]
     (on-created {:value id}))))

(handlers/register-handler-fx
 :extensions/schedule-start
 (fn [_ [_ _ m]]
   {::schedule-start m}))

(re-frame/reg-fx
 ::schedule-cancel
 (fn [{:keys [value]}]
   (js/clearInterval value)))

(handlers/register-handler-fx
 :extensions/schedule-cancel
 (fn [_ [_ _ m]]
   {::schedule-cancel m}))

(re-frame/reg-sub
 :extensions/identity
 (fn [_ [_ _ {:keys [value]}]]
   value))

(defn get-token-for [network all-tokens token]
  (if (= token "ETH")
    {:decimals 18
     :address  "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"}
    (tokens/token-for (ethereum/network->chain-keyword network) all-tokens token)))

(re-frame/reg-sub
 :extensions.wallet/balance
 :<- [:wallet/all-tokens]
 :<- [:network]
 :<- [:balance]
 (fn [[all-tokens network balance] [_ _ {token :token}]]
   (let [{:keys [decimals]} (get-token-for network all-tokens token)
         value (or (get balance (keyword token)) (money/bignumber 0))]
     {:value        (money/token->unit value decimals)
      :value-in-wei value})))

(re-frame/reg-sub
 :extensions.wallet/token
 :<- [:wallet/all-tokens]
 :<- [:network]
 (fn [[all-tokens network] [_ _ {token :token amount :amount amount-in-wei :amount-in-wei}]]
   (let [{:keys [decimals] :as m} (get-token-for network all-tokens token)]
     (merge m
            (when amount {:amount-in-wei (money/unit->token amount decimals)})
            (when amount-in-wei {:amount (money/token->unit amount-in-wei decimals)})))))

(defn normalize-token [m]
  (update m :symbol name))

(re-frame/reg-sub
 :extensions.wallet/tokens
 :<- [:wallet/all-tokens]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:network]
 (fn [[all-tokens visible-tokens-symbols network] [_ _ {filter-vector :filter visible :visible}]]
   (let [tokens (map normalize-token (filter #(and (not (:nft? %)) (if visible (contains? visible-tokens-symbols (:symbol %)) true))
                                             (tokens/sorted-tokens-for all-tokens (ethereum/network->chain-keyword network))))]
     (if filter-vector
       (filter #((set filter-vector) (:symbol %)) tokens)
       tokens))))

(re-frame/reg-sub
 :store/get
 (fn [db [_ {id :id} {:keys [key]}]]
   (get-in db [:extensions/store id key])))

(defn- ->contact [{:keys [photo-path address name public-key]}]
  {:photo      photo-path
   :name       name
   :address    (str "0x" address)
   :public-key public-key})

(re-frame/reg-sub
 :extensions.contacts/all
 :<- [:contacts/active]
 (fn [[contacts] _]
   (map #(update % :address ->contact))))

(defn- empty-value? [o]
  (cond
    (seqable? o) (empty? o)
    :else (nil? o)))

(defn put-or-dissoc [db id key value]
  (if (empty-value? value)
    (update-in db [:extensions/store id] dissoc key)
    (assoc-in db [:extensions/store id key] value)))

(handlers/register-handler-fx
 :store/put
 (fn [{:keys [db]} [_ {id :id} {:keys [key value]}]]
   {:db (put-or-dissoc db id key value)}))

(handlers/register-handler-fx
 :store/puts
 (fn [{:keys [db]} [_ {id :id} {:keys [value]}]]
   {:db (reduce #(put-or-dissoc %1 id (:key %2) (:value %2)) db value)}))

(defn- append [acc k v]
  (let [o (get acc k)]
    (assoc acc k (conj (if (vector? o) o (vector o)) v))))

(handlers/register-handler-fx
 :store/append
 (fn [{:keys [db]} [_ {id :id} {:keys [key value]}]]
   {:db (update-in db [:extensions/store id] append key value)}))

(handlers/register-handler-fx
 :store/clear
 (fn [{:keys [db]} [_ {id :id} {:keys [key]}]]
   {:db (update-in db [:extensions/store id] dissoc key)}))

(handlers/register-handler-fx
 :store/clear-all
 (fn [{:keys [db]} [_ {id :id} _]]
   {:db (update db :extensions/store dissoc id)}))

(defn- json? [res]
  (when-let [type (get-in res [:headers "content-type"])]
    (string/starts-with? type "application/json")))

(defn- parse-json [o]
  (when o
    (js->clj (js/JSON.parse o) :keywordize-keys true)))

(re-frame/reg-fx
 ::json-parse
 (fn [{:keys [value on-result]}]
   (on-result {:value (parse-json value)})))

(handlers/register-handler-fx
 :extensions/json-parse
 (fn [_ [_ _ m]]
   {::json-parse m}))

(re-frame/reg-fx
 ::json-stringify
 (fn [value on-result]
   (on-result {:value (js/JSON.stringify (clj->js value))})))

(handlers/register-handler-fx
 :extensions/json-stringify
 (fn [_ [_ _ {:keys [value]}]]
   {::json-stringify value}))

(defn- parse-result [o on-success]
  (let [res (if (json? o) (update o :body parse-json) o)]
    (on-success res)))

(re-frame/reg-event-fx
 :http/get
 (fn [_ [_ _ {:keys [url on-success on-failure timeout]}]]
   {:http-raw-get (merge {:url url
                          :success-event-creator #(parse-result % on-success)}
                         (when on-failure
                           {:failure-event-creator on-failure})
                         (when timeout
                           {:timeout-ms timeout}))}))

(re-frame/reg-event-fx
 :ipfs/cat
 (fn [cofx [_ _ args]]
   (ipfs/cat cofx args)))

(re-frame/reg-event-fx
 :ipfs/add
 (fn [cofx [_ _ args]]
   (ipfs/add cofx args)))

(re-frame/reg-event-fx
 :http/post
 (fn [_ [_ _ {:keys [url body on-success on-failure timeout]}]]
   {:http-raw-post (merge {:url  url
                           :body (clj->js body)
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
   {:db (update-in db [:chats current-chat-id :custom-params] merge params)
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

(defn operation->fn [k]
  (case k
    :plus   +
    :minus  -
    :times  *
    :divide /))

(re-frame/reg-fx
 ::arithmetic
 (fn [{:keys [operation values on-result]}]
   (on-result {:value (apply (operation->fn operation) values)})))

(handlers/register-handler-fx
 :extensions/arithmetic
 (fn [_ [_ _ m]]
   {::arithmetic m}))

(handlers/register-handler-fx
 :extensions/open-url
 (fn [cofx [_ _ {:keys [url]}]]
   (browser/open-url cofx url)))

(defn button [{:keys [on-click enabled disabled] :as m} label]
  [button/secondary-button (merge {:disabled? (or (when (contains? m :enabled) (or (nil? enabled) (false? enabled))) disabled)}
                                  (when on-click {:on-press #(on-click {})})) label])

(defn on-input-change-text [on-change value]
  (on-change {:value value}))

(defn- on-input-change-text-delay [current on-change value delay]
  ;; If an input change handler has been already scheduled cancel it.
  (when-let [id @current]
    (js/clearTimeout id))
  (reset! current (js/setTimeout #(on-input-change-text on-change value) delay)))

(defn input [{:keys [keyboard-type style on-change change-delay placeholder placeholder-text-color selection-color]}]
  [react/text-input (merge {:placeholder placeholder}
                           (when placeholder-text-color {:placeholder-text-color placeholder-text-color})
                           (when selection-color {:selection-color selection-color})
                           (when style {:style style})
                           (when keyboard-type {:keyboard-type keyboard-type})
                           (when on-change
                             {:on-change-text
                              (if change-delay
                                (let [current (atom nil)]
                                  #(on-input-change-text-delay current on-change % change-delay))
                                #(on-input-change-text on-change %))}))])

(defn touchable-opacity [{:keys [style on-press]} & children]
  (into [react/touchable-opacity (merge (when on-press {:on-press #(on-press {})})
                                        (when style {:style style}))] children))

(defn image [{:keys [source uri style]}]
  [react/image (merge {:style (merge {:width 100 :height 100} style)} {:source (if source source {:uri uri})})])

(defn link [{:keys [uri style open-in text]}]
  [react/text (merge {:style
                      {:color                colors/white
                       :text-decoration-line :underline}
                      :on-press (case open-in
                                  :device #(.openURL react/linking uri)
                                  :status #(re-frame/dispatch [:browser.ui/open-in-status-option-selected uri])
                                  #(re-frame/dispatch [:browser.ui/message-link-pressed uri]))}
                     (when style {:style style}))
   (or text uri)])

(defn map-link
  "create a link-view which opens native map/navigation app with marker and label"
  [{:keys [text lat lng style]}]
  (let [uri (cond
              platform/ios? (str "http://maps.apple.com/?q=" (js/encodeURIComponent text) "&ll=" lat "," lng)
              platform/android? (str "geo:0,0?q=" lat "," lng "(" (js/encodeURIComponent text) ")")
              :else (str "http://www.openstreetmap.org/?mlat=" lat "&mlon=" lng))]
    (link {:uri     uri
           :text    text
           :style   style
           :open-in :device})))

(defn list [{:keys [key data item-view]}]
  [list/flat-list {:data data :key-fn (or key (fn [_ i] (str i))) :render-fn item-view}])

(defn checkbox [{:keys [on-change checked]}]
  [react/view {:style {:background-color colors/white}}
   [checkbox/checkbox {:checked?        checked
                       :style           {:padding 0}
                       :on-value-change #(on-change {:value %})}]])

(defn activity-indicator-size [k]
  (condp = k
    :small "small"
    :large "large"
    nil))

(defn activity-indicator [{:keys [animating hides-when-stopped color size]}]
  [react/activity-indicator (merge (when animating {:animating animating})
                                   (when hides-when-stopped {:hidesWhenStopped hides-when-stopped})
                                   (when color {:color color})
                                   (when-let [size' (activity-indicator-size size)] {:size size'}))])

(defn picker [{:keys [style on-change selected enabled data]}]
  [react/picker {:style style :on-change #(on-change {:value %}) :selected selected :enabled enabled :data data}])

(defn- wrap-text-child [o]
  (if (ifn? o) o (str o)))

(defn text [o & children]
  (if (map? o)
    (into [react/text o] (map wrap-text-child children))
    (into [react/text {} o] (map wrap-text-child children))))

(defn- wrap-view-child [child]
  (if (vector? child) child [text {} child]))

(defn abstract-view [type o & children]
  (if (map? o)
    (into [type o] (map wrap-view-child children))
    (into [type {} (wrap-view-child o)] (map wrap-view-child children))))

(defn view [o & children]
  (apply abstract-view react/view o children))

(defn scroll-view [o & children]
  (apply abstract-view react/scroll-view o children))

(defn keyboard-avoiding-view [o & children]
  (apply abstract-view react/keyboard-avoiding-view o children))

(defn icon [{:keys [key] :as o}]
  [icons/icon key o])

(def capacities
  {:components {'view                   {:data view}
                'scroll-view            {:data scroll-view :properties {:keyboard-should-persist-taps :keyword :content-container-style :map}}
                'keyboard-avoiding-view {:data react/keyboard-avoiding-view}
                'text                   {:data text}
                'touchable-opacity      {:data touchable-opacity :properties {:on-press :event}}
                'icon                   {:data icon :properties {:key :keyword :color :any}}
                'image                  {:data image :properties {:uri :string :source :string}}
                'input                  {:data input :properties {:on-change :event :placeholder :string :keyboard-type :keyword :change-delay? :number :placeholder-text-color :any :selection-color :any}}
                'button                 {:data button :properties {:enabled :boolean :disabled :boolean :on-click :event}}
                'link                   {:data link :properties {:uri :string :text? :string :open-in? {:one-of #{:device :status}}}}
                'list                   {:data list :properties {:data :vector :item-view :view :key? :keyword}}
                'checkbox               {:data checkbox :properties {:on-change :event :checked :boolean}}
                'activity-indicator     {:data activity-indicator :properties {:animating :boolean :color :string :size :keyword :hides-when-stopped :boolean}}
                'picker                 {:data picker :properties {:on-change :event :selected :string :enabled :boolean :data :vector}}
                'nft-token-viewer       {:data transactions/nft-token :properties {:token :string}}
                'transaction-status     {:data transactions/transaction-status :properties {:outgoing :string :tx-hash :string}}
                'map                    {:data map/map-webview
                                         :properties {:marker {:lng :number
                                                               :lat :number
                                                               :boundingbox {:lng1 :number
                                                                             :lat1 :number
                                                                             :lng2 :number
                                                                             :lat2 :number}}
                                                      :fly? :boolean
                                                      :interactive? :boolean
                                                      :on-change :event}}
                'map-link               {:data map-link :properties {:text :string :lng :any :lat :any}}}
   :queries    {'identity            {:data :extensions/identity :arguments {:value :map}}
                'store/get           {:data :store/get :arguments {:key :string}}
                'contacts/all        {:data :extensions.contacts/all} ;; :photo :name :address :public-key
                'wallet/collectibles {:data :get-collectible-token :arguments {:token :string :symbol :string}}
                'wallet/balance      {:data :extensions.wallet/balance :arguments {:token :string}}
                'wallet/token        {:data :extensions.wallet/token :arguments {:token :string :amount? :number :amount-in-wei? :number}}
                'wallet/tokens       {:data :extensions.wallet/tokens :arguments {:filter? :vector :visible? :boolean}}}
   :events     {'identity
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
                 :arguments   {:key :string :value :any}}
                'store/puts
                {:permissions [:read]
                 :data        :store/puts
                 :arguments   {:value :vector}}
                'store/append
                {:permissions [:read]
                 :data        :store/append
                 :arguments   {:key :string :value :any}}
                'store/clear
                {:permissions [:read]
                 :data        :store/clear
                 :arguments   {:key :string}}
                'store/clear-all
                {:permissions [:read]
                 :data        :store/clear-all}
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
                 :arguments   {:id :string}}}
   :hooks       {:wallet.settings
                 {:properties
                  {:label     :string
                   :view      :view
                   :on-open?  :event
                   :on-close? :event}}
                 :chat.command
                 {:properties
                  {:description?   :string
                   :scope          #{:personal-chats :public-chats :group-chats}
                   :short-preview? :view
                   :preview?       :view
                   :on-send?       :event
                   :on-receive?    :event
                   :on-send-sync?  :event
                   :parameters?     [{:id           :keyword
                                      :type         {:one-of #{:text :phone :password :number}}
                                      :placeholder  :string
                                      :suggestions? :view}]}}}})

(defn dispatch-events [_ events]
  (doseq [event events]
    (when (vector? event)
      (re-frame/dispatch event))))

(defn resolve-query [[id :as data]]
  (when (registrar/get-handler :sub id)
    (re-frame/subscribe data)))

(defn parse [{:keys [data]} id]
  (try
    (pluto/parse {:capacities capacities
                  :env        {:id id}
                  :event-fn   dispatch-events
                  :query-fn   resolve-query}
                 data)
    (catch :default e {:errors [{:value (str e)}]})))

(defn parse-extension [{:keys [type value]} id]
  (if (= type :success)
    (parse (pluto/read (:content value)) id)
    {:errors [{:type type :value value}]}))

(def uri-prefix "https://get.status.im/extension/")
(def link-prefix "status-im://extension/")

(defn valid-uri? [s]
  (boolean
   (when s
     (let [s' (string/trim s)]
       (or
        (re-matches (re-pattern (str "^" uri-prefix "\\w+@.+")) s')
        (re-matches (re-pattern (str "^" link-prefix "\\w+@.+")) s'))))))

(defn url->uri [s]
  (when s
    (-> s
        (string/replace uri-prefix "")
        (string/replace link-prefix ""))))

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

(fx/defn fetch [cofx ext-key]
  (get-in cofx [:db :account/account :extensions ext-key]))

(fx/defn edit
  [cofx extension-key]
  (let [{:keys [url]} (fetch cofx extension-key)]
    (fx/merge (set-input cofx :url (str url))
              (navigation/navigate-to-cofx :edit-extension nil))))

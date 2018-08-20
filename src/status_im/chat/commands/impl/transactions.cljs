(ns status-im.chat.commands.impl.transactions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions.styles :as transactions-styles]
            [status-im.data-store.requests :as requests-store]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.chat-preview :as chat-preview]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.animation :as animation]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.money :as money]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.ui.screens.wallet.choose-recipient.events :as choose-recipient.events]
            [status-im.models.transactions :as wallet.transactions]
            [status-im.ui.screens.navigation :as navigation]))

;; common `send/request` functionality

(defn- render-asset [selected-event-creator]
  (fn [{:keys [name symbol amount decimals] :as asset}]
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch (selected-event-creator (clojure.core/name symbol)))}
     [react/view transactions-styles/asset-container
      [react/view transactions-styles/asset-main
       [react/image {:source (-> asset :icon :source)
                     :style  transactions-styles/asset-icon}]
       [react/text {:style transactions-styles/asset-symbol} symbol]
       [react/text {:style transactions-styles/asset-name} name]]
      ;;TODO(goranjovic) : temporarily disabled to fix https://github.com/status-im/status-react/issues/4963
      ;;until the resolution of https://github.com/status-im/status-react/issues/4972
      #_[react/text {:style transactions-styles/asset-balance}
         (str (money/internal->formatted amount symbol decimals))]]]))

(def assets-separator [react/view transactions-styles/asset-separator])

(defview choose-asset [selected-event-creator]
  (letsubs [assets [:wallet/visible-assets-with-amount]]
    [react/view
     [list/flat-list {:data                      (filter #(not (:nft? %)) assets)
                      :key-fn                    (comp name :symbol)
                      :render-fn                 (render-asset selected-event-creator)
                      :enableEmptySections       true
                      :separator                 assets-separator
                      :keyboardShouldPersistTaps :always
                      :bounces                   false}]]))

(defn choose-asset-suggestion [selected-event-creator]
  [choose-asset selected-event-creator])

(defn personal-send-request-short-preview
  [label-key {:keys [content]}]
  (let [{:keys [amount asset]} (:params content)]
    [chat-preview/text {}
     (str (i18n/label label-key)
          (i18n/label-number amount)
          " "
          asset)]))

(def personal-send-request-params
  [{:id          :asset
    :type        :text
    :placeholder "Currency"
    ;; Suggestion components should be structured in such way that they will just take
    ;; one argument, event-creator fn used to construct event to fire whenever something
    ;; is selected. 
    :suggestions choose-asset-suggestion}
   {:id          :amount
    :type        :number
    :placeholder "Amount"}])

;;TODO(goranjovic): currently we only allow tokens which are enabled in Manage assets here
;; because balances are only fetched for them. Revisit this decision with regard to battery/network consequences
;; if we were to update all balances.
(defn- allowed-assets [{:account/keys [account] :keys [chain]}]
  (let [chain-keyword  (keyword chain)
        visible-tokens (get-in account [:settings :wallet :visible-tokens chain-keyword])]
    (into {"ETH" 18}
          (comp (filter #(and (not (:nft? %))
                              (contains? visible-tokens (:symbol %))))
                (map (juxt (comp name :symbol) :decimals)))
          (tokens/tokens-for chain-keyword))))

(defn- personal-send-request-validation [{:keys [asset amount]} {:keys [db]}]
  (let [asset-decimals (get (allowed-assets db) asset)]
    (cond

      (not asset-decimals)
      {:title       "Invalid Asset"
       :description (str "Unknown token - " asset)}

      (not amount)
      {:title       "Amount"
       :description "Amount must be specified"}

      :else
      (let [sanitised-str (string/replace amount #"," ".")
            portions      (string/split sanitised-str ".")
            decimals      (count (get portions 1))
            amount        (js/parseFloat sanitised-str)]
        (cond

          (or (js/isNaN amount)
              (> (count portions) 2))
          {:title       "Amount"
           :description "Amount is not valid number"}

          (and decimals (> decimals asset-decimals))
          {:title       "Amount"
           :description (str "Max number of decimals is " asset-decimals)})))))

;; `/send` command

(defview send-status [tx-hash outgoing]
  (letsubs [confirmed? [:transaction-confirmed? tx-hash]
            tx-exists? [:wallet-transaction-exists? tx-hash]]
    [react/touchable-highlight {:on-press #(when tx-exists?
                                             (re-frame/dispatch [:show-transaction-details tx-hash]))}
     [react/view transactions-styles/command-send-status-container
      [vector-icons/icon (if confirmed? :icons/check :icons/dots)
       {:color           colors/blue
        :container-style (transactions-styles/command-send-status-icon outgoing)}]
      [react/view
       [react/text {:style transactions-styles/command-send-status-text}
        (i18n/label (cond
                      confirmed? :status-confirmed
                      tx-exists? :status-pending
                      :else :status-tx-not-found))]]]]))

(defview send-preview
  [{:keys [content timestamp-str outgoing group-chat]}]
  (letsubs [network [:network-name]]
    (let [{{:keys [amount fiat-amount tx-hash asset currency] send-network :network} :params} content
          recipient-name (get-in content [:params :bot-db :public :recipient])
          network-mismatch? (and (seq send-network) (not= network send-network))]
      [react/view transactions-styles/command-send-message-view
       [react/view
        [react/view transactions-styles/command-send-amount-row
         [react/view transactions-styles/command-send-amount
          [react/text {:style transactions-styles/command-send-amount-text
                       :font  :medium}
           amount
           [react/text {:style (transactions-styles/command-amount-currency-separator outgoing)}
            "."]
           [react/text {:style (transactions-styles/command-send-currency-text outgoing)
                        :font  :default}
            asset]]]]
        (when fiat-amount
          [react/view transactions-styles/command-send-fiat-amount
           [react/text {:style transactions-styles/command-send-fiat-amount-text}
            (str "~ " fiat-amount " " (or currency (i18n/label :usd-currency)))]])
        (when (and group-chat
                   recipient-name)
          [react/text {:style transactions-styles/command-send-recipient-text}
           (str
            (i18n/label :send-sending-to)
            " "
            recipient-name)])
        [react/view
         [react/text {:style (transactions-styles/command-send-timestamp outgoing)}
          (str (i18n/label :sent-at) " " timestamp-str)]]
        [send-status tx-hash outgoing]
        (when network-mismatch?
          [react/text send-network])]])))

;; TODO(goranjovic) - update to include tokens in https://github.com/status-im/status-react/issues/3233
(defn- transaction-details [contact symbol]
  (-> contact
      (select-keys [:name :address :whisper-identity])
      (assoc :symbol symbol
             :gas (ethereum/estimate-gas symbol)
             :from-chat? true)))

(defn- inject-network-price-info [{:keys [amount asset] :as parameters} {:keys [db]}]
  (let [{:keys [chain prices]} db
        currency               (-> db
                                   (get-in [:account/account :settings :wallet :currency] :usd)
                                   name
                                   string/upper-case)]
    (assoc parameters
           :network     chain
           ;; TODO(janherich) - shouldn't this be rather computed on the receiver side ?
           :fiat-amount (money/fiat-amount-value amount
                                                 (keyword asset)
                                                 (keyword currency)
                                                 prices)
           :currency    currency)))

(deftype PersonalSendCommand []
  protocol/Command
  (id [_] "send")
  (scope [_] #{:personal-chats})
  (description [_] "Send a payment")
  (parameters [_] personal-send-request-params)
  (validate [_ parameters cofx]
    ;; Only superficial/formatting validation, "real validation" will be performed
    ;; by the wallet, where we yield control in the next step
    (personal-send-request-validation parameters cofx))
  (on-send [_ {:keys [chat-id]} {:keys [db]}]
    (when-let [{:keys [responding-to]} (get-in db [:chats chat-id :input-metadata])]
      {:db            (update-in db [:chats chat-id :requests] dissoc responding-to)
       :data-store/tx [(requests-store/mark-request-as-answered-tx chat-id responding-to)]}))
  (on-receive [_ command-message cofx]
    (when-let [tx-hash (get-in command-message [:content :params :tx-hash])]
      (wallet.transactions/store-chat-transaction-hash tx-hash cofx)))
  (short-preview [_ command-message]
    (personal-send-request-short-preview :command-sending command-message))
  (preview [_ command-message]
    (send-preview command-message))
  protocol/Yielding
  (yield-control [_ {:keys [amount asset]} {:keys [db]}]
    ;; Prefill wallet and navigate there
    (let [recipient-contact     (get-in db [:contacts/contacts (:current-chat-id db)])
          sender-account        (:account/account db)
          chain                 (keyword (:chain db))
          symbol                (keyword asset)
          {:keys [decimals]}    (tokens/asset-for chain symbol)
          {:keys [value error]} (wallet.db/parse-amount amount decimals)]
      {:db (-> db
               (assoc-in [:wallet :send-transaction :amount] (money/formatted->internal value symbol decimals))
               (assoc-in [:wallet :send-transaction :amount-text] amount)
               (assoc-in [:wallet :send-transaction :amount-error] error)
               (choose-recipient.events/fill-request-details
                (transaction-details recipient-contact symbol))
               (update-in [:wallet :send-transaction] dissoc :id :password :wrong-password?)
               (navigation/navigate-to
                (if (:wallet-set-up-passed? sender-account)
                  :wallet-send-transaction-chat
                  :wallet-onboarding-setup)))
       ;; TODO(janherich) - refactor wallet send events, updating gas price
       ;; is generic thing which shouldn't be defined in wallet.send, then
       ;; we can include the utility helper without running into circ-dep problem
       :update-gas-price {:web3          (:web3 db)
                          :success-event :wallet/update-gas-price-success
                          :edit?         false}}))
  protocol/EnhancedParameters
  (enhance-parameters [_ parameters cofx]
    (inject-network-price-info parameters cofx)))

;; `/request` command

(def request-message-icon-scale-delay 600)

(def min-scale 1)
(def max-scale 1.3)

(defn button-animation [val to-value loop? answered?]
  (animation/anim-sequence
   [(animation/anim-delay
     (if (and @loop? (not @answered?))
       request-message-icon-scale-delay
       0))
    (animation/spring val {:toValue         to-value
                           :useNativeDriver true})]))

(defn request-button-animation-logic
  [{:keys [to-value val loop? answered?] :as context}]
  (animation/start
   (button-animation val to-value loop? answered?)
   #(if (and @loop? (not @answered?))
      (let [new-value (if (= to-value min-scale) max-scale min-scale)
            context'  (assoc context :to-value new-value)]
        (request-button-animation-logic context'))
      (animation/start
       (button-animation val min-scale loop? answered?)))))

(defn request-button-label
  "The request button label will be in the form of `request-the-command-name`"
  [command-name]
  (keyword (str "request-" (name command-name))))

(defn request-button [message-id _ on-press-handler]
  (let [scale-anim-val (animation/create-value min-scale)
        answered?      (re-frame/subscribe [:is-request-answered? message-id])
        loop?          (reagent/atom true)
        context        {:to-value  max-scale
                        :val       scale-anim-val
                        :answered? answered?
                        :loop?     loop?}]
    (reagent/create-class
     {:display-name "request-button"
      :component-did-mount
      (if (or (nil? on-press-handler) @answered?) (fn []) #(request-button-animation-logic context))
      :component-will-unmount
      #(reset! loop? false)
      :reagent-render
      (fn [message-id {command-icon :icon :as command} on-press-handler]
        (when command
          [react/touchable-highlight
           {:on-press            on-press-handler
            :style               transactions-styles/command-request-image-touchable
            :accessibility-label (request-button-label (:name command))}
           [react/animated-view {:style (transactions-styles/command-request-image-view command scale-anim-val)}
            (when command-icon
              [react/icon command-icon transactions-styles/command-request-image])]]))})))

(defview request-preview
  [{:keys [message-id content outgoing timestamp timestamp-str group-chat]}]
  (letsubs [id->command         [:get-id->command]
            answered?           [:is-request-answered? message-id]
            status-initialized? [:get :status-module-initialized?]
            network             [:network-name]
            prices              [:prices]]
    (let [{:keys [amount asset fiat-amount currency] request-network :network} (:params content)
          recipient-name    (get-in content [:params :bot-db :public :recipient])
          network-mismatch? (and request-network (not= request-network network))
          command           (get id->command ["send" #{:personal-chats}])
          on-press-handler  (cond
                              network-mismatch?
                              nil
                              (and (not answered?)
                                   status-initialized?)
                              #(re-frame/dispatch [:select-chat-input-command
                                                   command
                                                   [(or asset "ETH") amount]
                                                   {:responding-to message-id}]))]
      [react/view
       [react/touchable-highlight
        {:on-press on-press-handler}
        [react/view (transactions-styles/command-request-message-view outgoing)
         [react/view
          [react/view
           [react/text {:style (transactions-styles/command-request-header-text outgoing)}
            (i18n/label :transaction-request)]]
          [react/view transactions-styles/command-request-row
           [react/text {:style transactions-styles/command-request-amount-text
                        :font  :medium}
            amount
            [react/text {:style (transactions-styles/command-amount-currency-separator outgoing)}
             "."]
            [react/text {:style (transactions-styles/command-request-currency-text outgoing)
                         :font  :default}
             asset]]]
          [react/view transactions-styles/command-request-fiat-amount-row
           [react/text {:style transactions-styles/command-request-fiat-amount-text}
            (str "~ " fiat-amount " " (or currency (i18n/label :usd-currency)))]]
          (when (and group-chat recipient-name)
            [react/text {:style transactions-styles/command-request-recipient-text}
             (str
              (i18n/label :request-requesting-from)
              " "
              recipient-name)])
          (when network-mismatch?
            [react/text {:style transactions-styles/command-request-network-text}
             (str (i18n/label :on) " " request-network)])
          [react/view transactions-styles/command-request-timestamp-row
           [react/text {:style (transactions-styles/command-request-timestamp-text outgoing)}
            (str
             (datetime/timestamp->mini-date timestamp)
             " "
             (i18n/label :at)
             " "
             timestamp-str)]]
          (when-not outgoing
            [react/view
             [react/view transactions-styles/command-request-separator-line]
             [react/view transactions-styles/command-request-button
              [react/text {:style    (transactions-styles/command-request-button-text answered?)
                           :on-press on-press-handler}
               (i18n/label (if answered? :command-button-sent :command-button-send))]]])]]]])))

(deftype PersonalRequestCommand []
  protocol/Command
  (id [_] "request")
  (scope [_] #{:personal-chats})
  (description [_] "Send a payment")
  (parameters [_] personal-send-request-params)
  (validate [_ parameters cofx]
    (personal-send-request-validation parameters cofx))
  (on-send [_ _ _])
  (on-receive [_ {:keys [message-id chat-id]} {:keys [db]}]
    (let [request {:chat-id    chat-id
                   :message-id message-id
                   :response   "send"
                   :status     "open"}]
      {:db            (assoc-in db [:chats chat-id :requests message-id] request)
       :data-store/tx [(requests-store/save-request-tx request)]}))
  (short-preview [_ command-message]
    (personal-send-request-short-preview :command-requesting command-message))
  (preview [_ command-message]
    (request-preview command-message))
  protocol/EnhancedParameters
  (enhance-parameters [_ parameters cofx]
    (inject-network-price-info parameters cofx)))

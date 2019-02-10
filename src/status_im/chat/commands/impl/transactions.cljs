(ns status-im.chat.commands.impl.transactions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions.styles :as transactions-styles]
            [status-im.data-store.messages :as messages-store]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.chat-preview :as chat-preview]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.ui.screens.wallet.choose-recipient.events :as choose-recipient.events]
            [status-im.ui.screens.currency-settings.subs :as currency-settings.subs]
            [status-im.models.transactions :as wallet.transactions]
            [status-im.ui.screens.navigation :as navigation]
            status-im.chat.commands.impl.transactions.subs
            [status-im.ui.screens.wallet.utils :as wallet.utils]))

;; common `send/request` functionality

(defn- render-asset [{:keys [name symbol amount decimals] :as asset}]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:chat.ui/set-command-parameter (wallet.utils/display-symbol asset)])}
   [react/view transactions-styles/asset-container
    [react/view transactions-styles/asset-main
     [react/image {:source (-> asset :icon :source)
                   :style  transactions-styles/asset-icon}]
     [react/text {:style transactions-styles/asset-symbol}
      (wallet.utils/display-symbol asset)]
     [react/text {:style transactions-styles/asset-name} name]]
    ;;TODO(goranjovic) : temporarily disabled to fix https://github.com/status-im/status-react/issues/4963
    ;;until the resolution of https://github.com/status-im/status-react/issues/4972
    #_[react/text {:style transactions-styles/asset-balance}
       (str (money/internal->formatted amount symbol decimals))]]])

(defn- render-nft-asset [{:keys [name symbol amount] :as asset}]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:chat.ui/set-command-parameter (clojure.core/name symbol)])}
   [react/view transactions-styles/asset-container
    [react/view transactions-styles/asset-main
     [react/image {:source (-> asset :icon :source)
                   :style  transactions-styles/asset-icon}]
     [react/text {:style transactions-styles/asset-symbol} name]]
    [react/text {:style {:font-size     16
                         :color         colors/gray
                         :padding-right 14}}
     (money/to-fixed amount)]]])

(def assets-separator [react/view transactions-styles/asset-separator])

(defview choose-asset [nft?]
  (letsubs [assets [:wallet/visible-assets-with-amount]]
    [react/view
     [list/flat-list {:data                      (filter #(if nft?
                                                            (:nft? %)
                                                            (not (:nft? %)))
                                                         assets)
                      :key-fn                    (comp name :symbol)
                      :render-fn                 (if nft?
                                                   render-nft-asset
                                                   render-asset)
                      :enableEmptySections       true
                      :separator                 assets-separator
                      :keyboardShouldPersistTaps :always
                      :bounces                   false}]]))

(defn choose-asset-suggestion []
  [choose-asset false])

(defn personal-send-request-short-preview
  [label-key {:keys [content]}]
  (let [{:keys [amount coin]} (:params content)]
    [chat-preview/text {}
     (i18n/label label-key {:amount (i18n/label-number amount)
                            :asset  (wallet.utils/display-symbol coin)})]))

(def personal-send-request-params
  [{:id          :asset
    :type        :text
    :placeholder (i18n/label :t/send-request-currency)
    :suggestions choose-asset-suggestion}
   {:id          :amount
    :type        :number
    :placeholder (i18n/label :t/send-request-amount)}])

(defview choose-nft-token []
  (letsubs [{:keys [input-params]} [:chats/selected-chat-command]
            collectibles           [:collectibles]]
    (let [collectible-tokens (get collectibles (keyword (:symbol input-params)))]
      [react/view {:flex-direction   :row
                   :align-items      :center
                   :padding-vertical 11}
       (map
        (fn [[id {:keys [name image_url]}]]
          [react/touchable-highlight
           {:key      id
            :on-press #(re-frame/dispatch [:chat.ui/set-command-parameter (str id)])}
           [react/view {:flex-direction  :column
                        :align-items     :center
                        :margin-left     10
                        :border-radius   2
                        :border-width    1
                        :border-color    colors/gray}
            [svgimage/svgimage {:style  {:width        100
                                         :height       100
                                         :margin-left  20
                                         :margin-right 20}
                                :source {:uri image_url}}]
            [react/text {} name]]])
        collectible-tokens)])))

(defview nft-token [{{:keys [name image_url]} :token}]
  [react/view {:flex-direction :column
               :align-items    :center}
   [svgimage/svgimage {:style  {:width  100
                                :height 100}
                       :source {:uri image_url}}]
   [react/text {} name]])

;;TODO(goranjovic): currently we only allow tokens which are enabled in Manage assets here
;; because balances are only fetched for them. Revisit this decision with regard to battery/network consequences
;; if we were to update all balances.
(defn- allowed-assets [{:account/keys [account] :keys [chain] :as db}]
  (let [all-tokens                               (:wallet/all-tokens db)
        chain-keyword                            (keyword chain)
        {:keys [symbol symbol-display decimals]} (tokens/native-currency chain-keyword)
        visible-tokens                           (get-in account [:settings :wallet :visible-tokens chain-keyword])]
    (into {(name (or symbol-display symbol)) decimals}
          (comp (filter #(and (not (:nft? %))
                              (contains? visible-tokens (:symbol %))))
                (map (juxt (comp name :symbol) :decimals)))
          (tokens/tokens-for all-tokens chain-keyword))))

(defn- personal-send-request-validation [{:keys [asset amount]} {:keys [db]}]
  (let [asset-decimals (get (allowed-assets db) asset)]
    (cond

      (not asset-decimals)
      {:title       (i18n/label :t/send-request-invalid-asset)
       :description (i18n/label :t/send-request-unknown-token {:asset asset})}

      (not amount)
      {:title       (i18n/label :t/send-request-amount)
       :description (i18n/label :t/send-request-amount-must-be-specified)}

      :else
      (let [sanitised-str (string/replace amount #"," ".")
            portions      (string/split sanitised-str ".")
            decimals      (count (get portions 1))
            amount-string (str amount)
            amount        (js/Number sanitised-str)]
        (cond

          (or (js/isNaN amount)
              (> (count portions) 2)
              (re-matches #".+(\.|,)$" amount-string))
          {:title       (i18n/label :t/send-request-amount)
           :description (i18n/label :t/send-request-amount-invalid-number)}

          (and decimals (> decimals asset-decimals))
          {:title       (i18n/label :t/send-request-amount)
           :description (i18n/label :t/send-request-amount-max-decimals
                                    {:asset-decimals asset-decimals})})))))

;; `/send` command

(defview send-status [tx-hash outgoing]
  (letsubs [confirmed? [:chats/transaction-confirmed? tx-hash]
            tx-exists? [:chats/wallet-transaction-exists? tx-hash]]
    [react/touchable-highlight {:on-press #(when tx-exists?
                                             (re-frame/dispatch [:show-transaction-details tx-hash]))}
     [react/view transactions-styles/command-send-status-container
      [vector-icons/icon (if confirmed? :main-icons/check :main-icons/more)
       {:color           (if outgoing colors/blue-light colors/blue)
        :container-style (transactions-styles/command-send-status-icon outgoing)}]
      [react/view
       [react/text {:style (transactions-styles/command-send-status-text outgoing)}
        (i18n/label (cond
                      confirmed? :status-confirmed
                      tx-exists? :status-pending
                      :else :status-tx-not-found))]]]]))

(defn transaction-status [{:keys [tx-hash outgoing]}]
  [send-status tx-hash outgoing])

(defview send-preview
  [{:keys [content timestamp-str outgoing group-chat]}]
  (letsubs [network    [:network-name]
            all-tokens [:wallet/all-tokens]]
    (let [{{:keys [amount fiat-amount tx-hash asset currency] send-network :network} :params} content
          recipient-name (get-in content [:params :bot-db :public :recipient])
          network-mismatch? (and (seq send-network) (not= network send-network))
          token             (tokens/asset-for all-tokens (keyword send-network) (keyword asset))]
      [react/view transactions-styles/command-send-message-view
       [react/view
        [react/view transactions-styles/command-send-amount-row
         [react/view transactions-styles/command-send-amount
          [react/text {:style (transactions-styles/command-send-amount-text outgoing)
                       :font  :medium}
           amount
           [react/text {:style (transactions-styles/command-amount-currency-separator outgoing)}
            "."]
           [react/text {:style (transactions-styles/command-send-currency-text outgoing)
                        :font  :default}
            (wallet.utils/display-symbol token)]]]]
        (when (and fiat-amount
                   platform/mobile?
                   ;;NOTE(goranjovic) - have to hide cross network asset fiat value until we can support
                   ;; multiple chain prices simultaneously
                   (not network-mismatch?))
          [react/view transactions-styles/command-send-fiat-amount
           [react/text {:style (transactions-styles/command-send-fiat-amount-text outgoing)}
            (str "~ " fiat-amount " " (or currency (i18n/label :usd-currency)))]])
        (when (and group-chat
                   recipient-name)
          [react/text {:style transactions-styles/command-send-recipient-text}
           (i18n/label :send-sending-to {:recipient-name recipient-name})])
        [react/view
         [react/text {:style (transactions-styles/command-send-timestamp outgoing)}
          (str (i18n/label :sent-at) " " timestamp-str)]]
        (when platform/mobile?
          [send-status tx-hash outgoing])
        (when network-mismatch?
          [react/text send-network])]])))

;; TODO(goranjovic) - update to include tokens in https://github.com/status-im/status-react/issues/3233
(defn- transaction-details [contact symbol]
  (-> contact
      (select-keys [:name :address :public-key])
      (assoc :symbol symbol
             :gas (ethereum/estimate-gas symbol)
             :from-chat? true)))

(defn- inject-network-info [parameters {:keys [db]}]
  (assoc parameters :network (:chain db)))

(defn- inject-coin-info [{:keys [network asset] :as parameters} {:keys [db]}]
  (let [all-tokens (:wallet/all-tokens db)
        coin (when (and network asset)
               (tokens/asset-for all-tokens (keyword network) (keyword asset)))]
    (assoc parameters :coin coin)))

(defn- inject-price-info [{:keys [amount asset] :as parameters} {:keys [db]}]
  (let [currency (-> db
                     currency-settings.subs/get-currency
                     name
                     string/upper-case)]
    (assoc parameters
           :fiat-amount (money/fiat-amount-value (string/replace amount #"," ".")
                                                 (keyword asset)
                                                 (keyword currency)
                                                 (:prices db))
           :currency    currency)))

(defn- params-unchanged? [send-message request-message]
  (and (= (get-in send-message [:content :params :asset])
          (get-in request-message [:content :params :asset]))
       (= (get-in send-message [:content :params :amount])
          (get-in request-message [:content :params :amount]))))

(deftype PersonalSendCommand []
  protocol/Command
  (id [_] "send")
  (scope [_] #{:personal-chats})
  (description [_] (i18n/label :t/send-command-payment))
  (parameters [_] personal-send-request-params)
  (validate [_ parameters cofx]
    ;; Only superficial/formatting validation, "real validation" will be performed
    ;; by the wallet, where we yield control in the next step
    (personal-send-request-validation parameters cofx))
  (on-send [_ {:keys [chat-id] :as send-message} {:keys [db]}]
    (when-let [responding-to (get-in db [:chats chat-id :metadata :responding-to-command])]
      (when-let [request-message (get-in db [:chats chat-id :messages responding-to])]
        (when (params-unchanged? send-message request-message)
          (let [updated-request-message (assoc-in request-message [:content :params :answered?] true)]
            {:db            (assoc-in db [:chats chat-id :messages responding-to] updated-request-message)
             :data-store/tx [(messages-store/save-message-tx updated-request-message)]})))))
  (on-receive [_ command-message cofx])
  (short-preview [_ command-message]
    (personal-send-request-short-preview :command-sending command-message))
  (preview [_ command-message]
    (send-preview command-message))
  protocol/Yielding
  (yield-control [_ {{{amount :amount asset :asset} :params} :content} {:keys [db] :as cofx}]
    ;; Prefill wallet and navigate there
    (let [recipient-contact     (get-in db [:contacts/contacts (:current-chat-id db)])
          sender-account        (:account/account db)
          chain                 (keyword (:chain db))
          symbol-param          (keyword asset)
          all-tokens            (:wallet/all-tokens db)
          {:keys [symbol decimals]} (tokens/asset-for all-tokens chain symbol-param)
          {:keys [value error]}     (wallet.db/parse-amount amount decimals)
          next-view-id              (if (:wallet-set-up-passed? sender-account)
                                      :wallet-send-transaction-modal
                                      :wallet-onboarding-setup)]
      (fx/merge cofx
                {:db (-> db
                         (update-in [:wallet :send-transaction]
                                    assoc
                                    :amount (money/formatted->internal value symbol decimals)
                                    :amount-text amount
                                    :amount-error error)
                         (choose-recipient.events/fill-request-details
                          (transaction-details recipient-contact symbol))
                         (update-in [:wallet :send-transaction]
                                    dissoc :id :password :wrong-password?))
                 ;; TODO(janherich) - refactor wallet send events, updating gas price
                 ;; is generic thing which shouldn't be defined in wallet.send, then
                 ;; we can include the utility helper without running into circ-dep problem
                 :update-gas-price {:web3          (:web3 db)
                                    :success-event :wallet/update-gas-price-success
                                    :edit?         false}}
                (navigation/navigate-to-cofx next-view-id {}))))
  protocol/EnhancedParameters
  (enhance-send-parameters [_ parameters cofx]
    (-> parameters
        (inject-network-info cofx)
        (inject-coin-info cofx)
        (inject-price-info cofx)))
  (enhance-receive-parameters [_ parameters cofx]
    (-> parameters
        (inject-coin-info cofx)
        (inject-price-info cofx))))

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
  (letsubs [id->command         [:chats/id->command]
            network             [:network-name]
            prices              [:prices]]
    (let [{:keys [amount asset fiat-amount currency answered?] request-network :network} (:params content)
          network-mismatch? (and request-network (not= request-network network))
          command           (get id->command ["send" #{:personal-chats}])
          markup            [react/view (transactions-styles/command-request-message-view outgoing)
                             [react/view
                              [react/text {:style (transactions-styles/command-request-header-text outgoing)}
                               (i18n/label :transaction-request)]]
                             [react/view transactions-styles/command-request-row
                              [react/text {:style (transactions-styles/command-request-amount-text outgoing)
                                           :font  :medium}
                               amount
                               [react/text {:style (transactions-styles/command-amount-currency-separator outgoing)}
                                "."]
                               [react/text {:style (transactions-styles/command-request-currency-text outgoing)
                                            :font  :default}
                                asset]]]
                             (when (and platform/mobile?
                                        ;;NOTE(goranjovic) - have to hide cross network asset fiat value until we can support
                                        ;; multiple chain prices simultaneously
                                        (not network-mismatch?))
                               [react/view transactions-styles/command-request-fiat-amount-row
                                [react/text {:style (transactions-styles/command-request-fiat-amount-text outgoing)}
                                 (str "~ " fiat-amount " " (or currency (i18n/label :usd-currency)))]])
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
                             (when (and (not outgoing)
                                        platform/mobile?)
                               [react/view
                                [react/view transactions-styles/command-request-separator-line]
                                [react/view transactions-styles/command-request-button
                                 [react/text {:style (transactions-styles/command-request-button-text answered?)}
                                  (i18n/label (if answered? :command-button-sent :command-button-send))]]])]]
      (if (and (not network-mismatch?)
               (not outgoing)
               (not answered?))
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:chat.ui/select-chat-input-command
                                         command [(or asset "ETH") amount] message-id])}
         markup]
        markup))))

(deftype PersonalRequestCommand []
  protocol/Command
  (id [_] "request")
  (scope [_] #{:personal-chats})
  (description [_] (i18n/label :t/request-command-payment))
  (parameters [_] personal-send-request-params)
  (validate [_ parameters cofx]
    (personal-send-request-validation parameters cofx))
  (on-send [_ _ _])
  (on-receive [_ _ _])
  (short-preview [_ command-message]
    (personal-send-request-short-preview :command-requesting command-message))
  (preview [_ command-message]
    (request-preview command-message))
  protocol/EnhancedParameters
  (enhance-send-parameters [_ parameters cofx]
    (-> parameters
        (inject-network-info cofx)
        (inject-coin-info cofx)
        (inject-price-info cofx)))
  (enhance-receive-parameters [_ parameters cofx]
    (-> parameters
        (inject-coin-info cofx)
        (inject-price-info cofx))))

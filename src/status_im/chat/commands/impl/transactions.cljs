(ns status-im.chat.commands.impl.transactions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions.styles :as transactions-styles]
            [status-im.chat.events.requests :as request-events]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.ui.screens.wallet.send.events :as send.events]
            [status-im.ui.screens.wallet.choose-recipient.events :as choose-recipient.events]
            [status-im.ui.screens.navigation :as navigation]))

(defn- render-asset [selected-event-creator]
  (fn [{:keys [name symbol amount decimals] :as asset}]
    [react/touchable-highlight
     {:on-press #(status-im.thread/dispatch (selected-event-creator symbol))}
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

(defn personal-send-request-short-preview
  [{:keys [content]}]
  (let [parameters (:params content)]
    [react/text {}
     (str (i18n/label :command-sending)
          (i18n/label-number (:amount parameters))
          " "
          (:asset parameters))]))

(defview send-status [tx-hash outgoing]
  (letsubs [confirmed? [:transaction-confirmed? tx-hash]
            tx-exists? [:wallet-transaction-exists? tx-hash]]
    [react/touchable-highlight {:on-press #(when tx-exists?
                                             (status-im.thread/dispatch [:show-transaction-details tx-hash]))}
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

(def personal-send-request-params
  [{:id          :asset
    :type        :text
    :placeholder "Currency"
    ;; Suggestion components should be structured in such way that they will just take
    ;; one argument, event-creator fn used to construct event to fire whenever something
    ;; is selected.
    :suggestions choose-asset}
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
            decimals      (get portions 1)
            amount        (js/parseFloat sanitised-str)]
        (cond

          (or (js/isNaN amount)
              (> (count portions) 2))
          {:title       "Amount"
           :description "Amount is not valid number"}

          (and decimals (> decimals asset-decimals))
          {:title       "Amount"
           :description (str "Max number of decimals is " asset-decimals)})))))

;; TODO(goranjovic) - update to include tokens in https://github.com/status-im/status-react/issues/3233
(defn- transaction-details [contact symbol]
  (-> contact
      (select-keys [:name :address :whisper-identity])
      (assoc :symbol symbol
             :gas (ethereum/estimate-gas symbol)
             :from-chat? true)))

;; `/send` command

(deftype PersonalSendCommand []
  protocol/Command
  (id [_]
    "send")
  (scope [_]
    #{:personal-chats})
  (parameters [_]
    personal-send-request-params)
  (validate [_ parameters cofx]
    ;; Only superficial/formatting validation, "real validation" will be performed
    ;; by the wallet, where we yield control in the next step
    (personal-send-request-validation parameters cofx))
  (yield-control [_ parameters {:keys [db]}]
    ;; Prefill wallet and navigate there
    (let [recipient-contact  (get-in db [:contacts/contacts (:current-chat-id db)])
          sender-account     (:account/account db)
          chain              (keyword (:chain db))
          symbol             (-> parameters :asset keyword)
          {:keys [decimals]} (tokens/asset-for chain symbol)]
      (merge {:db (-> db
                      (send.events/set-and-validate-amount-db (:amount parameters) symbol decimals)
                      (choose-recipient.events/fill-request-details
                       (transaction-details recipient-contact symbol))
                      (update-in [:wallet :send-transaction] dissoc :id :password :wrong-password?)
                      (navigation/navigate-to
                       (if (:wallet-set-up-passed? sender-account)
                         :wallet-send-transaction-chat
                         :wallet-onboarding-setup)))}
             (send.events/update-gas-price db false))))
  (on-send [_ _ _ _]
    ;; TODO(janherich) - remove this once periodic updates are implemented
    {:dispatch [:update-transactions]})
  (on-receive [_ _ _]
    ;; TODOD(janherich) - this just copyies the current logic but still seems super weird,
    ;; remove/reconsider once periodic updates are implemented
    {:dispatch       [:update-transactions]
     :dispatch-later [{:ms       constants/command-send-status-update-interval-ms
                       :dispatch [:update-transactions]}]})
  (short-preview [_ command-message _]
    (personal-send-request-short-preview command-message))
  (preview [_ command-message _]
    (send-preview command-message)))

;; `/request` command

(deftype PersonalRequestCommand []
  protocol/Command
  (id [_]
    "request")
  (scope [_]
    #{:personal-chats})
  (parameters [_]
    personal-send-request-params)
  (validate [_ parameters cofx]
    (personal-send-request-validation parameters cofx))
  (yield-control [_ _ _])
  (on-send [_ _ _ _])
  (on-receive [_ command-message cofx]
    (let [{:keys [chat-id message-id]} command-message]
      (request-events/add-request chat-id message-id cofx)))
  (short-preview [_ command-message _]
    (personal-send-request-short-preview command-message))
  (preview [_ command-message _]
    nil))

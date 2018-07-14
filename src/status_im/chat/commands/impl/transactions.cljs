(ns status-im.chat.commands.impl.transactions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.i18n :as i18n]
            [status-im.chat.commands.impl.transactions.styles :as transactions-styles]
            [status-im.chat.styles.message.message :as message-styles]))

(defn- render-asset [selected-event-creator]
  (fn [{:keys [name symbol amount decimals] :as asset}]
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch (selected-event-creator symbol))}
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

(defn send-short-preview
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
                                             (re-frame/dispatch [:show-transaction-details tx-hash]))}
     [react/view message-styles/command-send-status-container
      [vector-icons/icon (if confirmed? :icons/check :icons/dots)
       {:color           colors/blue
        :container-style (message-styles/command-send-status-icon outgoing)}]
      [react/view
       [react/text {:style message-styles/command-send-status-text}
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
      [react/view message-styles/command-send-message-view
       [react/view
        [react/view message-styles/command-send-amount-row
         [react/view message-styles/command-send-amount
          [react/text {:style message-styles/command-send-amount-text
                       :font  :medium}
           amount
           [react/text {:style (message-styles/command-amount-currency-separator outgoing)}
            "."]
           [react/text {:style (message-styles/command-send-currency-text outgoing)
                        :font  :default}
            asset]]]]
        (when fiat-amount
          [react/view message-styles/command-send-fiat-amount
           [react/text {:style message-styles/command-send-fiat-amount-text}
            (str "~ " fiat-amount " " (or currency (i18n/label :usd-currency)))]])
        (when (and group-chat
                   recipient-name)
          [react/text {:style message-styles/command-send-recipient-text}
           (str
            (i18n/label :send-sending-to)
            " "
            recipient-name)])
        [react/view
         [react/text {:style (message-styles/command-send-timestamp outgoing)}
          (str (i18n/label :sent-at) " " timestamp-str)]]
        [send-status tx-hash outgoing]
        (when network-mismatch?
          [react/text send-network])]])))

(deftype PersonalSendCommand []
  protocol/Command
  (id [_]
    :send)
  (scope [_]
    #{:personal-chats})
  (parameters [_]
    [{:id :asset
      :type :text
      :placeholder "Currency"
      ;; Suggestion components should be structured in such way that they will just take
      ;; one argument, event-creator fn used to construct event to fire whenever something
      ;; is selected. 
      :suggestions choose-asset}
     {:id :amount
      :type :number
      :placeholder "Amount"}])
  (validate [_ _ _]
    ;; There is no validation for the `/send` command, as it's fully delegated to the wallet
    nil)
  (yield-control [_ parameters cofx]
    ;; navigate to wallet
    nil)
  (on-send [_ message-id parameters cofx]
    (when-let [tx-hash (get-in cofx [:db :wallet :send-transaction :tx-hash])]
      {:dispatch [:update-transactions]}))
  (on-receive [_ _ _]
    nil)
  (short-preview [_ command-message _]
    (send-short-preview command-message))
  (preview [_ command-message _]
    (send-preview command-message)))

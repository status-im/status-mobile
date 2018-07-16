(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.components :as wallet.components]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.utils :as utils]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]))

(defview sign-panel [message-label spinning?]
  (letsubs [account         [:get-current-account]
            wrong-password? [:wallet.send/wrong-password?]
            signing-phrase  (:signing-phrase @account)
            bottom-value    (animation/create-value -250)
            opacity-value   (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/spinner-container
       ;;NOTE(goranjovic) - android build doesn't seem to react on change in `:animating` property, so
       ;;we have this workaround of just using `when` around the whole element.
       (when spinning?
         [react/activity-indicator {:animating true
                                    :size      :large}])]
      [react/view styles/signing-phrase-container
       [react/text {:style               styles/signing-phrase
                    :accessibility-label :signing-phrase-text}
        signing-phrase]]
      [react/i18n-text {:style styles/signing-phrase-description :key message-label}]
      [react/view styles/password-container
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color components.styles/color-gray4
         :on-change-text         #(status-im.thread/dispatch [:wallet.send/set-password (security/mask-data %)])
         :style                  styles/password
         :accessibility-label    :enter-password-input}]
       (when wrong-password?
         [tooltip/tooltip (i18n/label :t/wrong-password)])]]]))

;; "Cancel" and "Sign Transaction >" buttons, signing with password
(defview signing-buttons [spinning? cancel-handler sign-handler sign-label]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [button/button {:style               components.styles/flex
                     :on-press            cancel-handler
                     :accessibility-label :cancel-button}
      (i18n/label :t/cancel)]
     [button/button {:style               (wallet.styles/button-container sign-enabled?)
                     :on-press            sign-handler
                     :disabled?           (or spinning? (not sign-enabled?))
                     :accessibility-label :sign-transaction-button}
      (i18n/label sign-label)
      [vector-icons/icon :icons/forward {:color :white}]]]))

(defn- sign-enabled? [amount-error to amount modal?]
  (and
   (nil? amount-error)
   (or modal? (not (nil? to)) (not= to "")) ;;NOTE(goranjovic) - contract creation will have empty `to`
   (not (nil? amount))))

;; "Sign Later" and "Sign Transaction >" buttons
(defn- sign-button [amount-error to amount sufficient-funds? sufficient-gas? modal?]
  (let [sign-enabled?           (sign-enabled? amount-error to amount modal?)
        immediate-sign-enabled? (and sign-enabled? sufficient-funds? sufficient-gas?)]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [react/view]
     [button/button {:style               components.styles/flex
                     :disabled?           (not immediate-sign-enabled?)
                     :on-press            #(status-im.thread/dispatch [:wallet.send/set-signing? true])
                     :text-style          {:color :white}
                     :accessibility-label :sign-transaction-button}
      (i18n/label :t/transactions-sign-transaction)
      [vector-icons/icon :icons/forward {:color (if immediate-sign-enabled? :white :gray)}]]]))

(defn return-to-transaction [dapp-transaction?]
  (if dapp-transaction?
    (status-im.thread/dispatch [:navigate-to-modal :wallet-send-transaction-modal])
    (act/default-handler)))

(defn handler [discard? dapp-transaction?]
  (if discard?
    #(status-im.thread/dispatch [:wallet/discard-transaction-navigate-back])
    #(return-to-transaction dapp-transaction?)))

(defn- toolbar [discard? dapp-transaction? action title]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (action (handler discard? dapp-transaction?))]
   [toolbar/content-title {:color :white} title]])

(defview transaction-fee []
  (letsubs [send-transaction            [:wallet.send/transaction]
            unsigned-transaction        [:wallet.send/unsigned-transaction]
            network                     [:get-current-account-network]
            {gas-edit       :gas
             max-fee        :max-fee
             gas-price-edit :gas-price} [:wallet/edit]]
    (let [modal?         (:id send-transaction)
          ;;TODO(goranjovic) - unify unsigned and regular transaction subs
          {:keys [amount symbol] :as transaction} (if modal? unsigned-transaction send-transaction)
          gas            (:value gas-edit)
          gas-price      (:value gas-price-edit)
          {:keys [decimals]} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)]
      [wallet.components/simple-screen {:status-bar-type :modal-wallet}
       [toolbar false modal? act/close-white
        (i18n/label :t/wallet-transaction-fee)]
       [react/view components.styles/flex
        [react/view {:flex-direction :row}

         [react/view styles/gas-container-wrapper
          [wallet.components/cartouche {}
           (i18n/label :t/gas-limit)
           [react/view styles/gas-input-wrapper
            [react/text-input (merge styles/transaction-fee-input
                                     {:on-change-text      #(status-im.thread/dispatch [:wallet.send/edit-value :gas %])
                                      :default-value       gas
                                      :accessibility-label :gas-limit-input})]]]
          (when (:invalid? gas-edit)
            [tooltip/tooltip (i18n/label :t/invalid-number)])]

         [react/view styles/gas-container-wrapper
          [wallet.components/cartouche {}
           (i18n/label :t/gas-price)
           [react/view styles/gas-input-wrapper
            [react/text-input (merge styles/transaction-fee-input
                                     {:on-change-text      #(status-im.thread/dispatch [:wallet.send/edit-value :gas-price %])
                                      :default-value       gas-price
                                      :accessibility-label :gas-price-input})]
            [wallet.components/cartouche-secondary-text
             (i18n/label :t/gwei)]]]
          (when (:invalid? gas-price-edit)
            [tooltip/tooltip (i18n/label (if (= :invalid-number (:invalid? gas-price-edit))
                                           :t/invalid-number
                                           :t/wallet-send-min-wei))])]]

        [react/view styles/transaction-fee-info
         [react/view styles/transaction-fee-info-icon
          [react/text {:style styles/transaction-fee-info-icon-text} "?"]]
         [react/view styles/transaction-fee-info-text-wrapper
          [react/i18n-text {:style styles/advanced-fees-text
                            :key   :wallet-transaction-fee-details}]]]
        [components/separator]
        [react/view styles/transaction-fee-block-wrapper
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/amount)
          [react/view {:accessibility-label :amount-input}
           [wallet.components/cartouche-text-content
            (str (money/to-fixed (money/internal->formatted amount symbol decimals)))
            (name symbol)]]]
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/wallet-transaction-total-fee)
          [react/view {:accessibility-label :total-fee-input}
           [wallet.components/cartouche-text-content
            (str max-fee " " (i18n/label :t/eth))]]]]

        [bottom-buttons/bottom-buttons styles/fee-buttons
         [button/button {:on-press            #(status-im.thread/dispatch [:wallet.send/reset-gas-default])
                         :accessibility-label :reset-to-default-button}
          (i18n/label :t/reset-default)]
         [button/button {:on-press            #(do (status-im.thread/dispatch [:wallet.send/set-gas-details
                                                                       (:value-number gas-edit)
                                                                       (:value-number gas-price-edit)])
                                                   (return-to-transaction modal?))
                         :accessibility-label :done-button
                         :disabled?           (or (:invalid? gas-edit)
                                                  (:invalid? gas-price-edit))}
          (i18n/label :t/done)]]]])))

(defn- advanced-cartouche [{:keys [max-fee gas gas-price]}]
  [react/view
   [wallet.components/cartouche {:on-press  #(do (status-im.thread/dispatch [:wallet.send/clear-gas])
                                                 (status-im.thread/dispatch [:navigate-to-modal :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view {:style               styles/advanced-options-text-wrapper
                 :accessibility-label :transaction-fee-button}
     [react/text {:style styles/advanced-fees-text}
      (str max-fee  " " (i18n/label :t/eth))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? transaction modal? scroll]
  [react/view {:style styles/advanced-wrapper}
   [react/touchable-highlight {:on-press (fn []
                                           (status-im.thread/dispatch [:wallet.send/toggle-advanced (not advanced?)])
                                           (when (and scroll @scroll) (utils/set-timeout #(.scrollToEnd @scroll) 350)))}
    [react/view {:style styles/advanced-button-wrapper}
     [react/view {:style               styles/advanced-button
                  :accessibility-label :advanced-button}
      [react/i18n-text {:style (merge wallet.components.styles/label
                                      styles/advanced-label)
                        :key   :wallet-advanced}]
      [vector-icons/icon (if advanced? :icons/up :icons/down) {:color :white}]]]]
   (when advanced?
     [advanced-cartouche transaction])])

(defn- send-transaction-panel [{:keys [modal? transaction scroll advanced? network]}]
  (let [{:keys [amount amount-text amount-error asset-error signing? to to-name sufficient-funds? sufficient-gas?
                in-progress? from-chat? symbol]} transaction
        {:keys [decimals] :as token} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)
        timeout (atom nil)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [toolbar from-chat? false (if modal? act/close-white act/back-white)
      (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                          #(reset! scroll %)
                          :on-content-size-change       #(when (and (not modal?) scroll @scroll)
                                                           (.scrollToEnd @scroll))}
       [react/view styles/send-transaction-form
        [components/recipient-selector {:disabled? (or from-chat? modal?)
                                        :address   to
                                        :name      to-name}]
        [components/asset-selector {:disabled? (or from-chat? modal?)
                                    :error     asset-error
                                    :type      :send
                                    :symbol    symbol}]
        [components/amount-selector {:disabled?     (or from-chat? modal?)
                                     :error         (or amount-error
                                                        (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds))
                                                        (when-not sufficient-gas? (i18n/label :t/wallet-insufficient-gas)))
                                     :amount        amount
                                     :amount-text   amount-text
                                     :input-options {:max-length     21
                                                     :on-focus       (fn [] (when (and scroll @scroll) (utils/set-timeout #(.scrollToEnd @scroll) 100)))
                                                     :on-change-text #(status-im.thread/dispatch [:wallet.send/set-and-validate-amount % symbol decimals])}} token]
        [advanced-options advanced? transaction modal? scroll]]]
      (if signing?
        [signing-buttons in-progress?
         #(status-im.thread/dispatch (if modal? [:wallet/cancel-signing-modal] [:wallet/discard-transaction]))
         #(status-im.thread/dispatch (if modal? [:wallet/sign-transaction-modal] [:wallet/sign-transaction]))
         :t/transactions-sign-transaction]
        [sign-button amount-error to amount sufficient-funds? sufficient-gas? modal?])
      (when signing?
        [sign-panel :t/signing-phrase-description in-progress?])
      (when in-progress? [react/view styles/processing-view])]]))

(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]
            network     [:get-current-account-network]
            scroll      (atom nil)]
    [send-transaction-panel {:modal? false :transaction transaction :scroll scroll :advanced? advanced?
                             :symbol symbol :network network}]))

(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/unsigned-transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]
            network     [:get-current-account-network]
            scroll      (atom nil)]
    (if transaction
      [send-transaction-panel {:modal? true :transaction transaction :scroll scroll :advanced? advanced?
                               symbol  symbol :network network}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar false false act/close-white
         (i18n/label :t/send-transaction)]
        [react/i18n-text {:style styles/empty-text
                          :key   :unsigned-transaction-expired}]]])))

(defview sign-message-modal []
  (letsubs [{:keys [data in-progress?]} [:wallet.send/unsigned-transaction]]
    [wallet.components/simple-screen {:status-bar-type :modal-wallet}
     [toolbar true false act/close-white
      (i18n/label :t/sign-message)]
     [react/view components.styles/flex
      [react/scroll-view
       [react/view styles/send-transaction-form
        [wallet.components/cartouche {:disabled? true}
         (i18n/label :t/message)
         [components/amount-input
          {:disabled?     true
           :input-options {:multiline true}
           :amount-text   data}
          nil]]]]
      [signing-buttons false
       #(status-im.thread/dispatch [:wallet/discard-transaction-navigate-back])
       #(status-im.thread/dispatch [:wallet/sign-message-modal])
       :t/transactions-sign]
      [sign-panel :t/signing-message-phrase-description false]
      (when in-progress?
        [react/view styles/processing-view])]]))

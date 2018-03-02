(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
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
            [status-im.utils.utils :as utils]))

(defn sign-later-popup
  [from-chat?]
  (utils/show-question
   (i18n/label :t/sign-later-title)
   (i18n/label :t/sign-later-text)
   #(re-frame/dispatch (if from-chat?
                         [:navigate-back]
                         [:wallet/sign-transaction true]))))

(defview sign-panel []
  (letsubs [account [:get-current-account]
            wrong-password? [:wallet.send/wrong-password?]
            signing-phrase (:signing-phrase @account)
            bottom-value (animation/create-value -250)
            opacity-value (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/signing-phrase-container
       [react/text {:style styles/signing-phrase} signing-phrase]]
      [react/text {:style styles/signing-phrase-description} (i18n/label :t/signing-phrase-description)]
      [react/view styles/password-container
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color components.styles/color-gray4
         :on-change-text         #(re-frame/dispatch [:wallet.send/set-password %])
         :style                  styles/password}]]]
     (when wrong-password?
       [tooltip/tooltip (i18n/label :t/wrong-password)])]))

;; "Cancel" and "Sign Transaction >" buttons, signing with password
(defview signing-buttons [cancel-handler sign-handler in-progress?]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [button/button {:style    components.styles/flex
                     :on-press cancel-handler}
      (i18n/label :t/cancel)]
     [button/button {:style    (wallet.styles/button-container sign-enabled?)
                     :on-press sign-handler}
      (i18n/label :t/transactions-sign-transaction)
      [vector-icons/icon :icons/forward {:color :white}]]]))

(defn- sign-enabled? [amount-error to amount]
  (and
   (nil? amount-error)
   (not (nil? to)) (not= to "")
   (not (nil? amount))))

;; "Sign Later" and "Sign Transaction >" buttons
(defn- sign-buttons [amount-error to amount sufficient-funds? sign-later-handler]
  (let [sign-enabled?           (sign-enabled? amount-error to amount)
        immediate-sign-enabled? (and sign-enabled? sufficient-funds?)]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     (when sign-enabled?
       [button/button {:style    components.styles/flex
                       :on-press sign-later-handler}
        (i18n/label :t/transactions-sign-later)])
     [button/button {:style      components.styles/flex
                     :disabled?  (not immediate-sign-enabled?)
                     :on-press   #(re-frame/dispatch [:wallet.send/set-signing? true])
                     :text-style {:color :white}}
      (i18n/label :t/transactions-sign-transaction)
      [vector-icons/icon :icons/forward {:color (if immediate-sign-enabled? :white :gray)}]]]))

(defn handler [discard?]
  (if discard?
    #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
    act/default-handler))

(defn- toolbar [discard? action title]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (action (handler discard?))]
   [toolbar/content-title {:color :white} title]])

(defn- max-fee [gas gas-price]
  (when (and gas gas-price)
    (money/wei->ether (.times gas gas-price))))

(defview ^{:theme :modal-wallet} ^:avoid-keyboard? transaction-fee []
  (letsubs [{:keys [amount symbol] :as transaction} [:wallet.send/transaction]
            edit [:wallet/edit]]
    (let [gas       (or (:gas edit) (:gas transaction))
          gas-price (or (:gas-price edit) (:gas-price transaction))]
      [react/view components.styles/flex
       [toolbar true act/close-white
        (i18n/label :t/wallet-transaction-fee)]
       [react/view components.styles/flex
        [react/view {:flex-direction :row}
         [wallet.components/cartouche {}
          (i18n/label :t/gas-limit)
          [react/text-input (merge styles/transaction-fee-input
                                   {:on-change-text #(re-frame/dispatch [:wallet.send/edit-gas %])
                                    :default-value  (str (money/to-fixed gas))})]]
         [wallet.components/cartouche {}
          (i18n/label :t/gas-price)
          [react/view styles/advanced-options-wrapper
           [react/text-input (merge styles/transaction-fee-input
                                    {:on-change-text #(re-frame/dispatch [:wallet.send/edit-gas-price (money/->wei :gwei %)])
                                     :default-value  (str (money/to-fixed (money/wei-> :gwei gas-price)))})]
           [wallet.components/cartouche-secondary-text
            (i18n/label :t/gwei)]]]]
        [react/view styles/transaction-fee-info
         [react/text {:style styles/advanced-fees-text}
          (i18n/label :t/wallet-transaction-fee-details)]]
        [components/separator]
        [react/view styles/transaction-fee-block-wrapper
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/amount)
          [wallet.components/cartouche-text-content
           (str (money/to-fixed (money/wei->ether amount)))
           (name symbol)]]
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/wallet-transaction-total-fee)
          [wallet.components/cartouche-text-content
           (str (money/to-fixed (max-fee gas gas-price)))
           (i18n/label :t/eth)]]]
        [bottom-buttons/bottom-buttons styles/fee-buttons
         [button/button {:on-press #(re-frame/dispatch [:wallet.send/reset-gas-default])}
          (i18n/label :t/reset-default)]
         [button/button {:on-press #(do (re-frame/dispatch [:wallet.send/set-gas-details gas gas-price]) (act/default-handler))}
          (i18n/label :t/done)]]]])))

(defn- advanced-cartouche [{:keys [gas gas-price]} modal?]
  [react/view styles/advanced-cartouche
   [wallet.components/cartouche {:disabled? modal? :on-press #(do (re-frame/dispatch [:wallet.send/clear-gas])
                                                                  (re-frame/dispatch [:navigate-to-modal :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view styles/advanced-options-text-wrapper
     [react/text {:style styles/advanced-fees-text}
      (str (money/to-fixed (max-fee gas gas-price))  " " (i18n/label :t/eth))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? transaction modal?]
  [react/view {:style styles/advanced-wrapper}
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:wallet.send/toggle-advanced (not advanced?)])}
    [react/view {:style styles/advanced-button-wrapper}
     [react/view {:style styles/advanced-button}
      [react/text {:style (merge wallet.components.styles/label styles/advanced-label)}
       (i18n/label :t/wallet-advanced)]
      [vector-icons/icon (if advanced? :icons/up :icons/down) {:color :white}]]]]
   (when advanced?
     [advanced-cartouche transaction modal?])])

(defn- send-transaction-panel [{:keys [modal? transaction scroll advanced? symbol]}]
  (let [{:keys [amount amount-error signing? to to-name sufficient-funds? in-progress? from-chat?]} transaction]
    [react/view components.styles/flex
     [toolbar from-chat? (if modal? act/close-white act/back-white)
      (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view (merge {:keyboardShouldPersistTaps :always} (when-not modal? {:ref #(reset! scroll %)}))
       [react/view styles/send-transaction-form
        [components/recipient-selector {:disabled? modal?
                                        :address   to
                                        :name      to-name}]
        [components/asset-selector {:disabled? modal?
                                    :type      :send
                                    :symbol    symbol}]
        [components/amount-selector {:disabled?     modal?
                                     :error         (or amount-error
                                                        (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
                                     :input-options {:default-value  (str (money/to-fixed (money/wei->ether amount)))
                                                     :max-length     21
                                                     :on-focus       (fn [] (when @scroll (utils/set-timeout #(.scrollToEnd @scroll) 100)))
                                                     :on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount %])}}]
        [advanced-options advanced? transaction modal?]]]
      (if signing?
        [signing-buttons
         #(re-frame/dispatch (if modal? [:wallet/cancel-signing-modal] [:wallet/discard-transaction]))
         #(re-frame/dispatch (if modal? [:wallet/sign-transaction-modal] [:wallet/sign-transaction]))
         in-progress?]
        [sign-buttons amount-error to amount sufficient-funds? (if modal? (if from-chat?
                                                                            #(sign-later-popup true)
                                                                            #(re-frame/dispatch [:navigate-back]))
                                                                   #(sign-later-popup false))])
      (when signing?
        [sign-panel])
      (when in-progress? [react/view styles/processing-view])]]))

(defview ^{:theme :wallet} ^:avoid-keyboard? send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]
            scroll      (atom nil)]
    [send-transaction-panel {:modal? false :transaction transaction :scroll scroll :advanced? advanced? :symbol symbol}]))

(defview ^{:theme :modal-wallet} ^:avoid-keyboard? send-transaction-modal []
  (letsubs [transaction [:wallet.send/unsigned-transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]]
    (if transaction
      [send-transaction-panel {:modal? true :transaction transaction :advanced? advanced? :symbol symbol}]
      [react/view components.styles/flex
       [toolbar false act/close-white
        (i18n/label :t/send-transaction)]
       [react/text {:style styles/empty-text} (i18n/label :t/unsigned-transaction-expired)]])))

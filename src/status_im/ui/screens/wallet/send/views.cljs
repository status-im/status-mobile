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
            [status-im.utils.utils :as utils]
            [status-im.transport.utils :as transport.utils]))

(defn sign-later-popup
  [from-chat?]
  (utils/show-question
   (i18n/label :t/sign-later-title)
   (i18n/label :t/sign-later-text)
   #(re-frame/dispatch (if from-chat?
                         [:sign-later-from-chat]
                         [:wallet/sign-transaction true]))))

(defview sign-panel [message?]
  (letsubs [account [:get-current-account]
            wrong-password? [:wallet.send/wrong-password?]
            signing-phrase (:signing-phrase @account)
            bottom-value (animation/create-value -250)
            opacity-value (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/signing-phrase-container
       [react/text {:style               styles/signing-phrase
                    :accessibility-label :signing-phrase-text}
        signing-phrase]]
      [react/text {:style styles/signing-phrase-description} (i18n/label (if message?
                                                                           :t/signing-message-phrase-description
                                                                           :t/signing-phrase-description))]
      [react/view styles/password-container
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color components.styles/color-gray4
         :on-change-text         #(re-frame/dispatch [:wallet.send/set-password %])
         :style                  styles/password
         :accessibility-label    :enter-password-input}]
       (when wrong-password?
         [tooltip/tooltip (i18n/label :t/wrong-password)])]]]))

;; "Cancel" and "Sign Transaction >" buttons, signing with password
(defview signing-buttons [cancel-handler sign-handler & [sign-label]]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [button/button {:style               components.styles/flex
                     :on-press            cancel-handler
                     :accessibility-label :cancel-button}
      (i18n/label :t/cancel)]
     [button/button {:style               (wallet.styles/button-container sign-enabled?)
                     :on-press            sign-handler
                     :disabled?           (not sign-enabled?)
                     :accessibility-label :sign-transaction-button}
      (i18n/label (or sign-label :t/transactions-sign-transaction))
      [vector-icons/icon :icons/forward {:color :white}]]]))

(defn- sign-enabled? [amount-error to amount]
  (and
   (nil? amount-error)
   (not (nil? to)) (not= to "")
   (not (nil? amount))))

;; "Sign Later" and "Sign Transaction >" buttons
(defn- sign-buttons [amount-error to amount sufficient-funds? sign-later-handler modal?]
  (let [sign-enabled?           (sign-enabled? amount-error to amount)
        immediate-sign-enabled? (or modal? (and sign-enabled? sufficient-funds?))]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [react/view]
     [button/button {:style               components.styles/flex
                     :disabled?           (not immediate-sign-enabled?)
                     :on-press            #(re-frame/dispatch [:wallet.send/set-signing? true])
                     :text-style          {:color :white}
                     :accessibility-label :sign-transaction-button}
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

(defview transaction-fee []
  (letsubs [{:keys [amount symbol] :as transaction} [:wallet.send/transaction]
            edit [:wallet/edit]]
    (let [gas (or (:gas edit) (:gas transaction))
          gas-price (or (:gas-price edit) (:gas-price transaction))]
      [wallet.components/simple-screen {:status-toolbar-type :modal-wallet}
       [toolbar true act/close-white
        (i18n/label :t/wallet-transaction-fee)]
       [react/view components.styles/flex
        [react/view {:flex-direction :row}
         [wallet.components/cartouche {}
          (i18n/label :t/gas-limit)
          [react/view styles/gas-input-wrapper
           [react/text-input (merge styles/transaction-fee-input
                                    {:on-change-text      #(re-frame/dispatch [:wallet.send/edit-gas %])
                                     :default-value       (str (money/to-fixed gas))
                                     :accessibility-label :gas-limit-input})]]]
         [wallet.components/cartouche {}
          (i18n/label :t/gas-price)
          [react/view styles/gas-input-wrapper
           [react/text-input (merge styles/transaction-fee-input
                                    {:on-change-text      #(re-frame/dispatch [:wallet.send/edit-gas-price (money/->wei :gwei %)])
                                     :default-value       (str (money/to-fixed (money/wei-> :gwei gas-price)))
                                     :accessibility-label :gas-price-input})]
           [wallet.components/cartouche-secondary-text
            (i18n/label :t/gwei)]]]]
        [react/view styles/transaction-fee-info
         [react/view styles/transaction-fee-info-icon
          [react/text {:style styles/transaction-fee-info-icon-text} "?"]]
         [react/view styles/transaction-fee-info-text-wrapper
          [react/text {:style styles/advanced-fees-text}
           (i18n/label :t/wallet-transaction-fee-details)]]]
        [components/separator]
        [react/view styles/transaction-fee-block-wrapper
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/amount)
          [react/view {:accessibility-label :amount-input}
           [wallet.components/cartouche-text-content
            (str (money/to-fixed (money/wei->ether amount)))
            (name symbol)]]]
         [wallet.components/cartouche {:disabled? true}
          (i18n/label :t/wallet-transaction-total-fee)
          [react/view {:accessibility-label :total-fee-input}
           [wallet.components/cartouche-text-content
            (str (money/to-fixed (max-fee gas gas-price)))
            (i18n/label :t/eth)]]]]
        [bottom-buttons/bottom-buttons styles/fee-buttons
         [button/button {:on-press            #(re-frame/dispatch [:wallet.send/reset-gas-default])
                         :accessibility-label :reset-to-default-button}
          (i18n/label :t/reset-default)]
         [button/button {:on-press            #(do (re-frame/dispatch [:wallet.send/set-gas-details gas gas-price]) (act/default-handler))
                         :accessibility-label :done-button}
          (i18n/label :t/done)]]]])))

(defn- advanced-cartouche [{:keys [gas gas-price]} modal?]
  [react/view
   [wallet.components/cartouche {:disabled? modal?
                                 :on-press  #(do (re-frame/dispatch [:wallet.send/clear-gas])
                                                 (re-frame/dispatch [:navigate-to-modal :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view {:style               styles/advanced-options-text-wrapper
                 :accessibility-label :transaction-fee-button}
     [react/text {:style styles/advanced-fees-text}
      (str (money/to-fixed (max-fee gas gas-price)) " " (i18n/label :t/eth))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? transaction modal? scroll]
  [react/view {:style styles/advanced-wrapper}
   [react/touchable-highlight {:on-press (fn []
                                           (re-frame/dispatch [:wallet.send/toggle-advanced (not advanced?)])
                                           (when (and scroll @scroll) (utils/set-timeout #(.scrollToEnd @scroll) 350)))}
    [react/view {:style styles/advanced-button-wrapper}
     [react/view {:style               styles/advanced-button
                  :accessibility-label :advanced-button}
      [react/text {:style (merge wallet.components.styles/label styles/advanced-label)}
       (i18n/label :t/wallet-advanced)]
      [vector-icons/icon (if advanced? :icons/up :icons/down) {:color :white}]]]]
   (when advanced?
     [advanced-cartouche transaction modal?])])

(defn- send-transaction-panel [{:keys [modal? transaction scroll advanced? symbol]}]
  (let [{:keys [amount amount-text amount-error signing? to to-name sufficient-funds? in-progress? from-chat?]} transaction
        timeout (atom nil)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [toolbar from-chat? (if modal? act/close-white act/back-white)
      (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                           #(reset! scroll %)
                          :on-content-size-change        #(when (and (not modal?) scroll @scroll)
                                                            (.scrollToEnd @scroll))}
       [react/view styles/send-transaction-form
        [components/recipient-selector {:disabled? (or from-chat? modal?)
                                        :address   to
                                        :name      to-name}]
        [components/asset-selector {:disabled? (or from-chat? modal?)
                                    :type      :send
                                    :symbol    symbol}]
        [components/amount-selector {:disabled?     (or from-chat? modal?)
                                     :error         (or amount-error
                                                        (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
                                     :amount amount
                                     :amount-text amount-text
                                     :input-options {:max-length     21
                                                     :on-focus       (fn [] (when (and scroll @scroll) (utils/set-timeout #(.scrollToEnd @scroll) 100)))
                                                     :on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount %])}}]
        [advanced-options advanced? transaction modal? scroll]]]
      (if signing?
        [signing-buttons
         #(re-frame/dispatch (if modal? [:wallet/cancel-signing-modal] [:wallet/discard-transaction]))
         #(re-frame/dispatch (if modal? [:wallet/sign-transaction-modal] [:wallet/sign-transaction]))]
        [sign-buttons
         amount-error
         to
         amount
         sufficient-funds?
         (if modal?
           (if from-chat?
             #(sign-later-popup true)
             #(re-frame/dispatch [:navigate-back]))
           #(sign-later-popup false))
         modal?])
      (when signing?
        [sign-panel])
      (when in-progress? [react/view styles/processing-view])]]))

(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            symbol [:wallet.send/symbol]
            advanced? [:wallet.send/advanced?]
            scroll (atom nil)]
    [send-transaction-panel {:modal? false :transaction transaction :scroll scroll :advanced? advanced? :symbol symbol}]))

(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/unsigned-transaction]
            symbol [:wallet.send/symbol]
            advanced? [:wallet.send/advanced?]
            scroll (atom nil)]
    (if transaction
      [send-transaction-panel {:modal? true :transaction transaction :scroll scroll :advanced? advanced? :symbol symbol}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar false act/close-white
         (i18n/label :t/send-transaction)]
        [react/text {:style styles/empty-text} (i18n/label :t/unsigned-transaction-expired)]]])))

(defview sign-message-modal []
  (letsubs [{:keys [data in-progress?]} [:wallet.send/unsigned-transaction]]
    [wallet.components/simple-screen {:status-bar-type :modal-wallet}
     [toolbar true act/close-white
      (i18n/label :t/sign-message)]
     [react/view components.styles/flex
      [react/scroll-view
       [react/view styles/send-transaction-form
        [wallet.components/cartouche {:disabled? true}
         (i18n/label :t/message)
         [components/amount-input {:disabled?     true
                                   :input-options {:multiline     true
                                                   :default-value data}}]]]]
      [signing-buttons
       #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
       #(re-frame/dispatch [:wallet/sign-transaction-modal])
       :t/transactions-sign]
      [sign-panel true]
      (when in-progress?
        [react/view styles/processing-view])]]))

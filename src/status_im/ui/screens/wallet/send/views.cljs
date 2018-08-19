(ns status-im.ui.screens.wallet.send.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
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
            [status-im.ui.screens.wallet.components.styles
             :as
             wallet.components.styles]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.main.views :as wallet.main.views]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- toolbar [modal? title]
  (let [action (if modal? act/close-white act/back-white)]
    [toolbar/toolbar {:style wallet.styles/toolbar}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(act/default-handler)))]
     [toolbar/content-title {:color :white} title]]))

(defn- advanced-cartouche [{:keys [max-fee gas gas-price]}]
  [react/view
   [wallet.components/cartouche {:on-press  #(do (re-frame/dispatch [:wallet.send/clear-gas])
                                                 (re-frame/dispatch [:navigate-to :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view {:style               styles/advanced-options-text-wrapper
                 :accessibility-label :transaction-fee-button}
     [react/text {:style styles/advanced-fees-text}
      (str max-fee  " " (i18n/label :t/eth))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? transaction scroll]
  [react/view {:style styles/advanced-wrapper}
   [react/touchable-highlight {:on-press (fn []
                                           (re-frame/dispatch [:wallet.send/toggle-advanced (not advanced?)])
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

(defn password-button [{:keys [label disabled? on-press]}]
  [button/secondary-button {:style               styles/password-button
                            :on-press            on-press
                            :disabled?           disabled?
                            :accessibility-label :sign-transaction-button}
   (i18n/label label)])

(def cancel-password-event #(re-frame/dispatch [:wallet/cancel-entering-password]))

(defview password-input-drawer [{:keys [transaction sign-handler password-button-label] :as opt}]
  (letsubs [wrong-password? [:wallet.send/wrong-password?]
            signing-phrase  [:wallet/signing-phrase]
            network-status  [:network-status]
            bottom-value    (animation/create-value -250)
            opacity-value   (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    (let [{:keys [in-progress? symbol amount-text]} transaction]
      [react/view {:style (cond-> {:top              0 :bottom 0 :right 0 :left 0 :position :absolute
                                   :flex             1
                                   :justify-content  :flex-end}
                            platform/android?
                            (assoc :background-color "rgba(0, 0, 0, 0.5)"))}
       [react/touchable-highlight {:style {:position :absolute
                                           :top 0
                                           :left 0
                                           :right 0
                                           :bottom 0}
                                   :on-press cancel-password-event}
        [react/view {:flex 1}]]
       [react/animated-view {:style {:position :absolute
                                     :left 0
                                     :right 0
                                     :margin-top 50}}
        [react/animated-view {:style {:border-top-left-radius 8
                                      :border-top-right-radius 8
                                      :background-color colors/white
                                      :opacity opacity-value
                                      :padding-top 5}}
         [react/view {:style {:flex-direction :column
                              :align-items :center
                              :justify-content :center
                              :padding-horizontal 15}}
          [react/view styles/signing-phrase-container
           [react/text {:style               styles/signing-phrase
                        :font                :roboto-mono
                        :number-of-lines     1
                        :accessibility-label :signing-phrase-text}
            signing-phrase]]
          (when amount-text
            [react/text {:style styles/transaction-amount}
             (if (= "0" amount-text)
               (str (i18n/label :t/wallet-sign-contract-transaction))
               (str (i18n/label :t/wallet-send) " " amount-text " " (name symbol)))])
          [react/view {:style                       styles/password-container
                       :important-for-accessibility :no-hide-descendants}
           [react/text-input
            {:auto-focus             true
             :secure-text-entry      true
             :placeholder            (i18n/label :t/enter-password-placeholder)
             :placeholder-text-color components.styles/color-gray4
             :on-change-text         #(re-frame/dispatch [:wallet.send/set-password (security/mask-data %)])
             :style                  styles/password
             :accessibility-label    :enter-password-input
             :auto-capitalize        :none}]]
          [password-button {:label     password-button-label
                            :disabled? (or in-progress?
                                           (= :offline network-status))
                            :on-press  sign-handler}]]]
        (when wrong-password?
          [tooltip/tooltip (i18n/label :t/wrong-password) (styles/password-error-tooltip amount-text)])
        [tooltip/tooltip (i18n/label :t/password-input-drawer-tooltip) styles/emojis-tooltip]
        (when in-progress?
          [react/view styles/spinner-container
           [react/activity-indicator {:animating true
                                      :size      :large}]])]])))

(defview password-input-drawer-screen []
  (letsubs
    [transaction [:wallet.send/transaction]
     {{:keys [password-button-label sign-handler]} :password-drawer} [:get-screen-params]]
    (when (and transaction
               password-button-label
               sign-handler)
      [password-input-drawer {:transaction           transaction
                              :password-button-label password-button-label
                              :sign-handler          sign-handler}])))

(defview send-view-container [{:keys [modal? transaction toolbar-title-label]}
                              current-view]
  (let [{:keys [in-progress?]} transaction]
    [react/view {:flex 1
                 :flex-direction :row}
     [react/view {:flex 1
                  :background-color colors/blue}
      [status-bar/status-bar {:type (if modal? :modal-wallet :wallet)}]
      [toolbar modal? (i18n/label toolbar-title-label)]
      current-view]
     (when in-progress?
       [react/view styles/processing-view])]))

(defn bottom-button [{:keys [disabled? on-press label]}]
  [bottom-buttons/bottom-buttons styles/sign-buttons
   [react/view]
   [button/button {:style               (wallet.styles/button-container disabled?)
                   :on-press            on-press
                   :disabled?           disabled?
                   :accessibility-label :sign-transaction-button}
    (i18n/label label)
    [vector-icons/icon :icons/forward {:color (if disabled? :gray :white)}]]])

(defn valid-transaction? [modal? {:keys [amount-error amount sufficient-funds? sufficient-gas? to]}]
  (and (nil? amount-error)
       (or modal? (not (empty? to))) ;;NOTE(goranjovic) - contract creation will have empty `to`
       (not (nil? amount))
       sufficient-funds?
       sufficient-gas?))

(defn- render-send-transaction-view
  [{:keys [online? modal? transaction scroll advanced? network amount-input network-status]}]
  (let [{:keys [amount amount-text amount-error asset-error to to-name sufficient-funds?
                sufficient-gas? in-progress? from-chat? symbol]} transaction
        {:keys [decimals] :as token} (tokens/asset-for (ethereum/network->chain-keyword network) symbol)]
    [send-view-container {:modal? modal?
                          :transaction transaction
                          :toolbar-title-label :t/send-transaction}
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                          #(reset! scroll %)
                          :on-content-size-change       #(when (and (not modal?) scroll @scroll)
                                                           (.scrollToEnd @scroll))}
       (when-not online?
         [wallet.main.views/snackbar :t/error-cant-send-transaction-offline])
       [react/view styles/send-transaction-form
        [wallet.components/recipient-selector {:disabled? (or from-chat? modal?)
                                               :address   to
                                               :name      to-name
                                               :modal?    modal?}]
        [wallet.components/asset-selector {:disabled? (or from-chat? modal?)
                                           :error     asset-error
                                           :type      :send
                                           :symbol    symbol}]
        [wallet.components/amount-selector {:disabled?     (or from-chat? modal?)
                                            :error         (or amount-error
                                                               (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds))
                                                               (when-not sufficient-gas? (i18n/label :t/wallet-insufficient-gas)))
                                            :amount        amount
                                            :amount-text   amount-text
                                            :input-options {:on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount % symbol decimals])
                                                            :ref            (partial reset! amount-input)}} token]
        [advanced-options advanced? transaction scroll]]]
      [bottom-button {:disabled? (or (not online?)
                                     (not (valid-transaction? modal? transaction)))
                      :on-press  #(re-frame/dispatch
                                   [:wallet.send.ui/sign-button-pressed
                                    {:password-button-label :t/command-button-send
                                     :sign-handler          (fn []
                                                              (re-frame/dispatch
                                                               [:wallet/send-transaction]))}])
                      :label :t/transactions-sign-transaction}]]]))

;; MAIN SEND TRANSACTION VIEW
(defn- send-transaction-view [{:keys [scroll] :as opts}]
  (let [amount-input (atom nil)
        handler      (fn [_]
                       (when (and scroll @scroll @amount-input
                                  (.isFocused @amount-input))
                         (log/debug "Amount field focused, scrolling down")
                         (.scrollToEnd @scroll)))]
    (reagent/create-class
     {:component-will-mount (fn [_]
                              (when platform/android?
                                (.addListener react/keyboard "keyboardDidShow" handler))
                              (when platform/ios?
                                (.addListener react/keyboard "keyboardWillShow" handler)))
      :reagent-render       (fn [opts] (render-send-transaction-view
                                        (assoc opts :amount-input amount-input)))})))

;; SEND TRANSACTION FROM WALLET (CHAT)
(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            advanced?   [:wallet.send/advanced?]
            network     [:get-current-account-network]
            scroll      (atom nil)
            network-status [:network-status]]
    [react/keyboard-avoiding-view {:flex 1}
     [send-transaction-view {:modal? false
                             :transaction transaction
                             :scroll scroll
                             :advanced? advanced?
                             :network network
                             :online? (= :online network-status)}]]))

;; SEND TRANSACTION FROM DAPP
(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/transaction]
            advanced? [:wallet.send/advanced?]
            network [:get-current-account-network]
            scroll (atom nil)
            network-status [:network-status]]
    (if transaction
      [send-transaction-view {:modal? true
                              :transaction transaction
                              :scroll scroll
                              :advanced? advanced?
                              :network network
                              :online? (= :online network-status)}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar true (i18n/label :t/send-transaction)]
        [react/i18n-text {:style styles/empty-text
                          :key   :unsigned-transaction-expired}]]])))

;; SIGN MESSAGE FROM DAPP
(defview sign-message-modal []
  (letsubs [transaction [:wallet.send/transaction]
            network-status [:network-status]]
    [send-view-container {:modal? true
                          :transaction transaction
                          :toolbar-title-label :t/sign-message}
     [react/view components.styles/flex
      [react/scroll-view
       (when (= network-status :offline)
         [wallet.main.views/snackbar :t/error-cant-sign-message-offline])
       [react/view styles/send-transaction-form
        [wallet.components/cartouche {:disabled? true}
         (i18n/label :t/message)
         [wallet.components/amount-input
          {:disabled?     true
           :input-options {:multiline true
                           :height    100}
           :amount-text   (:data transaction)}
          nil]]]]
      [bottom-button
       {:on-press #(re-frame/dispatch
                    [:wallet.send.ui/sign-button-pressed
                     {:password-button-label :t/transactions-sign
                      :sign-handler          (fn []
                                               (re-frame/dispatch
                                                [:wallet/sign-message]))}])
        :label    :t/transactions-sign}]]]))

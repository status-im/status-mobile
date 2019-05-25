(ns status-im.ui.screens.wallet.send.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.wallet.components.styles
             :as
             wallet.components.styles]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.main.views :as wallet.main.views]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.money :as money]
            [status-im.utils.security :as security]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- toolbar [modal? title]
  (let [action (if modal? actions/close-white actions/back-white)]
    [toolbar/toolbar {:transparent? true}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color :white} title]]))

(defn- advanced-cartouche [native-currency {:keys [max-fee gas gas-price]}]
  [react/view
   [wallet.components/cartouche {:on-press #(do (re-frame/dispatch [:wallet.send/clear-gas])
                                                (re-frame/dispatch [:navigate-to :wallet-transaction-fee]))}
    (i18n/label :t/wallet-transaction-fee)
    [react/view {:style               styles/advanced-options-text-wrapper
                 :accessibility-label :transaction-fee-button}
     [react/text {:style styles/advanced-fees-text}
      (str max-fee " " (wallet.utils/display-symbol native-currency))]
     [react/text {:style styles/advanced-fees-details-text}
      (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) (i18n/label :t/gwei))]]]])

(defn- advanced-options [advanced? native-currency transaction scroll]
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
      [vector-icons/icon (if advanced? :main-icons/dropdown-up :main-icons/dropdown) {:color :white}]]]]
   (when advanced?
     [advanced-cartouche native-currency transaction])])

(defview password-input-panel [message-label spinning?]
  (letsubs [account [:account/account]
            wrong-password? [:wallet.send/wrong-password?]
            signing-phrase (:signing-phrase @account)
            bottom-value (animation/create-value -250)
            opacity-value (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     (when wrong-password?
       [tooltip/tooltip (i18n/label :t/wrong-password) styles/password-error-tooltip])
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/spinner-container
       (when spinning?
         [react/activity-indicator {:animating true
                                    :size      :large}])]
      [react/view styles/signing-phrase-container
       [react/text {:accessibility-label :signing-phrase-text}
        signing-phrase]]
      [react/i18n-text {:style styles/signing-phrase-description :key message-label}]
      [react/view {:style                       styles/password-container
                   :important-for-accessibility :no-hide-descendants}
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color colors/gray
         :on-change-text         #(re-frame/dispatch [:wallet.send/set-password (security/mask-data %)])
         :style                  styles/password
         :accessibility-label    :enter-password-input
         :auto-capitalize        :none}]]]]))

;; "Cancel" and "Sign Transaction >" or "Sign >" buttons, signing with password
(defview enter-password-buttons [spinning? cancel-handler sign-handler sign-label]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]
            network-status [:network-status]]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [button/button {:style               components.styles/flex
                     :on-press            cancel-handler
                     :accessibility-label :cancel-button}
      (i18n/label :t/cancel)]
     [button/button {:style               (wallet.styles/button-container sign-enabled?)
                     :on-press            sign-handler
                     :disabled?           (or spinning?
                                              (not sign-enabled?)
                                              (= :offline network-status))
                     :accessibility-label :sign-transaction-button}
      (i18n/label sign-label)
      [vector-icons/icon :main-icons/next {:color colors/white}]]]))

;; "Sign Transaction >" button
(defn- sign-transaction-button [amount-error to amount sufficient-funds? sufficient-gas? modal? online?]
  (let [sign-enabled? (and (nil? amount-error)
                           (or modal? (not (empty? to)))    ;;NOTE(goranjovic) - contract creation will have empty `to`
                           (not (nil? amount))
                           sufficient-funds?
                           sufficient-gas?
                           online?)]
    [bottom-buttons/bottom-buttons
     styles/sign-buttons
     [react/view]
     [button/button {:style               components.styles/flex
                     :disabled?           (not sign-enabled?)
                     :on-press            #(re-frame/dispatch [:wallet.ui/sign-transaction-button-clicked])
                     :text-style          {:color :white}
                     :accessibility-label :sign-transaction-button}
      (i18n/label :t/transactions-sign-transaction)
      [vector-icons/icon :main-icons/next {:color (if sign-enabled? colors/white colors/white-light-transparent)}]]]))

(defn signing-phrase-view [signing-phrase]
  [react/view {:flex-direction :column
               :align-items    :center
               :margin-top     10}
   [react/view (assoc styles/signing-phrase-container :width "90%" :height 40)
    [react/text {:accessibility-label :signing-phrase-text
                 :style               {:padding-vertical 16
                                       :text-align       :center}}
     signing-phrase]]
   [react/text {:style {:color            :white
                        :text-align       :center
                        :font-size        12
                        :padding-vertical 14}}
    (i18n/label :t/signing-phrase-warning)]])

(defn- render-send-transaction-view [{:keys [chain modal? transaction scroll advanced? keycard? signing-phrase all-tokens amount-input network-status]}]
  (let [{:keys [amount amount-text amount-error asset-error show-password-input? to to-name sufficient-funds?
                sufficient-gas? in-progress? from-chat? symbol]} transaction
        native-currency (tokens/native-currency chain)
        {:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)
        online? (= :online network-status)]
    [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                      :status-bar-type (if modal? :modal-wallet :wallet)}
     [toolbar modal? (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                          #(reset! scroll %)
                          :on-content-size-change       #(when (and (not modal?) scroll @scroll)
                                                           (.scrollToEnd @scroll))}
       (when-not online?
         [wallet.main.views/snackbar :t/error-cant-send-transaction-offline])
       [react/view styles/send-transaction-form
        [wallet.components/recipient-selector
         {:disabled? (or from-chat? modal? show-password-input?)
          :address   to
          :name      to-name
          :modal?    modal?}]
        [wallet.components/asset-selector
         {:disabled? (or from-chat? modal? show-password-input?)
          :error     asset-error
          :type      :send
          :symbol    symbol}]
        [wallet.components/amount-selector
         {:disabled?     (or from-chat? modal? show-password-input?)
          :error         (or amount-error
                             (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds))
                             (when-not sufficient-gas? (i18n/label :t/wallet-insufficient-gas)))
          :amount        amount
          :amount-text   amount-text
          :input-options {:on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount % symbol decimals])
                          :ref            (partial reset! amount-input)}} token]
        [advanced-options advanced? native-currency transaction scroll]
        (when keycard?
          [signing-phrase-view signing-phrase])]]
      (if show-password-input?
        [enter-password-buttons in-progress?
         #(re-frame/dispatch [:wallet/cancel-entering-password])
         #(re-frame/dispatch [:wallet/send-transaction])
         :t/transactions-sign-transaction]
        [sign-transaction-button amount-error to amount sufficient-funds? sufficient-gas? modal? online?])
      (when show-password-input?
        [password-input-panel :t/signing-phrase-description in-progress?])
      (when in-progress? [react/view styles/processing-view])]]))

;; MAIN SEND TRANSACTION VIEW
(defn- send-transaction-view [{:keys [scroll] :as opts}]
  (let [amount-input (atom nil)
        handler (fn [_]
                  (when (and scroll @scroll @amount-input
                             (.isFocused @amount-input))
                    (log/debug "Amount field focused, scrolling down")
                    (.scrollToEnd @scroll)))]
    (reagent/create-class
     {:component-will-mount (fn [_]
                              ;;NOTE(goranjovic): keyboardDidShow is for android and keyboardWillShow for ios
                              (.addListener react/keyboard "keyboardDidShow" handler)
                              (.addListener react/keyboard "keyboardWillShow" handler))
      :reagent-render       (fn [opts] (render-send-transaction-view
                                        (assoc opts :amount-input amount-input)))})))

;; SEND TRANSACTION FROM WALLET (CHAT)
(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            advanced? [:wallet.send/advanced?]
            chain [:ethereum/chain-keyword]
            scroll (atom nil)
            network-status [:network-status]
            all-tokens [:wallet/all-tokens]
            signing-phrase [:wallet.send/signing-phrase-with-padding]
            keycard? [:keycard-account?]]
    [send-transaction-view {:modal?         false
                            :transaction    transaction
                            :scroll         scroll
                            :advanced?      advanced?
                            :keycard?       keycard?
                            :signing-phrase signing-phrase
                            :chain          chain
                            :all-tokens     all-tokens
                            :network-status network-status}]))

;; SEND TRANSACTION FROM DAPP
(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/transaction]
            advanced? [:wallet.send/advanced?]
            chain [:ethereum/chain-keyword]
            scroll (atom nil)
            network-status [:network-status]
            all-tokens [:wallet/all-tokens]
            signing-phrase [:wallet.send/signing-phrase-with-padding]
            keycard? [:keycard-account?]]
    (if transaction
      [send-transaction-view {:modal?         true
                              :transaction    transaction
                              :scroll         scroll
                              :advanced?      advanced?
                              :keycard?       keycard?
                              :signing-phrase signing-phrase
                              :chain          chain
                              :all-tokens     all-tokens
                              :network-status network-status}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar true (i18n/label :t/send-transaction)]
        [react/i18n-text {:style styles/empty-text
                          :key   :unsigned-transaction-expired}]]])))

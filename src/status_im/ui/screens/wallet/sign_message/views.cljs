(ns status-im.ui.screens.wallet.sign-message.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.screens.wallet.main.views :as wallet.main.views]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.utils.security :as security]
            [status-im.ui.components.tooltip.views :as tooltip]
            [reagent.core :as reagent]))

(defn- toolbar [modal? title]
  (let [action (if modal? actions/close-white actions/back-white)]
    [toolbar/toolbar {:style wallet.styles/toolbar}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color :white} title]]))

(defview enter-password-buttons [value-atom spinning? cancel-handler sign-handler sign-label]
  (letsubs [network-status [:network-status]]
    (let [password      (:password @value-atom)
          sign-enabled? (and (not (nil? password)) (not= password ""))]
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
        [vector-icons/icon :main-icons/next {:color colors/white}]]])))

(defview password-input-panel [value-atom message-label spinning?]
  (letsubs [account         [:account/account]
            signing-phrase  (:signing-phrase @account)
            bottom-value    (animation/create-value -250)
            opacity-value   (animation/create-value 0)]
    {:component-did-mount #(send.animations/animate-sign-panel opacity-value bottom-value)}
    [react/animated-view {:style (styles/animated-sign-panel bottom-value)}
     (when (:wrong-password? value-atom)
       [tooltip/tooltip (i18n/label :t/wrong-password) styles/password-error-tooltip])
     [react/animated-view {:style (styles/sign-panel opacity-value)}
      [react/view styles/spinner-container
       (when spinning?
         [react/activity-indicator {:animating true
                                    :size      :large}])]
      [react/view styles/signing-phrase-container
       [react/text {:style               styles/signing-phrase
                    :accessibility-label :signing-phrase-text}
        signing-phrase]]
      [react/i18n-text {:style styles/signing-phrase-description :key message-label}]
      [react/view {:style                       styles/password-container
                   :important-for-accessibility :no-hide-descendants}
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color colors/gray
         :on-change-text         #(swap! value-atom assoc :password (security/mask-data %))
         :style                  styles/password
         :accessibility-label    :enter-password-input
         :auto-capitalize        :none}]]]]))

;; SIGN MESSAGE FROM DAPP
(defview sign-message-modal []
  (letsubs [value-atom (reagent/atom nil)
            {:keys [decoded-data in-progress?] :as screen-params} [:get-screen-params :wallet-sign-message-modal]
            network-status [:network-status]]
    [wallet.components/simple-screen {:status-bar-type :modal-wallet}
     [toolbar true (i18n/label :t/sign-message)]
     [react/view components.styles/flex
      [react/scroll-view
       (when (= network-status :offline)
         [wallet.main.views/snackbar :t/error-cant-sign-message-offline])
       [react/view styles/send-transaction-form
        [wallet.components/cartouche {:disabled? true}
         (i18n/label :t/message)
         [components/amount-input
          {:disabled?     true
           :input-options {:multiline true
                           :height    100}
           :amount-text   decoded-data}
          nil]]]]
      [enter-password-buttons value-atom false
       #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
       #(re-frame/dispatch [:wallet/sign-message (merge screen-params @value-atom)])
       :t/transactions-sign]
      [password-input-panel value-atom :t/signing-message-phrase-description false]
      (when in-progress?
        [react/view styles/processing-view])]]))

(ns status-im.ui.screens.wallet.send.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as send.styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn toolbar-view [signing?]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/back-white (if signing?
                                         #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                         act/default-handler))]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

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
    [react/animated-view {:style (send.styles/animated-sign-panel bottom-value)}
     [react/animated-view {:style (send.styles/sign-panel opacity-value)}
      [react/view send.styles/signing-phrase-container
       [react/text {:style send.styles/signing-phrase} signing-phrase]]
      [react/text {:style send.styles/signing-phrase-description} (i18n/label :t/signing-phrase-description)]
      [react/view send.styles/password-container
       [react/text-input
        {:auto-focus             true
         :secure-text-entry      true
         :placeholder            (i18n/label :t/enter-password)
         :placeholder-text-color "#939ba1"
         :on-change-text         #(re-frame/dispatch [:wallet.send/set-password %])
         :style                  send.styles/password}]]]
     (when wrong-password?
       [components/tooltip (i18n/label :t/wrong-password)])]))

;; "Cancel" and "Sign Transaction >" buttons, signing with password
(defview signing-buttons [cancel-handler sign-handler in-progress?]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]]
    [react/view wallet.styles/buttons-container
     [react/touchable-highlight {:style wallet.styles/button :on-press cancel-handler}
      [react/view (wallet.styles/button-container true)
       [components/button-text (i18n/label :t/cancel)]]]
     [react/view components.styles/flex]
     [react/touchable-highlight {:style wallet.styles/button :on-press sign-handler}
      [react/view (wallet.styles/button-container sign-enabled?)
       (when in-progress? [react/activity-indicator {:animating? true}])
       [components/button-text (i18n/label :t/transactions-sign-transaction)]
       [vector-icons/icon :icons/forward {:color :white :container-style wallet.styles/forward-icon-container}]]]]))

(defn sign-enabled? [amount-error to-address amount]
  (and
   (nil? amount-error)
   (not (nil? to-address)) (not= to-address "")
   (not (nil? amount)) (not= amount "")))

;; "Sign Later" and "Sign Transaction >" buttons
(defn- sign-buttons [amount-error to-address amount sufficient-funds? sign-later-handler]
  (let [sign-enabled? (sign-enabled? amount-error to-address amount)
        immediate-sign-enabled? (and sign-enabled? sufficient-funds?)]
    [react/view wallet.styles/buttons-container
     (when sign-enabled?
       [react/touchable-highlight {:style wallet.styles/button :on-press sign-later-handler}
        [react/view (wallet.styles/button-container sign-enabled?)
         [components/button-text (i18n/label :t/transactions-sign-later)]]])
     [react/view components.styles/flex]
     [react/touchable-highlight {:style wallet.styles/button
                                 :on-press (when immediate-sign-enabled? #(re-frame/dispatch [:wallet.send/set-signing? true]))}
      [react/view (wallet.styles/button-container immediate-sign-enabled?)
       [components/button-text (i18n/label :t/transactions-sign-transaction)]
       [vector-icons/icon :icons/forward {:color :white :container-style wallet.styles/forward-icon-container}]]]]))

(defn request-camera-permissions []
  (when platform/android?
    (re-frame/dispatch [:request-permissions [:camera]]))
  (camera/request-access
   (fn [permitted?]
     (re-frame/dispatch [:set-in [:wallet :send-transaction :camera-permitted?] permitted?])
     (re-frame/dispatch [:navigate-to :choose-recipient]))))

(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            scroll      (atom nil)]
    (let [{:keys [amount amount-error signing? to-address to-name in-progress? sufficient-funds?]} transaction]
      [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :wallet}]
        [toolbar-view signing?]
        [common/network-info {:text-color :white}]
        [react/scroll-view {:keyboardShouldPersistTaps :always
                            :ref #(reset! scroll %)}
         [react/view components.styles/flex
          [react/view wallet.styles/choose-participant-container
           [components/choose-recipient {:address  to-address
                                         :name     to-name
                                         :on-press request-camera-permissions}]]
          [react/view wallet.styles/choose-wallet-container
           [components/choose-wallet]]
          [react/view wallet.styles/amount-container
           [components/amount-input
            {:error         (or amount-error
                                (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
             :input-options {:auto-focus     true
                             :on-focus       (fn [] (when @scroll (js/setTimeout #(.scrollToEnd @scroll) 100)))
                             :default-value  amount
                             :on-change-text #(re-frame/dispatch [:wallet/set-and-validate-amount %])}}]
           [react/view wallet.styles/choose-currency-container
            [components/choose-currency wallet.styles/choose-currency]]]]]
        [components/separator]
        (if signing?
          [signing-buttons
           #(re-frame/dispatch [:wallet/discard-transaction])
           #(re-frame/dispatch [:wallet/sign-transaction])
           in-progress?]
          [sign-buttons amount-error to-address amount sufficient-funds? #(sign-later-popup false)])
        (when signing?
          [sign-panel])]
       (when in-progress? [react/view send.styles/processing-view])])))

(defn toolbar-modal [from-chat?]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/close-white (if from-chat?
                                          #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                          act/default-handler))]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/unsigned-transaction]]
    (if transaction
      (let [{:keys [amount amount-error signing? to to-name sufficient-funds? in-progress? from-chat?]} transaction]
        [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
         [react/view components.styles/flex
          [status-bar/status-bar {:type :modal-wallet}]
          [toolbar-modal from-chat?]
          [common/network-info {:text-color :white}]
          [react/scroll-view {:keyboardShouldPersistTaps :always}
           [react/view components.styles/flex
            [react/view wallet.styles/choose-participant-container
             [components/choose-recipient-disabled {:address  to
                                                    :name     to-name}]]
            [react/view wallet.styles/choose-wallet-container
             [components/choose-wallet]]
            [react/view wallet.styles/amount-container
             [components/amount-input
              {:error (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds))
               :disabled? true
               :input-options {:default-value amount}}]
             [react/view wallet.styles/choose-currency-container
              [components/choose-currency wallet.styles/choose-currency]]]]]
          [components/separator]
          (if signing?
            [signing-buttons
             #(re-frame/dispatch [:wallet/cancel-signing-modal])
             #(re-frame/dispatch [:wallet/sign-transaction-modal])
             in-progress?]
            [sign-buttons amount-error to amount sufficient-funds? (if from-chat?
                                                                     #(sign-later-popup true)
                                                                     #(re-frame/dispatch [:navigate-back]))])
          (when signing?
            [sign-panel])
          (when in-progress? [react/view send.styles/processing-view])]])
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar-modal false]
        [react/text {:style send.styles/empty-text} (i18n/label :t/unsigned-transaction-expired)]]])))

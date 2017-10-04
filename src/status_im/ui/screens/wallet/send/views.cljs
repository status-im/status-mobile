(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.components.animation :as animation]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.components.react :as react]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.styles :as components.styles]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.camera :as camera]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.request.styles :as request.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as send.styles]))

(defn toolbar-view [signing?]
  [toolbar/toolbar2 {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/back-white (if signing?
                                         #(do (re-frame/dispatch [:wallet/discard-transaction])
                                              (act/default-handler))
                                         act/default-handler))]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

(defn sign-later []
  (utils/show-question
    (i18n/label :t/sign-later-title)
    (i18n/label :t/sign-later-text)
    #(re-frame/dispatch [:wallet/sign-transaction true])))

(defview sign-panel []
  (letsubs [account [:get-current-account]
            wrong-password? [:get-in [:wallet/send-transaction :wrong-password?]]
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
          :on-change-text         #(re-frame/dispatch [:set-in [:wallet/send-transaction :password] %])
          :style                  send.styles/password}]]]
     (when wrong-password?
       [components/tooltip (i18n/label :t/wrong-password)])]))


;; "Cancel" and "Sign Transaction >" buttons, signing with password
(defview signing-buttons [cancel-handler sign-handler]
  (letsubs [sign-enabled? [:wallet.send/sign-password-enabled?]]
    [react/view wallet.styles/buttons-container
     [react/touchable-highlight {:on-press cancel-handler}
      [react/view (wallet.styles/button-container true)
       [components/button-text (i18n/label :t/cancel)]]]
     [react/view components.styles/flex]
     [react/touchable-highlight {:on-press sign-handler}
      [react/view (wallet.styles/button-container sign-enabled?)
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
       [react/touchable-highlight {:on-press sign-later-handler}
        [react/view (wallet.styles/button-container sign-enabled?)
         [components/button-text (i18n/label :t/transactions-sign-later)]]])
     [react/view components.styles/flex]
     [react/touchable-highlight {:on-press (when immediate-sign-enabled? #(re-frame/dispatch [:set-in [:wallet/send-transaction :signing?] true]))}
      [react/view (wallet.styles/button-container immediate-sign-enabled?)
       [components/button-text (i18n/label :t/transactions-sign-transaction)]
       [vector-icons/icon :icons/forward {:color :white :container-style wallet.styles/forward-icon-container}]]]]))

(defn- sufficient-funds? [amount balance]
  (<= amount (money/wei->ether balance)))

(defview send-transaction []
  (letsubs [balance      [:balance]
            amount       [:get-in [:wallet/send-transaction :amount]]
            amount-error [:get-in [:wallet/send-transaction :amount-error]]
            signing?     [:get-in [:wallet/send-transaction :signing?]]
            to-address   [:get-in [:wallet/send-transaction :to-address]]
            to-name      [:get-in [:wallet/send-transaction :to-name]]]
    (let [sufficient-funds? (sufficient-funds? amount balance)]
      [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :wallet}]
        [toolbar-view signing?]
        [react/scroll-view {:keyboardShouldPersistTaps :always}
         [react/view components.styles/flex
          [react/view wallet.styles/choose-participant-container
           [components/choose-recipient {:address  to-address
                                         :name     to-name
                                         :on-press #(re-frame/dispatch [:navigate-to :choose-recipient])}]]
          [react/view wallet.styles/choose-wallet-container
           [components/choose-wallet]]
          [react/view wallet.styles/amount-container
           [components/amount-input
            {:error         (or amount-error (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
             :input-options {:auto-focus     true
                             :default-value  amount
                             :on-change-text #(let [value (string/trim %)]
                                                (re-frame/dispatch [:wallet/set-and-validate-amount value]))}}]
           [react/view wallet.styles/choose-currency-container
            [components/choose-currency wallet.styles/choose-currency]]]]]
        [components/separator]
        (if signing?
          [signing-buttons
           #(re-frame/dispatch [:wallet/discard-transaction])
           #(re-frame/dispatch [:wallet/sign-transaction])]
          [sign-buttons amount-error to-address amount sufficient-funds? sign-later])
        (when signing?
          [sign-panel])]])))

(defn toolbar-modal []
  [toolbar/toolbar2 {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/close-white act/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

(defview send-transaction-modal []
  (letsubs [amount       [:get-in [:wallet/send-transaction :amount]]
            amount-error [:get-in [:wallet/send-transaction :amount-error]]
            signing?     [:get-in [:wallet/send-transaction :signing?]]
            to-address   [:get-in [:wallet/send-transaction :to-address]]
            to-name      [:get-in [:wallet/send-transaction :to-name]]
            recipient    [:contact-by-address @to-name]]
    [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
     [react/view components.styles/flex
      [status-bar/status-bar {:type :wallet}]
      [toolbar-modal]
      [react/scroll-view {:keyboardShouldPersistTaps :always}
       [react/view components.styles/flex
        [react/view wallet.styles/choose-participant-container
         [components/choose-recipient-disabled {:address  to-address
                                                :name     (:name recipient)}]]
        [react/view wallet.styles/choose-wallet-container
         [components/choose-wallet]]
        [react/view wallet.styles/amount-container
         [components/amount-input-disabled amount]
         [react/view wallet.styles/choose-currency-container
          [components/choose-currency wallet.styles/choose-currency]]]]]
      [components/separator]
      (if signing?
        [signing-buttons
         #(re-frame/dispatch [:wallet/cancel-signing-modal])
         #(re-frame/dispatch [:wallet/sign-transaction-modal])]
        [sign-buttons amount-error to-address amount true #(re-frame/dispatch [:navigate-back])])
      (when signing?
        [sign-panel])]]))
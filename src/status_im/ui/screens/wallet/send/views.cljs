(ns status-im.ui.screens.wallet.send.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.send.animations :as send.animations]
            [status-im.ui.screens.wallet.send.styles :as send.styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

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
         :placeholder-text-color components.styles/color-gray4
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

(defn- sign-enabled? [amount-error to amount]
  (and
   (nil? amount-error)
   (not (nil? to)) (not= to "")
   (not (nil? amount))))

;; "Sign Later" and "Sign Transaction >" buttons
(defn- sign-buttons [amount-error to amount sufficient-funds? sign-later-handler]
  (let [sign-enabled? (sign-enabled? amount-error to amount)
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

(defn- request-camera-permissions []
  (when platform/android?
    (re-frame/dispatch [:request-permissions [:camera]]))
  (camera/request-access
   (fn [permitted?]
     (re-frame/dispatch [:set-in [:wallet :send-transaction :camera-permitted?] permitted?])
     (re-frame/dispatch [:navigate-to :choose-recipient]))))

(defn- toolbar-modal [from-chat?]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/close-white (if from-chat?
                                          #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                          act/default-handler))]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

(defn- toolbar-view [signing?]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/back-white (if signing?
                                         #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                         act/default-handler))]
   [toolbar/content-title {:color :white} (i18n/label :t/send-transaction)]])

(defn- transaction-fee-toolbar []
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/close-white #(re-frame/dispatch [:wallet/discard-transaction-navigate-back]))]
   [toolbar/content-title {:color :white} (i18n/label :t/wallet-transaction-fee)]])

(defn- max-fee [gas gas-price]
  (when (and gas gas-price)
    (money/wei->ether (.times gas gas-price))))

(defview transaction-fee []
  (letsubs [{:keys [amount gas gas-price symbol]} [:wallet.send/transaction]]
    [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
     [react/view components.styles/flex
      [status-bar/status-bar {:type :modal-wallet}]
      [transaction-fee-toolbar]
      [react/view send.styles/transaction-fee-block-wrapper
       [react/view send.styles/transaction-fee-column-wrapper
        [react/text {:style send.styles/advanced-fees-text}
         (i18n/label :t/gas-limit)]
        [react/view send.styles/advanced-options-wrapper
         [react/text-input (merge send.styles/transaction-fee-input
                                  {:on-change-text #(re-frame/dispatch [:wallet.send/set-gas %])
                                   :default-value  (str (money/to-fixed gas))})]]]

       [react/view send.styles/transaction-fee-column-wrapper
        [react/text {:style send.styles/advanced-fees-text}
         (i18n/label :t/gas-price)]
        [react/view send.styles/transaction-fee-bubble
         [react/text-input (merge send.styles/transaction-fee-input
                                  {:on-change-text #(re-frame/dispatch [:wallet.send/set-gas-price (money/->wei :gwei %)])
                                   :default-value  (str (money/to-fixed (money/wei-> :gwei gas-price)))})]
         [react/text {:style send.styles/advanced-fees-details-text}
          "Gwei"]]]]
      [react/view send.styles/transaction-fee-info
       [react/text {:style send.styles/advanced-fees-text}
        (i18n/label :t/wallet-transaction-fee-details)]]
      [components/separator]
      [react/view send.styles/transaction-fee-block-wrapper
       [react/view send.styles/transaction-fee-column-wrapper
        [react/text {:style send.styles/advanced-fees-text}
         (i18n/label :t/amount)]
        [react/view send.styles/transaction-fee-bubble-read-only
         [react/text {:style send.styles/advanced-fees-text}
          (str (money/to-fixed (money/wei->ether amount)))]
         [react/text {:style send.styles/advanced-fees-details-text}
          (name symbol)]]]
       [react/view send.styles/transaction-fee-column-wrapper
        [react/text {:style send.styles/advanced-fees-text}
         (i18n/label :t/wallet-transaction-total-fee)]
        [react/view send.styles/transaction-fee-bubble-read-only
         [react/text {:style send.styles/advanced-fees-text}
          (str (money/to-fixed (max-fee gas gas-price)))]
         [react/text {:style send.styles/advanced-fees-details-text}
          "ETH"]]]]]]))

(defn- advanced-options-wrapper [on-press content]
  (if on-press
    [react/touchable-highlight {:on-press on-press}
     [react/view
      content]]
    [react/view
     content]))

(defn- advanced-options [{:keys [gas gas-price]} on-press]
  [react/touchable-highlight {:on-press on-press}
   [react/view
    [react/text {:style wallet.components.styles/label}
     (i18n/label :t/wallet-transaction-fee)]
    [advanced-options-wrapper on-press
     [react/view send.styles/advanced-options-wrapper
      [react/view send.styles/advanced-options-text-wrapper
       [react/text {:style send.styles/advanced-fees-text}
        (str (money/to-fixed (max-fee gas gas-price))  " ETH")]
       [react/text {:style send.styles/advanced-fees-details-text}
        (str (money/to-fixed gas) " * " (money/to-fixed (money/wei-> :gwei gas-price)) "GWEI")]]
      (when on-press
        [vector-icons/icon :icons/forward {:color :white}])]]]])

(defn- send-transaction-panel [{:keys [modal? transaction scroll advanced? symbol]}]
  (let [{:keys [amount amount-error signing? to to-name sufficient-funds? in-progress? from-chat?]} transaction]
    [react/keyboard-avoiding-view wallet.styles/wallet-modal-container
     [react/view components.styles/flex
      [status-bar/status-bar {:type (if modal? :modal-wallet :wallet)}]
      (if modal? [toolbar-modal from-chat?] [toolbar-view signing?])
      [common/network-info {:text-color :white}]
      [react/scroll-view (merge {:keyboardShouldPersistTaps :always} (when-not modal? {:ref #(reset! scroll %)}))
       [react/view components.styles/flex
        [react/view wallet.styles/choose-participant-container
         [components/choose-recipient (merge {:address to
                                              :name    to-name}
                                             (when-not modal?
                                               {:on-press request-camera-permissions}))]]
        [react/view wallet.styles/choose-asset-container
         (if modal?
           [components/view-asset symbol]
           [components/choose-asset {:type   :send
                                     :symbol symbol}])]
        [react/view wallet.styles/amount-container
         [components/amount-input
          (merge
            {:error (or amount-error
                        (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
             :input-options {:default-value  (str (money/wei->ether amount))
                             :on-focus       (fn [] (when @scroll (js/setTimeout #(.scrollToEnd @scroll) 100)))
                             :on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount %])}}
            (when modal?
              {:disabled? true}))]]
        [react/view {:style send.styles/advanced-wrapper}
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:wallet.send/toggle-advanced (not advanced?)])}
          [react/view {:style send.styles/advanced-button-wrapper}
           [react/view {:style send.styles/advanced-button}
            [react/text {:style (merge wallet.components.styles/label send.styles/advanced-label)}
             (i18n/label :t/wallet-advanced)]
            [vector-icons/icon (if advanced? :icons/up :icons/down) {:color :white}]]]]
         (when advanced?
           [advanced-options transaction (when-not modal? #(re-frame/dispatch [:navigate-to-modal :wallet-transaction-fee]))])]]]
      [components/separator]
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
      (when in-progress? [react/view send.styles/processing-view])]]))

(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]
            scroll      (atom nil)]
    [send-transaction-panel {:modal? false :transaction transaction :scroll scroll :advanced? advanced? :symbol symbol}]))

(defview send-transaction-modal []
  (letsubs [transaction [:wallet.send/unsigned-transaction]
            symbol      [:wallet.send/symbol]
            advanced?   [:wallet.send/advanced?]]
    (if transaction
      [send-transaction-panel {:modal? true :transaction transaction :advanced? advanced? :symbol symbol}]
      [react/view wallet.styles/wallet-modal-container
       [react/view components.styles/flex
        [status-bar/status-bar {:type :modal-wallet}]
        [toolbar-modal false]
        [react/text {:style send.styles/empty-text} (i18n/label :t/unsigned-transaction-expired)]]])))

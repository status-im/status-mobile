(ns status-im.ui.screens.wallet.request.views
  (:require-macros [status-im.utils.views :as views])
  (:require
    [re-frame.core :as re-frame]
    [status-im.components.react :as react]
    [status-im.components.qr-code :as components.qr-code]
    [status-im.components.toolbar-new.actions :as actions]
    [status-im.components.toolbar-new.view :as toolbar]
    [status-im.components.status-bar :as status-bar]
    [status-im.ui.screens.wallet.send.styles :as wallet-styles]
    [status-im.components.icons.vector-icons :as vi]
    [status-im.ui.screens.wallet.components.views :as components]
    [status-im.ui.screens.wallet.request.styles :as styles]
    [status-im.i18n :as i18n]
    [status-im.utils.platform :as platform]))

(defn toolbar-view []
  [toolbar/toolbar2 {:style wallet-styles/toolbar :hide-border? true}
   [toolbar/nav-button (actions/back-white actions/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/request-transaction)]])

(defn send-request []
  (re-frame/dispatch [:navigate-to-modal
                      :contact-list-modal
                      {:handler #(re-frame/dispatch [:wallet-send-request %1])
                       :action  :request
                       :params  {:hide-actions? true}}]))

(views/defview qr-code []
  (views/letsubs [account [:get-current-account]]
    [components.qr-code/qr-code
     {:value   (.stringify js/JSON (clj->js {:address (:address account)
                                             :amount  0}))
      :bgColor :white
      :fgColor "#4360df"
      :size    256}]))

(views/defview request-transaction []
  [react/keyboard-avoiding-view  {:style wallet-styles/wallet-modal-container}
   [status-bar/status-bar {:type :wallet}]
   [toolbar-view]
   [react/view styles/main-container
    [react/scroll-view
     [react/view styles/network-container
      ;;TODO (andrey) name of active network should be used
      [components/network-label styles/network-label "Testnet"]
      [react/view styles/qr-container
       [qr-code]]]]
    [react/view styles/choose-wallet-container
     [components/choose-wallet]]
    [react/view styles/amount-container
     [components/amount-input
      {:input-options {:on-change-text
                       #(re-frame/dispatch [:set-in [:wallet/request-transaction :amount] %])}}]
     [react/view styles/choose-currency-container
      [components/choose-currency styles/choose-currency]]]]
   [react/view styles/separator]
   [react/view styles/buttons-container
    [vi/icon :icons/share {:color :white :container-style styles/share-icon-container}]
    [react/text {:style      styles/button-text
                 :font       (if platform/android? :medium :default)
                 :uppercase? (get-in platform/platform-specific [:uppercase?])} (i18n/label :t/share)]
    [react/view {:flex 1}]
    [react/touchable-highlight {:on-press send-request}
     [react/view styles/send-request-container
      [react/text {:style      styles/button-text
                   :font       (if platform/android? :medium :default)
                   :uppercase? (get-in platform/platform-specific [:uppercase?])} (i18n/label :t/send-request)]
      [vi/icon :icons/forward {:color :white :container-style styles/forward-icon-container}]]]]])
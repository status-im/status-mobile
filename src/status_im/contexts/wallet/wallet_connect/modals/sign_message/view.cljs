(ns status-im.contexts.wallet.wallet-connect.modals.sign-message.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.safe-area :as safe-area]
            [status-im.common.raw-data-block.view :as data-block]
            [status-im.contexts.wallet.wallet-connect.modals.common.fees-data-item.view :as
             fees-data-item]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.view :as footer]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.view :as header]
            [status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view :as page-nav]
            [status-im.contexts.wallet.wallet-connect.modals.common.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn typed-data-field
  [{:keys [label value]}]
  [quo/data-item
   {:card?           false
    :container-style style/typed-data-item
    :title           label
    :subtitle        value}])

(defn typed-data-view
  []
  (let [display-data (rf/sub [:wallet-connect/current-request-display-data])]
    [gesture/flat-list
     {:data                            display-data
      :style                           style/data-border-container
      :over-scroll-mode                :never
      :content-container-style         {:padding-bottom 12}
      :render-fn                       typed-data-field
      :shows-vertical-scroll-indicator false}]))

(defn message-data-view
  []
  (let [display-data (rf/sub [:wallet-connect/current-request-display-data])]
    [data-block/view display-data]))

(defn display-data-view
  []
  (let [typed-data? (rf/sub [:wallet-connect/typed-data-request?])]
    (if typed-data?
      [typed-data-view]
      [message-data-view])))

(defn view
  []
  (let [bottom          safe-area/bottom
        {:keys [customization-color]
         :as   account} (rf/sub [:wallet-connect/current-request-account-details])
        dapp            (rf/sub [:wallet-connect/current-request-dapp])]
    (rn/use-unmount #(rf/dispatch [:wallet-connect/on-request-modal-dismissed]))
    [rn/view {:style (style/container bottom)}
     [quo/gradient-cover {:customization-color customization-color}]
     [page-nav/view
      {:accessibility-label :wallet-connect-sign-message-close}]
     [rn/view {:flex 1}
      [rn/view {:style style/sign-message-content-container}
       [header/view
        {:label   (i18n/label :t/wallet-connect-sign-message-header)
         :dapp    dapp
         :account account}]
       [display-data-view]]
      [footer/view
       {:warning-label     (i18n/label :t/wallet-connect-sign-warning)
        :slide-button-text (i18n/label :t/slide-to-sign)}
       [fees-data-item/view]]]]))

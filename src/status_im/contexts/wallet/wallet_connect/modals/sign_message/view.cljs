(ns status-im.contexts.wallet.wallet-connect.modals.sign-message.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.wallet.wallet-connect.modals.common.fees-data-item.view :as
             fees-data-item]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.view :as footer]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.view :as header]
            [status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view :as page-nav]
            [status-im.contexts.wallet.wallet-connect.modals.common.style :as style]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as data-store]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- render-item
  [props]
  (let [[label value] props]
    [quo/data-item
     {:card?           false
      :container-style style/list-data-item
      :title           label
      :subtitle        value}]))

(defn view
  []
  (let [bottom                          (safe-area/get-bottom)
        {:keys [customization-color]
         :as   account}                 (rf/sub [:wallet-connect/current-request-account-details])
        dapp                            (rf/sub [:wallet-connect/current-request-dapp])
        {:keys [raw-data display-data]} (rf/sub [:wallet-connect/current-request])
        sign-items                      (data-store/raw-data->sign-view raw-data display-data)
        network                         (rf/sub [:wallet-connect/current-request-network])]
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
       [gesture/flat-list
        {:data                            sign-items
         :content-container-style         style/data-item-container
         :render-fn                       render-item
         :shows-vertical-scroll-indicator false}]]
      [footer/view
       {:warning-label     (i18n/label :t/wallet-connect-sign-warning)
        :slide-button-text (i18n/label :t/slide-to-sign)}
       [quo/data-item
        {:status          :default
         :card?           false
         :container-style style/data-item
         :title           (i18n/label :t/network)
         :subtitle-type   :network
         :network-image   (:source network)
         :subtitle        (:full-name network)}]
       [fees-data-item/view]]]]))


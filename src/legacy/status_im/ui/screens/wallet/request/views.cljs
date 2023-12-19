(ns legacy.status-im.ui.screens.wallet.request.views
  (:require
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im2.common.qr-codes.view :as qr-codes]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.ethereum.eip.eip681 :as eip681]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview share-address
  []
  (views/letsubs [{:keys [address]} [:popover/popover]
                  chain-id          [:chain-id]
                  width             (reagent/atom nil)]
    [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      (when @width
        [qr-codes/qr-code
         {:url  (eip681/generate-uri address {:chain-id chain-id})
          :size (- @width 32)}])
      [copyable-text/copyable-text-view
       {:label           :t/ethereum-address
        :container-style {:margin-top 12 :margin-bottom 4}
        :copied-text     (eip55/address->checksum address)}
       [quo/text
        {:number-of-lines     1
         :ellipsize-mode      :middle
         :accessibility-label :address-text
         :monospace           true}
        (eip55/address->checksum address)]]]
     [react/view
      {:padding-top        12
       :padding-horizontal 16
       :padding-bottom     16}
      [quo/button
       {:on-press            #(re-frame/dispatch [:wallet-legacy.accounts/share address])
        :accessibility-label :share-address-button}
       (i18n/label :t/share-address)]]]))

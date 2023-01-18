(ns status-im.ui.screens.wallet.request.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [utils.i18n :as i18n]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react])
  (:require-macros [status-im.utils.views :as views]))

(views/defview share-address
  []
  (views/letsubs [{:keys [address]} [:popover/popover]
                  chain-id          [:chain-id]
                  width             (reagent/atom nil)]
    [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      (when @width
        [qr-code-viewer/qr-code-view
         (- @width 32)
         (eip681/generate-uri address {:chain-id chain-id})])
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
       {:on-press            #(re-frame/dispatch [:wallet.accounts/share address])
        :accessibility-label :share-address-button}
       (i18n/label :t/share-address)]]]))

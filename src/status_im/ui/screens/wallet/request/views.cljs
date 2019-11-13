(ns status-im.ui.screens.wallet.request.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.react :as react])
  (:require-macros [status-im.utils.views :as views]))

(views/defview share-address []
  (views/letsubs [{:keys [address]} [:popover/popover]
                  chain-id    [:chain-id]
                  width       (reagent/atom nil)]
    [react/view {:on-layout #(reset! width (-> % .-nativeEvent .-layout .-width))}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      (when @width
        [qr-code-viewer/qr-code-view
         (- @width 32)
         (eip681/generate-uri address {:chain-id chain-id})])
      [copyable-text/copyable-text-view
       {:label           :t/wallet-address
        :container-style {:margin-top 12 :margin-bottom 4}
        :copied-text     (eip55/address->checksum address)}
       [react/text {:number-of-lines     1
                    :ellipsize-mode      :middle
                    :accessibility-label :address-text
                    :style               {:line-height 22 :font-size 15
                                          :font-family "monospace"}}
        (eip55/address->checksum address)]]]
     [react/view {:margin-top 12 :margin-bottom 8}
      [button/button
       {:on-press            #(re-frame/dispatch [:wallet.accounts/share address])
        :label               :t/share-address
        :accessibility-label :share-address-button}]]]))

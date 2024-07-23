(ns status-im.contexts.wallet.wallet-connect.modals.common.header.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.style :as style]))

(defn view
  [{:keys [label dapp account]}]
  [rn/view {:style style/header-container}
   (let [{:keys [name iconUrl]} dapp]
     [rn/view {:style style/header-dapp-name}
      [rn/view {:flex 1}
       [quo/summary-tag
        {:type         :dapp
         :label        name
         :image-source iconUrl}]]])
   [quo/text {:size   :heading-1
              :weight :semi-bold
              :style  style/header-text}
    label]
   (let [{:keys [emoji customization-color name]} account]
     [rn/view {:style style/header-account-name}
      [quo/summary-tag
       {:type                :account
        :emoji               emoji
        :label               name
        :customization-color customization-color}]])])

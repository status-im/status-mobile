(ns status-im.contexts.wallet.wallet-connect.modals.common.header.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.wallet-connect.core :as core]
    [status-im.contexts.wallet.wallet-connect.modals.common.header.style :as style]
    [utils.string]))

(defn view
  [{:keys [label dapp account]}]
  [rn/view
   {:style style/header-container}
   [quo/text
    {:size   :heading-1
     :weight :semi-bold}
    (let [{:keys [name iconUrl url]} dapp
          image-source               (core/compute-dapp-icon-path iconUrl url)]
      [rn/view {:style style/header-dapp-name}
       [quo/summary-tag
        {:type         :dapp
         :label        name
         :image-source image-source}]])
    (str " " label " ")
    (let [{:keys [emoji customization-color name]} account]
      [rn/view {:style style/header-account-name}
       [quo/summary-tag
        {:type                :account
         :emoji               emoji
         :label               name
         :customization-color customization-color}]])]])

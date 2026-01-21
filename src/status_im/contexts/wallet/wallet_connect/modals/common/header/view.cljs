(ns status-im.contexts.wallet.wallet-connect.modals.common.header.view
  (:require
    [status-im.contexts.wallet.wallet-connect.modals.common.header.components :as header]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as
     data-store]))

(defn view
  [{:keys [label dapp account]}]
  [header/title-container
   (let [{:keys [name iconUrl url]} dapp
         image-source               (data-store/compute-dapp-icon-path iconUrl url)]
     [header/title-summary
      {:type         :dapp
       :label        name
       :image-source image-source}])
   [header/title-text {:text label}]
   (let [{:keys [emoji customization-color name]} account]
     [header/title-summary
      {:type                :account
       :emoji               emoji
       :label               name
       :customization-color customization-color}])])

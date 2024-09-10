(ns status-im.contexts.wallet.wallet-connect.modals.common.header.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.style :as style]
            [status-im.contexts.wallet.wallet-connect.utils.data-transformations :as
             data-transformations]))

(defn view
  [{:keys [label dapp account]}]
  [rn/view {:style style/header-container}
   (let [{:keys [name iconUrl url]} dapp
         image-source               (data-transformations/compute-dapp-icon-path iconUrl url)]
     [rn/view {:style style/dapp-container}
      [quo/summary-tag
       {:type         :dapp
        :label        name
        :image-source image-source}]])
   (for [word (string/split label #" ")]
     ^{:key word}
     [rn/view {:style style/word-container}
      [quo/text
       {:size   :heading-1
        :weight :semi-bold}
       (str " " word)]])
   (let [{:keys [emoji customization-color name]} account]
     [rn/view {:style style/account-container}
      [quo/summary-tag
       {:type                :account
        :emoji               emoji
        :label               name
        :customization-color customization-color}]])])

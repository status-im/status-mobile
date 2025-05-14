(ns status-im.contexts.wallet.wallet-connect.modals.common.list-info-box.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.contexts.wallet.wallet-connect.modals.common.list-info-box.style :as style]
    [utils.i18n :as i18n]
    [utils.string]))

(defn view
  [{:keys [dapp-name container-style]}]
  (let [labels [(i18n/label :t/check-your-account-balance-and-activity)
                (i18n/label :t/request-txns-and-message-signing)]
        theme  (quo.context/use-theme)]
    [rn/view {:style (merge (style/container theme) container-style)}
     [quo/text
      {:style  style/title
       :weight :regular
       :size   :paragraph-2}
      (i18n/label :t/dapp-will-be-able-to {:dapp-name dapp-name})]
     (map-indexed
      (fn [idx label]
        ^{:key (str idx label)}
        [rn/view {:style style/item}
         [quo/icon :i/bullet
          {:color colors/neutral-40}]
         [quo/text
          {:weight :regular
           :size   :paragraph-2
           :color  colors/neutral-40}
          label]])
      labels)]))

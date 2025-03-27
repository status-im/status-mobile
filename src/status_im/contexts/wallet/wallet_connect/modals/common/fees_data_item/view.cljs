(ns status-im.contexts.wallet.wallet-connect.modals.common.fees-data-item.view
  (:require [quo.context]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [status-im.contexts.wallet.wallet-connect.modals.common.style :as style]
            [utils.i18n :as i18n]))

(defn- fees-subtitle
  [{:keys [text error?]}]
  (let [theme (quo.context/use-theme)]
    [quo/text
     {:weight :medium
      :size   :paragraph-2
      :style  {:color (if error?
                        (colors/resolve-color :danger theme)
                        (colors/theme-colors colors/neutral-100
                                             colors/white
                                             theme))}}
     text]))

(defn view
  [{:keys [fees fees-error]}]
  [quo/data-item
   {:size            :small
    :status          :default
    :card?           false
    :container-style style/data-item
    :title           (i18n/label :t/max-fees)
    :custom-subtitle (fn [] [fees-subtitle
                             {:text   (or fees (i18n/label :t/no-fees))
                              :error? (= fees-error :not-enough-assets-to-pay-gas-fees)}])}])

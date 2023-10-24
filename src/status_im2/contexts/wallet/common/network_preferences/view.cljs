(ns status-im2.contexts.wallet.common.network-preferences.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.foundations.resources :as resources]
            [quo.theme :as quo.theme]
            [status-im2.contexts.wallet.common.network-preferences.style :as style]
            [utils.i18n :as i18n]))

(def mainnet
  [{:title        "Mainnet"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :ethereum)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}])

(def networks-list
  [{:title        "Optimism"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :optimism)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}
   {:title        "Arbitrum"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :arbitrum)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}])

(defn- view-internal
  [{:keys [address on-save theme]}]
  [:<>
   [quo/drawer-top
    {:title       (i18n/label :t/network-preferences)
     :description (i18n/label :t/network-preferences-desc)}]
   [quo/data-item
    {:status          :default
     :size            :default
     :description     :default
     :label           :none
     :blur?           false
     :card?           true
     :title           (i18n/label :t/address)
     :subtitle        address
     :container-style (merge style/data-item
                             {:background-color (colors/theme-colors colors/neutral-2_5
                                                                     colors/neutral-90
                                                                     theme)})}]
   [quo/category
    {:list-type :settings
     :data      mainnet}]
   [quo/category
    {:list-type :settings
     :label     (i18n/label :t/layer-2)
     :data      networks-list}]
   [quo/bottom-actions
    {:button-one-label (i18n/label :t/update)
     :disabled?        true
     :button-one-press on-save}]])

(def view (quo.theme/with-theme view-internal))

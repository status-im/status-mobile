(ns status-im.contexts.wallet.sheets.buy-token.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.sheets.buy-token.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- crypto-on-ramp-item
  [{:keys [name description fees logo-url site-url]}]
  [quo/settings-item
   {:title             name
    :description       :text
    :description-props {:text description}
    :tag               :context
    :tag-props         {:icon    :i/fees
                        :context fees}
    :action            :arrow
    :action-props      {:alignment :flex-start
                        :icon      :i/external}
    :image             :icon-avatar
    :image-props       {:icon logo-url}
    :on-press          #(rn/open-url site-url)}])

(defn view
  []
  (rn/use-mount (fn []
                  (rf/dispatch [:wallet/get-crypto-on-ramps])))
  (let [crypto-on-ramps (rf/sub [:wallet/crypto-on-ramps])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/buy-assets)}]
     [rn/flat-list
      {:data      crypto-on-ramps
       :style     style/list-container
       :render-fn crypto-on-ramp-item}]]))

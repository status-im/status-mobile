(ns status-im.contexts.wallet.common.temp
  (:require
    [react-native.core :as rn]
    [status-im.common.resources :as status.resources]
    [utils.i18n :as i18n]))

(def buy-tokens-list
  [{:title             "Ramp"
    :description       :text
    :description-props {:text (i18n/label :t/ramp-description)}
    :tag               :context
    :tag-props         {:icon    :i/fees
                        :context "0.49% - 2.9%"}
    :action            :arrow
    :action-props      {:alignment :flex-start
                        :icon      :i/external}
    :image             :icon-avatar
    :image-props       {:icon (status.resources/get-service-image :ramp)}
    :on-press          #(rn/open-url "https://ramp.com")}
   {:title             "MoonPay"
    :description       :text
    :description-props {:text (i18n/label :t/moonpay-description)}
    :tag               :context
    :tag-props         {:icon    :i/fees
                        :context "1% - 4.5%"}
    :action            :arrow
    :action-props      {:alignment :flex-start
                        :icon      :i/external}
    :image             :icon-avatar
    :image-props       {:icon (status.resources/get-service-image :moonpay)}
    :on-press          #(rn/open-url "https://moonpay.com")}
   {:title             "Latamex"
    :description       :text
    :description-props {:text (i18n/label :t/latamex-description)}
    :tag               :context
    :tag-props         {:icon    :i/fees
                        :context "1% - 1.7%"}
    :action            :arrow
    :action-props      {:alignment :flex-start
                        :icon      :i/external}
    :image             :icon-avatar
    :image-props       {:icon (status.resources/get-service-image :latamex)}
    :on-press          #(rn/open-url "https://latamex.com")}])

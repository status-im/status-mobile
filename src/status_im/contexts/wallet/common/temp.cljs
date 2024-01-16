(ns status-im.contexts.wallet.common.temp
  (:require
    [react-native.core :as rn]
    [status-im.common.resources :as status.resources]
    [utils.i18n :as i18n]))

(def address "0x39cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd4")

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

(defn bridge-token-list
  [networks-list]
  [{:token               :snt
    :label               "Status"
    :token-value         "0.00 SNT"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}
   {:token               :eth
    :label               "Ethereum"
    :token-value         "0.00 ETH"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}
   {:token               :dai
    :label               "Dai"
    :token-value         "0.00 DAI"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}])

(def secret-phrase
  ["witch" "collapse" "practice" "feed" "shame" "open" "lion"
   "collapse" "umbrella" "fabric" "sadness" "obligue"])

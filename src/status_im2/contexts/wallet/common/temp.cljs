(ns status-im2.contexts.wallet.common.temp
  (:require
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im2.common.resources :as status.resources]
    [utils.i18n :as i18n]))

(defn wallet-overview-state
  [networks]
  {:state             :default
   :time-frame        :none
   :metrics           :none
   :balance           "€0.00"
   :date              "20 Nov 2023"
   :begin-date        "16 May"
   :end-date          "25 May"
   :currency-change   "€0.00"
   :percentage-change "0.00%"
   :networks          networks})

(def tokens
  [{:token               :snt
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}
   {:token               :eth
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}
   {:token               :dai
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}])

(def collectibles
  [{:image (status.resources/get-mock-image :collectible1)
    :id    1}
   {:image (status.resources/get-mock-image :collectible2)
    :id    2}
   {:image (status.resources/get-mock-image :collectible3)
    :id    3}
   {:image (status.resources/get-mock-image :collectible4)
    :id    4}
   {:image (status.resources/get-mock-image :collectible5)
    :id    5}
   {:image (status.resources/get-mock-image :collectible6)
    :id    6}])

(def collectible-details
  nil
  #_{:name             "#5946"
     :description      "Bored Ape Yacht Club"
     :image            (status.resources/get-mock-image :collectible-monkey)
     :collection-image (status.resources/get-mock-image :bored-ape)
     :traits           [{:title    "Background"
                         :subtitle "Blue"
                         :id       1}
                        {:title    "Clothes"
                         :subtitle "Bayc T Black"
                         :id       2}
                        {:title    "Eyes"
                         :subtitle "Sleepy"
                         :id       3}
                        {:title    "Fur"
                         :subtitle "Black"
                         :id       4}
                        {:title    "Hat"
                         :subtitle "Beanie"
                         :id       5}
                        {:title    "Mouth"
                         :subtitle "Bored Pipe"
                         :id       6}]})

(def account-overview-state
  {:current-value       "€0.00"
   :account-name        "Account 1"
   :account             :default
   :customization-color :blue})

(def network-names
  [{:network-name :ethereum :short-name "eth"}
   {:network-name :optimism :short-name "opt"}
   {:network-name :arbitrum :short-name "arb1"}])

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
  [{:token               (quo.resources/get-token :snt)
    :label               "Status"
    :token-value         "0.00 SNT"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}
   {:token               (quo.resources/get-token :eth)
    :label               "Ethereum"
    :token-value         "0.00 ETH"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}
   {:token               (quo.resources/get-token :dai)
    :label               "Dai"
    :token-value         "0.00 DAI"
    :fiat-value          "€0.00"
    :networks            networks-list
    :state               :default
    :customization-color :blue}])

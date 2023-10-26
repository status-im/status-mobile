(ns status-im2.contexts.wallet.common.temp
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im2.common.resources :as status.resources]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def networks
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn wallet-temporary-navigation
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   [quo/text {} "TEMPORARY NAVIGATION"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-accounts])}
    "Navigate to Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-create-account])}
    "Create Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-saved-addresses])}
    "Saved Addresses"]])

(def wallet-overview-state
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

(def network-names [:ethereum :optimism :arbitrum])

(def address "0x39cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd4")

(def data-item-state
  {:description         :default
   :icon-right?         true
   :right-icon          :i/options
   :card?               true
   :label               :none
   :status              :default
   :size                :default
   :title               "Address"
   :customization-color :yellow})

(def account-origin-state
  {:type            :default-keypair
   :stored          :on-keycard
   :profile-picture (status.resources/get-mock-image :user-picture-male5)
   :derivation-path (string/replace constants/path-default-wallet #"/" " / ")
   :user-name       "Alisher Yakupov"
   :on-press        #(js/alert "pressed")})

(defn dapps-list
  [{:keys [on-press-icon]}]
  [{:dapp          {:avatar (quo.resources/get-dapp :coingecko)
                    :name   "Coingecko"
                    :value  "coingecko.com"}
    :state         :default
    :action        :icon
    :on-press-icon on-press-icon}
   {:dapp          {:avatar (quo.resources/get-dapp :uniswap)
                    :name   "Uniswap"
                    :value  "uniswap.org"}
    :state         :default
    :action        :icon
    :on-press-icon on-press-icon}])

(def account-data
  {:title                "Trip to Vegas"
   :type                 :account
   :networks             [{:name :ethereum :short :eth}
                          {:name :optimism :short :opt}
                          {:name :arbitrum :short :arb1}]
   :description          "0x62b...0a5"
   :account-avatar-emoji "🍑"
   :customization-color  :purple})

(def other-accounts
  [{:customization-color :flamingo
    :emoji               "🍿"
    :name                "New House"
    :address             "0x21a...49e"
    :networks            [{:name :ethereum :short :eth}
                          {:name :optimism :short :opt}]}
   {:customization-color :blue
    :emoji               "🎮"
    :name                "My savings"
    :address             "0x43c...98d"
    :networks            [{:name :ethereum :short :eth}]}])

(def asset-snt
  {:size       24
   :type       :token
   :token-name "SNT"
   :amount     1500
   :token-logo (quo.resources/get-token :snt)})

(def piggy-bank
  {:size         24
   :type         :account
   :account-name "Piggy bank"
   :emoji        "🐷"})

(def aretha-gosling
  {:size            24
   :type            :default
   :full-name       "Aretha Gosling"
   :profile-picture (status.resources/mock-images :user-picture-female2)})

(def mainnet
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :ethereum)
   :network-name "Mainnet"})

(def activity-list
  [{:date              "Today"
    :transaction       :send
    :timestamp         "Today 22:20"
    :status            :pending
    :counter           1
    :first-tag         asset-snt
    :second-tag-prefix :t/from
    :second-tag        piggy-bank
    :third-tag-prefix  :t/to
    :third-tag         aretha-gosling
    :fourth-tag-prefix :t/via
    :fourth-tag        mainnet
    :blur?             false}
   {:date              "Yesterday"
    :transaction       :receive
    :timestamp         "Yesterday 22:20"
    :status            :pending
    :counter           1
    :first-tag         asset-snt
    :second-tag-prefix :t/from
    :second-tag        piggy-bank
    :third-tag-prefix  :t/to
    :third-tag         aretha-gosling
    :fourth-tag-prefix :t/via
    :fourth-tag        mainnet
    :blur?             false}])

(def collectible-list
  [(status.resources/get-mock-image :collectible1)
   (status.resources/get-mock-image :collectible2)
   (status.resources/get-mock-image :collectible3)
   (status.resources/get-mock-image :collectible4)])

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

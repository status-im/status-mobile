(ns status-im2.contexts.quo-preview.list-items.preview-lists
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as quo.resources]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key   :user
               :value "User"}
              {:key   :communities
               :value "Communities"}
              {:key   :collectibles
               :value "Collectibles"}
              {:key   :tokens
               :value "Tokens"}
              {:key   :dapps
               :value "dApps"}
              {:key   :accounts
               :value "Accounts"}
              {:key   :network
               :value "Network"}]}
   {:key     :size
    :type    :select
    :options [{:key   :size-32
               :value "32"}
              {:key   :size-24
               :value "24"}
              {:key   :size-20
               :value "20"}
              {:key   :size-16
               :value "16"}
              {:key   :size-14
               :value "14"}]}
   {:key  :number
    :type :text}
   {:key  :blur?
    :type :boolean}])

(def user-list
  [{:full-name           "A Y"
    :customization-color :blue}
   {:full-name           "B Z"
    :profile-picture     (resources/get-mock-image :user-picture-male4)
    :customization-color :army}
   {:full-name           "X R"
    :customization-color :orange}
   {:full-name           "T R"
    :profile-picture     (resources/get-mock-image :user-picture-male5)
    :customization-color :army}])

(def accounts-list
  [{:customization-color :purple
    :emoji               "🍑"
    :type                :default}
   {:customization-color :army
    :emoji               "🍓"
    :type                :default}
   {:customization-color :orange
    :emoji               "🍑"
    :type                :default}
   {:customization-color :blue
    :emoji               "🍓"
    :type                :default}])

(def tokens-list
  [{:source (quo.resources/get-token :snt)}
   {:source (quo.resources/get-token :eth)}
   {:source (quo.resources/get-token :knc)}
   {:source (quo.resources/get-token :mana)}
   {:source (quo.resources/get-token :rare)}])

(def communities-list
  [{:source (resources/get-mock-image :coinbase)}
   {:source (resources/get-mock-image :decentraland)}
   {:source (resources/get-mock-image :rarible)}
   {:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}])

(def collectibles-list
  [{:source (resources/get-mock-image :collectible)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}])

(def dapps-list
  [{:source (quo.resources/get-dapp :coingecko)}
   {:source (quo.resources/get-dapp :aave)}
   {:source (quo.resources/get-dapp :1inch)}
   {:source (quo.resources/get-dapp :zapper)}
   {:source (quo.resources/get-dapp :uniswap)}])

(def networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}
   {:source (quo.resources/get-network :zksync)}
   {:source (quo.resources/get-network :polygon)}])

(defn view
  []
  (let [state (reagent/atom {:type               :accounts
                             :size               :size-32
                             :number             4
                             :more-than-99-label "99+"})
        type  (reagent/cursor state [:type])
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 true
        :show-blur-background? @blur?}
       [quo/preview-list @state
        (case @type
          :user         user-list
          :communities  communities-list
          :accounts     accounts-list
          :tokens       tokens-list
          :collectibles collectibles-list
          :dapps        dapps-list
          :network      networks-list)]])))

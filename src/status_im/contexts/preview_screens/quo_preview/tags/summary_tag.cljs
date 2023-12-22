(ns status-im.contexts.preview-screens.quo-preview.tags.summary-tag
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn data
  [type]
  (case type
    :token
    {:customization-color "#9999991A"
     :label               "150 ETH"
     :token               :eth}
    :address
    {:label "0x39c...Bd2"}
    :user
    {:image-source        (resources/mock-images :user-picture-male4)
     :customization-color :blue
     :label               "Mark Libot"}
    :collectible
    {:label               "Isekai #1"
     :image-source        (resources/mock-images :collectible2)
     :customization-color :yellow}
    :network
    {:image-source (quo.resources/get-network :ethereum)
     :label        "Mainnet"}
    :saved-address
    {:customization-color :flamingo
     :label               "Peter Lambo"}
    :account
    {:label               "Account"
     :emoji               "🍿"
     :customization-color :purple}))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:value "Token"
               :key   :token}
              {:value "Address"
               :key   :address}
              {:value "User"
               :key   :user}
              {:value "Collectible"
               :key   :collectible}
              {:value "Network"
               :key   :network}
              {:value "Saved address"
               :key   :saved-address}
              {:value "Account"
               :key   :account}]}])

(defn view
  []
  (let [state (reagent/atom
               (merge {:type :token}
                      (data :token)))]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:align-items :center}}
        [quo/summary-tag (merge @state (data (:type @state)))]]])))

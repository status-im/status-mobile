(ns status-im.contexts.preview-screens.quo-preview.wallet.token-input
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

(def title "Max: 200 SNT")

(def descriptor
  [{:key     :token
    :type    :select
    :options [{:key :eth}
              {:key :snt}]}
   {:key     :currency
    :type    :select
    :options [{:key :usd}
              {:key :eur}]}])

(defn view
  []
  (let [state (reagent/atom {:token               :eth
                             :currency            :usd
                             :conversion          0.02
                             :networks            networks
                             :title               title
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 20
                                    :margin-top         50
                                    :align-items        :center}}
       [quo/token-input @state]])))

(ns status-im2.contexts.quo-preview.dropdowns.network-dropdown
  (:require [quo2.foundations.resources :as quo.resources]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.core :as quo]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :default}
              {:key :disabled}]}
   {:key     :blur?
    :type    :select
    :options [{:key true}
              {:key false}]}
   {:key     :amount
    :type    :select
    :options [{:key 1}
              {:key 2}
              {:key 3}]}])

(def ^:private networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn view
  []
  (let [component-state (reagent/atom {:state :default :blur? false :amount 3})]
    (fn []
      [preview/preview-container
       {:state                 component-state
        :descriptor            descriptor
        :blur?                 (:blur? @component-state)
        :show-blur-background? true}
       [rn/view {:style {:align-self :center}}
        [quo/network-dropdown
         (merge {:on-press #(js/alert "Dropdown pressed")}
                @component-state)
         (take (:amount @component-state) networks-list)]]])))

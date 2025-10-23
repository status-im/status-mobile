(ns status-im.contexts.preview.quo.dropdowns.network-dropdown
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

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
              {:key 3}]}
   {:key     :dropdown-icon?
    :type    :select
    :options [{:key true}
              {:key false}]}
   {:key  :label
    :type :text}])

(def ^:private networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn view
  []
  (let [component-state (reagent/atom {:state          :default
                                       :blur?          false
                                       :amount         3
                                       :dropdown-icon? false
                                       :label          nil})]
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

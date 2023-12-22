(ns status-im.contexts.preview-screens.quo-preview.list-items.dapp
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :active
               :value "Active"}]}
   {:key     :action
    :type    :select
    :options [{:key   :none
               :value "None"}
              {:key   :icon
               :value "Icon"}]}
   {:key  :blur?
    :type :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:dapp                {:avatar (resources/get-dapp :coingecko)
                                                   :name   "Coingecko"
                                                   :value  "coingecko.com"}
                             :state               :default
                             :action              :icon
                             :blur?               false
                             :customization-color :blue
                             :on-press-icon       (fn [] (js/alert "Button pressed"))})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [rn/view
        {:padding-vertical 60
         :flex-direction   :row
         :justify-content  :center}
        [quo/dapp @state]]])))

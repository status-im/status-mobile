(ns status-im2.contexts.quo-preview.settings.data-item
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]
            [react-native.blur :as blur]))

(def descriptor
  [{:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label "Card:"
    :key   :card?
    :type  :boolean}
   {:label "Icon Right:"
    :key   :icon-right?
    :type  :boolean}
   {:label   "Label:"
    :type    :select
    :key     :label
    :options [{:key   :none
               :value "None"}
              {:key   :graph
               :value "Graph"}
              {:key   :preview
               :value "Preview"}]}
   {:label   "Description:"
    :type    :select
    :key     :description
    :options [{:key   :default
               :value "Default"}
              {:key   :icon
               :value "Icon"}
              {:key   :network
               :value "Network"}
              {:key   :account
               :value "Account"}]}
   {:label   "Status:"
    :type    :select
    :key     :status
    :options [{:key   :default
               :value "Default"}
              {:key   :loading
               :value "Loading"}]}
   {:label   "Size:"
    :type    :select
    :key     :size
    :options [{:key   :default
               :value "Default"}
              {:key   :small
               :value "Small"}]}])

(def communities-list
  [{:source (resources/get-mock-image :coinbase)}
   {:source (resources/get-mock-image :decentraland)}
   {:source (resources/get-mock-image :rarible)}])

(defn preview-data-item
  []
  (let [state (reagent/atom {:on-press            #(js/alert (str "pressed"))
                             :blur?               false
                             :description         :account
                             :icon-right?         false
                             :card?               true
                             :label               :none
                             :status              :default
                             :size                :default
                             :title               "Label"
                             :subtitle            "Description"
                             :icon                :i/placeholder
                             :emoji               "🎮"
                             :customization-color :yellow
                             :communities-list    communities-list})
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:flex 1}}
        [preview/customizer state descriptor]]
       (when @blur?
         [blur/view
          {:style         {:position         :absolute
                           :left             0
                           :right            0
                           :bottom           0
                           :height           75
                           :background-color colors/neutral-80-opa-70}
           :overlay-color :transparent}])
       [rn/view
        {:style {:align-items       :center
                 :padding-vertical  10
                 :margin-horizontal 20}}
        [quo/data-item @state]]])))

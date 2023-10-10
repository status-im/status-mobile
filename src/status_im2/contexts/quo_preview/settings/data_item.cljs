(ns status-im2.contexts.quo-preview.settings.data-item
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

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
                             :communities-list    communities-list})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/data-item @state]])))

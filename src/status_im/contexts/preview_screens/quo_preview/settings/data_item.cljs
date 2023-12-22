(ns status-im.contexts.preview-screens.quo-preview.settings.data-item
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key :card? :type :boolean}
   {:key :icon-right? :type :boolean}
   {:type    :select
    :key     :label
    :options [{:key :none}
              {:key :graph}
              {:key :preview}]}
   {:type    :select
    :key     :subtitle-type
    :options [{:key :default}
              {:key :icon}
              {:key :network}
              {:key :account}]}
   {:type    :select
    :key     :status
    :options [{:key :default}
              {:key :loading}]}
   {:type    :select
    :key     :size
    :options [{:key :default}
              {:key :small}]}])

(def communities-list
  [{:source (resources/get-mock-image :coinbase)}
   {:source (resources/get-mock-image :decentraland)}
   {:source (resources/get-mock-image :rarible)}])

(defn view
  []
  (let [state (reagent/atom {:on-press            #(js/alert (str "pressed"))
                             :blur?               false
                             :subtitle-type       :account
                             :icon-right?         false
                             :card?               true
                             :label               :none
                             :status              :default
                             :size                :default
                             :title               "Label"
                             :subtitle            "Subtitle"
                             :icon                :i/placeholder
                             :right-icon          :i/chevron-right
                             :emoji               "🎮"
                             :customization-color :yellow
                             :network-image       (quo.resources/get-network :ethereum)
                             :communities-list    communities-list})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/data-item @state]])))

(ns status-im2.contexts.quo-preview.settings.data-item
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

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
    :key     :description
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

(ns status-im2.contexts.quo-preview.list-items.token-value
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :status
    :type    :select
    :options [{:key :empty}
              {:key :positive}
              {:key :negative}]}
   (preview/customization-color-option)
   {:key :metrics? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:token               {:symbol "SNT"
                                                   :name   "Status Network Token"}
                             :state               :default
                             :status              :empty
                             :customization-color :blue
                             :metrics?            true
                             :values              {:crypto-value      "0.00"
                                                   :fiat-value        "€0.00"
                                                   :percentage-change "0.00"
                                                   :fiat-change       "€0.00"}})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center
                                    :margin-top  50}}
       [quo/token-value @state]])))


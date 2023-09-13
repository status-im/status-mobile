(ns status-im2.contexts.quo-preview.list-items.account-list-card
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :action
    :type    :select
    :options [{:key :none}
              {:key :icon}]}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:account-props    {:customization-color :purple
                                                :size                32
                                                :emoji               "🍑"
                                                :type                :default
                                                :name                "Trip to Vegas"
                                                :address             "0x0ah...78b"}
                             :networks         [:ethereum :optimism]
                             :action           :none
                             :on-press         (fn [] (js/alert "Item pressed"))
                             :on-options-press (fn [] (js/alert "Options pressed"))
                             :blur?            false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/account-list-card @state]])))

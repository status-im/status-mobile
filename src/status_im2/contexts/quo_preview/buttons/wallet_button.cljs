(ns status-im2.contexts.quo-preview.buttons.wallet-button
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :disabled? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:icon          :i/placeholder
                             :on-press      #(js/alert "pressed")
                             :on-long-press #(js/alert "long pressed")})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [quo/wallet-button @state]])))

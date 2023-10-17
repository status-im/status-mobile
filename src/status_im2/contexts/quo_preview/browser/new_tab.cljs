(ns status-im2.contexts.quo-preview.browser.new-tab
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [(preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:flex        1
                 :padding-top 40
                 :align-items :center}}
        [quo/new-tab @state]]])))

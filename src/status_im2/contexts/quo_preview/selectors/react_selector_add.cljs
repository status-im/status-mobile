(ns status-im2.contexts.quo-preview.selectors.react-selector-add
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]
    [quo2.foundations.colors :as colors]))

(def descriptor
  [{:label "Pinned"
    :key   :pinned?
    :type  :boolean}
   (preview/customization-color-option
    {:label "Pinned BG (test)"})])

(defn preview-react-selector-add
  []
  (let [state (reagent/atom {:pinned?             false
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:padding-bottom 150
         :align-items    :center}
        [rn/view
         {:width            100
          :padding-vertical 60
          :border-radius    16
          :background-color (when (:pinned? @state)
                              (colors/custom-color
                               (:customization-color @state)
                               50
                               10))
          :justify-content  :space-evenly
          :flex-direction   :row
          :align-items      :center}
         [quo/react-selector-add
          (dissoc @state :customization-color)]]]])))

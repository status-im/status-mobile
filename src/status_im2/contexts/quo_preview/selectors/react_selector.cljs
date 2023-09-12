(ns status-im2.contexts.quo-preview.selectors.react-selector
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.foundations.colors :as colors]))

(def descriptor
  [{:key  :clicks
    :type :text}
   {:key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}
   {:key  :neutral?
    :type :boolean}
   {:key  :pinned?
    :type :boolean}
   (preview/customization-color-option
    {:label "Pinned BG (test)"})])

(defn preview-react-selector
  []
  (let [state (reagent/atom {:emoji               :reaction/love
                             :pinned?             false
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
         [quo/react-selector (dissoc @state :customization-color)]]]])))

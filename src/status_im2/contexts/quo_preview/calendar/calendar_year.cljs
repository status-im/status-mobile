(ns status-im2.contexts.quo-preview.calendar.calendar-year
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [quo2.core :as quo]))

(def descriptor
  [{:label "Selected?"
    :key   :selected?
    :type  :boolean}
   {:label "Disabled?"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:selected? false :disabled? false})]
    (fn
      []
      [rn/touchable-without-feedback
       {:on-press rn/dismiss-keyboard!}
       [rn/view
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/calendar-year @state "2023"]]]])))

(defn preview-calendar-year
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

(ns status-im2.contexts.quo-preview.calendar.calendar-day
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [quo2.core :as quo]))

(def descriptor
  [{:label   "State:"
    :key     :state
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :selected
               :value "Selected"}
              {:key   :disabled
               :value "Disabled"}
              {:key   :today
               :value "Today"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom
               {:state :default})]
    (fn
      []
      [rn/touchable-without-feedback
       {:on-press rn/dismiss-keyboard!}
       [rn/view
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/calendar-day (assoc @state :customization-color :blue) 12]]]])))

(defn preview-calendar-day
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

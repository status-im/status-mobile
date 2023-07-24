(ns status-im2.contexts.quo-preview.calendar.calendar-month
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [quo2.core :as quo]))

(def descriptor
  [{:label "Year"
    :key   :year
    :type  :text}
   {:label "Month"
    :key   :month
    :type  :text}])

(defn cool-preview
  []
  (let [state          (reagent/atom {:year "2023" :month "1"})
        on-change-date (fn [new-date]
                         (reset! state new-date))]
    (fn
      []
      [rn/touchable-without-feedback
       {:on-press rn/dismiss-keyboard!}
       [rn/view
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical   60
          :padding-horizontal 20}
         [quo/calendar-month
          (assoc @state
                 :on-change
                 on-change-date)]]]])))

(defn preview-calendar-month
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

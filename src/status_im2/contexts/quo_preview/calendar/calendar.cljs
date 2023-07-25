(ns status-im2.contexts.quo-preview.calendar.calendar
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [utils.datetime :as dt]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [quo2.core :as quo]))

(def descriptor
  [{:label "Start Date"
    :key   :start-date
    :type  :text}
   {:label "End Date"
    :key   :end-date
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom
               {:start-date nil :end-date nil})
        range (reagent/atom
               {:start-date nil :end-date nil})]
    (fn
      []
      [rn/touchable-without-feedback
       {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex 1}}
        [preview/customizer state descriptor]
        [rn/view
         {:padding   19
          :flex-grow 2}
         [quo/calendar
          {:start-date (:start-date @range)
           :end-date   (:end-date @range)
           :on-change  (fn [new-range]
                         (reset! state
                           {:start-date (dt/format-date (:start-date new-range))
                            :end-date   (dt/format-date (:end-date new-range))})
                         (reset! range new-range))}]]]])))

(defn preview-calendar
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [cool-preview]])

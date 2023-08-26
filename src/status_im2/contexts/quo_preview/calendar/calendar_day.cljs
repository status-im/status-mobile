(ns status-im2.contexts.quo-preview.calendar.calendar-day
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [(preview/customization-color-option)
   {:key     :state
    :type    :select
    :options [{:key :default}
              {:key :selected}
              {:key :disabled}
              {:key :today}]}])

(defn view
  []
  (let [state (reagent/atom {:state               :default
                             :customization-color :blue})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/calendar-day @state 12]])))

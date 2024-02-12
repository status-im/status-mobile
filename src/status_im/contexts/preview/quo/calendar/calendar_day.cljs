(ns status-im.contexts.preview.quo.calendar.calendar-day
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

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

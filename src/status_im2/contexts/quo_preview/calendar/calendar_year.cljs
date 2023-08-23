(ns status-im2.contexts.quo-preview.calendar.calendar-year
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :selected? :type :boolean}
   {:key :disabled? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:selected? false
                             :disabled? false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/calendar-year @state "2023"]])))

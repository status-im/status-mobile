(ns status-im2.contexts.quo-preview.calendar.calendar
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.datetime :as datetime]))

(def descriptor
  [{:key  :start-date
    :type :text}
   {:key  :end-date
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:start-date nil :end-date nil})
        range (reagent/atom {:start-date nil :end-date nil})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/calendar
        {:start-date (:start-date @range)
         :end-date   (:end-date @range)
         :on-change  (fn [new-range]
                       (reset! state
                         {:start-date (datetime/format-date (:start-date new-range))
                          :end-date   (datetime/format-date (:end-date new-range))})
                       (reset! range new-range))}]])))

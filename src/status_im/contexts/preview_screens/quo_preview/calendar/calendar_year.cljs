(ns status-im.contexts.preview-screens.quo-preview.calendar.calendar-year
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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

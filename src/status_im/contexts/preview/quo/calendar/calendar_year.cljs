(ns status-im.contexts.preview.quo.calendar.calendar-year
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

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

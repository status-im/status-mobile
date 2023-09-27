(ns status-im2.contexts.quo-preview.dividers.date
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :label :type :text}])

(defn view
  []
  (let [state (reagent/atom {:label "Today"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/divider-date (@state :label)]])))

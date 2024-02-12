(ns status-im.contexts.preview.quo.dividers.date
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :label :type :text}])

(defn view
  []
  (let [state (reagent/atom {:label "Today"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/divider-date (@state :label)]])))

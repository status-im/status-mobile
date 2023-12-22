(ns status-im.contexts.preview-screens.quo-preview.dividers.date
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :label :type :text}])

(defn view
  []
  (let [state (reagent/atom {:label "Today"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/divider-date (@state :label)]])))

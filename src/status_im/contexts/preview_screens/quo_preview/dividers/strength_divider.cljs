(ns status-im.contexts.preview-screens.quo-preview.dividers.strength-divider
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :very-weak}
              {:key :weak}
              {:key :okay}
              {:key :strong}
              {:key :very-strong}
              {:key :alert}
              {:key :info}]}
   {:label "Text (only works for info/alert)"
    :key   :text
    :type  :text}])

(defn view
  []
  (let [state (reagent/atom {:text "Common password, shouldn’t be used"
                             :type :alert})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/strength-divider
        {:type (:type @state)}
        (:text @state)]])))

(ns status-im.contexts.preview-screens.quo-preview.counter.counter
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :secondary}
              {:key :grey}
              {:key :outline}]}
   {:key  :value
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:value "5"
                             :type  :default})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/counter @state (:value @state)]])))

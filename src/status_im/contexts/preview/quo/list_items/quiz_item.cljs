(ns status-im.contexts.preview.quo.list-items.quiz-item
  (:require [quo.core :as quo]
            [utils.reagent :as reagent]
            [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :empty}
              {:key :disabled}
              {:key :success}
              {:key :error}]}
   {:key :blur? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:state  :empty
                             :word   "collapse"
                             :number 8
                             :blur?  false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/quiz-item @state]])))

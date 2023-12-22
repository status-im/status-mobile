(ns status-im.contexts.preview-screens.quo-preview.counter.step
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :neutral}
              {:key :active}
              {:key :complete}]}
   {:key  :in-blur-view?
    :type :boolean}
   {:key  :value
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:value         "5"
                             :type          :neutral
                             :in-blur-view? false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:in-blur-view? @state)
        :show-blur-background? (:in-blur-view? @state)}
       [quo/step (dissoc @state :value) (:value @state)]])))

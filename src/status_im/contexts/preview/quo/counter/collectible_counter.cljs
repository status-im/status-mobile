(ns status-im.contexts.preview.quo.counter.collectible-counter
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :status
    :type    :select
    :options [{:key :default}
              {:key :error}]}
   {:key     :size
    :type    :select
    :options [{:key :size-32}
              {:key :size-24}]}
   {:key  :value
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:value  "x500"
                             :status :default
                             :size   :size-32})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? true
        :blur?                 true}
       [quo/collectible-counter @state]])))

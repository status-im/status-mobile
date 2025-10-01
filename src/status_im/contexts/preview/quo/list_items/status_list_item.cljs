(ns status-im.contexts.preview.quo.list-items.status-list-item
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :status
    :type    :select
    :options [{:key   :positive
               :value "Positive"}
              {:key   :negative
               :value "Negative"}]}
   {:key  :title
    :type :text}
   {:key  :description
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:status      :positive
                             :title       "Account"
                             :description "This is a description"
                             :blur?       false})
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 true
        :show-blur-background? @blur?}
       [quo/status-list-item @state]])))

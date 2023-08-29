(ns status-im2.contexts.quo-preview.loaders.skeleton-list
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :content
    :type    :select
    :options [{:key :list-items}
              {:key :notifications}
              {:key :messages}]}
   {:key :blur? :type :boolean}
   {:key :animated? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:content       :messages
                             :blur?         false
                             :animated?     true
                             :parent-height 600})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/skeleton-list @state]])))

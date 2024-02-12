(ns status-im.contexts.preview.quo.loaders.skeleton-list
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :content
    :type    :select
    :options [{:key :list-items}
              {:key :notifications}
              {:key :messages}
              {:key :assets}]}
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

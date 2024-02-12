(ns status-im.contexts.preview.quo.community.discover-card
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :title :type :text}
   {:key :description :type :text}])

(defn view
  []
  (let [state (reagent/atom {:title       "Discover"
                             :description "Your favourite communities"
                             :joined?     false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/discover-card @state]])))

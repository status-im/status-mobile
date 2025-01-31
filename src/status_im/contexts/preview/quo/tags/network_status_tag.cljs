(ns status-im.contexts.preview.quo.tags.network-status-tag
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :label :type :text}])

(defn view
  []
  (let [state (reagent/atom {:label "Updated 5 min ago"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:align-items :center}}
        [quo/network-status-tag @state]]])))

(ns quo.components.community.channel-actions.view
  (:require
    [quo.components.community.channel-action.view :as channel-action]
    [react-native.core :as rn]))

(defn view
  [{:keys [container-style actions]}]
  [rn/view {:style (assoc container-style :flex-direction :row)}
   (map-indexed
    (fn [index action]
      (when action
        ^{:key index}
        [:<>
         [channel-action/view action]
         (when (not= action (last actions))
           [rn/view {:style {:width 16}}])]))
    actions)])

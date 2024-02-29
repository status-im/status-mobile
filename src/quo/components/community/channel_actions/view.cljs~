(ns quo.components.community.channel-actions
  (:require
    [quo.components.community.channel-action :as channel-action]
    [react-native.core :as rn]))

(defn view
  [{:keys [style actions]}]
  [rn/view {:style (merge {:flex-direction :row :flex 1} style)}
   (map-indexed
    (fn [index action]
      ^{:key index}
      [:<>
       [channel-action/view action]
       (when (not= action (last actions))
         [rn/view {:width 16}])])
    actions)])

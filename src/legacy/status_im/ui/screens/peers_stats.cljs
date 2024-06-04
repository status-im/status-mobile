(ns legacy.status-im.ui.screens.peers-stats
  (:require
    [react-native.core :as react-native.core]
    [utils.i18n :as i18n]
    [utils.re-frame :as re-frame]))

(defn peers-stats
  []
  (let [peers-count (re-frame/sub [:peers-count])]
    [react-native.core/view
     {:flex              1
      :margin-horizontal 8}
     [react-native.core/view
      {:style {:flex-direction  :row
               :margin-vertical 8
               :justify-content :space-between}}
      [react-native.core/text (str (i18n/label :t/peers-count) ": " peers-count)]]]))

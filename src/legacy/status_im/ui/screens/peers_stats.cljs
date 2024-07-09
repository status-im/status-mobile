(ns legacy.status-im.ui.screens.peers-stats
  (:require
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn peers-stats
  []
  (let [peers-count (rf/sub [:peer-stats/count])]
    (rn/use-mount
     (fn []
       (rf/dispatch [:peer-stats/get-count])))

    [rn/view
     {:style {:flex              1
              :margin-horizontal 8}}
     [rn/view
      {:style {:flex-direction  :row
               :margin-vertical 8
               :justify-content :space-between}}
      [rn/text (str (i18n/label :t/peers-count) ": " peers-count)]]]))

(ns quo2.components.user-avatar
  (:require [clojure.string :refer [split]]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(defn preview-user-avatar
  [{:keys [ring? online? size status-indicator? profile-picture full-name] :or {full-name "John Doe"}}]
  (let [initials (reduce str (map first (split full-name " ")))]
    [rn/view {:style {:width 80
                    :height 80
                    :border-radius 80}}
   [icons/icon :main-icons/identicon-ring {:width 80
                                           :height 80
                                           :color "nil"}]
   [rn/view {:style {:background-color colors/turquoise-50
                     :width 70
                     :position :absolute
                     :top 5
                     :left 5
                     :height 70
                     :border-radius 70
                     :justify-content :center
                     :align-items :center}}
    [rn/text {:style {:color colors/white-opa-70
                      :font-size 27}} initials]
    [rn/view {:style {:background-color colors/success-50
                      :width 16
                      :height 16
                      :border-width 3
                      :border-radius 16
                      :border-color "white"
                      :position :absolute
                      :bottom 2
                      :right 4}}]]]))
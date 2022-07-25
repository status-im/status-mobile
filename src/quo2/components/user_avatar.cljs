(ns quo2.components.user-avatar
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]))

(defn preview-user-avatar
  [{:keys [ring? online? size status-indicator? profile-picture full-name]}]
  [rn/view {:style {:width 80
                    :height 80
                    :border-radius 80}}
   [icons/icon :main-icons/identicon-ring80 {:width 80
                                             :height 80}]
   [rn/view {:style {:background-color "black"
                     :width 60
                     :height 60
                     :border-radius 60}}]])
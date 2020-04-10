(ns quo.previews.header
  (:require [quo.core :as quo]
            [quo.react-native :as rn]))

(def accessories [nil
                  [{:icon     :main-icons/close
                    :on-press identity}]
                  [{:icon     :main-icons/close
                    :on-press identity}
                   {:icon     :main-icons/add
                    :on-press identity}]
                  [{:icon     :main-icons/add
                    :on-press identity}
                   {:label    "Text"
                    :on-press identity}]
                  [{:label    "Text"
                    :on-press identity}]])

(defn preview-header []
  [rn/scroll-view {:flex 1}
   (for [left-accessories  accessories
         right-accessories accessories
         title             [nil "This is a title" "This is a very long super title"]
         subtitle          [nil "This is a subtitle"]
         title-align       [:left :center]]
     [rn/view {:border-bottom-color "#EEF2F5"
               :border-bottom-width 2}
      [quo/header {:left-accessories  left-accessories
                   :right-accessories right-accessories
                   :title             title
                   :subtitle          subtitle
                   :title-align       title-align}]])])

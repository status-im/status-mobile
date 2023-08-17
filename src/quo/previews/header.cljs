(ns quo.previews.header
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn])
  (:require-macros [quo.previews.preview :as preview]))

(def accessories
  [nil
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

(def all-props
  (preview/list-comp [left-accessories  accessories
                      right-accessories accessories
                      title             [nil "This is a title" "This is a very long super title"]
                      subtitle          [nil "This is a subtitle"]
                      title-align       [:left :center]]
    {:left-accessories  left-accessories
     :right-accessories right-accessories
     :title             title
     :subtitle          subtitle
     :title-align       title-align}))

(defn render-item
  [props]
  [rn/view
   {:border-bottom-color "#EEF2F5"
    :border-bottom-width 2}
   [quo/header props]])

(defn preview-header
  []
  [rn/view
   {:background-color (:ui-background @colors/theme)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :data                         all-props
     :render-fn                    render-item
     :key-fn                       str}]])

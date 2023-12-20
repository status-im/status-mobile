(ns legacy.status-im.ui.components.emoji-thumbnail.color-picker
  (:require
    [legacy.status-im.ui.components.emoji-thumbnail.styles :as styles]
    [react-native.core :as rn]))

(def emoji-picker-colors-row1
  [{:name "red" :color "#F5A3A3" :key "1"}
   {:name "pink" :color "#F5A3BF" :key "2"}
   {:name "magenta" :color "#E9A3F5" :key "3"}
   {:name "purple" :color "#C0A3F5" :key "4"}
   {:name "indigo" :color "#A3B0F5" :key "5"}
   {:name "blue" :color "#A3C2F5" :key "6"}
   {:name "cyan" :color "#A3DCF5" :key "7"}])

(def emoji-picker-colors-row2
  [{:name "teal" :color "#A3ECF5" :key "8"}
   {:name "mint" :color "#A3F5E2" :key "9"}
   {:name "green" :color "#A3F5BA" :key "10"}
   {:name "moss" :color "#CFF5A3" :key "11"}
   {:name "lemon" :color "#EEF5A3" :key "12"}
   {:name "yellow" :color "#F5F5A3" :key "13"}])

(def emoji-picker-colors-row3
  [{:name "honey" :color "#F5E4A3" :key "14"}
   {:name "orange" :color "#F5D7A3" :key "15"}
   {:name "peach" :color "#F5B6A3" :key "16"}
   {:name "brown" :color "#E0C2B8" :key "17"}
   {:name "grey" :color "#CCCCCC" :key "18"}
   {:name "dove" :color "#DAE2E7" :key "19"}
   {:name "white" :color "#FFFFFF" :key "20"}])

(defn colors-row
  [color-circle container-style colors]
  [rn/view {:style container-style :accessibility-label :colors-row}
   (for [x colors]
     [color-circle x])])

(defn color-picker-section
  [color-circle]
  [:<>
   [colors-row ;; Row - 1st
    color-circle
    (merge styles/emoji-picker-color-row-container styles/emoji-picker-row1-style)
    emoji-picker-colors-row1]
   [colors-row ;; Row - 2nd
    color-circle
    (merge styles/emoji-picker-color-row-container styles/emoji-picker-row2-style)
    emoji-picker-colors-row2]
   [colors-row ;; Row - 3rd
    color-circle
    (merge styles/emoji-picker-color-row-container styles/emoji-picker-row3-style)
    emoji-picker-colors-row3]])

(ns fiddle.views.colors
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn color [name color color-name usage & [bg?]]
  [react/view {:margin 10 :padding 10 :border-radius 8 :background-color (when bg? colors/blue)}
   [react/text {:style {:typography :main-medium :color (when bg? colors/white)}} name]
   [react/view {:margin-vertical 24 :width 156 :height 156 :border-radius 80 :background-color color
                :border-width    1 :border-color colors/gray}]
   [react/text  {:style {:color (when bg? colors/white)}} color-name]
   [react/text {:style {:color (when bg? colors/white)}} color]
   [react/text {:style {:margin-top 6 :color (if bg? colors/white-transparent colors/gray) :font-size 12}} usage]])

(defn colors-arr [title colors]
  [react/view
   [react/text title]
   [react/view {:flex-direction :row :flex-wrap :wrap :flex 1 :margin-vertical 24}
    (for [color colors]
      [react/view {:margin-left 10 :width 40 :height 40 :border-radius 20 :background-color color}])]])

(defn colors []
  [react/scroll-view
   [react/view {:flex-direction :row :flex-wrap :wrap :flex 1}
    [color "Accent Blue" colors/blue "blue" "Links, buttons"]
    [color "Light Blue" colors/blue-light "blue-light" "Button background"]
    [color "White" colors/white "white" "Background, icons"]
    [color "Dark Grey" colors/gray "gray" "Secondary text"]
    [color "Light Grey" colors/gray-lighter "gray-lighter" "Background, inputs, dividers"]
    [color "Black" colors/black "black" "Icons"]
    [color "Red" colors/red "red" "Errors"]
    [color "Green" colors/green "green" "Success"]
    [color "40% of Dark Grey" colors/gray-transparent-40 "gray-transparent-40" "Chevrons in lists (>) "]
    [color "10% of Black" colors/black-transparent "black-transparent" ""]
    [color "70% of White" colors/white-transparent-70 "white-transparent-70" "Secondary text on blue background" true]
    [color "40% of White" colors/white-transparent "white-transparent" "Chevrons in lists (>)  on the\nblue background\nText in Wallet\n\n" true]
    [color "10% of White" colors/white-transparent-10 "white-transparent-10" "Backgrounds behind icons" true]]
   [colors-arr "Chat colors" colors/chat-colors]
   [colors-arr "Account colors" colors/account-colors]])

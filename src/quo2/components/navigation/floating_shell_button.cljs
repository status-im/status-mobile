(ns quo2.components.navigation.floating-shell-button
  (:require [react-native.core :as rn]
            [quo2.components.buttons.dynamic-button :as dynamic-button]))

(defn dynamic-button-view [type dynamic-buttons style]
  (when-let [{:keys [count on-press customization-color label]} (get dynamic-buttons type)]
    [dynamic-button/dynamic-button
     {:type                type
      :label               label
      :on-press            on-press
      :count               count
      :style               style
      :customization-color customization-color}]))

(defn floating-shell-button
  "[floating-shell-button dynamic-buttons style]
  dynamic-buttons
  {:button-type {:on-press on-press :count count}}"
  [dynamic-buttons style]
  [rn/view {:style (merge {:flex-direction    :row
                           :margin-horizontal 12} style)}
   ;; Left Section
   [rn/view {:style {:flex 1}}
    [dynamic-button-view :search dynamic-buttons {:position :absolute
                                                  :right    8}]]
   ;; Mid Section (jump-to)
   [dynamic-button-view :jump-to dynamic-buttons nil]
   ;; Right Section   
   [rn/view {:style {:flex 1}}
    [rn/view {:style {:position       :absolute
                      :flex-direction :row
                      :right          0}}
     [dynamic-button-view :mention dynamic-buttons {:margin-left 8}]
     [dynamic-button-view :notification-down dynamic-buttons {:margin-left 8}]
     [dynamic-button-view :notification-up dynamic-buttons {:margin-left 8}]
     [dynamic-button-view :bottom dynamic-buttons {:margin-left 8}]]]])

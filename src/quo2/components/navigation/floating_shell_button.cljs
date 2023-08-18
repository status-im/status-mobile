(ns quo2.components.navigation.floating-shell-button
  (:require [quo2.components.buttons.dynamic-button.view :as dynamic-button]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]))

(defn dynamic-button-view
  [type dynamic-buttons style]
  (when-let [{:keys [on-press customization-color label] :as props} (get dynamic-buttons type)]
    [dynamic-button/view
     {:type                type
      :label               label
      :on-press            on-press
      :count               (:count props)
      :style               style
      :customization-color customization-color}]))

(defn- section
  [children]
  [rn/view {:style {:flex 1} :pointer-events :box-none} children])

(defn- f-floating-shell-button
  [dynamic-buttons style opacity-anim]
  (let [original-style (merge {:flex-direction    :row
                               :margin-horizontal 12
                               :pointer-events    :box-none}
                              style)
        animated-style (reanimated/apply-animations-to-style
                        (if opacity-anim
                          {:opacity opacity-anim}
                          {})
                        original-style)]
    [reanimated/view {:style animated-style}
     ;; Left Section
     [section
      [dynamic-button-view :search dynamic-buttons
       {:position :absolute
        :right    8}]]
     ;; Mid Section (jump-to)
     [dynamic-button-view :jump-to dynamic-buttons nil]
     ;; Right Section
     [section
      [rn/view
       {:style {:position       :absolute
                :flex-direction :row
                :right          0}}
       [dynamic-button-view :mention dynamic-buttons {:margin-left 8}]
       [dynamic-button-view :notification-down dynamic-buttons {:margin-left 8}]
       [dynamic-button-view :notification-up dynamic-buttons {:margin-left 8}]
       [dynamic-button-view :scroll-to-bottom dynamic-buttons {:margin-left 8}]]]]))

(defn floating-shell-button
  "[floating-shell-button dynamic-buttons style opacity-anim pointer-anim]
  dynamic-buttons {:button-type {:on-press on-press :count count}}
  style           override style
  opacity-anim    reanimated value (optional)"
  ([dynamic-buttons style]
   [:f> f-floating-shell-button dynamic-buttons style nil])
  ([dynamic-buttons style opacity-anim]
   [:f> f-floating-shell-button dynamic-buttons style opacity-anim]))

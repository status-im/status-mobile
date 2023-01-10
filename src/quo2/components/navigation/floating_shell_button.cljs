(ns quo2.components.navigation.floating-shell-button
  (:require [quo2.components.buttons.dynamic-button :as dynamic-button]
            [react-native.core :as rn]
            [react-native.reanimated :as ra]))

(defn dynamic-button-view
  [type dynamic-buttons style]
  (when-let [{:keys [count on-press customization-color label]} (get dynamic-buttons type)]
    [dynamic-button/dynamic-button
     {:type                type
      :label               label
      :on-press            on-press
      :count               count
      :style               style
      :customization-color customization-color}]))

(defn floating-shell-button
  "[floating-shell-button dynamic-buttons style opacity-anim pointer-anim]
  dynamic-buttons {:button-type {:on-press on-press :count count}}
  style           override style
  opacity-anim    reanimated value (optional)"
  ([dynamic-button style]
   (floating-shell-button dynamic-button style nil))
  ([dynamic-buttons style opacity-anim]
   [:f>
    (fn []
      (let [original-style (merge {:flex-direction    :row
                                   :margin-horizontal 12
                                   :pointer-events    :box-none}
                                  style)
            animated-style (ra/apply-animations-to-style
                            (if opacity-anim
                              {:opacity opacity-anim}
                              {})
                            original-style)]
        [ra/view {:style animated-style}
         ;; Left Section
         [rn/view {:style {:flex 1}}
          [dynamic-button-view :search dynamic-buttons
           {:position :absolute
            :right    8}]]
         ;; Mid Section (jump-to)
         [dynamic-button-view :jump-to dynamic-buttons nil]
         ;; Right Section
         [rn/view {:style {:flex 1}}
          [rn/view
           {:style {:position       :absolute
                    :flex-direction :row
                    :right          0}}
           [dynamic-button-view :mention dynamic-buttons {:margin-left 8}]
           [dynamic-button-view :notification-down dynamic-buttons {:margin-left 8}]
           [dynamic-button-view :notification-up dynamic-buttons {:margin-left 8}]
           [dynamic-button-view :scroll-to-bottom dynamic-buttons {:margin-left 8}]]]]))]))

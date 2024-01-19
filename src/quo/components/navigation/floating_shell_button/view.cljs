(ns quo.components.navigation.floating-shell-button.view
  (:require
    [quo.components.buttons.dynamic-button.view :as dynamic-button]
    [quo.components.navigation.floating-shell-button.style :as style]
    [react-native.pure :as rn.pure]
    [react-native.reanimated :as reanimated]))

(defn dynamic-button-view
  [type dynamic-buttons style]
  (when-let [{:keys [on-press customization-color label] :as props} (get dynamic-buttons type)]
    (dynamic-button/view
     {:type                type
      :label               label
      :on-press            on-press
      :count               (:count props)
      :style               style
      :customization-color customization-color})))

(defn- section
  [children]
  (rn.pure/view {:style {:flex 1} :pointer-events :box-none} children))

(defn- floating-shell-button-pure
  [dynamic-buttons style opacity-anim]
  (reanimated/view-pure
   {:style (style/floating-shell-button style opacity-anim)}
   ;; Left Section
   (section
    (dynamic-button-view :search
                         dynamic-buttons
                         {:position :absolute
                          :right    8}))
   ;; Mid Section (jump-to)
   (dynamic-button-view :jump-to dynamic-buttons nil)
   ;; Right Section
   (section
    (rn.pure/view
     {:style style/right-section}
     (dynamic-button-view :mention dynamic-buttons {:margin-left 8})
     (dynamic-button-view :notification-down dynamic-buttons {:margin-left 8})
     (dynamic-button-view :notification-up dynamic-buttons {:margin-left 8})
     (dynamic-button-view :scroll-to-bottom dynamic-buttons {:margin-left 8})))))

(defn view
  "[floating-shell-button dynamic-buttons style opacity-anim pointer-anim]
  dynamic-buttons {:button-type {:on-press on-press :count count}}
  style           override style
  opacity-anim    reanimated value (optional)"
  ([dynamic-buttons style]
   (rn.pure/func floating-shell-button-pure dynamic-buttons style nil))
  ([dynamic-buttons style opacity-anim]
   (rn.pure/func floating-shell-button-pure dynamic-buttons style opacity-anim)))

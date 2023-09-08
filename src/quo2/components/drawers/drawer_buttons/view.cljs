(ns quo2.components.drawers.drawer-buttons.view
  (:require [react-native.core :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [quo2.components.drawers.drawer-buttons.style :as style]
            [react-native.reanimated :as reanimated]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]))

(def default-height 216)

(defn render-bottom
  [children]
  [rn/view {:style style/bottom-container}
   children
   [rn/view
    {:style style/bottom-icon}
    [icon/icon :arrow-right
     {:color colors/white
      :size  20}]]])

(defn label? [el] (or (string? el) (keyword? el)))

(defn render-children-bottom
  [children]
  (if (label? children)
    [render-bottom
     [text/text
      {:size   :paragraph-2
       :style  style/bottom-text
       :weight :semi-bold}
      children]]
    [render-bottom children]))

(defn- f-render-children-top
  [children top-children-opacity]
  (let [scale        (reanimated/interpolate top-children-opacity [1 0] [1 1.1])
        padding-left (reanimated/interpolate scale [1 1.1] [0 14])]
    [reanimated/view
     {:style (reanimated/apply-animations-to-style
              {:opacity   top-children-opacity
               :transform [{:scale scale}
                           {:translate-x padding-left}]}
              {})}
     (if (label? children)
       [text/text
        {:size   :paragraph-2
         :style  style/top-text
         :weight :semi-bold}
        children]
       children)]))

(defn- f-card
  [{:keys [on-press default-on-press style heading gap accessibility-label top? top-title-opacity
           top-children-opacity
           animated-heading]
    :or   {top-title-opacity (reanimated/use-shared-value 1)}}
   children]
  [rn/touchable-highlight
   {:accessibility-label accessibility-label
    :on-press            (fn []
                           (when on-press
                             (on-press))
                           (when default-on-press
                             (default-on-press)))
    :border-radius       20
    :style               style
    :underlay-color      (:background-color style)}
   [rn/view
    [reanimated/view
     {:style (reanimated/apply-animations-to-style
              {:opacity top-title-opacity}
              {})}
     [text/text
      {:size   :heading-1
       :style  (style/heading-text gap)
       :weight :semi-bold}
      heading]]
    (when animated-heading
      (let [animated-heading-opacity (reanimated/interpolate top-children-opacity [1 0] [0 1])]
        [reanimated/view
         {:style (reanimated/apply-animations-to-style
                  {:opacity top-title-opacity}
                  {:position :absolute})}
         [reanimated/view
          {:style (reanimated/apply-animations-to-style
                   {:opacity animated-heading-opacity}
                   {})}
          [text/text
           {:size   :heading-1
            :style  (style/heading-text gap)
            :weight :semi-bold}
           animated-heading]]]))
    (if top?
      [:f> f-render-children-top children top-children-opacity]
      [render-children-bottom children])]])

(defn card
  [props children]
  [:f> f-card props children])

(defn f-view
  "[view opts]
   opts
   {:container-style  style-object
    :top-card         { :on-press event
                        :heading  string
                        :accessibility-label keyword}
    :bottom-card      { :on-press event
                        :heading  string
                        :accessibility-label keyword}}
    child-1           string, keyword or hiccup
    child-2           string, keyword or hiccup
   "
  [{:keys [container-style top-card bottom-card on-init animations-duration animations-delay]}
   child-1
   child-2]
  (let [max-height           (+ (:height (rn/get-window)) (if platform/android? (safe-area/get-top) 0))
        height               (reanimated/use-shared-value default-height)
        top-padding          (reanimated/use-shared-value 12)
        border-radius        (reanimated/use-shared-value 20)
        bottom-view-top      (reanimated/use-shared-value 80)
        top-title-opacity    (reanimated/use-shared-value 1)
        top-children-opacity (reanimated/use-shared-value 1)
        animations-delay     (/ animations-delay 1.4)
        start-top-animation  (fn []
                               (reanimated/animate-shared-value-with-delay bottom-view-top
                                                                           (:height (rn/get-screen))
                                                                           animations-duration
                                                                           :easing4
                                                                           animations-delay)
                               (reanimated/animate-shared-value-with-delay
                                height
                                max-height
                                animations-duration
                                :easing4
                                animations-delay)
                               (reanimated/animate-shared-value-with-delay
                                top-padding
                                (+ 68 (safe-area/get-top))
                                animations-duration
                                :easing4
                                animations-delay)
                               (reanimated/animate-shared-value-with-delay
                                top-children-opacity
                                0
                                animations-duration
                                :easing4
                                animations-delay)
                               (reanimated/animate-shared-value-with-delay
                                top-title-opacity
                                0
                                0
                                :linear
                                (+ animations-delay animations-duration 500)))
        reset-top-animation  (fn []
                               (reanimated/set-shared-value top-title-opacity 1)
                               (reanimated/animate-shared-value-with-delay bottom-view-top
                                                                           80
                                                                           animations-duration
                                                                           :easing4
                                                                           50)
                               (reanimated/animate-shared-value-with-timing
                                height
                                default-height
                                animations-duration
                                :easing4)
                               (reanimated/animate-shared-value-with-timing
                                top-padding
                                12
                                animations-duration
                                :easing4)
                               (reanimated/animate-shared-value-with-timing
                                top-children-opacity
                                1
                                animations-duration
                                :easing4))]
    (rn/use-effect (fn []
                     (when on-init
                       (on-init reset-top-animation))))
    [reanimated/view {:style (style/outer-container height border-radius container-style)}
     [blur/view
      {:blur-type   :dark
       :blur-amount 10
       :style       style/blur-view-style}
      [rn/view
       {:style style/blur-content-style}
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:padding-top top-padding}
                 style/top-card)}
        [card
         (merge {:gap                  4
                 :top?                 true
                 :style                {:flex 1}
                 :top-children-opacity top-children-opacity
                 :top-title-opacity    top-title-opacity
                 :default-on-press     start-top-animation}
                top-card)
         child-1]]
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:top bottom-view-top}
                 style/bottom-card)}
        [card
         (merge {:style {:flex 1}
                 :gap   10}
                bottom-card)
         child-2]]]]]))

(defn view
  [props child-1 child-2]
  [:f> f-view props child-1 child-2])

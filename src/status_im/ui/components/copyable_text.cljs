(ns status-im.ui.components.copyable-text
  (:require [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.typography :as typography]))

(defn hide-cue-atom [anim-opacity anim-y cue-atom]
  (animation/start
   (animation/parallel
    [(animation/timing
      anim-opacity
      {:toValue         0
       :duration        140
       :delay           1000
       :easing          (.-ease (animation/easing))
       :useNativeDriver true})
     (animation/timing
      anim-y
      {:toValue         0
       :duration        140
       :delay           1000
       :easing          (.-ease (animation/easing))
       :useNativeDriver true})])
   #(reset! cue-atom false)))

(defn show-cue-atom [anim-opacity anim-y cue-atom y]
  (when @cue-atom
    (animation/start
     (animation/parallel
      [(animation/timing
        anim-opacity
        {:toValue         1
         :duration        140
         :easing          (.-ease (animation/easing))
         :useNativeDriver true})
       (animation/timing
        anim-y
        {:toValue         y
         :duration        140
         :easing          (.-ease (animation/easing))
         :useNativeDriver true})])
     #(hide-cue-atom anim-opacity anim-y cue-atom))))

(defn copy-action-visual-cue [anim-opacity anim-y width cue-atom]
  [react/animated-view
   {:style
    {:opacity          anim-opacity
     :transform        [{:translateY anim-y}]
     :max-width        @width
     :z-index          (if @cue-atom 1 -1)
     :height           34
     :position         :absolute
     :border-radius    8
     :align-self       :center
     :align-items      :center
     :justify-content  :center
     :shadow-offset    {:width 0 :height 4}
     :shadow-radius    12
     :elevation        8
     :shadow-opacity   1
     :shadow-color     "rgba(0, 34, 51, 0.08)"
     :background-color colors/white}}
   [react/view
    {:padding-horizontal 16
     :padding-vertical   7
     :border-radius      8
     :background-color   colors/white
     :shadow-offset      {:width 0 :height 2}
     :shadow-radius      4
     :shadow-opacity     1
     :shadow-color       "rgba(0, 34, 51, 0.16)"}
    [react/text
     {:style
      {:typography  :main-medium
       ;; line height specified here because of figma spec
       :line-height 20
       :font-size   14}}
     (i18n/label :sharing-copied-to-clipboard)]]])

(defn copyable-text-view [{:keys [label copied-text container-style]}
                          content]
  (let [cue-atom     (reagent/atom false)
        width        (reagent/atom 0)
        height       (reagent/atom 0)
        anim-y       (animation/create-value 0)
        anim-opacity (animation/create-value 0)]
    (reagent/create-class
     {:reagent-render
      (fn [{:keys [copied-text]} _]
        [react/view
         {:style (if container-style container-style {})
          :on-layout
          #(do
             (reset! width (-> % .-nativeEvent .-layout .-width))
             (reset! height (-> % .-nativeEvent .-layout .-height)))}
         (when label
           [react/text
            {:style
             {:font-size     13
              ;; line height specified here because of figma spec
              :line-height   18
              :font-weight   "500"
              :color         colors/gray
              :margin-bottom 4}}
            (i18n/label label)])
         [copy-action-visual-cue anim-opacity anim-y width cue-atom]
         [react/touchable-highlight
          {:active-opacity (if @cue-atom 1 0.85)
           :underlay-color colors/black
           :on-press
           #(when (not @cue-atom)
              (reset! cue-atom true)
              (show-cue-atom
               anim-opacity
               anim-y
               cue-atom
               (if (> @height 34)
                 (- (/ @height 2))
                 (- (+ 17 @height))))
              (react/copy-to-clipboard copied-text))}
          [react/view {:background-color colors/white}
           content]]])})))

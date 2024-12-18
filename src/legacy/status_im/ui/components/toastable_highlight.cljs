(ns legacy.status-im.ui.components.toastable-highlight
  "A wrapped touchable highlight that presents a toast when clicked"
  (:require
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [reagent.core :as reagent]))

(defn hide-cue-atom
  [anim-opacity anim-y cue-atom]
  (animation/start
   (animation/parallel
    [(animation/timing
      anim-opacity
      {:toValue         0
       :duration        140
       :delay           1000
       :easing          (.-ease ^js animation/easing)
       :useNativeDriver true})
     (animation/timing
      anim-y
      {:toValue         0
       :duration        140
       :delay           1000
       :easing          (.-ease ^js animation/easing)
       :useNativeDriver true})])
   #(reset! cue-atom false)))

(defn show-cue-atom
  [anim-opacity anim-y cue-atom y]
  (when @cue-atom
    (animation/start
     (animation/parallel
      [(animation/timing
        anim-opacity
        {:toValue         1
         :duration        140
         :easing          (.-ease ^js animation/easing)
         :useNativeDriver true})
       (animation/timing
        anim-y
        {:toValue         y
         :duration        140
         :easing          (.-ease ^js animation/easing)
         :useNativeDriver true})])
     #(hide-cue-atom anim-opacity anim-y cue-atom))))

(defn toast
  [anim-opacity anim-y width cue-atom label]
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
     label]]])

(defn toastable-highlight-view
  [{:keys [toast-label on-press
           container-style]}
   content]
  (let [cue-atom     (reagent/atom false)
        width        (reagent/atom 0)
        height       (reagent/atom 0)
        anim-y       (animation/create-value 0)
        anim-opacity (animation/create-value 0)]
    (reagent/create-class
     {:reagent-render
      (fn [{:keys []} _]
        (let [press-fn #(when (not @cue-atom)
                          (reset! cue-atom true)
                          (show-cue-atom
                           anim-opacity
                           anim-y
                           cue-atom
                           (if (> @height 34)
                             (- (/ @height 2))
                             (- (+ 17 @height))))
                          (when on-press
                            (on-press)))]
          [react/view
           {:style (if container-style container-style {})
            :on-layout
            #(do
               (reset! width (-> ^js % .-nativeEvent .-layout .-width))
               (reset! height (-> ^js % .-nativeEvent .-layout .-height)))}
           [toast anim-opacity anim-y width cue-atom toast-label]
           [react/touchable-highlight
            {:active-opacity (if @cue-atom 1 0.85)
             :underlay-color colors/black
             :on-press       press-fn
             :on-long-press  press-fn}
            [react/view {:background-color colors/white}
             content]]]))})))

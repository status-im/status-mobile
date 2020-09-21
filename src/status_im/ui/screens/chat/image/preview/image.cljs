(ns status-im.ui.screens.chat.image.preview.image
  (:require [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo.animated :as animated]
            [quo.vectors :as vec]
            [quo.gesture-handler :as gh]))

(def min-scale 1)
(def max-scale 3)

(defn decay
  [position velocity clock]
  (let [state  #js {:finished (animated/value 0)
                    :position (animated/value 0)
                    :time     (animated/value 0)
                    :velocity (animated/value 0)}
        config #js {:deceleration 0.998}]
    (animated/block
     [(animated/cond* (animated/not* (animated/clock-running clock))
                      [(animated/set (.-finished state) 0)
                       (animated/set (.-position state) position)
                       (animated/set (.-velocity state) velocity)
                       (animated/set (.-time state) 0)
                       (animated/start-clock clock)])
      (animated/decay clock state config)
      (.-position state)])))

(defn decay-vector
  [position velocity clock]
  (let [x (decay (.-x position) (.-x velocity) (.-x clock))
        y (decay (.-y position) (.-y velocity) (.-y clock))]
    #js {:x x
         :y y}))

(defn use-pinch
  [pinch pan translate translation-y scale min-vec max-vec center]
  (let [should-decay  (animated/use-value 0)
        clock         (vec/create (animated/clock) (animated/clock))
        offset        (vec/create-value 0 0)
        scale-offset  (animated/value 1)
        origin        (vec/create-value 0 0)
        translation   (vec/create-value 0 0)
        adjustedFocal (vec/sub (:focal pinch) (vec/add center offset))
        clamped       (vec/sub
                       (vec/clamp (vec/add offset (:translation pan)) min-vec max-vec)
                       offset)
        pinch-began   (animated/pinch-began (:state pinch))
        pinch-active  (animated/pinch-active (:state pinch) (:number-of-pointers pinch))
        pinch-end     (animated/pinch-end (:state pinch) (:number-of-pointers pinch))]
    (animated/code!
     (fn []
       (animated/block
        [(animated/cond* (animated/eq (:state pan) (:active gh/states))
                         [(animated/set translation-y (animated/sub (.-y (:translation pan)) (.-y clamped)))
                          (vec/set translation clamped)])
         (animated/cond* pinch-began (vec/set origin adjustedFocal))
         (animated/cond* pinch-active
                         (vec/set
                          translation
                          (vec/add
                           (vec/sub adjustedFocal origin)
                           origin
                           (vec/multiply -1 (:scale pinch) origin))))
         (animated/cond* (animated/and*
                          (animated/or* (animated/eq (:state pinch) (:undetermined gh/states))
                                        pinch-end)
                          (animated/or*
                           (animated/eq (:state pan) (:undetermined gh/states))
                           (animated/eq (:state pan) (:end gh/states))))
                         [(animated/set scale-offset scale)
                          (animated/cond* (animated/and*
                                           (animated/not* (animated/less scale-offset min-scale))
                                           (animated/not* (animated/greater scale-offset max-scale)))
                                          (vec/set offset (vec/add offset translation)))
                          (animated/set (:scale pinch) 1)
                          (vec/set translation 0)
                          (vec/set (:focal pinch) 0)
                          (animated/cond* (animated/less scale-offset min-scale)
                                          (animated/set scale-offset min-scale))
                          (animated/cond* (animated/greater scale-offset max-scale)
                                          (animated/set scale-offset max-scale))])
         (animated/cond* (animated/or* (animated/eq (:state pan) (:active gh/states))
                                       pinch-active)
                         [(animated/stop-clock (.-x clock))
                          (animated/stop-clock (.-y clock))
                          (animated/set should-decay 0)])
         (animated/cond* (animated/and*
                          (animated/neq (animated/diff (:state pan)) 0)
                          (animated/eq (:state pan) (:end gh/states))
                          (animated/not* pinch-active))
                         (animated/set should-decay 1))
         (animated/cond* should-decay
                         (vec/set offset
                                  (vec/clamp
                                   (decay-vector offset (:velocity pan) clock)
                                   min-vec
                                   max-vec)))
         (animated/set scale (animated/multiply (:scale pinch) scale-offset))
         (vec/set translate (vec/add translation offset))]))
     [])))

(defn pinch-zoom [props]
  (let [{uri           :uri
         on-close      :onClose
         screen-height :screenHeight
         screen-width  :screenWidth
         width         :width
         height        :height}   (bean/bean props)
        pinch-ref                 (react/create-ref nil)
        pan-ref                   (react/create-ref nil)
        {:keys [gesture-handler]
         :as   pinch}             (animated/use-pinch-gesture-handler)
        {pan-gesture-handler :gesture-handler
         :as                 pan} (animated/use-pan-gesture-handler)
        translate                 (vec/create-value 0 0)
        translation-y             (animated/use-value 0)
        translate-y               (animated/use-value 0)
        scale                     (animated/use-value 1)
        clock                     (animated/use-clock)
        swipe-scale               (animated/interpolate translate-y
                                                        {:inputRange  [0 screen-height]
                                                         :outputRange [1 0.1]
                                                         :extrapolate (:clamp animated/extrapolate)})
        canvas                    (vec/create screen-width screen-height)
        center                    (vec/divide canvas 2)
        min-vec                   (vec/min* (vec/multiply -0.5 canvas (animated/sub scale 1)) 0)
        max-vec                   (vec/max* (vec/minus min-vec) 0)
        snap-to                   (animated/snap-point translate-y (.-x (:velocity pan)) [0 (.-y center)])]

    (use-pinch pinch pan translate translation-y scale min-vec max-vec center)
    (animated/code!
     (fn []
       (animated/block
        [(animated/on-change
          translation-y
          (animated/cond* (animated/eq (:state pan) (:active gh/states))
                          (animated/set translate-y (animated/clamp translation-y 0 screen-height))))
         (animated/cond* (animated/and* (animated/eq (:state pan) (:end gh/states))
                                        (animated/neq translation-y 0))
                         [(animated/set translate-y (animated/re-timing {:clock clock
                                                                         :from  translate-y
                                                                         :to    snap-to}))
                          (animated/cond* (animated/and* (animated/not* (animated/clock-running clock))
                                                         (animated/eq translate-y (.-y center)))
                                          [(animated/call* [] on-close)])])]))
     [on-close])

    (reagent/as-element
     [animated/view {:style {:align-items     :center
                             :justify-content :center
                             :flex            1
                             :width           screen-width
                             :height          screen-height}}
      [animated/view {:style {:position         "absolute"
                              :top              0
                              :bottom           0
                              :left             0
                              :right            0
                              :opacity          (animated/interpolate translate-y
                                                                      {:inputRange  [0 (* screen-height 0.25)]
                                                                       :outputRange [1 0.1]
                                                                       :extrapolate (:clamp animated/extrapolate)})
                              :background-color :black}}]
      [gh/pan-gesture-handler (merge {:ref                  pan-ref
                                      :min-dist             10
                                      :avg-touches          true
                                      :simultaneousHandlers pinch-ref}
                                              pan-gesture-handler)
       [animated/view {:style {:position "absolute"
                               :top      0
                               :bottom   0
                               :left     0
                               :right    0}}
        [gh/pinch-gesture-handler (merge {:ref                  pinch-ref
                                          :simultaneousHandlers pan-ref}
                                         gesture-handler)
         [animated/view {:style {:position        "absolute"
                                 :top             0
                                 :bottom          0
                                 :left            0
                                 :right           0
                                 :overflow        "hidden"
                                 :transform       [{:scale swipe-scale} 
                                                   {:translateY (animated/multiply translate-y
                                                                                   (animated/sub 2 swipe-scale))}]
                                 :justify-content :center
                                 :align-items     :center}}
          (when (string? uri)
            [animated/image {:source {:uri uri}
                             :style  {:resize-mode "contain"
                                      :width       width
                                      :height      height
                                      :transform   [{:translateX (.-x translate)}
                                                    {:translateY (.-y translate)}
                                                    {:scale scale}]}}])]]]]])))

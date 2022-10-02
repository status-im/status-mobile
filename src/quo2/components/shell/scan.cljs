(ns quo2.components.shell.scan
  (:require [quo2.foundations.colors :as colors]
            [quo.react :as react]
            [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.icon :as icons]
            [status-im.ui.components.react :as rn-comps]
            [quo2.reanimated :as reanimated]
            [status-im.utils.dimensions :as dimensions]))

(defn corner [outside-border]
  (let [{:keys [width height]} (dimensions/window)]
    [rn/view {:style (merge {:border-color     colors/white
                             :border-width     2
                             :width            (* 0.20 width)
                             :height           (* 0.12 height)
                             :position         :absolute
                             :background-color :transparent}
                            (case outside-border
                              :top-left {:top                    0
                                         :left                   0
                                         :border-top-left-radius 16
                                         :border-right-width 0
                                         :border-bottom-width 0}
                              :top-right {:top                     0
                                          :right                   0
                                          :border-top-right-radius 16
                                          :border-left-width 0
                                          :border-bottom-width 0}
                              :bottom-left {:bottom                    0
                                            :left                      0
                                            :border-bottom-left-radius 16
                                            :border-right-width 0
                                            :border-top-width 0}
                              :bottom-right {:bottom                     0
                                             :right                      0
                                             :border-bottom-right-radius 16
                                             :border-left-width 0
                                             :border-top-width 0}))}]))

(defn- viewfinder [finished-animation? hole-dimensions size flashlight-on?]
  [:f>
   (fn []
     [rn/view {:style {:position        :absolute
                       :align-items     :center
                       :justify-content :center}}
      [rn/view
       [rn/view {:style {:flex-direction :row :justify-content :space-between}}
        [corner :top-left]
        [corner :top-right]]
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:width  size
                                  :height size}
                                ;;  ^^^^^ TODO Border radius not working making edges
                                 {})}
        [rn/touchable-opacity {:style
                               {:width            32
                                :height           32
                                :background-color (if @flashlight-on?
                                                    colors/white
                                                    colors/white-opa-60)
                                :border-radius    10
                                :position         :absolute
                                :justify-content  :center
                                :z-index          10
                                :elevation        10
                                :align-items      :center
                                :bottom           20
                                :right            20}
                               :on-press #(swap! flashlight-on? not)}
         [icons/icon (if @flashlight-on?
                       :main-icons/flashlight-on
                       :main-icons/flashlight-off) {:size 20
                                                    :color colors/neutral-95}]]
        [rn/view {:style {:position :absolute :z-index 0 :elevation 0}}
         [rn/hole-view (cond->
                        {:style {:width            2000
                                 :height           2000
                                 :top              -1000
                                 :left             -1000
                                 :background-color "rgba(0,0,0,0.4)"}}
                         @finished-animation? (assoc :holes [{:x            1000
                                                              :y            1000
                                                              :width        @hole-dimensions
                                                              :height       @hole-dimensions
                                                              :borderRadius 16}]))]]]
       [rn/view {:style {:flex-direction :row :justify-content :space-between :z-index 5
                         :elevation 5}}
        [corner :bottom-left]
        [corner :bottom-right]]]])])

(defn preview
  []
  [:f>
   (fn []
     (let [{:keys [width]}               (dimensions/window)
           {:keys [min-scale max-scale]} {:min-scale (* width 0.4)
                                          :max-scale (* width 0.9)}
           hole-dimensions               (reagent/atom (* width 0.9))
           finished-animation?           (reagent/atom false)
           updateHoleWidth               #(do
                                            (reset! finished-animation? true)
                                            (reset! hole-dimensions (* % 0.9)))
           flashlight-on?                (reagent/atom false)
           size                          (reanimated/use-shared-value min-scale)
           difference                    (- max-scale min-scale)]
       [:<>
        (react/effect!
         (fn []
           (reanimated/animate-shared-value-with-delay size
                                                       max-scale
                                                       difference
                                                       :easing1 0)
           (js/setTimeout #(updateHoleWidth width) 300)
           []))
        [rn/image-background {:resize-mode :cover
                              :source {:uri "https://www.theagilityeffect.com/app/uploads/2019/09/00_VINCI-ICONOGRAPHIE_GettyImages-1065377816-1280x680.jpg"}
                              :style {:justify-content  :center
                                      :align-items      :center
                                      :z-index          -1
                                      :elevation        -1
                                      :height           "100%"
                                      :width            "100%"}}
         [rn-comps/blur-view {:flex               1
                              :style              {:width "100%" :height "100%"}
                              :blur-amount        16
                              :overlay-color      :transparent}]
         [viewfinder finished-animation? hole-dimensions size flashlight-on?]]]))])
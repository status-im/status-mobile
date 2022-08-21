(ns quo2.components.qr-scan-animation
  (:require [quo2.foundations.colors :as colors]
            [quo.react :as react]
            [reagent.core :as reagent]
            [quo.react-native :as rn]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.reanimated :as reanimated]
            [quo.theme :as theme]
            [status-im.utils.dimensions :as dimensions]))

(defn corner [border1 border2 outside-border]
  (let [{:keys [width height]} (dimensions/window)]
    [reanimated/view {:style (merge (assoc {:border-color colors/white
                                            :width (* 0.20 width)
                                            :height (* 0.12 height)
                                            :position :absolute
                                            :z-index -1000
                                            :background-color (when-not
                                                               (theme/dark?)
                                                                colors/white)}
                                           border1 3
                                           border2 3)
                                    (case outside-border
                                      :top-left {:top -1
                                                 :left -1
                                                 :border-top-left-radius 16}
                                      :top-right {:top -1
                                                  :right -1
                                                  :border-top-right-radius 16}
                                      :bottom-left {:bottom -1
                                                    :left -1
                                                    :border-bottom-left-radius 16}
                                      :bottom-right {:bottom -1
                                                     :right -1
                                                     :border-bottom-right-radius 16}))}]))

(def viewfinder-port
  {:position        :absolute
   :align-items     :center
   :justify-content :center
   :flex            1})

(defn- viewfinder [size flashlight-on?]
  [:f>
   (fn []
     [rn/view {:style viewfinder-port}

      [rn/view {:style {:border-width 700
                        :border-color "rgba(0,0,0,0.7)"}}
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:width size
                                  :height size}
                                 {:width size
                                  :height size
                                  :justify-content :space-between})}
        [rn/touchable-opacity {:style {:width 32
                                       :height 32
                                       :background-color (if @flashlight-on?
                                                          colors/white
                                                           colors/white-opa-60)
                                       :border-radius 10
                                       :position :absolute
                                       :justify-content :center
                                       :align-items :center
                                       :bottom 20
                                       :right 20}
                               :on-press #(swap! flashlight-on? not)}
         [icons/icon (if @flashlight-on?
                       :main-icons/flashlight-on20
                       :main-icons/flashlight-off20) {:color colors/black}]]
        [rn/view {:style {:flex-direction :row :justify-content :space-between}}
         [corner :border-top-width :border-left-width :top-left]
         [corner :border-top-width :border-right-width :top-right]]
        [rn/view {:style {:flex-direction :row :justify-content :space-between}}
         [corner :border-bottom-width :border-left-width :bottom-left]
         [corner :border-bottom-width :border-right-width :bottom-right]]]]])])

(defn preview
  []
  [:f>
   (fn []
     (let [{:keys [width]} (dimensions/window)
           flashlight-on? (reagent/atom false)
           {:keys [min-scale max-scale]} {:min-scale (* width 0.4)
                                          :max-scale (* width 0.8)}
           size (reanimated/use-shared-value min-scale)
           difference (- max-scale min-scale)]
       [:<>
        (react/effect!
         (fn []
           (reanimated/animate-shared-value-with-delay size
                                                       max-scale
                                                       difference
                                                       :easing1 400)
           []))
        [rn/view {:style {:justify-content :center
                          :align-items :center
                          :height "100%"}}
         [viewfinder size flashlight-on?]]]))])
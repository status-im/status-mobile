(ns status-im2.common.sticky-scroll-view.view
  (:require [react-native.reanimated :as reanimated]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [utils.worklets.scroll-view :as worklets.scroll-view]
            [quo2.foundations.colors :as colors]))

(defn sticky-item
  [{:keys [height translation-y background-color]} content]
  [:f>
   (fn []
     [rn/view (merge {:height height} (when platform/ios? {:z-index 1}))
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:transform [{:translateY translation-y}]}
                {:position         :absolute
                 :z-index          (when platform/android? 1)
                 :top              0
                 :left             0
                 :right            0
                 :height           height
                 :background-color background-color})}
       content]])])

(defn scroll-view
  [{:keys [ref scroll-y blur]} & content]
  [:f>
   (fn []
     (let [scroll-handler (worklets.scroll-view/use-animated-scroll-handler scroll-y)
           opacity        (when blur
                            (reanimated/interpolate scroll-y
                                                    [0 (:delta blur)]
                                                    [0 1]
                                                    {:extrapolateLeft  "clamp"
                                                     :extrapolateRight "extend"}))]
       (into [reanimated/scroll-view
              {:ref                             ref
               :scroll-event-throttle           1
               :shows-vertical-scroll-indicator false
               :contentInsetAdjustmentBehavior  :never
               :on-scroll                       scroll-handler}]
             (concat
              (when blur
                ;bug on Android
                ;https://github.com/Kureev/react-native-blur/issues/520}
                [[reanimated/view
                  {:style (reanimated/apply-animations-to-style
                           {:transform [{:translateY scroll-y}]
                            :opacity   opacity}
                           {:overflow (if platform/ios? :visible :hidden)
                            :position :absolute
                            :z-index  1
                            :top      0
                            :height   (:height blur)
                            :right    0
                            :left     0})}
                  [reanimated/blur-view
                   {:blur-amount 10
                    :blur-type   (if (colors/dark?) :dark :xlight)
                    :style       {:position :absolute
                                  :top      0
                                  :height   (:height blur)
                                  :right    0
                                  :left     0}}]]])
              content))))])

(defn flat-list
  [{:keys [scroll-y blur header render-fn data]}]
  [:f>
   (fn []
     (let [scroll-handler (worklets.scroll-view/use-animated-scroll-handler scroll-y)
           opacity        (when blur
                            (reanimated/interpolate scroll-y
                                                    [0 (:delta blur)]
                                                    [0 1]
                                                    {:extrapolateLeft  "clamp"
                                                     :extrapolateRight "extend"}))]
       [reanimated/flat-list
        {:scroll-event-throttle           1
         :shows-vertical-scroll-indicator false
         :contentInsetAdjustmentBehavior  :never
         :data                            data
         :render-fn                       render-fn
         :header                          (if blur
                                            [:<>
                                             [reanimated/blur-view
                                              {:blur-amount 10
                                               :blur-type   :xlight
                                               :style       (reanimated/apply-animations-to-style
                                                             {:transform [{:translateY scroll-y}]
                                                              :opacity   opacity}
                                                             {:z-index  1
                                                              :position :absolute
                                                              :top      0
                                                              :height   (:height blur)
                                                              :right    0
                                                              :left     0})}]
                                             header]
                                            header)
         :on-scroll                       scroll-handler}]))])

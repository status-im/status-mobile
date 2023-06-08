(ns status-im2.common.parallax.view
  (:require [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [utils.worklets.parallax :as worklets.parallax]
            ["react-native-transparent-video" :default TV]
            [react-native.safe-area :as safe-area]
            [react-native.platform :as platform]))

(def transparent-video (reagent/adapt-react-class TV))

(defn f-sensor-animated-video
  [{:keys [offset stretch order source disable-parallax?]}]
  (let [double-stretch          (* 2 stretch)
        {window-width  :width
         window-height :height} (rn/get-window)
        image-style             (if (not disable-parallax?)
                                  (worklets.parallax/sensor-animated-image order offset stretch)
                                  {:top    0
                                   :right  0
                                   :bottom 0
                                   :left   0})]
    (fn []
      [reanimated/view
       {:needsOffscreenAlphaCompositing true
        :style                          [{:position :absolute
                                          :width    (+ window-width double-stretch)
                                          :height   (+ window-height double-stretch)}
                                         image-style]}
       [transparent-video
        {:source source
         :style  {:position :absolute
                  :top      0
                  :left     0
                  :right    0
                  :bottom   0}}]])))

(defn sensor-animated-video
  [props]
  [:f> f-sensor-animated-video props])

(defn f-video
  [{:keys [layers disable-parallax? offset stretch]
    :or   {offset  50
           stretch 0}}]
  [rn/view
   {:style {:position :absolute
            :top      (if platform/android?
                        (+ (safe-area/get-top) (safe-area/get-bottom))
                        (safe-area/get-bottom))
            :left     0
            :right    0
            :bottom   0}}
   (map-indexed (fn [idx layer]
                  [sensor-animated-video
                   {:key               (str layer idx)
                    :source            layer
                    :offset            offset
                    :stretch           stretch
                    :order             (inc idx)
                    :disable-parallax? disable-parallax?}])
                layers)])

(defn video
  [props]
  [:f> f-video props])




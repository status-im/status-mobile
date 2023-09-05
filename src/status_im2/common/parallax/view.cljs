(ns status-im2.common.parallax.view
  (:require [react-native.reanimated :as reanimated]
            [react-native.transparent-video :as transparent-video]
            [react-native.core :as rn]
            [utils.worklets.parallax :as worklets.parallax]
            [status-im2.common.parallax.style :as style]))

(defn f-sensor-animated-video
  [{:keys [offset stretch order source disable-parallax? enable-looping?]}]
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
        :style                          [(style/container-view
                                          (+ window-width double-stretch)
                                          (+ window-height double-stretch))
                                         image-style]}
       [transparent-video/view
        {:source source
         :style  style/video
         :loop   enable-looping?}]])))

(defn sensor-animated-video
  [props]
  [:f> f-sensor-animated-video props])

(defn f-video
  [{:keys [layers disable-parallax? offset stretch container-style enable-looping?]
    :or   {offset 50 stretch 0}}]
  [rn/view
   {:style (style/outer-container container-style)}
   (map-indexed (fn [idx layer]
                  [sensor-animated-video
                   {:key               (str layer idx)
                    :source            layer
                    :offset            offset
                    :stretch           stretch
                    :order             (inc idx)
                    :disable-parallax? disable-parallax?
                    :enable-looping?   enable-looping?}])
                layers)])

(defn video
  [props]
  [:f> f-video props])

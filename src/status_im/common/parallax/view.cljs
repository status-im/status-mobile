(ns status-im.common.parallax.view
  (:require
   [react-native.core :as rn]
   [status-im.common.rive.view :as rive]
   [react-native.reanimated :as reanimated]
   [reagent.core :as reagent]
   [react-native.transparent-video :as transparent-video]
   [status-im.common.parallax.style :as style]
   [utils.worklets.parallax :as worklets.parallax]))

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

(defn f-rive
  [{:keys [resource-name artboard-name state-machine-name container-style]}]

  (let [rive-ref2 (rn/use-ref nil)]
    (worklets.parallax/use-giro rive-ref2)
    (rive/view
     {:ref   rive-ref2
                :resourceName resource-name
                :artboardName     artboard-name
                :stateMachineName state-machine-name
      :style container-style})))

(defn rive
  [props]
  [:f> f-rive props])

(ns status-im2.common.parallax.view
  (:require [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [utils.worklets.parallax :as worklets.parallax]
            ["react-native-transparent-video" :default TV]))

(def transparent-video (reagent/adapt-react-class TV))

(defn f-sensor-animated-image
  [{:keys [order source] :or {order 1}}]
  (let [image-style (worklets.parallax/sensor-animated-image order)]
    (fn []
      [:<>
       [reanimated/image
        {:source source
         :style  [{:position :absolute
                   :overflow :visible
                   :margin   :auto
                   :top      0
                   :bottom   0
                   :left     0
                   :right    0}
                  image-style]}]])))

(defn sensor-animated-image
  [props]
  [:f> f-sensor-animated-image props])

(defn f-sensor-animated-video
  [{:keys [order source] :or {order 1}}]
  (let [image-style (if (pos? order)
                      (worklets.parallax/sensor-animated-image order)
                      {:top    0
                       :left   0
                       :right  0
                       :bottom 0})]
    (fn []
      [reanimated/view
       {:style [:shouldRasterizeIOS true
                {:position :absolute}
                image-style]}
       [transparent-video
        {:source source
         :style  {:overflow :visible
                  :position :absolute
                  :top      0
                  :left     0
                  :right    0
                  :bottom   0}}]])))

(defn sensor-animated-video
  [props]
  [:f> f-sensor-animated-video props])

(defn f-image
  [{:keys [layers]}]
  [:<>
   (map-indexed (fn [idx layer]
                  [sensor-animated-image
                   {:key    (str layer idx)
                    :source layer
                    :order  idx}])
                layers)])

(defn image
  [props]
  [:f> f-image props])

(defn f-video
  [{:keys [layers]}]
  [:<>
   (map-indexed (fn [idx layer]
                  [sensor-animated-video
                   {:key    (str layer idx)
                    :source layer
                    :order  idx}])
                layers)])

(defn video
  [props]
  [:f> f-video props])




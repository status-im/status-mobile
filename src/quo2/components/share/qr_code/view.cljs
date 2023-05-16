(ns quo2.components.share.qr-code.view
  (:require [quo2.components.share.qr-code.style :as style]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn qr-code
  [{:keys [source width height]}]
  [rn/view
   {:style style/container}
   [fast-image/fast-image
    {:source source
     :style  (style/image width height)}]])

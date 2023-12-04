(ns quo.components.overlay.view
  (:require
    [quo.components.overlay.style :as style]
    [react-native.blur :as blur]
    [react-native.core :as rn]))

(defn view
  [{:keys [type]} & children]
  [rn/view {:style (style/overlay-background type)}
   (if (= type :shell)
     [blur/view
      {:blur-amount   20
       :blur-radius   25
       :blur-type     :transparent
       :overlay-color :transparent
       :style         style/container}
      [rn/view {:style style/blur-container}
       children]]
     [rn/view {:style style/container}
      children])])

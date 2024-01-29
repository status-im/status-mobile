(ns quo.components.overlay.view
  (:require
    [quo.components.overlay.style :as style]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.pure :as rn.pure]))

(defn view-pure
  [{:keys [type container-style]} & children]
  (rn.pure/view
   {:style (style/overlay-background type)}
   (if (= type :shell)
     (blur/view-pure
      {:blur-amount   20
       :blur-radius   25
       :blur-type     :transparent
       :overlay-color :transparent
       :style         style/container}
      (apply rn.pure/view
             {:style (merge style/blur-container container-style)}
             children))
     (apply rn.pure/view
            {:style (merge style/container container-style)}
            children))))

(defn view
  [{:keys [type container-style]} & children]
  [rn/view {:style (style/overlay-background type)}
   (if (= type :shell)
     [blur/view
      {:blur-amount   20
       :blur-radius   25
       :blur-type     :transparent
       :overlay-color :transparent
       :style         style/container}
      [rn/view {:style (merge style/blur-container container-style)}
       children]]
     [rn/view {:style (merge style/container container-style)}
      children])])

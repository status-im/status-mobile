(ns quo.components.overlay.view
  (:require
    [quo.components.overlay.style :as style]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]))

(defn view
  [{:keys [type container-style top-inset?]} & children]
  (let [top-style (when top-inset? {:padding-top (safe-area/get-top)})]
    [rn/view {:style (style/overlay-background type)}
     (if (= type :shell)
       [blur/view
        {:blur-amount   20
         :blur-radius   25
         :blur-type     :transparent
         :overlay-color :transparent
         :style         style/container}
        (into [rn/view
               {:style (merge style/blur-container top-style container-style)}]
              children)]
       (into [rn/view
              {:style (merge style/container top-style container-style)}]
             children))]))

(ns quo.components.overlay.view
  (:require
    [quo.components.blur.view :as blur]
    [quo.components.overlay.style :as style]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]))

(defn view
  [{:keys [type container-style top-inset? bottom-inset? insets?]} & children]
  (let [top-style    (when (or insets? top-inset?) {:padding-top safe-area/top})
        bottom-style (when (or insets? bottom-inset?) {:padding-bottom safe-area/bottom})]
    [rn/view {:style (style/overlay-background type)}
     (if (= type :shell)
       [blur/view
        {:blur-amount   20
         :blur-radius   25
         :blur-type     :transparent
         :overlay-color :transparent
         :style         style/container}
        (into [rn/view
               {:style [style/blur-container top-style bottom-style container-style]}]
              children)]
       (into [rn/view
              {:style [style/blur-container top-style bottom-style container-style]}]
             children))]))

(ns quo.components.colors.color.view
  (:require
    [quo.components.colors.color.style :as style]
    [quo.components.icon :as icon]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- feng-shui
  [{:keys [color theme]}]
  [rn/view
   {:accessibility-label color
    :style               (style/feng-shui theme)}
   [rn/view {:style (style/left-half theme)}]
   [rn/view {:style (style/right-half theme)}]])

(defn view
  [{:keys [color selected? on-press blur? idx window-width]
    :as   props}]
  (let [theme     (quo.theme/use-theme)
        border?   (and (not blur?) (not selected?))

        hex-color (if (= :feng-shui color)
                    (colors/theme-colors colors/neutral-100 colors/white theme)
                    (colors/theme-colors (colors/custom-color color 50)
                                         (colors/custom-color color 60)
                                         theme))]

    [rn/pressable
     {:style                   (style/color-button hex-color selected? idx window-width)
      :accessibility-label     :color-picker-item
      :allow-multiple-presses? true
      :on-press                #(on-press color)}
     (if (and (= :feng-shui color) (not selected?))
       [feng-shui
        (assoc props
               :theme     theme
               :hex-color hex-color
               :border?   border?)]
       [rn/view
        {:accessibility-label color
         :style               (style/color-circle hex-color border?)}
        (when selected?
          [icon/icon :i/check
           {:size  20
            :color (if (= :feng-shui color)
                     (colors/theme-colors colors/white colors/neutral-100 theme)
                     colors/white)}])])]))

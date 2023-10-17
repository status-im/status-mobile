(ns quo2.components.colors.color.view
  (:require
    [quo2.components.colors.color.style :as style]
    [quo2.components.icon :as icon]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- feng-shui
  [{:keys [color theme]}]
  [rn/view
   {:accessibility-label color
    :style               (style/feng-shui theme)}
   [rn/view {:style (style/left-half theme)}]
   [rn/view {:style (style/right-half theme)}]])

(defn- view-internal
  [{:keys [color
           selected?
           on-press
           blur?
           theme]
    :as   props}]
  (let [border?   (and (not blur?) (not selected?))
        hex-color (if (= :feng-shui color)
                    (colors/theme-colors colors/neutral-100 colors/white theme)
                    (colors/theme-colors (colors/custom-color color 50)
                                         (colors/custom-color color 60)
                                         theme))]

    [rn/pressable
     {:style               (style/color-button hex-color selected?)
      :accessibility-label :color-picker-item
      :on-press            #(on-press color)}
     (if (and (= :feng-shui color) (not selected?))
       [feng-shui
        (assoc props
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

(def view (quo.theme/with-theme view-internal))

(ns quo.components.colors.color.style
  (:require
    [quo.components.colors.color.constants :as constants]
    [quo.foundations.colors :as colors]
    [utils.responsiveness :as responsiveness]))

(defn color-button-common
  [window-width]
  {:width         constants/color-size
   :height        constants/color-size
   :border-width  4
   :border-radius 24
   :margin-right  (-> window-width
                      (- responsiveness/IPHONE_11_PRO_VIEWPORT_WIDTH) ;We want the design to be 100%
                      ;identical to Figma on iPhone 11
                      ;Pro, So we're using it's VW here.
                      (/ 6) ;Dividing by an appropriate factor to get a reasonable value for each VW
                            ;on other devices (This will yield 0 on iPhone 11 Pro, Which is what we
                            ;want)
                      (+ 8)) ;Add same margin that's on Figma for 11 Pro, This value will increase
                             ;based on the device VW because of the division above.
   :transform     [{:rotate "45deg"}]
   :border-color  :transparent})

(defn color-button
  ([color selected?]
   (color-button color selected? nil nil))
  ([color selected? idx window-width]
   (cond-> (color-button-common window-width)
     selected?   (assoc :border-top-color    (colors/alpha color 0.4)
                        :border-end-color    (colors/alpha color 0.4)
                        :border-bottom-color (colors/alpha color 0.2)
                        :border-start-color  (colors/alpha color 0.2)
                        :border-color        nil)
     (zero? idx) (assoc :margin-left -4))))

(defn color-circle
  [color border?]
  {:width            40
   :height           40
   :transform        [{:rotate "-45deg"}]
   :background-color color
   :justify-content  :center
   :align-items      :center
   :border-color     color
   :border-width     (if border? 2 0)
   :overflow         :hidden
   :border-radius    20})

(defn feng-shui
  [theme]
  {:width            40
   :height           40
   :transform        [{:rotate "45deg"}]
   :overflow         :hidden
   :border-color     (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-width     2
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-radius    20})

(defn left-half
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn right-half
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)})

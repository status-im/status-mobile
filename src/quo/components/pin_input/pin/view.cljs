(ns quo.components.pin-input.pin.view
  (:require quo.context
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn view
  [{:keys [theme state blur?]}]
  (let [app-theme (quo.context/use-theme)
        theme     (or theme app-theme)]
    [rn/view {:style {:width 36 :height 36 :align-items :center :justify-content :center}}
     (case state
       :active
       [rn/view
        {:style {:width            16
                 :height           16
                 :border-radius    8
                 :background-color (if blur?
                                     colors/white-opa-20
                                     (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))}}]
       :filled
       [rn/view
        {:style {:width            16
                 :height           16
                 :border-radius    8
                 :background-color (if blur?
                                     colors/white
                                     (colors/theme-colors colors/neutral-100 colors/white theme))}}]
       :error
       [rn/view
        {:style {:width            16
                 :height           16
                 :border-radius    8
                 :background-color (if blur?
                                     colors/danger-60
                                     (colors/theme-colors colors/danger-50 colors/danger-60 theme))}}]
       [rn/view
        {:style
         {:width            12
          :height           12
          :border-radius    6
          :background-color (if blur?
                              colors/white-opa-20
                              (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))}}])]))

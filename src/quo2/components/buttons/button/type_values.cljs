(ns quo2.components.buttons.button.type-values
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as quo2.theme]))

(defn custom-color-type
  [customization-color]
  {:icon-color           colors/white
   :icon-secondary-color colors/white-opa-70
   :label-color          colors/white
   :background-color     (colors/custom-color
                          customization-color
                          50)})

(defn get-value-from-type
  [{:keys [customization-color theme type background pressed?]}]
  (cond
    (= type :primary) (custom-color-type customization-color)
    (= type :positive) (custom-color-type customization-color)
    (= type :danger) (custom-color-type customization-color)
    ;; TODO Remove type blurred
    (or (= type :blurred) (and (= :photo background) (= type :grey)))
    {:icon-color            (colors/theme-colors
                             colors/neutral-100
                             colors/white
                             theme)
     :icon-secondary-color  (colors/theme-colors
                             colors/neutral-80-opa-40
                             colors/white-opa-70
                             theme)
     :label-color           (colors/theme-colors
                             colors/neutral-100
                             colors/white
                             theme)
     :background-color      (if-not pressed?
                              (colors/theme-colors
                               colors/neutral-80-opa-5
                               colors/white-opa-5
                               theme)
                              (colors/theme-colors
                               colors/neutral-80-opa-10
                               colors/white-opa-10
                               theme))
     :icon-background-color (quo2.theme/theme-value
                             {:default colors/neutral-20
                              :blurred colors/neutral-80-opa-10}
                             {:default colors/neutral-80
                              :blurred colors/white-opa-10})

     :blur-overlay-color    (colors/theme-colors
                             colors/neutral-10-opa-40-blur
                             colors/neutral-80-opa-40
                             theme)
     :blur-type             (if (= theme :light) :light :dark)}

    ;; TODO Remove type blur-bg
    (or (= type :blur-bg) (and (= :blur background) (= type :grey)))
    {:icon-color           (colors/theme-colors
                            colors/neutral-100
                            colors/white
                            theme)
     :icon-secondary-color (colors/theme-colors
                            colors/neutral-80-opa-40
                            colors/white-opa-70
                            theme)
     :label-color          (colors/theme-colors
                            colors/neutral-100
                            colors/white
                            theme)
     :background-color     (if-not pressed?
                             (colors/theme-colors
                              colors/neutral-80-opa-5
                              colors/white-opa-5
                              theme)
                             (colors/theme-colors
                              colors/neutral-80-opa-10
                              colors/white-opa-10
                              theme))}
    (and (= :blur background) (= type :outline)) {:icon-color           (colors/theme-colors
                                                                         colors/neutral-100
                                                                         colors/white
                                                                         theme)
                                                  :icon-secondary-color (colors/theme-colors
                                                                         colors/neutral-80-opa-40
                                                                         colors/white-opa-40
                                                                         theme)

                                                  :label-color          (colors/theme-colors
                                                                         colors/neutral-100
                                                                         colors/white
                                                                         theme)
                                                  :border-color         (if-not pressed?
                                                                          (colors/theme-colors
                                                                           colors/neutral-80-opa-10
                                                                           colors/white-opa-10)
                                                                          (colors/theme-colors
                                                                           colors/neutral-80-opa-20
                                                                           colors/white-opa-20))}
    (= type :grey) {:icon-color           (colors/theme-colors
                                           colors/neutral-100
                                           colors/white
                                           theme)
                    :icon-secondary-color (colors/theme-colors
                                           colors/neutral-50
                                           colors/neutral-40
                                           theme)
                    :label-color          (colors/theme-colors
                                           colors/neutral-100
                                           colors/white
                                           theme)
                    :background-color     (if-not pressed?
                                            (colors/theme-colors
                                             colors/neutral-10
                                             colors/neutral-90
                                             theme)
                                            (colors/theme-colors
                                             colors/neutral-20
                                             colors/neutral-60
                                             theme))}

    (= type :dark-grey) {:icon-color           (colors/theme-colors
                                                colors/neutral-100
                                                colors/white
                                                theme)
                         :icon-secondary-color (colors/theme-colors
                                                colors/neutral-50
                                                colors/neutral-40
                                                theme)
                         :label-color          (colors/theme-colors
                                                colors/neutral-100
                                                colors/white
                                                theme)
                         :background-color     (if-not pressed?
                                                 (colors/theme-colors
                                                  colors/neutral-20
                                                  colors/neutral-70
                                                  theme)
                                                 (colors/theme-colors
                                                  colors/neutral-30
                                                  colors/neutral-60
                                                  theme))}

    (= type :outline) {:icon-color           (colors/theme-colors
                                              colors/neutral-50
                                              colors/neutral-40
                                              theme)
                       :icon-secondary-color (colors/theme-colors
                                              colors/neutral-50
                                              colors/neutral-40
                                              theme)
                       :label-color          (colors/theme-colors
                                              colors/neutral-100
                                              colors/white
                                              theme)
                       :border-color         (if-not pressed?
                                               (colors/theme-colors
                                                colors/neutral-30
                                                colors/neutral-70
                                                theme)
                                               (colors/theme-colors
                                                colors/neutral-40
                                                colors/neutral-60
                                                theme))}

    (= type :ghost) {:icon-color           (colors/theme-colors
                                            colors/neutral-50
                                            colors/neutral-40
                                            theme)
                     :icon-secondary-color (colors/theme-colors
                                            colors/neutral-50
                                            colors/neutral-40
                                            theme)
                     :label-color          (colors/theme-colors
                                            colors/neutral-100
                                            colors/white
                                            theme)
                     :background-color     (when pressed?
                                             (colors/theme-colors
                                              colors/neutral-10
                                              colors/neutral-80
                                              theme))}
    ;; TODO: remove type shell
    (or (= type :shell) (= type :black)) {:icon-color       colors/white
                                          :label-color      colors/white
                                          :background-color (if-not pressed?
                                                              colors/neutral-95
                                                              colors/neutral-80)}))

(ns quo2.components.selectors.styles
  (:require [quo2.foundations.colors :as colors]))

(defn toggle-background-color
  [custom-color]
  (let [on             (colors/custom-color-by-theme custom-color 50 60)
        off            (colors/theme-colors colors/neutral-30 colors/neutral-80)
        on-blur-light  (colors/custom-color custom-color 50)
        on-blur-dark   colors/white-opa-70
        off-blur-light colors/neutral-80-opa-20
        off-blur-dark  colors/white-opa-10]
    {:normal {:enabled  {:on  on
                         :off off}
              :disabled {:on  (colors/alpha on 0.3)
                         :off (colors/alpha off 0.3)}}
     :blur   {:enabled  {:on  (colors/theme-colors on-blur-light on-blur-dark)
                         :off (colors/theme-colors off-blur-light off-blur-dark)}
              :disabled {:on  (colors/theme-colors (colors/alpha on-blur-light 0.3)
                                                   (colors/alpha on-blur-dark 0.21))
                         :off (colors/theme-colors (colors/alpha off-blur-light 0.06)
                                                   (colors/alpha off-blur-dark 0.03))}}}))

(defn radio-border-color
  [customization-color]
  (let [on             (colors/custom-color-by-theme customization-color 50 60)
        off            (colors/theme-colors colors/neutral-30 colors/neutral-70)
        on-blur        (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white)
        off-blur-light colors/neutral-80-opa-20
        off-blur-dark  colors/white-opa-40]
    {:normal {:enabled  {:on  on
                         :off off}
              :disabled {:on  (colors/alpha on 0.3)
                         :off (colors/alpha off 0.3)}}
     :blur   {:enabled  {:on  on-blur
                         :off (colors/theme-colors off-blur-light off-blur-dark)}
              :disabled {:on  (colors/alpha on-blur 0.3)
                         :off (colors/theme-colors (colors/alpha off-blur-light 0.06)
                                                   (colors/alpha off-blur-dark 0.12))}}}))

(def radio-background-color
  (let [off      (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
        off-blur colors/white-opa-5]
    {:normal {:enabled  off
              :disabled (colors/alpha off 0.12)}
     :blur   {:enabled  off-blur
              :disabled (colors/theme-colors (colors/alpha off-blur 0.12)
                                             (colors/alpha off-blur 0.015))}}))

(defn checkbox-background-color
  [customization-color]
  (let [on       (colors/custom-color-by-theme customization-color 50 60)
        off      (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
        on-blur  (colors/theme-colors (colors/custom-color customization-color 50)
                                      colors/white)
        off-blur colors/white-opa-5]
    {:normal {:enabled  {:on  on
                         :off off}
              :disabled {:on  (colors/alpha on 0.3)
                         :off (colors/alpha off 0.12)}}
     :blur   {:enabled  {:on  on-blur
                         :off off-blur}
              :disabled {:on  (colors/alpha on-blur 0.3)
                         :off (colors/theme-colors (colors/alpha off-blur 0.12)
                                                   (colors/alpha off-blur 0.015))}}}))

(def checkbox-border-color
  (let [border     (colors/theme-colors colors/neutral-30 colors/neutral-70)
        blur-light colors/neutral-80-opa-20
        blur-dark  colors/white-opa-40]
    {:normal {:enabled  border
              :disabled (colors/alpha border 0.3)}
     :blur   {:enabled  (colors/theme-colors blur-light blur-dark)
              :disabled (colors/theme-colors (colors/alpha blur-light 0.06)
                                             (colors/alpha blur-dark 0.12))}}))

(def checkbox-prefill-background-color
  (let [bg      (colors/theme-colors colors/neutral-30 colors/neutral-80)
        bg-blur (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10)]
    {:normal {:enabled  bg
              :disabled (colors/alpha bg 0.3)}
     :blur   {:enabled  bg-blur
              :disabled (colors/alpha bg-blur 0.03)}}))

(defn- get-color
  [color-map [& [disabled? blur? checked?]]]
  (let [blur-kw     (if blur? :blur :normal)
        disabled-kw (if disabled? :disabled :enabled)
        route       (if (boolean? checked?)
                      [blur-kw disabled-kw (if checked? :on :off)]
                      [blur-kw disabled-kw])]
    (get-in color-map route)))

(defn toggle
  [checked? disabled? blur? container-style customization-color]
  (assoc container-style
         :height           20
         :width            30
         :border-radius    20
         :background-color (-> customization-color
                               (toggle-background-color)
                               (get-color [disabled? blur? checked?]))))

(defn toggle-inner
  [checked? disabled? _blur? _customization-color]
  {:margin-left      (if checked? 12 2)
   :height           16
   :width            16
   :background-color (colors/alpha colors/white (if disabled? 0.3 1))
   :border-radius    20
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})

(defn radio
  [checked? disabled? blur? container-style customization-color]
  (assoc container-style
         :height           20
         :width            20
         :border-radius    20
         :border-width     1.2
         :background-color (when-not checked?
                             (get-color radio-background-color [disabled? blur?]))
         :border-color     (-> customization-color
                               (radio-border-color)
                               (get-color [disabled? blur? checked?]))))

(defn radio-inner
  [checked? disabled? blur? customization-color]
  {:height           14
   :width            14
   :margin           1.8
   :border-radius    7
   :background-color (when checked?
                       (-> customization-color
                           (radio-border-color)
                           (get-color [disabled? blur? checked?])))})

(defn checkbox
  [checked? disabled? blur? container-style customization-color]
  (assoc container-style
         :height           20
         :width            20
         :border-radius    6
         :border-width     (if checked? 0 1.2)
         :background-color (-> customization-color
                               (checkbox-background-color)
                               (get-color [disabled? blur? checked?]))
         :border-color     (when-not checked?
                             (get-color checkbox-border-color [disabled? blur?]))))

(defn common-checkbox-inner
  [checked? _disabled? _blur? _customization-color]
  (let [size (if checked? 20 0)]
    {:height size :width size}))

(defn checkbox-check
  [_checked? disabled? blur?]
  (let [check-color (if blur?
                      (colors/theme-colors colors/white colors/neutral-100)
                      colors/white)]
    {:size  20
     :color (colors/alpha check-color (if disabled? 0.3 1))}))

(defn checkbox-prefill
  [_checked? disabled? blur? container-style _customization-color]
  (assoc container-style
         :height           21
         :width            21
         :border-radius    6
         :background-color (get-color checkbox-prefill-background-color [disabled? blur?])))

(defn checkbox-prefill-check
  [checked? disabled? _blur?]
  (let [check-color (colors/theme-colors colors/neutral-100 colors/white)]
    {:size  20
     :color (when checked? (colors/alpha check-color (if disabled? 0.3 1)))}))

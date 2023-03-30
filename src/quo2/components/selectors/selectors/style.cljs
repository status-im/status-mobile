(ns quo2.components.selectors.selectors.style
  (:require [quo2.foundations.colors :as colors]))

(defn toggle-background-color
  [custom-color]
  (let [checked              (colors/custom-color-by-theme custom-color 50 60)
        unchecked            (colors/theme-colors colors/neutral-30 colors/neutral-80)
        checked-blur-light   (colors/custom-color custom-color 50)
        checked-blur-dark    colors/white-opa-70
        unchecked-blur-light colors/neutral-80-opa-20
        unchecked-blur-dark  colors/white-opa-10]
    {:normal {:enabled  {:checked   checked
                         :unchecked unchecked}
              :disabled {:checked   (colors/alpha checked 0.3)
                         :unchecked (colors/alpha unchecked 0.3)}}
     :blur   {:enabled  {:checked   (colors/theme-colors checked-blur-light checked-blur-dark)
                         :unchecked (colors/theme-colors unchecked-blur-light unchecked-blur-dark)}
              :disabled {:checked   (colors/theme-colors (colors/alpha checked-blur-light 0.3)
                                                         (colors/alpha checked-blur-dark 0.21))
                         :unchecked (colors/theme-colors (colors/alpha unchecked-blur-light 0.06)
                                                         (colors/alpha unchecked-blur-dark 0.03))}}}))

(defn radio-border-color
  [customization-color]
  (let [checked              (colors/custom-color-by-theme customization-color 50 60)
        unchecked            (colors/theme-colors colors/neutral-30 colors/neutral-70)
        checked-blur         (colors/theme-colors (colors/custom-color customization-color 50)
                                                  colors/white)
        unchecked-blur-light colors/neutral-80-opa-20
        unchecked-blur-dark  colors/white-opa-40]
    {:normal {:enabled  {:checked   checked
                         :unchecked unchecked}
              :disabled {:checked   (colors/alpha checked 0.3)
                         :unchecked (colors/alpha unchecked 0.3)}}
     :blur   {:enabled  {:checked   checked-blur
                         :unchecked (colors/theme-colors unchecked-blur-light unchecked-blur-dark)}
              :disabled {:checked   (colors/alpha checked-blur 0.3)
                         :unchecked (colors/theme-colors (colors/alpha unchecked-blur-light 0.06)
                                                         (colors/alpha unchecked-blur-dark 0.12))}}}))

(def radio-background-color
  (let [unchecked      (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
        unchecked-blur colors/white-opa-5]
    {:normal {:enabled  unchecked
              :disabled (colors/alpha unchecked 0.12)}
     :blur   {:enabled  unchecked-blur
              :disabled (colors/theme-colors (colors/alpha unchecked-blur 0.12)
                                             (colors/alpha unchecked-blur 0.015))}}))

(defn checkbox-background-color
  [customization-color]
  (let [checked        (colors/custom-color-by-theme customization-color 50 60)
        unchecked      (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
        checked-blur   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white)
        unchecked-blur colors/white-opa-5]
    {:normal {:enabled  {:checked   checked
                         :unchecked unchecked}
              :disabled {:checked   (colors/alpha checked 0.3)
                         :unchecked (colors/alpha unchecked 0.12)}}
     :blur   {:enabled  {:checked   checked-blur
                         :unchecked unchecked-blur}
              :disabled {:checked   (colors/alpha checked-blur 0.3)
                         :unchecked (colors/theme-colors (colors/alpha unchecked-blur 0.12)
                                                         (colors/alpha unchecked-blur 0.015))}}}))

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
  (let [bg-color      (colors/theme-colors colors/neutral-30 colors/neutral-80)
        bg-blur-color (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10)]
    {:normal {:enabled  bg-color
              :disabled (colors/alpha bg-color 0.3)}
     :blur   {:enabled  bg-blur-color
              :disabled (colors/alpha bg-blur-color 0.03)}}))

(defn- get-color
  [color-map [& [disabled? blur? checked?]]]
  (let [blur-kw     (if blur? :blur :normal)
        disabled-kw (if disabled? :disabled :enabled)
        route       (if (boolean? checked?)
                      [blur-kw disabled-kw (if checked? :checked :unchecked)]
                      [blur-kw disabled-kw])]
    (get-in color-map route)))

(defn toggle
  [{:keys [checked? disabled? blur? container-style customization-color]}]
  (assoc container-style
         :height           20
         :width            30
         :border-radius    20
         :background-color (-> customization-color
                               (toggle-background-color)
                               (get-color [disabled? blur? checked?]))))

(defn toggle-inner
  [{:keys [checked? disabled?]}]
  {:margin-left      (if checked? 12 2)
   :height           16
   :width            16
   :background-color (colors/alpha colors/white (if disabled? 0.3 1))
   :border-radius    20
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})

(defn radio
  [{:keys [checked? disabled? blur? container-style customization-color]}]
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
  [{:keys [checked? disabled? blur? customization-color]}]
  {:height           14
   :width            14
   :margin           1.8
   :border-radius    7
   :background-color (when checked?
                       (-> customization-color
                           (radio-border-color)
                           (get-color [disabled? blur? checked?])))})

(defn checkbox
  [{:keys [checked? disabled? blur? container-style customization-color]}]
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
  [{:keys [checked?]}]
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
  [{:keys [disabled? blur? container-style]}]
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

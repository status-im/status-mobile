(ns quo2.components.selectors.selectors.style
  (:require [quo2.foundations.colors :as colors]))

(defn toggle-background-color
  [custom-color]
  {:normal {:checked   (colors/custom-color-by-theme custom-color 50 60)
            :unchecked (colors/theme-colors colors/neutral-30 colors/neutral-80)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color custom-color 50) colors/white-opa-70)
            :unchecked (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-10)}})

(defn radio-border-color
  [customization-color]
  {:normal {:checked   (colors/custom-color-by-theme customization-color 50 60)
            :unchecked (colors/theme-colors colors/neutral-30 colors/neutral-70)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white)
            :unchecked (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40)}})

(def radio-background-unchecked-color
  {:normal (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
   :blur   colors/white-opa-5})

(defn checkbox-background-color
  [customization-color]
  {:normal {:checked   (colors/custom-color-by-theme customization-color 50 60)
            :unchecked (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white)
            :unchecked colors/white-opa-5}})

(def checkbox-border-unchecked-color
  {:normal (colors/theme-colors colors/neutral-30 colors/neutral-70)
   :blur   (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40)})

(def checkbox-prefill-background-color
  {:normal (colors/theme-colors colors/neutral-30 colors/neutral-80)
   :blur   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10)})

(defn- get-color
  [color-map & [blur? checked?]]
  (let [blur-type (if blur? :blur :normal)]
    (if (some? checked?)
      (get-in color-map [blur-type (if checked? :checked :unchecked)])
      (get color-map blur-type))))

(defn toggle
  [{:keys [checked? disabled? blur? container-style customization-color]}]
  (assoc container-style
         :height           20
         :width            30
         :border-radius    20
         :opacity          (if disabled? 0.3 1)
         :background-color (get-color (toggle-background-color customization-color) blur? checked?)))

(defn toggle-inner
  [{:keys [checked?]}]
  {:margin-left      (if checked? 12 2)
   :height           16
   :width            16
   :background-color colors/white
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
         :opacity          (if disabled? 0.3 1)
         :background-color (when-not checked?
                             (get-color radio-background-unchecked-color blur?))
         :border-color     (get-color (radio-border-color customization-color) blur? checked?)))

(defn radio-inner
  [{:keys [checked? blur? customization-color]}]
  {:height           14
   :width            14
   :margin           1.8
   :border-radius    7
   :background-color (when checked?
                       (get-color (radio-border-color customization-color) blur? checked?))})

(defn checkbox
  [{:keys [checked? disabled? blur? container-style customization-color]}]
  (assoc container-style
         :height           20
         :width            20
         :border-radius    6
         :opacity          (if disabled? 0.3 1)
         :border-width     (if checked? 0 1.2)
         :background-color (get-color (checkbox-background-color customization-color) blur? checked?)
         :border-color     (when-not checked?
                             (get-color checkbox-border-unchecked-color blur?))))

(defn common-checkbox-inner
  [{:keys [checked?]}]
  (let [size (if checked? 20 0)]
    {:height size :width size}))

(defn checkbox-check
  [_checked? blur?]
  {:size  20
   :color (if blur?
            (colors/theme-colors colors/white colors/neutral-100)
            colors/white)})

(defn checkbox-prefill
  [{:keys [disabled? blur? container-style]}]
  (assoc container-style
         :height           21
         :width            21
         :border-radius    6
         :opacity          (if disabled? 0.3 1)
         :background-color (get-color checkbox-prefill-background-color blur?)))

(defn checkbox-prefill-check
  [checked? _blur?]
  {:size  20
   :color (when checked? (colors/theme-colors colors/neutral-100 colors/white))})


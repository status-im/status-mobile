(ns quo2.components.selectors.selectors.style
  (:require [quo2.foundations.colors :as colors]))

(defn- toggle-background-color
  [customization-color theme]
  {:normal {:checked   (colors/resolve-color customization-color theme)
            :unchecked (colors/theme-colors colors/neutral-30 colors/neutral-80 theme)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white-opa-70
                                            theme)
            :unchecked (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-10 theme)}})

(defn- radio-border-color
  [customization-color theme]
  {:normal {:checked   (colors/resolve-color customization-color theme)
            :unchecked (colors/theme-colors colors/neutral-30 colors/neutral-70 theme)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white
                                            theme)
            :unchecked (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40 theme)}})

(defn- radio-background-unchecked-color
  [theme]
  {:normal (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40 theme)
   :blur   colors/white-opa-5})

(defn- checkbox-background-color
  [customization-color theme]
  {:normal {:checked   (colors/resolve-color customization-color theme)
            :unchecked (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40 theme)}
   :blur   {:checked   (colors/theme-colors (colors/custom-color customization-color 50)
                                            colors/white)
            :unchecked colors/white-opa-5}})

(defn- checkbox-border-unchecked-color
  [theme]
  {:normal (colors/theme-colors colors/neutral-30 colors/neutral-70 theme)
   :blur   (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40 theme)})

(defn- checkbox-prefill-background-color
  [theme]
  {:normal (colors/theme-colors colors/neutral-30 colors/neutral-80 theme)
   :blur   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)})

(defn- get-color
  [color-map & [blur? checked?]]
  (let [blur-type (if blur? :blur :normal)]
    (if (some? checked?)
      (get-in color-map [blur-type (if checked? :checked :unchecked)])
      (get color-map blur-type))))

(defn toggle
  [{:keys [checked? disabled? blur? container-style customization-color theme]}]
  (assoc container-style
         :height           20
         :width            30
         :border-radius    20
         :opacity          (if disabled? 0.3 1)
         :background-color (get-color (toggle-background-color customization-color theme)
                                      blur?
                                      checked?)))

(defn toggle-inner
  [{:keys [checked?]}]
  {:height           16
   :width            16
   :background-color colors/white
   :border-radius    20
   :margin-left      (if checked? 12 2)
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})

(defn radio
  [{:keys [checked? disabled? blur? container-style customization-color theme]}]
  (assoc container-style
         :height           20
         :width            20
         :border-radius    20
         :border-width     1.2
         :opacity          (if disabled? 0.3 1)
         :background-color (when-not checked?
                             (get-color (radio-background-unchecked-color theme) blur?))
         :border-color     (get-color (radio-border-color customization-color theme) blur? checked?)))

(defn radio-inner
  [{:keys [checked? blur? customization-color theme]}]
  {:height           14
   :width            14
   :margin           1.8
   :border-radius    7
   :background-color (when checked?
                       (get-color (radio-border-color customization-color theme) blur? checked?))})

(defn checkbox
  [{:keys [checked? disabled? blur? container-style customization-color theme]}]
  (assoc container-style
         :height           20
         :width            20
         :border-radius    6
         :opacity          (if disabled? 0.3 1)
         :border-width     (if checked? 0 1.2)
         :background-color (get-color (checkbox-background-color customization-color theme)
                                      blur?
                                      checked?)
         :border-color     (when-not checked?
                             (get-color (checkbox-border-unchecked-color theme) blur?))))

(defn common-checkbox-inner
  [{:keys [checked?]}]
  (let [size (if checked? 20 0)]
    {:height size :width size}))

(defn checkbox-check
  [_checked? blur? theme]
  {:size  20
   :color (if blur?
            (colors/theme-colors colors/white colors/neutral-100 theme)
            colors/white)})

(defn checkbox-prefill
  [{:keys [disabled? blur? container-style theme]}]
  (assoc container-style
         :height           21
         :width            21
         :border-radius    6
         :opacity          (if disabled? 0.3 1)
         :background-color (get-color (checkbox-prefill-background-color theme) blur?)))

(defn checkbox-prefill-check
  [checked? _blur? theme]
  {:size  20
   :color (when checked? (colors/theme-colors colors/neutral-100 colors/white theme))})


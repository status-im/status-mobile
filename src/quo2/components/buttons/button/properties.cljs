(ns quo2.components.buttons.button.properties
  (:require [quo2.foundations.colors :as colors]))

(defn custom-color-type
  [customization-color icon-only?]
  {:icon-color       colors/white-opa-70
   :label-color      colors/white
   :background-color (colors/custom-color customization-color 50)
   :border-radius    (when icon-only? 24)})

(defn grey-photo
  [theme pressed?]
  {:icon-color         (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-70 theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :blur-overlay-color (if pressed?
                         (colors/theme-colors colors/white-opa-50
                                              colors/neutral-80-opa-50
                                              theme)
                         (colors/theme-colors colors/white-opa-40
                                              colors/neutral-80-opa-40
                                              theme))
   :blur-type          (if (= theme :light) :light :dark)})

(defn grey-blur
  [theme pressed?]
  {:icon-color       (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-70 theme)
   :label-color      (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color (if pressed?
                       (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
                       (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme))})

(defn outline-blur
  [theme pressed?]
  {:icon-color   (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
   :label-color  (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color (if pressed?
                   (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
                   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme))})

(defn grey
  [theme pressed?]
  {:icon-color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :label-color      (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color (if pressed?
                       (colors/theme-colors colors/neutral-20 colors/neutral-60 theme)
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn dark-grey
  [theme pressed?]
  {:icon-color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :label-color      (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color (if pressed?
                       (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)
                       (colors/theme-colors colors/neutral-20 colors/neutral-90 theme))})

(defn outline
  [theme pressed?]
  {:icon-color   (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :label-color  (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color (if pressed?
                   (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
                   (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))})

(defn ghost
  [theme pressed?]
  {:icon-color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :label-color      (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color (when pressed?
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn black
  [pressed?]
  {:label-color      colors/white
   :background-color (if pressed? colors/neutral-80 colors/neutral-95)})

(defn get-values
  [{:keys [customization-color theme type background pressed? icon-only?]}]
  (cond
    (= type :primary)                            (custom-color-type customization-color icon-only?)
    (= type :positive)                           (custom-color-type customization-color icon-only?)
    (= type :danger)                             (custom-color-type customization-color icon-only?)
    (and (= :photo background) (= type :grey))   (grey-photo theme pressed?)
    (and (= :blur background) (= type :grey))    (grey-blur theme pressed?)
    (and (= :blur background) (= type :outline)) (outline-blur theme pressed?)
    (= type :grey)                               (grey theme pressed?)
    (= type :dark-grey)                          (dark-grey theme pressed?)
    (= type :outline)                            (outline theme pressed?)
    (= type :ghost)                              (ghost theme pressed?)
    (= type :black)                              (black pressed?)))

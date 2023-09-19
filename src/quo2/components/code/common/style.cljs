(ns quo2.components.code.common.style
  (:require [quo2.foundations.colors :as colors]))

;; Example themes:
;; https://github.com/react-syntax-highlighter/react-syntax-highlighter/tree/master/src/styles/hljs
(defn theme
  [theme-key]
  (case theme-key
    :hljs-comment      (colors/theme-colors colors/neutral-40 colors/neutral-60)
    :hljs-title        (colors/custom-color-by-theme :sky 50 60)
    :hljs-keyword      (colors/custom-color-by-theme :green 50 60)
    :hljs-string       (colors/custom-color-by-theme :turquoise 50 60)
    :hljs-literal      (colors/custom-color-by-theme :turquoise 50 60)
    :hljs-number       (colors/custom-color-by-theme :turquoise 50 60)
    :hljs-symbol       (colors/custom-color-by-theme :orange 50 60)
    :hljs-builtin-name (colors/custom-color-by-theme :pink 50 60)
    :line-number       colors/neutral-40
    nil))

(defn text-style
  [class-names]
  (let [text-color (->> class-names
                        (map keyword)
                        (some (fn [class-name]
                                (when-let [text-color (theme class-name)]
                                  text-color))))]
    (cond-> {:flex-shrink 1
             :line-height 18}
      text-color (assoc :color text-color))))

(defn border-color
  []
  (colors/theme-colors colors/neutral-20 colors/neutral-80))

(defn container
  [preview?]
  (if preview?
    {:overflow         :hidden
     :width            108
     :background-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
     :border-radius    8}
    {:overflow         :hidden
     :padding          8
     :background-color (colors/theme-colors colors/white colors/neutral-80-opa-40)
     :border-color     (border-color)
     :border-width     1
     :border-radius    16}))

(def gradient-container
  {:position :absolute
   :bottom   0
   :left     0
   :right    0
   :z-index  1})

(def gradient {:height 48})

(defn line-number-container
  [line-number-width preview?]
  (cond-> {:position         :absolute
           :bottom           0
           :top              0
           :left             0
           :width            (+ line-number-width 8 7)
           :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80)}
    preview? (assoc :width            20
                    :background-color (colors/theme-colors colors/neutral-10 colors/neutral-70 theme))))

(defn divider
  [line-number-width]
  {:position         :absolute
   :bottom           0
   :top              0
   :left             (+ line-number-width 7 7)
   :width            1
   :z-index          2
   :background-color (border-color)})

(def line {:flex-direction :row})

(defn line-number
  [width preview?]
  (if preview?
    {:width           width
     :align-items     :center
     :justify-content :center}
    {:margin-right 20 ; 8+12 margin
     :width        width}))

(def line-content
  {:flex               1
   :padding-horizontal 8
   :padding-vertical   3})

(def copy-button
  {:position :absolute
   :bottom   8
   :right    8
   :z-index  1})

(defn gradient-color [] (colors/theme-colors colors/white colors/neutral-80))

(defn button-background-color
  []
  (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5))

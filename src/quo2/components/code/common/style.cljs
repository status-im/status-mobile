(ns quo2.components.code.common.style
  (:require [quo2.foundations.colors :as colors]))

;; Example themes:
;; https://github.com/react-syntax-highlighter/react-syntax-highlighter/tree/master/src/styles/hljs
(defn highlight-theme
  [class-name theme]
  (case class-name
    :hljs-comment      (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
    :hljs-title        (colors/resolve-color :sky theme)
    :hljs-keyword      (colors/resolve-color :green theme)
    :hljs-string       (colors/resolve-color :turquoise theme)
    :hljs-literal      (colors/resolve-color :turquoise theme)
    :hljs-number       (colors/resolve-color :turquoise theme)
    :hljs-symbol       (colors/resolve-color :orange theme)
    :hljs-builtin-name (colors/resolve-color :pink theme)
    :line-number       colors/neutral-40
    nil))

(defn text-style
  [class-names preview? theme]
  (let [text-color (->> class-names
                        (map keyword)
                        (some (fn [class-name]
                                (when-let [text-color (highlight-theme class-name theme)]
                                  text-color))))]
    (cond-> {:flex-shrink 1
             :line-height 18}
      preview?   (assoc :color colors/white)
      text-color (assoc :color text-color))))

(defn border-color
  [theme]
  (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))

(defn container
  [preview? theme]
  (if preview?
    {:overflow         :hidden
     :width            108
     :background-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
     :border-radius    8}
    {:overflow         :hidden
     :padding          8
     :background-color (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)
     :border-color     (border-color theme)
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
  [line-number-width preview? theme]
  (cond-> {:position         :absolute
           :bottom           0
           :top              0
           :left             0
           :width            (+ line-number-width 8 7)
           :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)}
    preview? (assoc :width            20
                    :background-color (colors/theme-colors colors/neutral-10 colors/neutral-70 theme))))

(defn divider
  [line-number-width theme]
  {:position         :absolute
   :bottom           0
   :top              0
   :left             (+ line-number-width 7 7)
   :width            1
   :z-index          2
   :background-color (border-color theme)})

(def line {:flex-direction :row})

(defn line-number
  [width preview?]
  (if preview?
    {:width            width
     :padding-vertical 3
     :padding-left     8
     :padding-right    4}
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

(defn gradient-color [theme] (colors/theme-colors colors/white colors/neutral-80 theme))

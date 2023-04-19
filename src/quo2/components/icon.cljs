(ns quo2.components.icon
  (:require [clojure.string :as string]
            [quo2.components.icons.icons :as icons]
            [quo2.components.icons.svg :as icons.svg]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn- valid-color?
  [color]
  (and (string? color) (not (string/blank? color))))

(defn- append-to-keyword
  [k & xs]
  (keyword (apply str
                  (subs (str k) 1)
                  xs)))

(defn memo-icon-fn
  ([icon-name] (memo-icon-fn icon-name nil))
  ([icon-name
    {:keys [color no-color
            background-color foreground-color
            container-style size accessibility-label]
     :or   {accessibility-label :icon}}]
   (let [size (or size 20)]
     ^{:key icon-name}
     (if-let [svg-icon (get icons.svg/icons (append-to-keyword icon-name "-" size))]
       (let [foreground-color (cond
                                (valid-color? foreground-color)
                                foreground-color

                                (valid-color? color)
                                color

                                :else
                                (colors/theme-colors colors/neutral-100 colors/white))]
         [svg-icon
          {:size                size
           :background-color    background-color
           :foreground-color    foreground-color
           :accessibility-label accessibility-label
           :style               container-style}])
       [rn/image
        {:style
         (merge {:width  size
                 :height size}

                (when (not no-color)
                  {:tint-color (if (and (string? color) (not (string/blank? color)))
                                 color
                                 (colors/theme-colors colors/neutral-100 colors/white))})

                container-style)
         :accessibility-label accessibility-label
         :source (icons/icon-source (str (name icon-name) size))}]))))

(def icon (memoize memo-icon-fn))

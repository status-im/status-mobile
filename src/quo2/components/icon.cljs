(ns quo2.components.icon
  (:require [clojure.string :as string]
            [quo2.components.icons.icons :as icons]
            [quo2.components.icons.svg :as icons.svg]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn- valid-color?
  [color]
  (or (keyword? color)
      (and (string? color)
           (not (string/blank? color)))))

(defn memo-icon-fn
  ([icon-name] (memo-icon-fn icon-name nil))
  ([icon-name
    {:keys [color color-2 no-color
            container-style size accessibility-label]
     :or   {accessibility-label :icon}}]
   (let [size (or size 20)]
     ^{:key icon-name}
     (if-let [svg-icon (icons.svg/get-icon icon-name size)]
       [svg-icon
        {:size                size
         :color               (when (valid-color? color) color)
         :color-2             (when (valid-color? color-2) color-2)
         :accessibility-label accessibility-label
         :style               container-style}]
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

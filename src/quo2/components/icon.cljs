(ns quo2.components.icon
  (:require [clojure.string :as string]
            [quo2.components.icons.icons :as icons]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn memo-icon-fn
  ([icon-name] (memo-icon-fn icon-name nil))
  ([icon-name
    {:keys [color container-style size
            accessibility-label no-color]
     :or   {accessibility-label :icon}}]
   (let [size (or size 20)]
     ^{:key icon-name}
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
       :source (icons/icon-source (str (name icon-name) size))}])))

(def icon (memoize memo-icon-fn))

(ns quo2.components.icon
  (:require
   [quo2.components.icons.icons :as icons]
   [status-im.ui.components.react :as react]
   [clojure.string :as string]
   [quo2.foundations.colors :as colors]))

(defn memo-icon-fn
  ([icon-name] (memo-icon-fn icon-name nil))
  ([icon-name {:keys [color container-style size
                      accessibility-label no-color]
               :or   {accessibility-label :icon}}]
   (let [size (or size 20)]
     ^{:key icon-name}
     [react/image
      {:style
       (merge {:width  size
               :height size}

              (when (not no-color)
                {:tint-color (if (and (string? color) (not (string/blank? color)))
                               color
                               (colors/theme-colors colors/neutral-100 colors/white))})

              container-style)
       :accessibility-label accessibility-label
       :source              (icons/icon-source (str (name icon-name) size))}])))

(def icon (memoize memo-icon-fn))

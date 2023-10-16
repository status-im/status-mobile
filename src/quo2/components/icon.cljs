(ns quo2.components.icon
  (:require
    [clojure.string :as string]
    [quo2.components.icons.icons :as icons]
    [quo2.components.icons.svg :as icons.svg]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- valid-color?
  [color]
  (or (keyword? color)
      (and (string? color)
           (not (string/blank? color)))))

(defn memo-icon-fn
  [{:keys [color color-2 no-color
           container-style size accessibility-label theme]
    :or   {accessibility-label :icon}}
   icon-name]
  (let [size (or size 20)]
    ^{:key icon-name}
    (if-let [svg-icon (icons.svg/get-icon icon-name size)]
      [svg-icon
       (cond-> {:size                size
                :accessibility-label accessibility-label
                :style               container-style}

         (and color (valid-color? color))
         (assoc :color color)

         (and color-2 (valid-color? color-2))
         (assoc :color-2 color-2))]
      [rn/image
       {:style
        (merge {:width  size
                :height size}

               (when (not no-color)
                 {:tint-color (if (and (string? color) (not (string/blank? color)))
                                color
                                (colors/theme-colors colors/neutral-100 colors/white theme))})

               container-style)
        :accessibility-label accessibility-label
        :source (icons/icon-source (str (name icon-name) size))}])))

(def ^:private themed-icon (memoize (quo.theme/with-theme memo-icon-fn)))

(defn icon
  ([icon-name] (icon icon-name nil))
  ([icon-name params]
   (themed-icon params icon-name)))

(ns quo.components.icon
  (:require
    [clojure.string :as string]
    [quo.components.icons.icons :as icons]
    [quo.components.icons.svg :as icons.svg]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]))

(defn- valid-color?
  [color]
  (and color
       (or (keyword? color)
           (and (string? color)
                (not (string/blank? color))))))

(defn- image-icon-style
  [{:keys [color no-color size container-style]} theme]
  (cond-> {:width  size
           :height size}
    (not no-color)
    (assoc :tint-color
           (if (and (string? color) (not (string/blank? color)))
             color
             (colors/theme-colors colors/neutral-100 colors/white theme)))
    :always
    (merge container-style)))

(defn icon-pure
  [{:keys [color color-2 container-style size accessibility-label]
    :or   {accessibility-label :icon}
    :as   props}
   icon-name]
  (let [size  (or size 20)
        theme (quo.theme/use-theme)]
    (if-let [svg-icon (icons.svg/icon icon-name size)]
      (svg-icon
       (cond-> {:size                size
                :accessibility-label accessibility-label
                :style               container-style}
         (valid-color? color)   (assoc :color color)
         (valid-color? color-2) (assoc :color-2 color-2)))
      (rn.pure/image
       {:key                 icon-name
        :style               (image-icon-style (assoc props :size size) theme)
        :accessibility-label accessibility-label
        :source              (icons/icon-source (str (name icon-name) size))}))))

(def icon-pure-memo (memoize icon-pure))

(defn icon
  ([icon-name] (icon icon-name nil))
  ([icon-name params]
   (rn.pure/func icon-pure-memo params icon-name)))

(ns quo.components.icon
  (:require
    [clojure.string :as string]
    [quo.components.icons.icons :as icons]
    [quo.components.icons.svg :as icons.svg]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

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

(def memo-icon-fn
  (fn [{:keys [color color-2 container-style size accessibility-label]
        :or   {accessibility-label :icon}
        :as   props}
       icon-name
       theme]
    (let [size (or size 20)]
      (with-meta
        (if-let [svg-icon (icons.svg/get-icon icon-name size)]
          [svg-icon
           (cond-> {:size                size
                    :accessibility-label accessibility-label
                    :style               container-style}
             (valid-color? color)   (assoc :color color)
             (valid-color? color-2) (assoc :color-2 color-2))]
          [rn/image
           {:style               (image-icon-style (assoc props :size size) theme)
            :accessibility-label accessibility-label
            :source              (icons/icon-source (str (name icon-name) size))}])
        {:key icon-name}))))

(def ^:private memoized-icon (memoize memo-icon-fn))

(defn icon
  ([icon-name] (icon icon-name nil))
  ([icon-name params]
   (let [theme (quo.theme/use-theme)]
     (memoized-icon params icon-name theme))))

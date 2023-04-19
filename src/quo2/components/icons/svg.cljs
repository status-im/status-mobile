(ns quo2.components.icons.svg
  "Declare icons in this namespace when they have two possible colors, because the
  ReactNative `:tint-color` prop affects all non-transparent pixels of PNGs. If
  the icon has only one color, prefer a PNG.

  Keep all SVG components private and expose them by name via the `icons` var."
  (:require [react-native.svg :as svg]))

(defn- container
  [{:keys [size accessibility-label style]
    :or   {size 20}}
   & children]
  (into [svg/svg
         {:accessibility-label accessibility-label
          :style               style
          :width               size
          :height              size
          :view-box            (str "0 0 " size " " size)
          :fill                :none}]
        children))

(defn- clear
  [{:keys [foreground-color background-color] :as props}]
  [container props
   [svg/path
    {:d
     "M3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10Z"
     :fill background-color}]
   [svg/path
    {:d
     "M9.15142 9.99998L7.07566 12.0757L7.9242 12.9243L9.99994 10.8485L12.0757 12.9242L12.9242 12.0757L10.8485 9.99998L12.9242 7.92421L12.0757 7.07568L9.99994 9.15145L7.92421 7.07572L7.07568 7.92425L9.15142 9.99998Z"
     :fill foreground-color}]])

(def icons
  {:i/clear clear})

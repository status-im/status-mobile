(ns quo2.components.icons.svg
  "Declare icons in this namespace when they have two possible colors, because the
  ReactNative `:tint-color` prop affects all non-transparent pixels of PNGs. If
  the icon has only one color, prefer a PNG.

  Keep all SVG components private and expose them by name in the `icons` var."
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.svg :as svg]))

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

(defn- clear-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/path
    {:d
     "M3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10Z"
     :fill color}]
   [svg/path
    {:d
     "M9.15142 9.99998L7.07566 12.0757L7.9242 12.9243L9.99994 10.8485L12.0757 12.9242L12.9242 12.0757L10.8485 9.99998L12.9242 7.92421L12.0757 7.07568L9.99994 9.15145L7.92421 7.07572L7.07568 7.92425L9.15142 9.99998Z"
     :fill color-2}]])

(defn- dropdown-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/path
    {:d
     "M3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10Z"
     :fill color}]
   [svg/path
    {:d
     "M7 8.5L10 11.5L13 8.5"
     :stroke color-2
     :stroke-width 1.2}]])

(defn- pullup-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/path
    {:d
     "M3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10Z"
     :fill color}]
   [svg/path
    {:d
     "M7 11.5L10 8.5L13 11.5"
     :stroke color-2
     :stroke-width 1.2}]])

(defn- dropdown-12
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/circle
    {:cx   6
     :cy   6
     :r    6
     :fill color}]
   [svg/path
    {:d
     "M3.5 5L6 7.5L8.5 5"
     :stroke color-2
     :stroke-width 1.1}]])

(defn- pullup-12
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/circle
    {:cx   6
     :cy   6
     :r    6
     :fill color}]
   [svg/path
    {:d
     "M3.5 7L6 4.5L8.5 7"
     :stroke color-2
     :stroke-width 1.1}]])

(defn- contact-12
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/circle
    {:cx   6
     :cy   6
     :r    6
     :fill color}]
   [svg/path
    {:d
     "M2.79504 9.01494C2.89473 8.85584 3.00297 8.70977 3.11963 8.5761C3.90594 7.67512 4.97212 7.45024 5.99992 7.45024C7.02772 7.45024 8.0939 7.67512 8.88021 8.5761C8.99688 8.70978 9.10512 8.85585 9.2048 9.01495C8.9501 9.28561 8.66149 9.52401 8.34577 9.72335C8.25403 9.55824 8.15512 9.41818 8.05145 9.29939C7.56503 8.74204 6.88121 8.55024 5.99992 8.55024C5.11862 8.55024 4.43481 8.74204 3.94839 9.29939C3.84472 9.41818 3.74582 9.55824 3.65408 9.72334C3.33835 9.524 3.04975 9.2856 2.79504 9.01494ZM5.99992 3.5502C5.47525 3.5502 5.04992 3.97552 5.04992 4.5002C5.04992 5.02487 5.47525 5.4502 5.99992 5.4502C6.52459 5.4502 6.94992 5.02487 6.94992 4.5002C6.94992 3.97552 6.52459 3.5502 5.99992 3.5502ZM3.94992 4.5002C3.94992 3.36801 4.86773 2.4502 5.99992 2.4502C7.1321 2.4502 8.04992 3.36801 8.04992 4.5002C8.04992 5.63238 7.1321 6.5502 5.99992 6.5502C4.86773 6.5502 3.94992 5.63238 3.94992 4.5002Z"
     :fill color-2}]])

(def ^:private icons
  {:i/clear-20    clear-20
   :i/dropdown-20 dropdown-20
   :i/pullup-20   pullup-20
   :i/dropdown-12 dropdown-12
   :i/pullup-12   pullup-12
   :i/contact-12  contact-12})

(defn- append-to-keyword
  [k & xs]
  (keyword (apply str
                  (subs (str k) 1)
                  xs)))

(defn get-icon
  [icon-name size]
  (get icons (append-to-keyword icon-name "-" size)))

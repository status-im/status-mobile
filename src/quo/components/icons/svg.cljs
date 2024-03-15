(ns quo.components.icons.svg
  "Declare icons in this namespace when they have two possible colors, because the
  ReactNative `:tint-color` prop affects all non-transparent pixels of PNGs. If
  the icon has only one color, prefer a PNG.

  Keep all SVG components private and expose them by name in the `icons` var."
  (:require
    [quo.foundations.colors :as colors]
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

(defn- contact-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/circle
    {:cx   10
     :cy   10
     :r    7.5
     :fill color}]
   [svg/path
    {:d
     "M8.65002 7.99998C8.65002 7.25439 9.25443 6.64998 10 6.64998C10.7456 6.64998 11.35 7.25439 11.35 7.99998C11.35 8.74556 10.7456 9.34998 10 9.34998C9.25443 9.34998 8.65002 8.74556 8.65002 7.99998ZM10 5.34998C8.53646 5.34998 7.35002 6.53642 7.35002 7.99998C7.35002 9.46353 8.53646 10.65 10 10.65C11.4636 10.65 12.65 9.46353 12.65 7.99998C12.65 6.53642 11.4636 5.34998 10 5.34998ZM10 13.65C8.67557 13.65 7.53064 14.4186 6.98692 15.5341C6.60094 15.3235 6.23942 15.0737 5.90771 14.79C6.69408 13.3369 8.23179 12.35 10 12.35C11.7682 12.35 13.306 13.3369 14.0923 14.79C13.7606 15.0737 13.3991 15.3235 13.0131 15.5341C12.4694 14.4186 11.3245 13.65 10 13.65Z"
     :fill color-2}]])

(defn- verified-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/path
    {:d
     "M18 10C18 8.77003 17.1118 7.74751 15.9418 7.53891C16.6216 6.56407 16.5267 5.21294 15.6569 4.34321C14.7872 3.47344 13.436 3.37852 12.4611 4.05845C12.2526 2.88834 11.23 2 10 2C8.77 2 7.74747 2.88827 7.53889 4.05832C6.56406 3.37846 5.21292 3.47339 4.34318 4.34313C3.47343 5.21288 3.3785 6.56404 4.05839 7.53888C2.88831 7.74743 2 8.76998 2 10C2 11.23 2.88824 12.2525 4.05826 12.4611C3.3784 13.4359 3.47333 14.7871 4.34307 15.6568C5.21284 16.5266 6.56403 16.6215 7.53887 15.9416C7.74741 17.1117 8.76996 18 10 18C11.23 18 12.2525 17.1117 12.4611 15.9417C13.4359 16.6215 14.7871 16.5266 15.6568 15.6569C16.5265 14.7871 16.6215 13.436 15.9416 12.4611C17.1117 12.2526 18 11.23 18 10Z"
     :fill color}]
   [svg/path
    {:d            "M7.25 10.75L9.25 12.25L12.75 7.75"
     :stroke       color-2
     :stroke-width "1.2"}]])

(defn- untrustworthy-20
  [{:keys [color color-2]
    :or   {color   colors/neutral-100
           color-2 colors/white}
    :as   props}]
  [container props
   [svg/path
    {:d    "M10 2L15.6569 4.34315L18 10L15.6569 15.6569L10 18L4.34315 15.6569L2 10L4.34315 4.34315L10 2Z"
     :fill color}]
   [svg/path
    {:d
     "M10.75 5.5L10.55 11.5H9.45L9.25 5.5H10.75ZM10 13C10.4142 13 10.75 13.3358 10.75 13.75C10.75 14.1642 10.4142 14.5 10 14.5C9.58579 14.5 9.25 14.1642 9.25 13.75C9.25 13.3358 9.58579 13 10 13Z"
     :fill color-2}]])

(def ^:private icons
  {:i/clear-20         clear-20
   :i/dropdown-20      dropdown-20
   :i/pullup-20        pullup-20
   :i/dropdown-12      dropdown-12
   :i/pullup-12        pullup-12
   :i/contact-12       contact-12
   :i/contact-20       contact-20
   :i/verified-20      verified-20
   :i/untrustworthy-20 untrustworthy-20})

(defn- append-to-keyword
  [k & xs]
  (keyword (apply str
                  (subs (str k) 1)
                  xs)))

(defn get-icon
  [icon-name size]
  (get icons (append-to-keyword icon-name "-" size)))

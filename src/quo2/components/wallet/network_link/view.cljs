(ns quo2.components.wallet.network-link.view
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.svg :as svg]))

(defn link-linear
  [{:keys [source theme]}]
  [svg/svg {:xmlns "http://www.w3.org/2000/svg" :width "73" :height "10" :fill :none}
   [svg/path {:stroke (get colors/networks source) :d "M68 5H5"}]
   [svg/circle
    {:cx     "68"
     :cy     "5"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks source)}]
   [svg/circle
    {:cx     "5"
     :cy     "5"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks source)}]])

(defn link-1x
  [{:keys [source destination theme]}]
  [svg/svg {:xmlns "http://www.w3.org/2000/svg" :width "73" :height "66" :fill :none}
   [svg/path
    {:stroke "url(#gradient)" :d "M68 5h-9.364c-11.046 0-20 8.954-20 20v16c0 11.046-8.955 20-20 20H5"}]
   [svg/circle
    {:cx     "68"
     :cy     "5"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks destination)}]
   [svg/circle
    {:cx     "5"
     :cy     "61"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks source)}]
   [svg/defs
    [svg/linear-gradient
     {:id "gradient" :x1 "72.271" :x2 "82.385" :y1 "5" :y2 "34.155" :gradientUnits "userSpaceOnUse"}
     [svg/stop {:stopColor (get colors/networks destination)}]
     [svg/stop {:offset "1" :stopColor (get colors/networks source)}]]]])

(defn link-2x
  [{:keys [source destination theme]}]
  [svg/svg
   {:width "73" :height "122" :viewBox "0 0 73 122" :fill "none" :xmlns "http://www.w3.org/2000/svg"}
   [svg/path
    {:d
     "M67.9999 5L58.6356 5C47.5899 5 38.6356 13.9543 38.6356 25L38.6356 97C38.6356 108.046 29.6813 117 18.6356 117L5.00006 117"
     :stroke "url(#gradient)"}]
   [svg/circle
    {:cx     "68"
     :cy     "5"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks destination)}]
   [svg/circle
    {:cx     "5"
     :cy     "117"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (get colors/networks source)}]
   [svg/defs
    [svg/linear-gradient
     {:id            "gradient"
      :x1            "72.2711"
      :y1            "5.00001"
      :x2            "102.867"
      :y2            "49.0993"
      :gradientUnits "userSpaceOnUse"}
     [svg/stop {:stop-color (get colors/networks destination)}]
     [svg/stop {:offset "1" :stop-color (get colors/networks source)}]]]])

(defn- view-internal
  [{:keys [shape] :as props}]
  (case shape
    :linear [link-linear props]
    :1x     [link-1x props]
    :2x     [link-2x props]))

(def view (quo.theme/with-theme view-internal))

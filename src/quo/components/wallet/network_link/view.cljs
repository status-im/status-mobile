(ns quo.components.wallet.network-link.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.wallet.network-link.schema :as component-schema]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.svg :as svg]
    [reagent.core :as reagent]
    [schema.core :as schema]))

(defn circle
  [fill stroke]
  [svg/svg
   {:height "8" :width "8"}
   [svg/circle
    {:cx          "4"
     :cy          "4"
     :r           "3.5"
     :fill        fill
     :stroke      stroke
     :strokeWidth "1"}]])

(defn line
  [stroke]
  [rn/view
   {:style {:flex 1 :height 10}}
   [svg/svg
    {:height "10" :width "100%"}
    [svg/line
     {:x1 "0" :y1 "5" :x2 "100%" :y2 "5" :stroke stroke}]]])

(defn link-linear
  [{:keys [source theme]}]
  (let [stroke-color (colors/resolve-color source theme)
        fill-color   (colors/theme-colors colors/white colors/neutral-90 theme)]
    [rn/view
     {:style {:flex-direction :row :align-items :center :height 10}}
     [line stroke-color]
     [rn/view {:style {:position :absolute :left -3}}
      [circle fill-color stroke-color]]
     [rn/view {:style {:position :absolute :right -3}}
      [circle fill-color stroke-color]]]))

(def central-figure-width 63)

(defn- calculate-side-lines-path
  "Calculates the `d` attribute for the side lines based on the SVG width."
  [width]
  (let [side-offset (/ (- width central-figure-width) 2)]
    {:left  (str "M0 57 L" side-offset " 57")
     :right (str "M" (+ side-offset central-figure-width) " 1 L" width " 1")}))

(defn- calculate-transform
  "Calculates the transform attribute for the central figure based on the SVG width."
  [width]
  (let [translate-x (/ (- width central-figure-width) 2)]
    (str "translate(" translate-x " 0)")))

(defn link-1x
  "Arranges two circles with a custom path between them, designed for React Native."
  [{:keys [source destination theme]}]
  (let [container-width   (reagent/atom 100)
        source-color      (colors/resolve-color source theme)
        destination-color (colors/resolve-color destination theme)
        fill-color        (colors/theme-colors colors/white colors/neutral-90 theme)
        stroke-color      "url(#gradient)"]
    (fn []
      (let [view-box          (str "0 0 " @container-width " 58")
            side-lines-path   (calculate-side-lines-path @container-width)
            central-transform (calculate-transform @container-width)]
        [rn/view
         {:style     {:flex-direction :row :align-items :center :height 58}
          :on-layout #(reset! container-width
                        (oget % :nativeEvent :layout :width))}
         [rn/view
          {:style {:flex 1 :height 58 :justify-content "center"}}
          [svg/svg
           {:xmlns   "http://www.w3.org/2000/svg"
            :height  "100%"
            :width   "100%"
            :viewBox view-box
            :fill    :none}
           [svg/path
            {:d      (:left side-lines-path)
             :stroke stroke-color}]
           [svg/path
            {:d
             "M63 1L53.6356 1C42.5899 1 33.6356 9.9543 33.6356 21L33.6356 37C33.6356 48.0457 24.6813 57 13.6356 57L0 57"
             :transform central-transform
             :stroke stroke-color}]
           [svg/path
            {:d      (:right side-lines-path)
             :stroke stroke-color}]
           [svg/defs
            [svg/linear-gradient
             {:id            "gradient"
              :x1            "72.271"
              :x2            "82.385"
              :y1            "5"
              :y2            "34.155"
              :gradientUnits "userSpaceOnUse"}
             [svg/stop {:stopColor (colors/resolve-color destination theme)}]
             [svg/stop {:offset "1" :stopColor (colors/resolve-color source theme)}]]]]
          [rn/view {:style {:position :absolute :bottom -3 :left -3}}
           [circle fill-color source-color]]
          [rn/view {:style {:position :absolute :top -3 :right -3}}
           [circle fill-color destination-color]]]]))))

(defn link-2x
  [{:keys [source destination theme]}]
  [svg/svg
   {:xmlns               "http://www.w3.org/2000/svg"
    :width               "108%"
    :height              "122"
    :viewBox             "0 0 73 122"
    :preserveAspectRatio "xMinYMid meet"
    :fill                :none}
   [svg/path
    {:d
     "M67.9999 5L58.6356 5C47.5899 5 38.6356 13.9543 38.6356 25L38.6356 97C38.6356 108.046 29.6813 117 18.6356 117L5.00006 117"
     :stroke "url(#gradient)"}]
   [svg/circle
    {:cx     "68"
     :cy     "5"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (colors/resolve-color destination theme)}]
   [svg/circle
    {:cx     "5"
     :cy     "117"
     :r      "4"
     :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
     :stroke (colors/resolve-color source theme)}]
   [svg/defs
    [svg/linear-gradient
     {:id            "gradient"
      :x1            "72.2711"
      :y1            "5.00001"
      :x2            "102.867"
      :y2            "49.0993"
      :gradientUnits "userSpaceOnUse"}
     [svg/stop {:stop-color (colors/resolve-color destination theme)}]
     [svg/stop {:offset "1" :stop-color (colors/resolve-color source theme)}]]]])

(defn- view-internal
  [{:keys [shape container-style] :as props}]
  [rn/view {:style container-style}
   (case shape
     :linear [link-linear props]
     :1x     [link-1x props]
     :2x     [link-2x props])])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))

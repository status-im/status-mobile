(ns quo.components.wallet.network-link.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.wallet.network-link.helpers :as helpers]
    [quo.components.wallet.network-link.schema :as component-schema]
    [quo.components.wallet.network-link.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.svg :as svg]
    [reagent.core :as reagent]
    [schema.core :as schema]))

(defn- circle
  [fill stroke]
  [svg/svg
   {:height "8"
    :width  "8"}
   [svg/circle
    {:cx          "4"
     :cy          "4"
     :r           "3.5"
     :fill        fill
     :stroke      stroke
     :strokeWidth "1"}]])

(defn- line
  [stroke width]
  [svg/svg
   {:height   "10"
    :width    "100%"
    :view-box (str "0 0 " width " 10")}
   [svg/path
    {:d            (str "M0,5 L" width ",5")
     :stroke       stroke
     :stroke-width "1"}]])

(defn link-linear
  []
  (let [container-width (reagent/atom 100)]
    (fn [{:keys [source]}]
      (let [theme        (quo.theme/use-theme)
            stroke-color (colors/resolve-color source theme)
            fill-color   (colors/theme-colors colors/white colors/neutral-90 theme)]
        [rn/view
         {:style     style/link-linear-container
          :on-layout (fn [e]
                       (reset! container-width
                         (oget e :nativeEvent :layout :width)))}
         [line stroke-color @container-width]
         [rn/view {:style style/left-circle-container}
          [circle fill-color stroke-color]]
         [rn/view {:style style/right-circle-container}
          [circle fill-color stroke-color]]]))))

(defn link-1x
  []
  (let [container-width (reagent/atom 100)
        stroke-color    "url(#gradient)"]
    (fn [{:keys [source destination]}]
      (let [theme             (quo.theme/use-theme)
            source-color      (colors/resolve-color source theme)
            destination-color (colors/resolve-color destination theme)
            fill-color        (colors/theme-colors colors/white colors/neutral-90 theme)
            view-box          (str "0 0 " @container-width " 58")
            side-lines-path   (helpers/calculate-side-lines-path-1x @container-width)
            central-transform (helpers/calculate-transform @container-width)]
        [rn/view
         {:style     style/link-1x-container
          :on-layout (fn [e]
                       (reset! container-width
                         (oget e :nativeEvent :layout :width)))}
         [svg/svg
          {:xmlns    "http://www.w3.org/2000/svg"
           :height   "100%"
           :width    "100%"
           :view-box view-box
           :fill     :none}
          [svg/path
           {:d      (:left side-lines-path)
            :stroke source-color}]
          [svg/path
           {:d
            "M63 1L53.6356 1C42.5899 1 33.6356 9.9543 33.6356 21L33.6356 37C33.6356 48.0457 24.6813 57 13.6356 57L2.85889e-05 57"
            :transform central-transform
            :stroke stroke-color}]
          [svg/path
           {:d      (:right side-lines-path)
            :stroke destination-color}]
          [svg/defs
           [svg/linear-gradient
            {:id             "gradient"
             :x1             "72.271"
             :x2             "82.385"
             :y1             "5"
             :y2             "34.155"
             :gradient-units "userSpaceOnUse"}
            [svg/stop {:stop-color (colors/resolve-color destination theme)}]
            [svg/stop {:offset "1" :stop-color (colors/resolve-color source theme)}]]]]
         [rn/view {:style style/bottom-left-circle-container}
          [circle fill-color source-color]]
         [rn/view {:style style/top-right-circle-container}
          [circle fill-color destination-color]]]))))

(defn link-2x
  []
  (let [container-width (reagent/atom 100)
        stroke-color    "url(#gradient)"]
    (fn [{:keys [source destination]}]
      (let [theme             (quo.theme/use-theme)
            source-color      (colors/resolve-color source theme)
            destination-color (colors/resolve-color destination theme)
            fill-color        (colors/theme-colors colors/white colors/neutral-90 theme)
            view-box          (str "0 0 " @container-width " 114")
            side-lines-path   (helpers/calculate-side-lines-path-2x @container-width)
            central-transform (helpers/calculate-transform @container-width)]
        [rn/view
         {:style     style/link-2x-container
          :on-layout #(reset! container-width
                        (oget % :nativeEvent :layout :width))}
         [svg/svg
          {:xmlns    "http://www.w3.org/2000/svg"
           :height   "100%"
           :width    "100%"
           :view-box view-box
           :fill     :none}
          [svg/path
           {:d      (:left side-lines-path)
            :stroke source-color}]
          [svg/path
           {:d
            "M62.9999 1L53.6356 1C42.5899 1 33.6356 9.9543 33.6356 21L33.6356 93C33.6356 104.046 24.6813 113 13.6356 113L5.71778e-05 113"
            :transform central-transform
            :stroke stroke-color}]
          [svg/path
           {:d      (:right side-lines-path)
            :stroke destination-color}]
          [svg/defs
           [svg/linear-gradient
            {:id             "gradient"
             :x1             "72.2711"
             :y1             "5.00001"
             :x2             "102.867"
             :y2             "49.0993"
             :gradient-units "userSpaceOnUse"}
            [svg/stop {:stop-color (colors/resolve-color destination theme)}]
            [svg/stop {:offset "1" :stop-color (colors/resolve-color source theme)}]]]]
         [rn/view {:style style/bottom-left-circle-container}
          [circle fill-color source-color]]
         [rn/view {:style style/top-right-circle-container}
          [circle fill-color destination-color]]]))))

(defn- view-internal
  [{:keys [shape container-style] :as props}]
  [rn/view {:style container-style}
   (case shape
     :linear [link-linear props]
     :1x     [link-1x props]
     :2x     [link-2x props])])

(def view (schema/instrument #'view-internal component-schema/?schema))

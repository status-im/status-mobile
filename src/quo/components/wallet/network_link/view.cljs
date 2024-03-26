(ns quo.components.wallet.network-link.view
  (:require
    [quo.components.wallet.network-link.schema :as component-schema]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.svg :as svg]
    [schema.core :as schema]
    [reagent.core :as reagent]))

(defn circle
  [fill stroke]
  [svg/svg
   {:height "8" :width "8"}
   [svg/circle
    {:cx          "4"
     :cy          "4"
     :r           "3.5" ;; Adjusted radius to account for stroke width
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

(defn path
  [stroke d defs]
  [rn/view
   {:style {:flex 1 :height 66 :justifyContent "center"}}
   [svg/svg
    {:height "66" :width "100%"}
    [svg/path {:stroke stroke :d d :fill "none"}]
    (when defs defs)]])

(defn calculate-path
  "Calculates a dynamic SVG path based on the container's width."
  [{:keys [width height]}]
  (let [start-x                 (dec width) ; Assuming you want a slight margin, adjust as needed
        curve-begin-x           (- width 10) ; Adjust based on where you want the curve to start
        first-line-end-x        (- width 20)
        first-curve-control-x1  (- width 30)
        first-curve-control-y1  1
        first-curve-control-x2  (- width 40)
        first-curve-control-y2  10
        first-curve-end-x       (- width 40)
        first-curve-end-y       21
        vertical-line-end-y     37
        second-curve-control-x1 (- width 40)
        second-curve-control-y1 48
        second-curve-control-x2 (- width 50)
        second-curve-control-y2 height
        second-curve-end-x      (- width 60)
        second-curve-end-y      height
        end-x                   0
        path                    (str
                                 "M"
                                 start-x
                                 " 1 "
                                 "L"
                                 first-line-end-x
                                 " 1 "
                                 "C"
                                 first-curve-control-x1
                                 " "
                                 first-curve-control-y1
                                 " "
                                 first-curve-control-x2
                                 " "
                                 first-curve-control-y2
                                 " "
                                 first-curve-end-x
                                 " "
                                 first-curve-end-y
                                 " "
                                 "L"
                                 first-curve-end-x
                                 " "
                                 vertical-line-end-y
                                 " "
                                 "C"
                                 second-curve-control-x1
                                 " "
                                 second-curve-control-y1
                                 " "
                                 second-curve-control-x2
                                 " "
                                 second-curve-control-y2
                                 " "
                                 second-curve-end-x
                                 " "
                                 second-curve-end-y
                                 " "
                                 "L"
                                 end-x
                                 " height")] ; Assuming the path ends at the left edge
    (println path width)
    path))

(defn link-linear
  [{:keys [source theme]}]
  (let [stroke-color (colors/resolve-color source theme)
        fill-color   (colors/theme-colors colors/white colors/neutral-90 theme)]
    [rn/view
     {:style {:flex-direction :row :align-items :center :height 10}}
     [circle fill-color stroke-color]
     [line stroke-color]
     [circle fill-color stroke-color]]))

#_(defn link-linear
    [{:keys [source theme]}]
    [svg/svg
     {:xmlns               "http://www.w3.org/2000/svg"
      :width               "108%"
      :height              "20"
      :viewBox             "0 0 73 10"
      :preserveAspectRatio "xMinYMid meet"
      :fill                :none}
     [svg/path
      {:stroke (colors/resolve-color source theme)
       :d      "M68 5H5"}]
     [svg/circle
      {:cx     "68"
       :cy     "5"
       :r      "4"
       :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
       :stroke (colors/resolve-color source theme)}]
     [svg/circle
      {:cx     "5"
       :cy     "5"
       :r      "4"
       :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
       :stroke (colors/resolve-color source theme)}]])

(defn link-1x
  "Arranges two circles with a custom path between them, designed for React Native."
  [{:keys [source destination theme]}]
  (let [dimensions        (reagent/atom {:width 100 :height 66})
        source-color      (colors/resolve-color source theme)
        destination-color (colors/resolve-color destination theme)
        fill-color        (colors/theme-colors colors/white colors/neutral-90 theme)
        stroke-color      "black"]
    [rn/view
     {:style      {:flex-direction :row :align-items :center :height 66}
      ::on-layout #(let [event  (.-nativeEvent %)
                         layout (.-layout event)]
                     (reset! dimensions {:width  (.-width layout)
                                         :height (.-height layout)}))}
     [circle fill-color destination-color]
     [path stroke-color (calculate-path @dimensions)]
     [circle fill-color source-color]]))

#_(defn link-1x
    [{:keys [source destination theme]}]
    [svg/svg
     {:xmlns               "http://www.w3.org/2000/svg"
      :width               "100%"
      :height              "66"
      :viewBox             "0 0 73 66"
      :preserveAspectRatio "xMinYMid meet"
      :fill                :none}
     [svg/path
      {:stroke "url(#gradient)"
       :d      "M68 5h-9.364c-11.046 0-20 8.954-20 20v16c0 11.046-8.955 20-20 20H5"}]
     [svg/circle
      {:cx     "68"
       :cy     "5"
       :r      "4"
       :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
       :stroke (colors/resolve-color destination theme)}]
     [svg/circle
      {:cx     "5"
       :cy     "61"
       :r      "4"
       :fill   (colors/theme-colors colors/white colors/neutral-90 theme)
       :stroke (colors/resolve-color source theme)}]
     [svg/defs
      [svg/linear-gradient
       {:id "gradient" :x1 "72.271" :x2 "82.385" :y1 "5" :y2 "34.155" :gradientUnits "userSpaceOnUse"}
       [svg/stop {:stopColor (colors/resolve-color destination theme)}]
       [svg/stop {:offset "1" :stopColor (colors/resolve-color source theme)}]]]])

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

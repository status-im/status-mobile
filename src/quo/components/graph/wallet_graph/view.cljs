(ns quo.components.graph.wallet-graph.view
  (:require
    [quo.components.graph.utils :as utils]
    [quo.components.graph.wallet-graph.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.charts :as charts]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.linear-gradient :as linear-gradient]))

(defn- time-frame->max-data-points
  [time-frame]
  (case time-frame
    :empty    0
    :1-week   7
    :1-month  30
    :3-months 90
    :1-year   365
    500))

(defn- get-line-color
  [customization-color state theme]
  (let [color-keyword (cond
                        customization-color customization-color
                        (= state :positive) :success
                        :else               :danger)]
    (colors/resolve-color color-keyword theme)))

(defn view
  [{:keys [data state time-frame customization-color]}]
  (let [theme           (quo.theme/use-theme)
        max-data-points (time-frame->max-data-points time-frame)
        data            (if (and (not= time-frame :empty) (> (count data) max-data-points))
                          (utils/downsample-data data max-data-points)
                          data)
        max-value       (when-not (= time-frame :empty) (utils/find-highest-value data))
        width           (:width (rn/get-window))
        line-color      (get-line-color customization-color state theme)
        gradient-colors [(colors/alpha line-color 0.1) (colors/alpha line-color 0)]
        fill-color      (colors/theme-colors colors/white colors/neutral-95 theme)]
    (if (= time-frame :empty)
      [fast-image/fast-image
       {:style               style/empty-state
        :source              (resources/get-themed-image :no-funds theme)
        :accessibility-label :illustration}]
      [rn/view
       [linear-gradient/linear-gradient
        {:colors gradient-colors
         :start  {:x 0 :y 1}
         :end    {:x 0 :y 0}
         :style  style/gradient-background}]
       [rn/view {:accessibility-label :line-chart}
        [charts/line-chart
         {:height                  96
          :width                   (inc width)
          :max-value               max-value
          :min-value               0
          :adjust-to-width         true
          :data                    data
          :area-chart              true
          :start-fill-color        fill-color
          :end-fill-color          fill-color
          :hide-data-points        true
          :hide-rules              true
          :hide-y-axis-text        true
          :x-axis-indices-height   100
          :thickness               2
          :color                   line-color
          :y-axis-thickness        0
          :x-axis-thickness        0
          :initial-spacing         0
          :end-spacing             0
          :disable-scroll          true
          :y-axis-label-width      0.01
          :labels-extra-height     -36
          :x-axis-label-text-style style/x-axis-label-text-style}]]])))

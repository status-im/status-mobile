(ns quo2.components.graph.wallet-graph.view
  (:require [quo2.theme :as quo.theme]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.charts :as charts]
            [react-native.core :as rn]
            [quo2.components.graph.wallet-graph.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.graph.utils :as utils]))

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
    (colors/theme-colors
     (colors/custom-color color-keyword 50)
     (colors/custom-color color-keyword 60)
     theme)))

(defn- view-internal
  [{:keys [data state time-frame customization-color theme]}]
  (let [max-data-points (time-frame->max-data-points time-frame)
        data            (if (and (not= time-frame :empty) (> (count data) max-data-points))
                          (utils/downsample-data data max-data-points)
                          data)
        max-value       (when-not (= time-frame :empty) (utils/find-highest-value data))
        width           (:width (rn/get-window))
        line-color      (get-line-color customization-color state theme)
        gradient-colors [(colors/alpha line-color 0.1) (colors/alpha line-color 0)]
        fill-color      (colors/theme-colors colors/white colors/neutral-95)]
    (if (= time-frame :empty)
      [rn/view
       {:accessibility-label :illustration
        :style               style/illustration}
       [text/text {:style {:color colors/white}}
        "Illustration here"]]
      [rn/view
       [linear-gradient/linear-gradient
        {:colors gradient-colors
         :start  {:x 0 :y 1}
         :end    {:x 0 :y 0}
         :style  style/gradient-background}]
       [rn/view {:accessibility-label :line-chart}
        [charts/line-chart
         {:height                  96
          :width                   (+ width 1)
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

(def view (quo.theme/with-theme view-internal))

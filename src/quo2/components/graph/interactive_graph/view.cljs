(ns quo2.components.graph.interactive-graph.view
  (:require [quo2.components.graph.utils :as utils]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.charts :as charts]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.components.graph.interactive-graph.style :as style]))

(def chart-height 375)
(def max-data-points 500)
(def no-of-sections 4)
(def initial-spacing 56)
(def end-spacing 22)
(def y-axis-label-width -33)
(def inspecting? (reagent/atom false))

(defn- pointer
  [customization-color]
  (reagent/as-element
   [rn/view
    {:style
     (style/pointer-component
      customization-color)}]))

(defn- pointer-config
  [customization-color]
  {:stroke-dash-array             [2 2]
   :pointer-component             #(pointer customization-color)
   :pointer-strip-color           customization-color
   :pointer-color                 customization-color
   :pointer-strip-enable-gradient true})

(defn- get-pointer-props
  [pointer-props]
  (let [pointer-index (.-pointerIndex ^js pointer-props)]
    (reset! inspecting? (not= pointer-index -1))))

(defn- get-line-color
  [state theme]
  (if @inspecting?
    (colors/theme-colors colors/neutral-80-opa-40
                         colors/white-opa-20
                         theme)
    (if (= state :positive)
      (colors/theme-colors colors/success-50
                           colors/success-60
                           theme)
      (colors/theme-colors colors/danger-50
                           colors/danger-60
                           theme))))

(defn- view-internal
  [{:keys [data state customization-color theme reference-value reference-prefix decimal-separator]
    :or   {reference-prefix  "$"
           decimal-separator :dot}}]
  (let [data                             (if (> (count data) max-data-points)
                                           (utils/downsample-data data max-data-points)
                                           data)
        highest-value                    (utils/find-highest-value data)
        lowest-value                     (utils/find-lowest-value data)
        min-value                        (utils/calculate-rounded-min lowest-value)
        max-value                        (- (utils/calculate-rounded-max highest-value) min-value)
        step-value                       (/ max-value 4)
        width                            (:width (rn/get-window))
        line-color                       (get-line-color state theme)
        rules-color                      (colors/theme-colors colors/neutral-80-opa-10
                                                              colors/white-opa-5
                                                              theme)
        y-axis-label-text-color          (colors/theme-colors colors/neutral-80-opa-40
                                                              colors/white-opa-40
                                                              theme)
        price-reference-label-text-color (colors/theme-colors colors/neutral-100 colors/white theme)
        reference-label-border-color     (colors/theme-colors colors/white colors/neutral-95 theme)
        y-axis-label-background-color    (colors/theme-colors colors/white-70-blur-opaque
                                                              colors/neutral-95
                                                              theme)
        customization-color              (colors/theme-colors
                                          (colors/custom-color customization-color 60)
                                          (colors/custom-color customization-color 50)
                                          theme)
        y-axis-label-texts               (utils/calculate-y-axis-labels min-value step-value 4)
        x-axis-label-texts               (utils/calculate-x-axis-labels data 5)
        reference-label-background-color (colors/theme-colors colors/neutral-80-opa-5-opaque
                                                              colors/neutral-80
                                                              theme)
        reference-value                  (or reference-value (/ (+ highest-value lowest-value) 2))
        formatted-reference-value        (utils/format-currency-number reference-value decimal-separator)
        chart-width                      (+ width 13)]
    [rn/view {:accessibility-label :interactive-graph}
     [charts/line-chart
      {:height                           chart-height
       :width                            chart-width
       :max-value                        max-value
       :x-axis-length                    chart-width
       :y-axis-offset                    min-value
       :y-axis-label-texts               y-axis-label-texts
       :y-axis-label-texts-ignore-offset true
       :adjust-to-width                  true
       :data                             data
       :hide-data-points                 true
       :no-of-sections                   no-of-sections
       :step-value                       step-value
       :rules-color                      rules-color
       :dash-width                       2
       :dash-gap                         2
       :hide-y-axis-text                 false
       :thickness                        1
       :color                            line-color
       :y-axis-thickness                 0
       :x-axis-thickness                 0
       :initial-spacing                  initial-spacing
       :end-spacing                      end-spacing
       :disable-scroll                   true
       :hide-origin                      true
       :show-reference-line-1            true
       :get-pointer-props                get-pointer-props
       :show-strip-on-focus              true
       :reference-line-1-config          {:color rules-color}
       :reference-line-1-position        0
       :show-reference-line-2            (and (not @inspecting?)
                                              (<= reference-value highest-value)
                                              (>= reference-value lowest-value))
       :reference-line-2-config          {:color            y-axis-label-text-color
                                          :label-text-style (style/reference-line-label
                                                             reference-label-border-color
                                                             reference-label-background-color
                                                             price-reference-label-text-color)
                                          :label-text       (str reference-prefix
                                                                 formatted-reference-value)
                                          :dash-width       2}
       :reference-line-2-position        (- reference-value min-value)
       :y-axis-text-style                (style/y-axis-text y-axis-label-text-color
                                                            y-axis-label-background-color)
       :y-axis-label-width               y-axis-label-width
       :pointer-config                   (pointer-config customization-color)
       :x-axis-label-text-style          (style/x-axis-label-text (/ width (count x-axis-label-texts))
                                                                  y-axis-label-text-color)
       :x-axis-label-texts               x-axis-label-texts}]]))

(def view (quo.theme/with-theme view-internal))

(ns legacy.status-im.ui.components.tooltip
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.spacing :as spacing]
    [oops.core :refer [oget]]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [reagent.core :as reagent]))

(def ^:private initial-height 22)

(defn tooltip-style
  [{:keys [bottom-value]}]
  (merge
   (:base spacing/padding-horizontal)
   {:position    :absolute
    :align-items :center
    :left        0
    :right       0
    :top         (- bottom-value)
    :opacity     1
    :transform   [{:translateY 10}]}))

(defn container-style
  []
  {:z-index        2
   :align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}})

(defn content-style
  []
  (merge (:base spacing/padding-horizontal)
         {:padding-vertical 6
          :elevation        2
          :background-color (:ui-background @colors/theme)
          :border-radius    8}))

(defn tooltip
  []
  (let [layout    (reagent/atom {:height initial-height})
        on-layout (fn [evt]
                    (let [width  (oget evt "nativeEvent" "layout" "width")
                          height (oget evt "nativeEvent" "layout" "height")]
                      (reset! layout {:width  width
                                      :height height})))]
    (fn [{:keys [bottom-value accessibility-label]} & children]
      [:<>
       [rn/view
        {:style          (tooltip-style {:bottom-value (- (get @layout :height)
                                                          bottom-value)})
         :pointer-events :box-none}
        [rn/view
         {:style          (container-style)
          :pointer-events :box-none}
         (into [rn/view
                {:style               (content-style)
                 :pointer-events      :box-none
                 :accessibility-label accessibility-label
                 :on-layout           on-layout}]
               children)

         (when platform/ios?
           ;; NOTE(Ferossgp): Android does not show elevation for tooltip making it lost on white bg
           [icons/icon :icons/tooltip-tip
            {:width           18
             :height          8
             :container-style {:elevation 3}
             :color           (:ui-background @colors/theme)}])]]])))

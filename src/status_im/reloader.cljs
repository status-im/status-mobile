(ns status-im.reloader
  (:require [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]))

(def cnt (reagent/atom 0))
(defonce cnt-prev (reagent/atom 0))
(defonce warning? (reagent/atom false))
(defonce visible (reagent/atom false))
(defonce timeout (reagent/atom false))
(defonce label (reagent/atom ""))

(defn reload []
  (reset! warning? false)
  (reset! label "reloading UI")
  (swap! cnt inc))

(defn build-competed []
  (reset! label "reloading code")
  (reset! warning? false)
  (reset! visible true))

(defn build-failed []
  (reset! warning? true)
  (reset! label "building failed")
  (reset! visible true))

(defn build-start []
  (reset! warning? false)
  (reset! label "building")
  (reset! visible true))

(defn reload-view [_]
  (fn [cnt]
    (when @timeout (js/clearTimeout @timeout))
    (when (not= @cnt-prev cnt)
      (reset! cnt-prev cnt)
      (reset! visible true)
      (reset! timeout (js/setTimeout #(reset! visible false) 1000)))
    (when @visible
      [react/view {:pointerEvents :none
                   :style         {:position        :absolute :top 0 :left 0 :right 0 :bottom 0
                                   :justify-content :center :align-items :center}}
       [react/image {:source      (resources/get-image :status-logo)
                     :resize-mode :center
                     :style       (merge {:width  64
                                          :height 64}
                                         (when @warning?
                                           {:opacity          0.8
                                            :borderWidth      2
                                            :border-color     :red
                                            :background-color "rgba(255,0,0,0.5))"}))}]
       [react/text {:style {:margin-top 10 :color (if @warning? :red colors/black)}}
        @label]])))

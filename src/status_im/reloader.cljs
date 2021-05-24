(ns status-im.reloader
  (:require [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]))

(def cnt (reagent/atom 0))
(defonce cnt-prev (reagent/atom 0))
(defonce warning? (reagent/atom false))
(defonce visible (reagent/atom false))
(defonce timeout (reagent/atom false))
(defonce label (reagent/atom ""))

(defn reload []
  (when @timeout (js/clearTimeout @timeout))
  (reset! timeout (js/setTimeout #(reset! visible false) 500))
  (reset! warning? false)
  (reset! visible true)
  (reset! label "reloading UI")
  (re-frame/clear-subscription-cache!)
  (swap! cnt inc))

(defn build-competed []
  (reset! label "reloading code")
  (reset! warning? false)
  (reset! visible true))

(defn build-failed [warnings]
  (reset! warning? true)
  (reset! label (str "building failed"
                     (when (seq warnings)
                       (str "\n" (count warnings) " warnings"))))
  (reset! visible true))

(defn build-start []
  (reset! warning? false)
  (reset! label "building")
  (reset! visible true))

(defn build-notify [{:keys [type info]}]
  (cond (= :build-start type)
        (build-start)
        (or (= :build-failure type)
            (and (= :build-complete type) (seq (:warnings info))))
        (build-failed (:warnings info))
        (= :build-complete type)
        (build-competed)))

(defn reload-view [_]
  (fn []
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

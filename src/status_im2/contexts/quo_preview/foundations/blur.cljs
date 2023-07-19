(ns status-im2.contexts.quo-preview.foundations.blur
  (:require [react-native.core :as rn]
            [status-im2.contexts.quo-preview.preview :as preview]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [quo2.foundations.blur.view :as blur]
            [react-native.blur :as rn-blur]
            [quo2.foundations.colors :as colors]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :blur-light
               :value "Blur Light"}
              {:key   :blur-dark
               :value "Blur Dark"}
              {:key   :blur-over-blur
               :value "Blur Over Blur"}
              {:key   :notification-blur-light
               :value "Notification Blur Light"}
              {:key   :notiifcation-blur-dark
               :value "Notification Blur Dark"}]}])

(defn blur-over-blur-example
  []
  [rn/view
   {:style {:position :absolute
            :top      0
            :left     0
            :right    0
            :bottom   0}}
   [rn-blur/view
    {:blur-radius   20
     :blur-amount   40
     :blur-type     :dark
     :overlay-color colors/neutral-80
     :style         {:position :absolute
                     :top      0
                     :left     0
                     :right    0
                     :bottom   0
                     :opacity  0.9}}]
   [blur/view
    {:blur-type       :blur-over-blur-back
     :container-style {:top           10
                       :left          40
                       :right         40
                       :bottom        30
                       :border-radius 20}}]
   [blur/view
    {:blur-type       :blur-over-blur-middle
     :container-style {:top           20
                       :left          50
                       :right         30
                       :bottom        20
                       :border-radius 20}}]
   [blur/view
    {:blur-type       :blur-over-blur-front
     :container-style {:top           30
                       :left          60
                       :right         20
                       :bottom        10
                       :border-radius 20}}]])


(defn preview-blur
  []
  (let [window-width (:width (rn/get-window))
        state        (reagent/atom {:type :blur-light})]
    (fn []
      [:<>
       [rn/view
        {:style {:height 50}}
        [preview/customizer state descriptor]]
       [rn/view {:style {}}

        [rn/image
         {:style  {:width  window-width
                   :height 250}
          :source (resources/get-mock-image :blur-background)}]
        (if (= (:type @state) :blur-over-blur)
          [blur-over-blur-example]
          [blur/view
           {:blur-type       (:type @state)
            :container-style {:top           20
                              :left          40
                              :right         40
                              :bottom        20
                              :border-radius 20}}])]])))

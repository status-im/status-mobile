(ns status-im2.contexts.chat.messages.content.image.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
            [status-im2.common.constants :as constants]
            [reagent.core :as reagent]))

(defn image-set-size
  [dimensions]
  (fn [evt]
    (let [width      (.-width (.-nativeEvent evt))
          height     (.-height (.-nativeEvent evt))
          max-width  (if (> width height) 320 190)
          max-height (if (> width height) 190 320)]
      (if (> height width)
        (let [calculated-height (* (min height max-height) (/ (max width max-width) width))
              calculated-width  (* (max width max-width) (/ (min height max-height) height))]
          (reset! dimensions {:width calculated-width :height calculated-height :loaded true}))

        (let [calculated-height (* (max height max-height) (/ (min width max-width) width))
              calculated-width  (* (min width max-width) (/ (max height max-height) height))]
          (reset! dimensions {:width calculated-width :height calculated-height :loaded true}))))))

(defn image-message
  [{:keys [content]}]
  (let [dimensions (reagent/atom {:width 320 :height 320 :loaded false})]
    (fn [message]
      (let [style-opts {:outgoing false
                        :opacity  (if (:loaded @dimensions) 1 0)
                        :width    (:width @dimensions)
                        :height   (:height @dimensions)}]
        [fast-image/fast-image
         {:source   {:uri (:image content)}
          :on-load  (image-set-size dimensions)
          :on-error #(swap! dimensions assoc :error true)
          :style    (dissoc style-opts :outgoing)}]))))

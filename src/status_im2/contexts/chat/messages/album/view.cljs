(ns status-im2.contexts.chat.messages.album.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.messages.album.style :as style]))

(def image-max-width 260)
(def image-max-height 192)

(def image-sizes
  {4        {0 146
             1 146
             2 146
             3 146}
   5        {0 146
             1 146
             2 97
             3 97
             4 97}
   :default {0 146
             1 146
             2 72.5
             3 72.5
             4 72.5
             5 72.5}})

(defn image-set-size
  [dimensions]
  (fn [evt]
    (let [width  (.-width (.-nativeEvent evt))
          height (.-height (.-nativeEvent evt))]
      (if (< width height)
        ;; if width less than the height we reduce width proportionally to height
        (let [k (/ height image-max-height)]
          (when (not= (/ width k) (first @dimensions))
            (reset! dimensions {:width (/ width k) :height image-max-height :loaded true})))
        (swap! dimensions assoc :loaded true)))))

(defn album-message
  [message]
  (let [dimensions (reagent/atom {:width image-max-width :height image-max-height :loaded false})]
    [rn/view
     {:style {:flex-direction :row
              :flex-wrap      :wrap
              :border-radius  12
              :width          293
              :overflow       :hidden}}
     (map-indexed (fn [index item]
                    (let [images-count    (count (:album message))
                          images-size-key (if (< images-count 6) images-count :default)
                          size            (get-in image-sizes [images-size-key index])]
                      [rn/view {:key (str index)}
                       [fast-image/fast-image
                        {:style    {:width         size
                                    :height        size
                                    :margin-left   (when (and (not= index 0) (not= index 2)) 1)
                                    :margin-bottom (when (or (= index 0) (= index 1)) 1)}
                         :on-load  (image-set-size dimensions)
                         :on-error #(swap! dimensions assoc :error true)
                         :source   {:uri (:image (:content item))}}]
                       (when (and (> images-count 6) (= index 5))
                         [rn/view {:style style/overlay}
                          [quo/text
                           {:weight :bold
                            :size   :heading-2
                            :style  {:color colors/white}} (str "+" (- images-count 5))]])]))
                  (:album message))]))

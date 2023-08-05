(ns quo2.components.profile.collectible.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.profile.collectible.style :as style]
            [react-native.core :as rn]))

(defn remaining-tiles
  [amount]
  [rn/view {:style (merge style/bottom-right (style/remaining-tiles))}
   [text/text
    {:style  (style/remaining-tiles-text)
     :size   :paragraph-2
     :weight :medium}
    (str "+" amount)]])

(defn tile
  [{:keys [style resource size]}]
  (let [source (if (string? resource) {:uri resource} resource)]
    [rn/view {:style style}
     [rn/image
      {:style  (style/tile-style-by-size size)
       :source source}]]))

(defn two-tiles
  [{:keys [images size]}]
  [:<>
   [tile
    {:style    style/top-left
     :size     size
     :resource (first images)}]
   [tile
    {:style    style/bottom-right
     :size     size
     :resource (second images)}]])

(defn three-tiles
  [{:keys [tiles size]}]
  (let [[image-1 image-2 image-3 & _] tiles]
    [:<>
     [tile
      {:style    style/top-left
       :size     size
       :resource image-1}]
     [tile
      {:style    style/top-right
       :size     size
       :resource image-2}]
     [tile
      {:style    style/bottom-left
       :size     size
       :resource image-3}]]))

(defn tile-container
  [{:keys [images]}]
  (let [num-images (count images)]
    (case num-images
      1 [tile
         {:resource (first images)
          :size     :xl}]
      2 [two-tiles
         {:images images
          :size   :lg}]
      3 [three-tiles
         {:tiles images
          :size  :md}]
      (let [[first-three-images remaining-images] (split-at 3 images)]
        [:<>
         [three-tiles {:tiles first-three-images :size :md}]
         [rn/view {:style style/tile-sub-container}
          (case num-images
            4 [tile
               {:resource (nth images 3 nil)
                :size     :md}]
            5 [two-tiles
               {:images (take 2 remaining-images)
                :size   :sm}]
            6 [three-tiles
               {:tiles (take 3 remaining-images)
                :size  :xs}]
            7 [:<>
               [three-tiles
                {:tiles (take 3 remaining-images)
                 :size  :xs}]
               [tile
                {:style    style/bottom-right
                 :size     :xs
                 :resource (nth remaining-images 3 nil)}]]
            [:<>
             [three-tiles
              {:tiles (take 3 remaining-images)
               :size  :xs}]
             [remaining-tiles (- (count remaining-images) 3)]])]]))))

(defn collectible
  [{:keys [images on-press]}]
  [rn/view {:style style/tile-outer-container}
   [rn/pressable
    {:on-press on-press
     :style    style/tile-inner-container}
    [tile-container {:images images}]]])

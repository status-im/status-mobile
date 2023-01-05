(ns status-im2.contexts.chat.messages.content.album.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
            [status-im2.common.constants :as constants]))

(def max-display-count 6)

(defn border-tlr [index]
  (when (= index 0) 12))

(defn border-trr [index]
  (when (= index 1) 12))

(defn border-blr [index count]
  (when (and (= index 2) (> count 2)) 12))

(defn border-brr [index count]
  (when (and (= index (- (min count max-display-count) 1)) (> count 2)) 12))

(defn album-message
  [message]
  [rn/view
   {:style style/album-container}
   (map-indexed (fn [index item]
                  (let [images-count    (count (:album message))
                        images-size-key (if (< images-count 6) images-count :default)
                        size            (get-in constants/album-image-sizes [images-size-key index])]
                    [rn/view {:key (:message-id item)}
                     [fast-image/fast-image
                      {:style  (merge (style/image size index)
                                      {:border-top-left-radius (border-tlr index)
                                       :border-top-right-radius (border-trr index)
                                       :border-bottom-left-radius (border-blr index images-count)
                                       :border-bottom-right-radius (border-brr index images-count)})
                       :source {:uri (:image (:content item))}}]
                     (when (and (> images-count max-display-count) (= index (- max-display-count 1)))
                       [rn/view {:style (merge style/overlay {:border-bottom-right-radius (border-brr index images-count)})}
                        [quo/text
                         {:weight :bold
                          :size   :heading-2
                          :style  {:color colors/white}} (str "+" (- images-count 5))]])]))
                (:album message))])

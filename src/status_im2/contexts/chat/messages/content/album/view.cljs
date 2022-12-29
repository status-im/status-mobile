(ns status-im2.contexts.chat.messages.content.album.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
            [status-im2.common.constants :as constants]))


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
                      {:style  (style/image size index)
                       :source {:uri (:image (:content item))}}]
                     (when (and (> images-count 6) (= index 5))
                       [rn/view {:style style/overlay}
                        [quo/text
                         {:weight :bold
                          :size   :heading-2
                          :style  {:color colors/white}} (str "+" (- images-count 5))]])]))
                (:album message))])

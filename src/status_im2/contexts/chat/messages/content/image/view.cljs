(ns status-im2.contexts.chat.messages.content.image.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
            [status-im2.common.constants :as constants]))

(defn image-message
  [{:keys [content]}]
  [fast-image/fast-image
   {:source {:uri (:image content)}}])

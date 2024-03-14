(ns status-im.contexts.chat.messenger.messages.content.sticker.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn sticker-message
  [{:keys [url]}]
  [rn/view {:style {:margin-top 6 :margin-bottom 4}}
   [fast-image/fast-image
    {:style  {:width 120 :height 120}
     :source {:uri (str url "&download=true")}}]])

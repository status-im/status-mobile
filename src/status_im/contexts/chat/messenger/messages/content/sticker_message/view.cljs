(ns status-im.contexts.chat.messenger.messages.content.sticker-message.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn view
  [{:keys [url]}]
  [rn/view {:style {:margin-top 6 :margin-bottom 4}}
   (print "------------ parvesh" url)
   [fast-image/fast-image
    {:style  {:width 120 :height 120}
     :on-error #(js/console.log "------------ parvesh js-logs" %)
     :source {:uri (str url "&download=true")}}]])

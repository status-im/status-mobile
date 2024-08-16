(ns status-im.contexts.chat.messenger.messages.content.sticker-message.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [oops.core :as  oops]))

(defn view
  [{:keys [url]}]
  [rn/view {:style {:margin-top 6 :margin-bottom 4}}
   (print "------------ parvesh" url)
   [rn/image
    {:style  {:width 120 :height 120}
     :on-error (fn [e]
                 (print "------ parvesh error"
                        (oops/oget e "nativeEvent.error")))
     :source {:uri (str url "&download=true")}}]])

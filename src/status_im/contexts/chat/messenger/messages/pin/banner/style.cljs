(ns status-im.contexts.chat.messenger.messages.pin.banner.style
  (:require
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]))

(def container
  {:position :absolute
   :overflow :hidden
   :left     0
   :right    0
   :height   messages.constants/pinned-banner-height})

(defn container-with-top-offset
  [top-offset]
  (assoc container :top top-offset))

(ns status-im.contexts.chat.messenger.messages.scroll-to-bottom.style
  (:require [react-native.safe-area :as safe-area]
            [status-im.contexts.shell.jump-to.constants :as shell.constants]))

(defn shell-button-container
  [able-to-send-messages?]
  {:z-index 1
   :bottom  (+ (safe-area/get-bottom)
               (when-not able-to-send-messages? 46)
               shell.constants/floating-shell-button-height)})

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})

(ns status-im.contexts.chat.messenger.messages.scroll-to-bottom.style
  (:require [react-native.safe-area :as safe-area]
            [status-im.contexts.shell.constants :as shell.constants]))

(def ^:private bottom-drawer-height 46)

(defn shell-button-container
  [able-to-send-messages?]
  {:z-index 1
   :bottom  (+ (safe-area/get-bottom)
               (when-not able-to-send-messages? bottom-drawer-height)
               shell.constants/floating-shell-button-height)})

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})

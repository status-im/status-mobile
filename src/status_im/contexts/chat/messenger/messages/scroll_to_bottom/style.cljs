(ns status-im.contexts.chat.messenger.messages.scroll-to-bottom.style
  (:require [react-native.safe-area :as safe-area]
            [status-im.contexts.shell.constants :as shell.constants]))

(def ^:private bottom-drawer-height 48)

(defn shell-button-container
  [able-to-send-messages?]
  {:z-index          1
   :background-color :red
   :bottom           (if-not able-to-send-messages?
                       (+ shell.constants/floating-shell-button-height
                          safe-area/bottom
                          bottom-drawer-height)
                       shell.constants/floating-shell-button-height)})

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})

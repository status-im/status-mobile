(ns status-im.contexts.chat.messenger.messages.scroll-to-bottom.style
  (:require [status-im.contexts.shell.jump-to.constants :as shell.constants]))

(def shell-button-container
  {:z-index 1
   :bottom  shell.constants/floating-shell-button-height})

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})

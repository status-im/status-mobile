(ns status-im.chat.default-chats
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(def default-chats
  (slurp "resources/chats.json"))
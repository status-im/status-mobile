(ns ^{:doc "Utils needed for chats related operations"}
 utils.chats
  (:require [status-im2.constants :as constants]))

(defn not-community-chat? [chat-type]
  (contains? #{constants/public-chat-type
               constants/private-group-chat-type
               constants/one-to-one-chat-type}
             chat-type))

(ns utils.worklets.chat.messenger.composer)

(def ^:private worklets (js/require "../src/js/worklets/chat/messenger/composer.js"))

(defn scroll-down-button-opacity
  [chat-list-scroll-y composer-focused? window-height]
  (.scrollDownButtonOpacity ^js worklets chat-list-scroll-y composer-focused? window-height))

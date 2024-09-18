(ns utils.worklets.chat.messenger.placeholder)

(def ^:private worklets (js/require "../src/js/worklets/chat/messenger/placeholder.js"))

(defn placeholder-opacity
  [chat-screen-layout-calculations-complete?]
  (.placeholderOpacity ^js worklets chat-screen-layout-calculations-complete?))

(defn placeholder-z-index
  [chat-screen-layout-calculations-complete?]
  (.placeholderZIndex ^js worklets chat-screen-layout-calculations-complete?))

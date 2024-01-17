(ns utils.worklets.chat.messenger.placeholder)

(def ^:private worklets (js/require "../src/js/worklets/chat/messenger/placeholder.js"))

(defn placeholder-opacity
  [calculations-complete?]
  (.placeholderOpacity ^js worklets calculations-complete?))

(defn placeholder-z-index
  [calculations-complete?]
  (.placeholderZIndex ^js worklets calculations-complete?))

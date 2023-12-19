(ns utils.worklets.chat.messages)

(def ^:private messages-worklets (js/require "../src/js/worklets/chat/messages.js"))

;;;; Navigtion
(defn navigation-header-opacity
  [distance-from-list-top all-loaded? calculations-complete? start-position]
  (.navigationHeaderOpacity ^js messages-worklets
                            distance-from-list-top
                            all-loaded?
                            calculations-complete?
                            start-position))

(defn navigation-header-position
  [distance-from-list-top all-loaded? top-bar-height start-position]
  (.navigationHeaderPosition ^js messages-worklets
                             distance-from-list-top
                             all-loaded?
                             top-bar-height
                             start-position))

(defn interpolate-navigation-view-opacity
  [props]
  (.interpolateNavigationViewOpacity ^js messages-worklets (clj->js props)))

(defn messages-list-on-scroll
  [distance-from-list-top callback]
  (.messagesListOnScroll ^js messages-worklets distance-from-list-top callback))

;;;; Placeholder
(defn placeholder-opacity
  [calculations-complete?]
  (.placeholderOpacity ^js messages-worklets calculations-complete?))

(defn placeholder-z-index
  [calculations-complete?]
  (.placeholderZIndex ^js messages-worklets calculations-complete?))

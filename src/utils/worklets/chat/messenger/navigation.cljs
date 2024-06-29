(ns utils.worklets.chat.messenger.navigation)

(def ^:private worklets (js/require "../src/js/worklets/chat/messenger/navigation.js"))

(defn navigation-header-opacity
  [distance-from-list-top all-loaded? chat-screen-layout-calculations-complete? start-position]
  (.navigationHeaderOpacity ^js worklets
                            distance-from-list-top
                            all-loaded?
                            chat-screen-layout-calculations-complete?
                            start-position))

(defn navigation-header-position
  [distance-from-list-top all-loaded? top-bar-height start-position]
  (.navigationHeaderPosition ^js worklets
                             distance-from-list-top
                             all-loaded?
                             top-bar-height
                             start-position))

(defn navigation-buttons-complete-opacity
  [chat-screen-layout-calculations-complete?]
  (.navigationButtonsCompleteOpacity ^js worklets chat-screen-layout-calculations-complete?))

(defn interpolate-navigation-view-opacity
  [props]
  (.interpolateNavigationViewOpacity ^js worklets (clj->js props)))

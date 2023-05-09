(ns status-im2.contexts.chat.composer.sub-view
  (:require
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.style :as style]))

(defn bar
  []
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar)}]])

(defn f-blur-view
  [layout-height focused?]
  [reanimated/view {:style (style/blur-container layout-height focused?)}
   [blur/view (style/blur-view)]])
(defn blur-view
  [layout-height focused?]
  [:f> f-blur-view layout-height focused?])

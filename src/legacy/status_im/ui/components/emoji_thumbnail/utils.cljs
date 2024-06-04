(ns legacy.status-im.ui.components.emoji-thumbnail.utils
  (:require
    [react-native.platform :as platform]))

(defn emoji-font-size
  [container_size]
  (int (* (/ container_size 10) 6)))

;; React Native Bug: Till version 0.65 React Native has a bug. It doesn't center text/emoji
;; inside the container(In Android & Web Only). Even with the textAlign: "center" and other properties.
;; So this top margin is required so that emoji will center in the emoji circle.
;; More Info: https://github.com/facebook/react-native/issues/32198
;; TODO: Remove this top margin, if future updates of react-native fix this issue.
(defn emoji-top-margin-for-vertical-alignment
  [container_size]
  (if platform/android? (- (int (/ container_size 20))) 0))

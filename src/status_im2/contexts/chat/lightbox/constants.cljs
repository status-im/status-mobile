(ns status-im2.contexts.chat.lightbox.constants
  (:require [react-native.platform :as platform]))

(def ^:const small-image-size 40)
(def ^:const focused-extra-size 16)
(def ^:const focused-image-size (+ small-image-size focused-extra-size))
(def ^:const small-list-height 80)
(def ^:const small-list-padding-vertical 12)
(def ^:const top-view-height 56)
(def ^:const separator-width 16)
(def ^:const drag-threshold 100)

;;; TEXT SHEET
(def ^:const text-min-height 68)
(def ^:const text-margin 12)
(def ^:const bar-container-height 20)
(def ^:const line-height 22)

;; 0.01 on Android because of this bug: https://github.com/status-im/status-mobile/pull/16471#issuecomment-1620153153
;; On Android having a component with opacity of zero will behave as if it doesn't exist
(def ^:const gradient-min-opacity (if platform/ios? 0 0.01))

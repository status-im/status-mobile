(ns status-im2.contexts.chat.bottom-sheet-composer.utils
  (:require
    [oops.core :as oops]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as c]))

(defn bounded-val
  [f min max]
  (Math/max min (Math/min f max)))

(defn get-min-height
  [lines]
  (if (> lines 1) c/multiline-minimized-height c/input-height))

(defn calc-reopen-height
  [text-value min-height content-height saved-height]
  (if (empty? @text-value)
    min-height
    (Math/min @content-height (reanimated/get-shared-value saved-height))))

(defn update-height?
  [content-size height max-height maximized?]
  (when-not @maximized?
    (let [diff (Math/abs (- content-size (reanimated/get-shared-value height)))]
      (and (not= (reanimated/get-shared-value height) max-height)
           (> diff c/content-change-threshold)))))

(defn show-top-gradient?
  [y lines max-lines gradient-opacity focused?]
  (and
   (> y c/line-height)
   (>= lines max-lines)
   (= (reanimated/get-shared-value gradient-opacity) 0)
   @focused?))

(defn hide-top-gradient?
  [y gradient-opacity]
  (and
   (<= y c/line-height)
   (= (reanimated/get-shared-value gradient-opacity) 1)))

(defn show-background?
  [saved-height max-height new-height]
  (or (= (reanimated/get-shared-value saved-height) max-height)
      (> new-height (* c/background-threshold max-height))))

(defn update-blur-height?
  [e lock-layout? layout-height]
  (or (not @lock-layout?)
      (> (reanimated/get-shared-value layout-height) (oops/oget e "nativeEvent.layout.height"))))

(defn calc-lines
  [height]
  (let [lines (Math/round (/ height c/line-height))]
    (if platform/ios? lines (dec lines))))

(defn calc-max-height
  [window-height kb-height insets images]
  (let [margin-top (if platform/ios? (:top insets) 10)
        max-height (- window-height
                      margin-top
                      kb-height
                      c/bar-container-height
                      c/actions-container-height)]
    (if (seq images)
      (- max-height c/images-container-height)
      max-height)))

(defn empty-input?
  [input-text images]
  (and (nil? input-text) (empty? images)))

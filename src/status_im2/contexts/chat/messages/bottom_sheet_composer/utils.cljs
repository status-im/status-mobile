(ns status-im2.contexts.chat.messages.bottom-sheet-composer.utils
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]))

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

(defn should-update-height
  [content-size height max-height]
  (let [diff (Math/abs (- content-size (reanimated/get-shared-value height)))]
    (and (not= (reanimated/get-shared-value height) max-height)
         (> diff c/content-change-threshold))))

(defn should-show-top-gradient
  [y lines max-lines gradient-opacity focused?]
  (and
   (> y c/line-height)
   (>= lines max-lines)
   (= (reanimated/get-shared-value gradient-opacity) 0)
   @focused?))

(defn should-hide-top-gradient
  [y gradient-opacity]
  (and
   (<= y c/line-height)
   (= (reanimated/get-shared-value gradient-opacity) 1)))

(defn calc-lines
  [height]
  (let [lines (Math/round (/ height c/line-height))]
    (if platform/ios? lines (dec lines))))

(defn calc-max-height
  [window-height margin-top kb-height images]
  (let [max-height
        (- window-height margin-top kb-height c/handle-container-height c/actions-container-height)]
    (if (seq images)
      (- max-height c/images-container-height)
      max-height)))

(defn empty-input?
  [input-text images]
  (and (nil? input-text) (not (seq images))))

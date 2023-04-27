(ns status-im2.contexts.chat.bottom-sheet-composer.utils
  (:require
    [oops.core :as oops]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]))

(defn bounded-val
  [val min-val max-val]
  (max min-val (min val max-val)))

(defn get-min-height
  [lines]
  (if (> lines 1) constants/multiline-minimized-height constants/input-height))

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
           (> diff constants/content-change-threshold)))))

(defn show-top-gradient?
  [y lines max-lines gradient-opacity focused?]
  (and
   (> y constants/line-height)
   (>= lines max-lines)
   (= (reanimated/get-shared-value gradient-opacity) 0)
   @focused?))

(defn hide-top-gradient?
  [y gradient-opacity]
  (and
   (<= y constants/line-height)
   (= (reanimated/get-shared-value gradient-opacity) 1)))

(defn show-bottom-gradient?
  [{:keys [text-value focused?]} {:keys [lines]}]
  (and (not-empty @text-value) (not @focused?) (> lines 2)))

(defn show-background?
  [saved-height max-height new-height]
  (or (= (reanimated/get-shared-value saved-height) max-height)
      (> new-height (* constants/background-threshold max-height))))

(defn update-blur-height?
  [event lock-layout? layout-height]
  (or (not @lock-layout?)
      (> (reanimated/get-shared-value layout-height) (oops/oget event "nativeEvent.layout.height"))))

(defn calc-lines
  [height]
  (let [lines (Math/round (/ height constants/line-height))]
    (if platform/ios? lines (dec lines))))

(defn calc-max-height
  [window-height kb-height insets images? reply?]
  (let [margin-top (if platform/ios? (:top insets) (+ 10 (:top insets)))
        max-height (- window-height
                      margin-top
                      kb-height
                      constants/bar-container-height
                      constants/actions-container-height)
        max-height (if images? (- max-height constants/images-container-height) max-height)
        max-height (if reply? (- max-height constants/reply-container-height) max-height)]
    max-height))

(defn empty-input?
  [text images reply?]
  (and (empty? text) (empty? images) (not reply?)))

(defn android-elevation?
  [lines images reply?]
  (or (> lines 1) (seq images) reply?))

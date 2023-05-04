(ns status-im2.contexts.chat.bottom-sheet-composer.utils
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]
    [utils.re-frame :as rf]))

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
  [window-height kb-height insets images? reply? edit?]
  (let [margin-top (if platform/ios? (:top insets) (+ 10 (:top insets)))
        max-height (- window-height
                      margin-top
                      kb-height
                      constants/bar-container-height
                      constants/actions-container-height)
        max-height (if images? (- max-height constants/images-container-height) max-height)
        max-height (if reply? (- max-height constants/reply-container-height) max-height)
        max-height (if edit? (- max-height constants/edit-container-height) max-height)]
    max-height))

(defn empty-input?
  [text images reply? audio?]
  (and (empty? text) (empty? images) (not reply?) (not audio?)))

(defn android-elevation?
  [lines images reply? edit?]
  (or (> lines 1) (seq images) reply? edit?))

(defn cancel-edit-message
  [{:keys [text-value maximized?]}
   {:keys [height saved-height last-height]}]
  (when-not @maximized?
    (reanimated/animate height constants/input-height)
    (reanimated/set-shared-value saved-height constants/input-height)
    (reanimated/set-shared-value last-height constants/input-height))
  (reset! text-value "")
  (rf/dispatch [:chat.ui/set-input-content-height constants/input-height]))

(defn update-input
  [{:keys [input-ref]}
   {:keys [text-value]}
   input-text]
  (when (and input-text (not= @text-value input-text))
    (reset! text-value input-text)
    (when @input-ref
      (.setNativeProps ^js @input-ref (clj->js {:text input-text})))))

(defn count-lines
  [s]
  (-> s
      (string/split #"\n" -1)
      (butlast)
      count))

(defn cursor-y-position-relative-to-container
  [{:keys [scroll-y]}
   {:keys [cursor-position text-value]}]
  (let [sub-text               (subs @text-value 0 @cursor-position)
        sub-text-lines         (count-lines sub-text)
        scrolled-lines         (Math/round (/ @scroll-y constants/line-height))
        sub-text-lines-in-view (- sub-text-lines scrolled-lines)]
    (* sub-text-lines-in-view constants/line-height)))

(defn calc-suggestions-position
  [cursor-pos max-height size
   {:keys [maximized?]}
   {:keys [insets curr-height window-height keyboard-height edit reply]}]
  (let [base             (+ constants/composer-default-height (:bottom insets) 8)
        base             (+ base (- curr-height constants/input-height))
        base             (if edit
                           (+ base constants/edit-container-height)
                           base)
        base             (if reply
                           (+ base constants/reply-container-height)
                           base)
        view-height      (- window-height keyboard-height (:top insets))
        container-height (bounded-val
                          (* (/ constants/mentions-max-height 4) size)
                          (/ constants/mentions-max-height 4)
                          constants/mentions-max-height)]
    (if @maximized?
      (if (< (+ cursor-pos container-height) max-height)
        (+ constants/actions-container-height (:bottom insets))
        (+ constants/actions-container-height (:bottom insets) (- max-height cursor-pos) 18))
      (if (< (+ base container-height) view-height)
        base
        (+ constants/actions-container-height (:bottom insets) (- curr-height cursor-pos) 18)))))

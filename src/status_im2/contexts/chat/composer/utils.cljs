(ns status-im2.contexts.chat.composer.utils
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.selection :as selection]
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
  [max-height new-height maximized?]
  (or @maximized?
      (> new-height (* constants/background-threshold max-height))))

(defn update-blur-height?
  [event {:keys [lock-layout? focused?]} layout-height]
  (or (not @lock-layout?)
      (not @focused?)
      (> (reanimated/get-shared-value layout-height) (oops/oget event "nativeEvent.layout.height"))))

(defn calc-lines
  [height]
  (let [lines (Math/round (/ height constants/line-height))]
    (if platform/ios? lines (dec lines))))

(defn calc-extra-content-height
  [images? reply? edit?]
  (let [height (if images? constants/images-container-height 0)
        height (if reply? (+ height constants/reply-container-height) height)
        height (if edit? (+ height constants/edit-container-height) height)]
    height))

(defn calc-max-height
  [{:keys [images reply edit]} window-height kb-height insets]
  (let [margin-top (if platform/ios? (:top insets) (+ 10 (:top insets)))
        max-height (- window-height
                      margin-top
                      kb-height
                      constants/bar-container-height
                      constants/actions-container-height)
        max-height (- max-height (calc-extra-content-height images reply edit))]
    max-height))

(defn empty-input?
  [text images reply? audio?]
  (and (empty? text) (empty? images) (not reply?) (not audio?)))

(defn android-elevation?
  [lines images reply? edit?]
  (or (> lines 1) (seq images) reply? edit?))

(defn cancel-edit-message
  [{:keys [text-value]}]
  (reset! text-value "")
  (rf/dispatch [:chat.ui/set-input-content-height constants/input-height]))

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

(defn calc-shell-neg-y
  [insets maximized? extra-height]
  (let [padding 6
        neg-y   (if @maximized? -50 0)]
    (- (+ constants/bar-container-height
          constants/actions-container-height
          (:bottom insets)
          padding
          extra-height
          neg-y))))

(defn calc-suggestions-position
  [cursor-pos max-height size
   {:keys [maximized?]}
   {:keys [insets curr-height window-height keyboard-height images reply edit]}]
  (let [base             (+ constants/composer-default-height (:bottom insets) 8)
        base             (+ base (- curr-height constants/input-height))
        base             (+ base (calc-extra-content-height images reply edit))
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


(defn init-props
  []
  {:input-ref                   (atom nil)
   :selectable-input-ref        (atom nil)
   :keyboard-show-listener      (atom nil)
   :keyboard-frame-listener     (atom nil)
   :keyboard-hide-listener      (atom nil)
   :emoji-kb-extra-height       (atom nil)
   :saved-emoji-kb-extra-height (atom nil)
   :sending-images?             (atom false)
   :record-reset-fn             (atom nil)
   :scroll-y                    (atom 0)
   :selection-event             (atom nil)
   :selection-manager           (rn/selectable-text-input-manager)})

(defn init-state
  []
  {:text-value            (reagent/atom "")
   :cursor-position       (reagent/atom 0)
   :saved-cursor-position (reagent/atom 0)
   :gradient-z-index      (reagent/atom 0)
   :kb-default-height     (reagent/atom 0)
   :kb-height             (reagent/atom 0)
   :gesture-enabled?      (reagent/atom true)
   :lock-selection?       (reagent/atom true)
   :focused?              (reagent/atom false)
   :lock-layout?          (reagent/atom false)
   :maximized?            (reagent/atom false)
   :record-permission?    (reagent/atom true)
   :recording?            (reagent/atom false)
   :first-level?          (reagent/atom true)
   :menu-items            (reagent/atom selection/first-level-menu-items)})

(defn init-subs
  []
  (let [chat-input (rf/sub [:chats/current-chat-input])]
    {:images               (seq (rf/sub [:chats/sending-image]))
     :audio                (rf/sub [:chats/sending-audio])
     :reply                (rf/sub [:chats/reply-message])
     :edit                 (rf/sub [:chats/edit-message])
     :input-with-mentions  (rf/sub [:chat/input-with-mentions])
     :input-text           (:input-text chat-input)
     :input-content-height (:input-content-height chat-input)}))

(defn init-animations
  [{:keys [input-text images reply audio]}
   lines content-height max-height opacity background-y]
  (let [initial-height (if (> lines 1)
                         constants/multiline-minimized-height
                         constants/input-height)]
    {:gradient-opacity  (reanimated/use-shared-value 0)
     :container-opacity (reanimated/use-shared-value
                         (if (empty-input?
                              input-text
                              images
                              reply
                              audio)
                           0.7
                           1))
     :height            (reanimated/use-shared-value
                         initial-height)
     :saved-height      (reanimated/use-shared-value
                         initial-height)
     :last-height       (reanimated/use-shared-value
                         (bounded-val
                          @content-height
                          constants/input-height
                          max-height))
     :opacity           opacity
     :background-y      background-y}))

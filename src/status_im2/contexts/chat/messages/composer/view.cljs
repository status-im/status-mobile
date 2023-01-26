(ns status-im2.contexts.chat.messages.composer.view
  (:require [clojure.string :as string]
            [oops.core :refer [oget]]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [react-native.background-timer :as background-timer]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.composer.style :as style]
            [status-im2.contexts.chat.messages.composer.controls.view :as controls]
            [status-im.ui2.screens.chat.composer.edit.view :as edit]
            [status-im.ui2.screens.chat.composer.input :as input]
            [status-im.ui2.screens.chat.composer.mentions :as mentions]
            [status-im.ui2.screens.chat.composer.reply :as reply]))

(def initial-content-height (atom nil))
(def keyboard-hiding? (atom false))

(defn minimize
  [{:keys [min-y set-bg-opacity set-translate-y set-parent-height]}]
  (set-bg-opacity 0)
  (set-translate-y (- min-y))
  (set-parent-height min-y))

(defn maximize
  [{:keys [max-y set-bg-opacity set-translate-y set-parent-height max-parent-height]}]
  (set-bg-opacity 1)
  (set-translate-y (- max-y))
  (set-parent-height max-parent-height))

(defn clean-and-minimize
  [{:keys [chat-id refs] :as params}]
  (input/clear-input chat-id refs)
  (minimize params))

(def gesture-values (atom {}))

(defn get-bottom-sheet-gesture
  [{:keys [translate-y refs keyboard-shown min-y max-y] :as params}]
  (let [{:keys [text-input-ref]} refs]
    (-> (gesture/gesture-pan)
        (gesture/on-start
         (fn [_]
           (if keyboard-shown
             (swap! gesture-values assoc :pan-y (reanimated/get-shared-value translate-y))
             (input/input-focus text-input-ref))))
        (gesture/on-update
         (fn [evt]
           (when keyboard-shown
             (let [tY (oget evt "translationY")]
               (swap! gesture-values assoc :dy (- tY (:pdy @gesture-values)))
               (swap! gesture-values assoc :pdy tY)
               (reanimated/set-shared-value
                translate-y
                (max (min (+ tY (:pan-y @gesture-values)) (- min-y)) (- max-y)))))))
        (gesture/on-end
         (fn [_]
           (when keyboard-shown
             (if (< (:dy @gesture-values) 0)
               (maximize params)
               (do
                 (reset! keyboard-hiding? true)
                 (minimize params)
                 (background-timer/set-timeout #(rf/dispatch [:dismiss-keyboard]) 200)))))))))

(defn update-y
  [{:keys [max-parent-height set-bg-opacity keyboard-shown max-y set-translate-y set-parent-height
           min-y chat-id]
    :as   params}]
  (let [content-height (get @input/input-text-content-heights chat-id)
        new-y          (+ min-y
                          (if (and content-height @initial-content-height)
                            (- content-height
                               @initial-content-height)
                            0))]
    (if (< new-y max-y)
      (when keyboard-shown
        (if (> new-y max-parent-height)
          (set-bg-opacity 1)
          (set-bg-opacity 0))
        (set-translate-y (- new-y))
        (set-parent-height (min new-y max-parent-height)))
      (if keyboard-shown
        (maximize params)
        (set-bg-opacity 0)))))

(defn get-input-content-change
  [{:keys [chat-id] :as params}]
  (fn [evt]
    (let [new-height (oget evt "nativeEvent.contentSize.height")]
      (swap! input/input-text-content-heights assoc chat-id new-height)
      (if @initial-content-height
        (do
          ;;on Android when last symbol removed, height value is wrong, like 300px
          (when (string/blank? (get @input/input-texts chat-id))
            (swap! input/input-text-content-heights assoc chat-id @initial-content-height))
          (update-y params))
        (reset! initial-content-height new-height)))))

(defn mentions
  [{:keys [refs suggestions max-y]} insets]
  [:f>
   (fn []
     (let [translate-y (reanimated/use-shared-value 0)]
       (rn/use-effect
        (fn []
          (reanimated/set-shared-value translate-y
                                       (reanimated/with-timing (if (seq suggestions) 0 200)))))
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:transform [{:translateY translate-y}]}
                 {:bottom     (or (:bottom insets) 0)
                  :position   :absolute
                  :z-index    5
                  :elevation  5
                  :max-height (/ max-y 2)})}
        [mentions/autocomplete-mentions suggestions (:text-input-ref refs)]]))])

(defn effect!
  [{:keys [keyboard-shown] :as params}]
  (rn/use-effect
   (fn []
     (when (or (not @keyboard-hiding?)
               (and @keyboard-hiding? (not keyboard-shown)))
       (reset! keyboard-hiding? false)
       (if (not keyboard-shown)
         (minimize params)
         (update-y params))))))

(defn prepare-params
  [[refs window-height translate-y bg-opacity bg-bottom min-y max-y parent-height
    max-parent-height chat-id suggestions reply edit images keyboard-shown]]
  {:chat-id           chat-id
   :translate-y       translate-y
   :min-y             min-y
   :max-y             max-y
   :max-parent-height max-parent-height
   :parent-height     parent-height
   :suggestions       suggestions
   :reply             reply
   :edit              edit
   :images            images
   :refs              refs
   :keyboard-shown    keyboard-shown
   :bg-opacity        bg-opacity
   :bg-bottom         bg-bottom
   :window-height     window-height
   :set-bg-opacity    (fn [value]
                        (reanimated/set-shared-value bg-bottom (if (= value 1) 0 (- window-height)))
                        (reanimated/set-shared-value bg-opacity (reanimated/with-timing value)))
   :set-translate-y   (fn [value]
                        (reanimated/set-shared-value translate-y (reanimated/with-timing value)))
   :set-parent-height (fn [value]
                        (reanimated/set-shared-value parent-height (reanimated/with-timing value)))})

;; IMPORTANT! PLEASE READ BEFORE MAKING CHANGES

;; 1) insets are not stable so we can't rely on the first values and we have to use it from render
;; function, so effect will be triggered a few times, for example first time it will be 0 and second
;; time 38 on ios, and 400 and 0 on Android
;; 2) for keyboard we use hooks, for some reason it triggers effect a few times with the same values,
;; also it changes value only when keyboard completely closed or opened, that's why its not visually
;; smooth, there is a small visible delay between keyboard and sheet animations
;; 3) when we minimize sheet and hide keyboard in `sheet-gesture` function, keyboard hook triggers
;; effect and there is a visual glitch when we have more than one line of text, so we use
;; `keyboard-hiding?` flag
;; 4) we store input content height in `input/input-text-content-heights` atom , we need it when chat
;; screen is reopened
(defn composer
  [_ _]
  (let [text-input-ref (rn/create-ref)
        send-ref       (rn/create-ref)
        refs           {:send-ref       send-ref
                        :text-input-ref text-input-ref}]
    (fn [chat-id insets]
      [:f>
       (fn []
         (let [reply                                    (rf/sub [:chats/reply-message])
               edit                                     (rf/sub [:chats/edit-message])
               suggestions                              (rf/sub [:chat/mention-suggestions])
               images                                   (rf/sub [:chats/sending-image])

               {window-height :height}                  (rn/use-window-dimensions)
               {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
               translate-y                              (reanimated/use-shared-value 0)
               bg-opacity                               (reanimated/use-shared-value 0)
               bg-bottom                                (reanimated/use-shared-value (- window-height))

               suggestions?                             (and (seq suggestions)
                                                             keyboard-shown
                                                             (not @keyboard-hiding?))

               max-y                                    (- window-height
                                                           (- (if (> keyboard-height 0)
                                                                keyboard-height
                                                                360)
                                                              (:bottom insets))
                                                           46)

               min-y                                    (+ 108
                                                           (:bottom insets)
                                                           (if suggestions?
                                                             (min (/ max-y 2)
                                                                  (+ 16
                                                                     (* 46 (dec (count suggestions)))))
                                                             (+ 0
                                                                (when (or edit reply) 38)
                                                                (when (seq images) 80))))

               parent-height                            (reanimated/use-shared-value min-y)
               max-parent-height                        (Math/abs (- max-y 110 (:bottom insets)))

               params
               (prepare-params
                [refs window-height translate-y bg-opacity bg-bottom min-y max-y parent-height
                 max-parent-height chat-id suggestions reply edit images keyboard-shown])

               input-content-change                     (get-input-content-change params)
               bottom-sheet-gesture                     (get-bottom-sheet-gesture params)]
           (effect! params)
           [reanimated/view
            {:style (reanimated/apply-animations-to-style
                     {:height parent-height}
                     {})}
            ;;;;input
            [gesture/gesture-detector {:gesture bottom-sheet-gesture}
             [reanimated/view
              {:style (reanimated/apply-animations-to-style
                       {:transform [{:translateY translate-y}]}
                       (style/input-bottom-sheet window-height))}
              [rn/view {:style (style/bottom-sheet-handle)}]
              [edit/edit-message-auto-focus-wrapper text-input-ref edit #(clean-and-minimize params)]
              [reply/reply-message-auto-focus-wrapper text-input-ref reply]
              [rn/view
               {:style {:height (- max-y (- min-y 38))}}
               [input/text-input
                {:chat-id                chat-id
                 :on-content-size-change input-content-change
                 :sending-image          (seq images)
                 :refs                   refs}]]]]
            (if suggestions?
              [mentions params insets]
              [controls/view send-ref params insets chat-id images #(clean-and-minimize params)])
            ;;;;black background
            [reanimated/view
             {:style (reanimated/apply-animations-to-style
                      {:opacity   bg-opacity
                       :transform [{:translateY bg-bottom}]}
                      (style/bottom-sheet-background window-height))}]]))])))

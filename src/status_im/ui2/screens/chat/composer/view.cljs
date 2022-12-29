(ns status-im.ui2.screens.chat.composer.view
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [oops.core :refer [oget]]
            [quo.components.safe-area :as safe-area]
            [quo.react]
            [quo.react-native :as rn :refer [navigation-const]]
            [quo2.components.buttons.button :as quo2.button]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui2.screens.chat.composer.edit.view :as edit]
            [status-im.ui2.screens.chat.composer.images.view :as composer-images]
            [status-im.ui2.screens.chat.composer.input :as input]
            [status-im.ui2.screens.chat.composer.mentions :as mentions]
            [status-im.ui2.screens.chat.composer.reply :as reply]
            [status-im.ui2.screens.chat.composer.style :as style]
            [status-im2.contexts.chat.photo-selector.view :as photo-selector]
            [utils.re-frame :as rf]
            [status-im.utils.utils :as utils]
            [status-im2.contexts.chat.messages.list.view :refer [scroll-to-bottom]]
            [status-im.utils.platform :as platform]
            [status-im2.common.not-implemented :as not-implemented]))

(defn calculate-y
  [context min-y max-y added-value chat-id set-bg-opacity]
  (let [input-text (:input-text (get (rf/sub [:chat/inputs]) chat-id))
        num-lines  (count (string/split input-text "\n"))]
    (if (<= 5 num-lines)
      (do (when-not (:minimized-from-handlebar? @context)
            (swap! context assoc :state :max)
            (set-bg-opacity 1))
          max-y)
      (when (:minimized-from-handlebar? @context)
        (swap! context assoc :state :min :minimized-from-handlebar? false)
        (set-bg-opacity 0)
        min-y))
    (if (= (:state @context) :max)
      (do
        (set-bg-opacity 1)
        max-y)
      (when (< (:y @context) max-y)
        (+ (:y @context) added-value)))))

(defn calculate-y-with-mentions
  [y max-y max-height chat-id suggestions reply]
  (let [input-text               (:input-text (get (rf/sub [:chat/inputs]) chat-id))
        num-lines                (count (string/split input-text "\n"))
        text-height              (* num-lines 22)
        mentions-height          (min 132 (+ 16 (* 46 (- (count suggestions) 1))))
        should-translate?        (if (< (- max-height text-height) mentions-height) true false)
        min-value                (if-not reply mentions-height (+ mentions-height 44))
        ; translate value when mentions list appear while at bottom of expanded input sheet
        mentions-translate-value (if should-translate?
                                   (min min-value (- mentions-height (- max-height text-height)))
                                   mentions-height)]
    (when (or (< y max-y) should-translate?) mentions-translate-value)))

(defn get-y-value
  [context min-y max-y added-value max-height chat-id suggestions reply edit images set-bg-opacity]
  (let [y               (calculate-y context min-y max-y added-value chat-id set-bg-opacity)
        y-with-mentions (calculate-y-with-mentions y max-y max-height chat-id suggestions reply)]
    (+ y (when (seq suggestions) y-with-mentions) (when (seq images) 80) (when edit 38))))

(defn- clean-and-minimize-composer
  ([context chat-id refs min-y]
   (clean-and-minimize-composer context chat-id refs min-y false))
  ([context chat-id refs min-y edit?]
   (input/clear-input chat-id refs)
   (swap! context assoc
     :y
     (if edit?
       (- min-y 38)
       min-y))
   (swap! context assoc :clear true :state :min)))

(defn get-bottom-sheet-gesture
  [context translate-y text-input-ref keyboard-shown min-y max-y shared-height max-height set-bg-opacity]
  (-> (gesture/gesture-pan)
      (gesture/on-start
       (fn [_]
         (if keyboard-shown
           (swap! context assoc :pan-y (reanimated/get-shared-value translate-y))
           (input/input-focus text-input-ref))))
      (gesture/on-update
       (fn [evt]
         (when keyboard-shown
           (swap! context assoc :dy (- (.-translationY evt) (:pdy @context)))
           (swap! context assoc :pdy (.-translationY evt))
           (reanimated/set-shared-value
            translate-y
            (max (min (+ (.-translationY evt) (:pan-y @context)) (- min-y)) (- max-y))))))
      (gesture/on-end
       (fn [_]
         (when keyboard-shown
           (if (< (:dy @context) 0)
             (do
               (swap! context assoc :minimized-from-handlebar? false)
               (swap! context assoc :state :max)
               (input/input-focus text-input-ref)
               (reanimated/set-shared-value translate-y (reanimated/with-timing (- max-y)))
               (reanimated/set-shared-value shared-height (reanimated/with-timing max-height))
               (set-bg-opacity 1))
             (do
               (swap! context assoc :minimized-from-handlebar? true)
               (reanimated/set-shared-value translate-y (reanimated/with-timing (- min-y)))
               (reanimated/set-shared-value shared-height (reanimated/with-timing min-y))
               (set-bg-opacity 0)
               (rf/dispatch [:dismiss-keyboard]))))))))

(defn get-input-content-change
  [context translate-y shared-height max-height set-bg-opacity keyboard-shown min-y max-y blank-composer?
   initial-value]
  (fn [evt]
    (when-not (or blank-composer? (some? initial-value))
      (swap! context assoc :clear false))
    (if (:clear @context)
      (do
        (swap! context dissoc :clear)
        (swap! context assoc :state :min)
        (swap! context assoc :y min-y)
        (reanimated/set-shared-value translate-y (reanimated/with-timing (- min-y)))
        (reanimated/set-shared-value shared-height (reanimated/with-timing min-y))
        (set-bg-opacity 0))
      (when (not= (:state @context) :max)
        (let [offset-value (if platform/ios? 22 40)
              new-y        (+ min-y
                              (- (max (oget evt "nativeEvent" "contentSize" "height") offset-value)
                                 offset-value))]
          (if (< new-y max-y)
            (do
              (if (> (- max-y new-y) 120)
                (do
                  (swap! context assoc :state :custom-chat-available)
                  (set-bg-opacity 0))
                (do
                  (set-bg-opacity 1)
                  (swap! context assoc :state :custom-chat-unavailable)))
              (swap! context assoc :y new-y)
              (when keyboard-shown
                (reanimated/set-shared-value
                 translate-y
                 (reanimated/with-timing (- new-y)))
                (reanimated/set-shared-value
                 shared-height
                 (reanimated/with-timing (min new-y max-height)))))
            (do
              (swap! context assoc :state :max)
              (swap! context assoc :y max-y)
              (if keyboard-shown
                (do (set-bg-opacity 1)
                    (reanimated/set-shared-value
                     translate-y
                     (reanimated/with-timing (- max-y))))
                (set-bg-opacity 0)))))))))

(defn composer
  [chat-id]
  [safe-area/consumer
   (fn [insets]
     (let [min-y               112
           context             (atom {:y                         min-y ;current y value
                                      :min-y                     min-y ;minimum y value
                                      :dy                        0     ;used for gesture
                                      :pdy                       0     ;used for gesture
                                      :state                     :min  ;:min, :custom-chat-available,
                                                                       ;:custom-chat-unavailable, :max
                                      :clear                     false
                                      :minimized-from-handlebar? false})
           keyboard-was-shown? (atom false)
           text-input-ref      (quo.react/create-ref)
           send-ref            (quo.react/create-ref)
           refs                {:send-ref       send-ref
                                :text-input-ref text-input-ref}]
       (fn []
         [:f>
          (fn []
            (let [reply                                    (rf/sub [:chats/reply-message])
                  edit                                     (rf/sub [:chats/edit-message])
                  suggestions                              (rf/sub [:chat/mention-suggestions])
                  images                                   (get-in (rf/sub [:chat/inputs])
                                                                   [chat-id :metadata :sending-image])
                  {window-height :height}                  (rn/use-window-dimensions)
                  {:keys [keyboard-shown keyboard-height]} (rn/use-keyboard)
                  max-y                                    (- window-height
                                                              (if (> keyboard-height 0)
                                                                keyboard-height
                                                                360)
                                                              (:top insets)
                                                              (:status-bar-height @navigation-const)) ; 360
                                                                                                      ; -
                                                                                                      ; default
                                                                                                      ; height
                  max-height                               (Math/abs (- max-y 56 (:bottom insets))) ; 56
                                                                                                    ; -
                                                                                                    ; top-bar
                                                                                                    ; height
                  added-value                              (if (and (not (seq suggestions))
                                                                    (or edit reply))
                                                             38
                                                             0) ; increased height
                                                                ; of input box
                                                                ; needed when reply
                  min-y                                    (+ min-y (when (or edit reply) 38))
                  bg-opacity                               (reanimated/use-shared-value 0)
                  bg-bottom                                (reanimated/use-shared-value (-
                                                                                         window-height))
                  set-bg-opacity
                  (fn [value]
                    (reanimated/set-shared-value bg-bottom (if (= value 1) 0 (- window-height)))
                    (reanimated/set-shared-value bg-opacity (reanimated/with-timing value)))
                  y                                        (get-y-value
                                                            context
                                                            min-y
                                                            max-y
                                                            added-value
                                                            max-height
                                                            chat-id
                                                            suggestions
                                                            reply
                                                            edit
                                                            images
                                                            set-bg-opacity)
                  translate-y                              (reanimated/use-shared-value 0)
                  shared-height                            (reanimated/use-shared-value min-y)
                  clean-and-minimize-composer-fn
                  #(clean-and-minimize-composer context chat-id refs min-y %)
                  blank-composer?                          (string/blank? (get @input/input-texts
                                                                               chat-id))
                  initial-value                            (or (get @input/input-texts chat-id) nil)
                  input-content-change                     (get-input-content-change
                                                            context
                                                            translate-y
                                                            shared-height
                                                            max-height
                                                            set-bg-opacity
                                                            keyboard-shown
                                                            min-y
                                                            max-y
                                                            blank-composer?
                                                            initial-value)
                  bottom-sheet-gesture                     (get-bottom-sheet-gesture
                                                            context
                                                            translate-y
                                                            text-input-ref
                                                            keyboard-shown
                                                            min-y
                                                            max-y
                                                            shared-height
                                                            max-height
                                                            set-bg-opacity)]
              (quo.react/effect!
               #(do
                  (when (and @keyboard-was-shown? (not keyboard-shown))
                    (swap! context assoc :state :min)
                    (set-bg-opacity 0))
                  (when (and blank-composer? (not (seq images)) (not edit))
                    (clean-and-minimize-composer-fn false))
                  (when (seq images)
                    (input/show-send refs))
                  (reset! keyboard-was-shown? keyboard-shown)
                  (if (#{:max :custom-chat-unavailable} (:state @context))
                    (set-bg-opacity 1)
                    (set-bg-opacity 0))
                  (reanimated/set-shared-value translate-y (reanimated/with-timing (- y)))
                  (reanimated/set-shared-value shared-height
                                               (reanimated/with-timing (min y max-height)))))
              [reanimated/view
               {:style (reanimated/apply-animations-to-style
                        {:height shared-height}
                        {:z-index 2})}
               ;;INPUT MESSAGE bottom sheet
               [gesture/gesture-detector {:gesture bottom-sheet-gesture}
                [reanimated/view
                 {:style (reanimated/apply-animations-to-style
                          {:transform [{:translateY translate-y}]}
                          (style/input-bottom-sheet window-height))}
                 ;handle
                 [rn/view {:style (style/bottom-sheet-handle)}]
                 [edit/edit-message-auto-focus-wrapper text-input-ref edit
                  clean-and-minimize-composer-fn]
                 [reply/reply-message-auto-focus-wrapper text-input-ref reply]
                 [rn/view {:style {:height (- max-y 80 added-value)}}
                  [input/text-input
                   {:chat-id                chat-id
                    :on-content-size-change input-content-change
                    :sending-image          (seq images)
                    :initial-value          initial-value
                    :refs                   refs
                    :set-active-panel       #()}]]]]
               ;CONTROLS
               (when-not (seq suggestions)
                 [rn/view {:style (style/bottom-sheet-controls insets)}
                  [quo2.button/button
                   {:on-press (fn []
                                (permissions/request-permissions
                                 {:permissions [:read-external-storage :write-external-storage]
                                  :on-allowed  #(rf/dispatch
                                                 [:bottom-sheet/show-sheet
                                                  {:content (fn []
                                                              (photo-selector/photo-selector chat-id))}])
                                  :on-denied   (fn []
                                                 (utils/set-timeout
                                                  #(utils/show-popup (i18n/label :t/error)
                                                                     (i18n/label
                                                                      :t/external-storage-denied))
                                                  50))}))
                    :icon     true
                    :type     :outline
                    :size     32} :i/image]
                  [rn/view {:width 12}]
                  [not-implemented/not-implemented
                   [quo2.button/button
                    {:icon true
                     :type :outline
                     :size 32} :i/reaction]]
                  [rn/view {:flex 1}]
                  ;;SEND button
                  [rn/view
                   {:ref   send-ref
                    :style (when (seq images)
                             {:width 0
                              :right -100})}
                   [quo2.button/button
                    {:icon                true
                     :size                32
                     :accessibility-label :send-message-button
                     :on-press            #(do (clean-and-minimize-composer-fn false)
                                               (scroll-to-bottom)
                                               (rf/dispatch [:chat.ui/send-current-message]))}
                    :i/arrow-up]]])
               ;black background
               [reanimated/view
                {:style (reanimated/apply-animations-to-style
                         {:opacity   bg-opacity
                          :transform [{:translateY bg-bottom}]}
                         (style/bottom-sheet-background window-height))}]
               [composer-images/images-list images]
               [mentions/autocomplete-mentions suggestions text-input-ref]]))])))])

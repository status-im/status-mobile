(ns status-im2.contexts.chat.lightbox.text-sheet.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.lightbox.constants :as c]
    [oops.core :as oops]
    [status-im2.contexts.chat.lightbox.text-sheet.style :as style]
    [status-im2.contexts.chat.messages.content.text.view :as message-view]
    [utils.worklets.lightbox :as worklet]))


(defn drag-gesture
  [derived-value saved-top expanded-height max-height opacity gradient-opacity overlay-z-index expanded?]
  (-> (gesture/gesture-pan)
      (gesture/enabled true)
      (gesture/max-pointers 1)
      (gesture/on-start (fn []
                          (reset! overlay-z-index 1)
                          (reanimated/animate gradient-opacity 0)))
      (gesture/on-update
       (fn [e]
         (let [new-value (+ (reanimated/get-shared-value saved-top) (oops/oget e "translationY"))
               progress  (/ (- new-value) max-height)]
           (reanimated/set-shared-value opacity progress)
           (reanimated/set-shared-value derived-value (max (min (- new-value) expanded-height) c/small-list-height)))))
      (gesture/on-end (fn [e]
                        (if (or (> (- (reanimated/get-shared-value derived-value))
                                   (reanimated/get-shared-value saved-top))
                                (= (reanimated/get-shared-value derived-value) c/small-list-height))
                          (do
                            (reanimated/animate derived-value c/small-list-height)
                            (reanimated/animate opacity 0)
                            (reanimated/set-shared-value saved-top (- c/small-list-height))
                            (reset! expanded? false)
                            (js/setTimeout #(reset! overlay-z-index 0) 300))
                          (reanimated/set-shared-value saved-top (- (reanimated/get-shared-value derived-value))))
                        (when (= (reanimated/get-shared-value derived-value) expanded-height)
                          (reset! expanded? true))))))

(defn bar
  []
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar)}]])

(defn text-sheet
  [messages text-height expanded? opacity overlay-z-index]
  (let [{:keys [chat-id content]} (first messages)
        insets                    (safe-area/get-insets)
        window-height             (:height (rn/get-window))
        max-height                (- window-height
                                     c/small-list-height
                                     c/top-view-height
                                     (:bottom insets)
                                     (when platform/ios? (:top insets)))
        text-padding              24
        expanded-height           (min max-height (+ 20 @text-height text-padding))
        derived-value             (reanimated/use-shared-value c/small-list-height)
        height                    (worklet/text-sheet derived-value true)
        top                       (worklet/text-sheet derived-value false)
        saved-top                 (reanimated/use-shared-value (- c/small-list-height))
        gradient-opacity          (reanimated/use-shared-value 0)]
    [gesture/gesture-detector
     {:gesture (drag-gesture derived-value saved-top expanded-height max-height opacity gradient-opacity overlay-z-index expanded?)}
     [reanimated/touchable-opacity
      {:active-opacity 1
       :on-press       (fn []
                         (reanimated/animate derived-value expanded-height)
                         (reanimated/animate opacity 1)
                         (reanimated/set-shared-value saved-top (- expanded-height))
                         (reset! overlay-z-index 1)
                         (reset! expanded? true))
       :style          (reanimated/apply-animations-to-style
                        {:height height
                         :top    top}
                        {:position :absolute
                         :left     0
                         :right    0})}
      (when (> @text-height 44)
        [bar])
      [reanimated/linear-gradient
       {:colors [colors/neutral-100-opa-0 colors/neutral-100]
        :start  {:x 0 :y 1}
        :end    {:x 0 :y 0}
        :style  (reanimated/apply-animations-to-style
                 {:opacity gradient-opacity}
                 {:position :absolute
                  :left     0
                  :right    0
                  :top      (- (+ c/top-view-height (:top insets)))
                  :height   (+ c/top-view-height (:top insets) 20 12 44)
                  :z-index  1})}]
      [linear-gradient/linear-gradient
       {:colors [colors/neutral-100-opa-50 colors/neutral-100-opa-0]
        :start  {:x 0 :y 1}
        :end    {:x 0 :y 0}
        :style  {:position :absolute
                 :left     0
                 :right    0
                 :height   28
                 :bottom   0
                 :z-index  1}}]
      [gesture/scroll-view
       {:scroll-enabled true
        :scroll-event-throttle 16
        :on-scroll      (fn [e]
                          (if (and (> (oops/oget e "nativeEvent.contentOffset.y") 0) @expanded?)
                            (reanimated/animate gradient-opacity 1)
                            (reanimated/animate gradient-opacity 0)))
        :style          {:height (- max-height 20)}}
       [message-view/render-parsed-text
        {:content        content
         :chat-id        chat-id
         :style-override style/text-style
         :on-layout      (fn [event]
                           (reset! text-height (oops/oget event "nativeEvent.layout.height")))}]]]]))

(defn view
  [messages {:keys [overlay-opacity]} {:keys [overlay-z-index]}]
  (let [text-height (reagent/atom 0)
        expanded?   (reagent/atom false)]
    [:f> text-sheet messages text-height expanded? overlay-opacity overlay-z-index]))

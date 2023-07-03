(ns status-im2.contexts.chat.lightbox.text-sheet.view
  (:require
    [quo2.foundations.typography :as typography]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.lightbox.constants :as c]
    [oops.core :as oops]
    [status-im2.contexts.chat.lightbox.text-sheet.style :as style]
    [status-im2.contexts.chat.messages.content.text.view :as message-view]))


(defn drag-gesture
  []
  (-> (gesture/gesture-pan)))

(defn bar
  []
  [rn/view {:style style/bar-container}
   [rn/view {:style (style/bar)}]])

(defn text-sheet
  [messages text-height]
  (let [{:keys [chat-id content]} (first messages)
        line-height   (:line-height typography/paragraph-1)
        insets        (safe-area/get-insets)
        text-padding  24
        top-animation (reanimated/use-shared-value (- c/small-list-height))]
    [gesture/gesture-detector
     [reanimated/touchable-opacity
      {:active-opacity 1
       :on-press (fn [] (reanimated/animate top-animation (- (+ 20 @text-height text-padding))))
       :style          (reanimated/apply-animations-to-style
                         {:top top-animation}
                         {:background-color :red
                          :position         :absolute
                          ;:top (- c/small-list-height)
                          ;:top (- (+ 20 @text-height text-padding))
                          :left             0
                          :right            0
                          :z-index          0})}
      [bar]
      [message-view/render-parsed-text
       {:content        content
        :chat-id        chat-id
        :style-override style/text-style
        :on-layout      (fn [event] (println "yoooo" (oops/oget event "nativeEvent.layout.height")) (reset! text-height (oops/oget event "nativeEvent.layout.height")))}]]]))

(defn view
  [messages]
  (let [text-height (reagent/atom 0)]
    [:f> text-sheet messages text-height]))

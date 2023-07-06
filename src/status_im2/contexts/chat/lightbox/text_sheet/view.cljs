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
    [status-im2.contexts.chat.lightbox.constants :as constants]
    [status-im2.contexts.chat.lightbox.text-sheet.style :as style]
    [status-im2.contexts.chat.lightbox.text-sheet.utils :as utils]
    [status-im2.contexts.chat.messages.content.text.view :as message-view]))

(defn bar
  [text-height]
  (when (> text-height (* constants/line-height 2))
    [rn/view {:style style/bar-container}
     [rn/view {:style style/bar}]]))

(defn text-sheet
  [messages overlay-opacity overlay-z-index]
  (let [text-height (reagent/atom 0)
        expanded?   (reagent/atom false)
        dragging?   (atom false)]
    (fn []
      (let [{:keys [chat-id content]} (first messages)
            insets                    (safe-area/get-insets)
            window-height             (:height (rn/get-window))
            max-height                (- window-height
                                         constants/text-min-height
                                         constants/top-view-height
                                         (:bottom insets)
                                         (when platform/ios? (:top insets)))
            expanded-height           (min max-height
                                           (+ constants/bar-container-height
                                              @text-height
                                              constants/text-margin))
            animations                (utils/init-animations overlay-opacity)
            derived                   (utils/init-derived-animations animations)]
        [gesture/gesture-detector
         {:gesture (utils/sheet-gesture animations
                                        expanded-height
                                        max-height
                                        overlay-z-index
                                        expanded?
                                        dragging?)}
         [reanimated/touchable-opacity
          {:active-opacity 1
           :on-press
           #(utils/expand-sheet animations expanded-height max-height overlay-z-index expanded?)
           :style (style/sheet-container derived)}
          [bar @text-height]
          [reanimated/linear-gradient
           {:colors [colors/neutral-100-opa-0 colors/neutral-100]
            :start  {:x 0 :y 1}
            :end    {:x 0 :y 0}
            :style  (style/top-gradient animations insets)}]
          [linear-gradient/linear-gradient
           {:colors [colors/neutral-100-opa-50 colors/neutral-100-opa-0]
            :start  {:x 0 :y 1}
            :end    {:x 0 :y 0}
            :style  style/bottom-gradient}]
          [gesture/scroll-view
           {:scroll-enabled        @expanded?
            :scroll-event-throttle 16
            :on-scroll             #(utils/on-scroll % @expanded? @dragging? animations)
            :style                 {:height (- max-height constants/bar-container-height)}}
           [message-view/render-parsed-text
            {:content        content
             :chat-id        chat-id
             :style-override style/text-style
             :on-layout      #(utils/on-layout % text-height)}]]]]))))

(defn view
  [messages {:keys [overlay-opacity]} {:keys [overlay-z-index]}]
  [:f> text-sheet messages overlay-opacity overlay-z-index])

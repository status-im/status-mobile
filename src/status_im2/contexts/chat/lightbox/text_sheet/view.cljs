(ns status-im2.contexts.chat.lightbox.text-sheet.view
  (:require
    [quo.foundations.colors :as colors]
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

(defn- text-sheet
  [messages overlay-opacity overlay-z-index text-sheet-lock?]
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
            full-height               (+ constants/bar-container-height
                                         constants/text-margin
                                         constants/line-height
                                         @text-height)
            expanded-height           (min max-height full-height)
            animations                (utils/init-animations overlay-opacity)
            derived                   (utils/init-derived-animations animations)
            expanding-message?        (> @text-height (* constants/line-height 2))]
        [rn/view
         [reanimated/linear-gradient
          {:colors         [colors/neutral-100-opa-0 colors/neutral-100]
           :pointer-events :none
           :locations      [0 0.3]
           :start          {:x 0 :y 1}
           :end            {:x 0 :y 0}
           :style          (style/top-gradient animations derived insets max-height)}]
         [gesture/gesture-detector
          {:gesture (utils/sheet-gesture animations
                                         expanded-height
                                         max-height
                                         full-height
                                         overlay-z-index
                                         expanded?
                                         dragging?
                                         expanding-message?)}
          [gesture/gesture-detector
           {:gesture (-> (gesture/gesture-tap)
                         (gesture/enabled (and expanding-message? (not @expanded?)))
                         (gesture/on-start (fn []
                                             (utils/expand-sheet animations
                                                                 expanded-height
                                                                 max-height
                                                                 overlay-z-index
                                                                 expanded?
                                                                 text-sheet-lock?))))}
           [reanimated/view {:style (style/sheet-container derived)}
            (when expanding-message?
              [rn/view {:style style/bar-container}
               [rn/view {:style style/bar}]])
            [linear-gradient/linear-gradient
             {:colors    [colors/neutral-100-opa-100 colors/neutral-100-opa-70 colors/neutral-100-opa-0]
              :start     {:x 0 :y 1}
              :end       {:x 0 :y 0}
              :locations [0.7 0.8 1]
              :style     (style/bottom-gradient (:bottom insets))}]
            [gesture/scroll-view
             {:scroll-enabled          false
              :scroll-event-throttle   16
              :bounces                 false
              :style                   {:height (- max-height constants/bar-container-height)}
              :content-container-style {:padding-top (when (not expanding-message?)
                                                       constants/bar-container-height)}}
             [message-view/render-parsed-text
              {:content        content
               :chat-id        chat-id
               :style-override (style/text-style expanding-message?)
               :on-layout      #(utils/on-layout % text-height)}]]]]]]))))

(defn view
  [messages {:keys [overlay-opacity]} {:keys [overlay-z-index]} {:keys [text-sheet-lock?]}]
  [:f> text-sheet messages overlay-opacity overlay-z-index text-sheet-lock?])

(ns status-im2.contexts.shell.stacks.chat-stack
  (:require [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.view :as chat]))

(def window-width-value (atom nil))
(def load-chat-stack? (reagent/atom true))
(def chat-stack-left-position (atom nil))


(fx/defn close-chat-stack
  {:events [:shell/close-chat-stack]}
  [{:keys [db] :as cofx}]
  {:shell/navigate-from-chat-stack-fx nil
   :dispatch-later                    [{:dispatch [:close-chat]
                                        :ms       300}]})

(re-frame/reg-fx
 :shell/navigate-to-chat-stack-fx
 (fn []
   (reset! load-chat-stack? true)
   (reanimated/animate-shared-value-with-delay
    @chat-stack-left-position 0 200 :ease-out 300)))


(re-frame/reg-fx
 :shell/navigate-from-chat-stack-fx
 (fn []
   (reanimated/animate-shared-value-with-timing
    @chat-stack-left-position @window-width-value 200 :ease-out)))

(defn chat-stack []
  (when @load-chat-stack?
    [safe-area/consumer
     (fn [insets]
       [:f>
        (fn []
          (let [window-width (rf/sub [:dimensions/window-width])]
            (reset! chat-stack-left-position (reanimated/use-shared-value window-width))
            (reset! window-width-value window-width)
            [reanimated/view
             {:style (reanimated/apply-animations-to-style
                      {:left @chat-stack-left-position}
                      {:position            :absolute
                       :padding-top         (:top insets)
                       :top                 0
                       :bottom              0
                       :background-color    (colors/theme-colors colors/white colors/neutral-95)
                       :z-index             3
                       :accessibility-label :chat-stack})}           
             [chat/chat]]))])]))

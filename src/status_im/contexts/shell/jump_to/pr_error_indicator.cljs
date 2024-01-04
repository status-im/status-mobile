(ns status-im.contexts.shell.jump-to.pr-error-indicator
  (:require [quo.core :as quo]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]))

(defn chat-open-error
  []
  (let [current-chat-id (rf/sub [:chats/current-chat-id])
        view-id         (rf/sub [:view-id])]
    (when (and (not (#{:chat :profile} view-id)) current-chat-id)
      "Error: Chat is not closed. \n(messages will be marked read automatically).\nPlease create an issue with the steps that lead to this state.")))

;; https://github.com/status-im/status-mobile/pull/18338
(defn f-view
  []
  (let [z-index       (reanimated/use-shared-value -1)
        error-message (chat-open-error)]
    (if error-message
      (reanimated/animate-delay z-index 2 2000)
      (reanimated/animate-delay z-index -1 2000))
    [reanimated/touchable-opacity
     {:style    (reanimated/apply-animations-to-style
                 {:z-index z-index}
                 {:position           :absolute
                  :top                (safe-area/get-top)
                  :align-self         :center
                  :border-radius      5
                  :padding-horizontal 5
                  :background-color   :red})
      :on-press #(js/alert error-message)}
     [quo/text
      {:weight :regular
       :size   :paragraph-2
       :align  :center
       :style  {:color :white}} "Error"]]))

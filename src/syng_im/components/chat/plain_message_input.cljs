(ns syng-im.components.chat.plain-message-input
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              icon
                                              touchable-highlight
                                              text-input]]
            [syng-im.components.chat.suggestions :refer [suggestions-view]]
            [syng-im.components.chat.plain-message-input-styles :as st]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send [chat input-message]
  (let [{:keys [group-chat chat-id]} chat]
    (if group-chat
      ;; todo how much are different both events? is there real reason
      ;; for differentiation here?
      (dispatch [:send-group-chat-msg chat-id input-message])
      (dispatch [:send-chat-msg]))))

(defn plain-message-input-view []
  (let [chat                 (subscribe [:get-current-chat])
        input-message-atom   (subscribe [:get-chat-input-text])
        staged-commands-atom (subscribe [:get-chat-staged-commands])]
    (fn []
      (let [input-message @input-message-atom]
        [view st/input-container
         [suggestions-view]
         [view st/input-view
          [touchable-highlight {:on-press #(dispatch [:switch-command-suggestions])}
           [view nil [icon :list st/list-icon]]]
          [text-input {:style           st/message-input
                       :autoFocus       (pos? (count @staged-commands-atom))
                       :onChangeText    set-input-message
                       :onSubmitEditing #(send @chat input-message)}
           input-message]
          [icon :smile st/smile-icon]
          (when (or (pos? (count input-message))
                    (pos? (count @staged-commands-atom)))
            [touchable-highlight {:on-press #(send @chat input-message)}
             [view st/send-container
              [icon :send st/send-icon]]])]]))))

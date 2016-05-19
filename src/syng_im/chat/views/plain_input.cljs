(ns syng-im.chat.views.plain-input
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              icon
                                              touchable-highlight
                                              text-input]]
            [syng-im.chat.views.suggestions :refer [suggestions-view]]
            [syng-im.chat.styles.plain-input :as st]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send [chat input-message]
  (let [{:keys [group-chat chat-id]} chat]
    (dispatch [:send-chat-msg])))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn try-send [chat staged-commands message]
  (when (message-valid? staged-commands message)
    (send chat message)))

(defn plain-message-input-view []
  (let [chat                 (subscribe [:get-current-chat])
        input-message-atom   (subscribe [:get-chat-input-text])
        staged-commands-atom (subscribe [:get-chat-staged-commands])
        typing-command?      (subscribe [:typing-command?])]
    (fn []
      (let [input-message @input-message-atom]
        [view st/input-container
         [suggestions-view]
         [view st/input-view
          [touchable-highlight {:on-press #(dispatch [:switch-command-suggestions])
                                :style    st/switch-commands-touchable}
           [view nil
            (if @typing-command?
              [icon :close-gray st/close-icon]
              [icon :list st/list-icon])]]
          [text-input {:style           st/message-input
                       :autoFocus       (pos? (count @staged-commands-atom))
                       :onChangeText    set-input-message
                       :onSubmitEditing #(try-send @chat @staged-commands-atom
                                                   input-message)}
           input-message]
          [icon :smile st/smile-icon]
          (when (message-valid? @staged-commands-atom input-message)
            [touchable-highlight {:on-press #(send @chat input-message)}
             [view st/send-container
              [icon :send st/send-icon]]])]]))))

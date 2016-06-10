(ns status-im.chat.views.plain-input
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.chat.views.suggestions :refer [suggestions-view]]
            [status-im.chat.styles.plain-input :as st]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send [] (dispatch [:send-chat-msg]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn try-send [staged-commands message]
  (when (message-valid? staged-commands message) (send)))

(defn plain-message-input-view []
  (let [input-message-atom   (subscribe [:get-chat-input-text])
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
          [text-input {:style              st/message-input
                       :autoFocus          (pos? (count @staged-commands-atom))
                       :onChangeText       set-input-message
                       :onSubmitEditing    #(try-send @staged-commands-atom
                                                      input-message)}
           input-message]
          ;; TODO emoticons: not implemented
          [icon :smile st/smile-icon]
          (when (message-valid? @staged-commands-atom input-message)
            [touchable-highlight {:on-press #(send)
                                  :accessibility-label :send-message}
             [view st/send-container
              [icon :send st/send-icon]]])]]))))

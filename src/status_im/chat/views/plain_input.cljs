(ns status-im.chat.views.plain-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.chat.views.suggestions :refer [suggestions-view]]
            [status-im.chat.views.content-suggestions :refer [content-suggestions-view]]
            [status-im.chat.views.command :as command]
            [status-im.chat.styles.plain-input :as st]
            [status-im.chat.styles.input :as st-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send []
  (dispatch [:send-chat-msg]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn try-send [staged-commands message]
  (when (message-valid? staged-commands message)
    (send)))

(defn plain-message-input-view [{:keys [command input-options validator]}]
  (let [chat (subscribe [:get-current-chat])
        input-message-atom (subscribe [:get-chat-input-text])
        staged-commands-atom (subscribe [:get-chat-staged-commands])
        typing-command? (subscribe [:typing-command?])]
    (fn [{:keys [command input-options validator]}]
      (let [input-message @input-message-atom]
        [view st/input-container
         (if command
           [content-suggestions-view]
           [suggestions-view])
         [view st/input-view
          (if command
            [command/command-icon command]
            [touchable-highlight {:on-press #(dispatch [:switch-command-suggestions])
                                  :style    st/switch-commands-touchable}
             [view nil
              (if @typing-command?
                [icon :close-gray st/close-icon]
                [icon :list st/list-icon])]])
          [text-input (if command
                        (merge {:style           st-command/command-input
                                :autoFocus       true
                                :onChangeText    command/set-input-message
                                :onSubmitEditing (fn []
                                                   (when (command/valid? input-message validator)
                                                     (command/send-command)))}
                               input-options)
                        ;; plain
                        {:style           st/message-input
                         :autoFocus       (pos? (count @staged-commands-atom))
                         :onChangeText    set-input-message
                         :onSubmitEditing #(try-send @staged-commands-atom
                                                     input-message)})
           input-message]
          ;; TODO emoticons: not implemented
          (when (not command)
            [icon :smile st/smile-icon])
          (if command
            (if (command/valid? input-message validator)
              [touchable-highlight {:on-press command/send-command}
               [view st/send-container [icon :send st/send-icon]]]
              [touchable-highlight {:on-press command/cancel-command-input}
               [view st-command/cancel-container
                [icon :close-gray st-command/cancel-icon]]])
            (when (message-valid? @staged-commands-atom input-message)
              [touchable-highlight {:on-press send}
               [view st/send-container
                [icon :send st/send-icon]]]))]]))))

(comment
  [text-input {:style           st/message-input
               :autoFocus       (pos? (count @staged-commands-atom))
               ;:keyboardType (if (< 3 (count input-message)) :default :numeric)
               :onChangeText    set-input-message
               :onSubmitEditing #(try-send @chat @staged-commands-atom
                                           input-message)}
   input-message])
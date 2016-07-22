(ns status-im.chat.views.message-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.response :as st-response]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-container
    [icon :send st/send-icon]]])

(defn message-input-container [input]
  [view st/message-input-container input])

(defn plain-input-options [disbale?]
  {:style           st-message/message-input
   :onChangeText    (when-not disbale? plain-message/set-input-message)
   :editable        (not disbale?)
   :onSubmitEditing plain-message/send})

(defn on-press-commands-handler
  [{:keys [suggestions-trigger]}]
  (if (= :on-send (keyword suggestions-trigger))
    #(dispatch [:invoke-commands-suggestions!])
    command/send-command))

(defn command-input-options [command icon-width disbale?]
  {:style           (st-response/command-input icon-width disbale?)
   :onChangeText    (when-not disbale? command/set-input-message)
   :onSubmitEditing (on-press-commands-handler command)})

(defview message-input [input-options {:keys [suggestions-trigger] :as command}]
  [command? [:command?]
   input-message [:get-chat-input-text]
   input-command [:get-chat-command-content]
   icon-width [:command-icon-width]
   disbale? [:get :disable-input]]
  [text-input (merge
                (if command?
                  (command-input-options command icon-width disbale?)
                  (plain-input-options disbale?))
                {:autoFocus           false
                 :blurOnSubmit        false
                 :accessibility-label :input
                 :on-focus #(dispatch [:set :focused true])
                 :on-blur #(dispatch [:set :focused false])}
                input-options)
   (if command? input-command input-message)])

(defview plain-message-input-view [{:keys [input-options]}]
  [command? [:command?]
   {:keys [type] :as command} [:get-chat-command]
   input-command [:get-chat-command-content]
   valid-plain-message? [:valid-plain-message?]]
  [view st/input-container
   [view st/input-view
    [plain-message/commands-button]
    [message-input-container
     [message-input input-options command]]
    ;; TODO emoticons: not implemented
    [plain-message/smile-button]
    (when (or command? valid-plain-message?)
      (let [on-press (if command?
                       (on-press-commands-handler command)
                       plain-message/send)]
        [send-button {:on-press            on-press
                      :accessibility-label :send-message}]))
    (when (and command? (= :command type))
      [command/command-icon command])]])

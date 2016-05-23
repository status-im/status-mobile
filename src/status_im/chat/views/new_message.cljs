(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.plain-input :refer [plain-message-input-view input]]
    [status-im.chat.views.command :refer [simple-command-input-view]]
    [status-im.chat.views.phone :refer [phone-input-view]]
    [status-im.chat.views.password :refer [password-input-view]]
    [status-im.chat.views.confirmation-code :refer [confirmation-code-input-view]]
    [status-im.chat.views.money :refer [money-input-view]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.utils.phone-number :refer [valid-mobile-number?]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])



(comment
  (defn default-command-input-view [command input]
    [simple-command-input-view command input {}])

  (defn special-input-view [input command]
           (case (:command command)
             :phone [phone-input-view command input]
             :keypair-password [password-input-view command input]
             :confirmation-code [confirmation-code-input-view command input]
             :money [money-input-view command input]
             :request [money-input-view command input]
             [default-command-input-view command input])))

(defn show-input [command]
  [plain-message-input-view
   (merge {:command command}
          (case (:command command)
            :phone {:keyboardType :phone-pad
                    :validator    valid-mobile-number?}
            :keypair-password {:secureTextEntry true}
            :confirmation-code {:keyboardType :numeric}
            :money {:keyboardType :numeric}
            :request {:keyboardType :numeric}
            nil))])

(defn chat-message-new []
  (let [command-atom (subscribe [:get-chat-command])
        staged-commands-atom (subscribe [:get-chat-staged-commands])]
    (fn []
      (let [staged-commands @staged-commands-atom]
        [view st/new-message-container
         (when (and staged-commands (pos? (count staged-commands)))
           [staged-commands-view staged-commands])
         [show-input @command-atom]]))))

(comment
  (if command
    [special-input-view command]
    ))
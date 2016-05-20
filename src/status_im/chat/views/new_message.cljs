(ns status-im.chat.views.new-message
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.plain-input :refer [plain-message-input-view]]
    [status-im.chat.views.command :refer [simple-command-input-view]]
    [status-im.chat.views.phone :refer [phone-input-view]]
    [status-im.chat.views.password :refer [password-input-view]]
    [status-im.chat.views.confirmation-code :refer [confirmation-code-input-view]]
    [status-im.chat.views.money :refer [money-input-view]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])

(defn default-command-input-view [command]
  [simple-command-input-view command {}])

(defn special-input-view [command]
  (case (:command command)
    :phone [phone-input-view command]
    :keypair-password [password-input-view command]
    :confirmation-code [confirmation-code-input-view command]
    :money [money-input-view command]
    :request [money-input-view command]
    [default-command-input-view command]))

(defn chat-message-new []
  (let [command-atom         (subscribe [:get-chat-command])
        staged-commands-atom (subscribe [:get-chat-staged-commands])]
    (fn []
      (let [command         @command-atom
            staged-commands @staged-commands-atom]
        [view st/new-message-container
         (when (and staged-commands (pos? (count staged-commands)))
           [staged-commands-view staged-commands])
         (if command
           [special-input-view command]
           [plain-message-input-view])]))))

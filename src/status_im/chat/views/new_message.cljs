(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.message-input :refer [message-input]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])

(defn show-input [command]
  [message-input
   (when command
     (case (:command command)
       :phone {:input-options {:keyboardType :phone-pad}}
       :keypair-password {:input-options {:secureTextEntry true}}
       :confirmation-code {:input-options {:keyboardType :numeric}}
       :money {:input-options {:keyboardType :numeric}}
       :request {:input-options {:keyboardType :numeric}}
       (throw (js/Error. "Uknown command type"))))])

(defview chat-message-new []
  [command [:get-chat-command]
   staged-commands [:get-chat-staged-commands]]
  [view st/new-message-container
   (when (and staged-commands (pos? (count staged-commands)))
     [staged-commands-view staged-commands])
   [show-input command]])

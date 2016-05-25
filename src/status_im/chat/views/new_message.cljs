(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.plain-input :refer [plain-message-input-view]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.utils.phone-number :refer [valid-mobile-number?]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])

(defn show-input [command]
  [plain-message-input-view
   (merge {:command command}
          (case (:command command)
            :phone {:input-options {:keyboardType :phone-pad}
                    :validator    valid-mobile-number?}
            :keypair-password {:input-options {:secureTextEntry true}}
            :confirmation-code {:input-options {:keyboardType :numeric}}
            :money {:input-options {:keyboardType :numeric}}
            :request {:input-options {:keyboardType :numeric}}
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

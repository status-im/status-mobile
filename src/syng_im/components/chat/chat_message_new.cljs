(ns syng-im.components.chat.chat-message-new
  (:require
   [re-frame.core :refer [subscribe dispatch dispatch-sync]]
   [syng-im.components.react :refer [android?
                                     view
                                     image
                                     text
                                     text-input]]
   [syng-im.components.styles :refer [color-white]]
   [syng-im.components.chat.plain-message-input :refer [plain-message-input-view]]
   [syng-im.components.chat.input.simple-command :refer [simple-command-input-view]]
   [syng-im.components.chat.input.phone :refer [phone-input-view]]
   [syng-im.components.chat.input.password :refer [password-input-view]]
   [syng-im.components.chat.input.confirmation-code :refer [confirmation-code-input-view]]
   [syng-im.components.chat.input.money :refer [money-input-view]]
   [syng-im.components.chat.input.simple-command-staged :refer [simple-command-staged-view]]
   [syng-im.utils.utils :refer [log toast http-post]]
   [syng-im.utils.logging :as log]
   [syng-im.resources :as res]
   [reagent.core :as r]))

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
  (let [command-atom (subscribe [:get-chat-command])
        staged-commands-atom (subscribe [:get-chat-staged-commands])]
    (fn []
      (let [command @command-atom
            staged-commands @staged-commands-atom]
        [view {:style {:backgroundColor color-white
                       :elevation       4}}
         (when (and staged-commands (pos? (count staged-commands)))
           [staged-commands-view staged-commands])
         (if command
           [special-input-view command]
           [plain-message-input-view])]))))

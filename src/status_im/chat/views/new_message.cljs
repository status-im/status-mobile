(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.message-input :refer [plain-message-input-view]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.utils.phone-number :refer [valid-mobile-number?]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])

(defn get-options [{:keys [type placeholder]}]
  (let [options (case (keyword type)
                  :phone {:input-options {:keyboardType :phone-pad}
                          :validator     valid-mobile-number?}
                  :password {:input-options {:secureTextEntry true}}
                  :number {:input-options {:keyboardType :numeric}}
                  ;; todo maybe nil is fine for now :)
                  nil #_(throw (js/Error. "Uknown command type")))]
    (assoc-in options [:input-options :placeholder] "")))

(defview show-input []
  [parameter [:get-command-parameter]
   command? [:command?]]
  [plain-message-input-view
   (when command? (get-options parameter))])

(defview chat-message-new []
  [staged-commands [:get-chat-staged-commands]]
  [view st/new-message-container
   (when (seq staged-commands)
     [staged-commands-view staged-commands])
   [show-input]])

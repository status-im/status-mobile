(ns status-im.chat.views.new-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view]]
    [status-im.chat.views.message-input :refer [plain-message-input-view]]
    [status-im.chat.views.staged-command :refer [simple-command-staged-view]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.chat.styles.message :as st]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defn staged-commands-view [staged-commands]
  [view {}
   (for [command staged-commands]
     ^{:key command} [staged-command-view command])])

(defn get-options [{:keys [type placeholder]} command-type]
  (let [options (case (keyword type)
                  :phone {:input-options {:keyboard-type "phone-pad"}}
                  :password {:input-options {:secure-text-entry true}}
                  :number {:input-options {:keyboard-type "numeric"}}
                  ;; todo maybe nil is fine for now :)
                  nil #_(throw (js/Error. "Uknown command type")))]
    (if (= :response command-type)
      (if placeholder
        (assoc-in options [:input-options :placeholder] placeholder)
        options)
      (assoc-in options [:input-options :placeholder] ""))))

(defview show-input []
  [parameter [:get-command-parameter]
   command? [:command?]
   type [:command-type]]
  [plain-message-input-view
   (when command? (get-options parameter type))])

(defview chat-message-new []
  [staged-commands [:get-chat-staged-commands]
   margin [:input-margin]]
  (let [style (get-in platform-specific [:component-styles :chat :new-message])]
    [view (merge (st/new-message-container margin)
                 style)
     (when (seq staged-commands)
       [staged-commands-view staged-commands])
     [show-input]]))

(ns status-im.group-settings.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [status-im.constants :refer [max-chat-name-length]]))

(register-sub :selected-participant
  (fn [db _]
    (reaction
      (let [identity (first (:selected-participants @db))]
        (get-in @db [:contacts identity])))))

(defn get-chat-name-validation-messages [chat-name]
  (filter some?
          (list (when (zero? (count chat-name))
                  "Chat name can't be empty")
                (when (< max-chat-name-length (count chat-name))
                  "Chat name is too long"))))

(register-sub :new-chat-name-validation-messages
  (fn [db [_]]
    (let [chat-name (reaction (:new-chat-name @db))]
      (reaction (get-chat-name-validation-messages @chat-name)))))

(register-sub :new-chat-name-valid?
  (fn [db [_]]
    (let [chat-name (reaction (:new-chat-name @db))]
      (reaction (zero? (count (get-chat-name-validation-messages @chat-name)))))))
